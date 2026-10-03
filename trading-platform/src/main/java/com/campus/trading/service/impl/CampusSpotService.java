package com.campus.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.CampusSpot;
import com.campus.trading.mapper.ProductMapper;
import com.campus.trading.mapper.CampusSpotMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampusSpotService {
    private final CampusSpotMapper campusSpotMapper;
    private final ProductMapper productMapper;

    public CampusSpotService(CampusSpotMapper campusSpotMapper, ProductMapper productMapper) {
        this.campusSpotMapper = campusSpotMapper;
        this.productMapper = productMapper;
    }

    public List<CampusSpot> listAll(Integer userId) {
        QueryWrapper<CampusSpot> query = new QueryWrapper<>();
        query.and(scope -> {
            scope.eq("is_public", 1).or().isNull("is_public");
            if (userId != null) {
                scope.or().eq("owner_id", userId);
            }
        });
        query.orderByAsc("sort_order").orderByAsc("id");
        return campusSpotMapper.selectList(query);
    }

    public List<CampusSpot> listMy(Integer userId) {
        QueryWrapper<CampusSpot> query = new QueryWrapper<>();
        query.eq("owner_id", userId).eq("is_public", 0);
        query.orderByAsc("sort_order").orderByAsc("id");
        return campusSpotMapper.selectList(query);
    }

    public CampusSpot getById(Long id) {
        return id == null ? null : campusSpotMapper.selectById(id);
    }

    public boolean add(CampusSpot spot) {
        return campusSpotMapper.insert(spot) > 0;
    }

    public CampusSpot addPrivate(CampusSpot spot, Integer ownerId) {
        String normalizedName = spot.getName().trim();
        Long publicNameMatches = campusSpotMapper.selectCount(
                new QueryWrapper<CampusSpot>()
                        .eq("is_public", 1)
                        .apply("LOWER(name) = LOWER({0})", normalizedName));
        if (publicNameMatches > 0) {
            throw new IllegalArgumentException("私有交易点不能与公共交易点重名");
        }

        spot.setName(normalizedName);
        spot.setOwnerId(ownerId.longValue());
        spot.setIsPublic(0);
        spot.setSortOrder(0);
        if (campusSpotMapper.insert(spot) == 0) {
            throw new IllegalStateException("新增私有交易点失败");
        }
        return spot;
    }

    public boolean deletePublicById(Long id) {
        CampusSpot spot = getById(id);
        if (spot == null || !Integer.valueOf(1).equals(spot.getIsPublic())) {
            return false;
        }
        ensureNotInUse(id);
        return campusSpotMapper.deleteById(id) > 0;
    }

    public boolean deleteMyById(Long id, Integer ownerId) {
        CampusSpot spot = getById(id);
        if (spot == null || !Integer.valueOf(0).equals(spot.getIsPublic())
                || !Long.valueOf(ownerId.longValue()).equals(spot.getOwnerId())) {
            return false;
        }
        ensureNotInUse(id);
        return campusSpotMapper.deleteById(id) > 0;
    }

    public boolean isAvailableToUser(Long id, Integer userId) {
        if (id == null) {
            return false;
        }
        QueryWrapper<CampusSpot> query = new QueryWrapper<>();
        query.eq("id", id)
                .and(scope -> {
                    scope.eq("is_public", 1).or().isNull("is_public");
                    if (userId != null) {
                        scope.or().eq("owner_id", userId);
                    }
                });
        return campusSpotMapper.selectCount(query) > 0;
    }

    private void ensureNotInUse(Long id) {
        Long linkedProducts = productMapper.selectCount(
                new QueryWrapper<com.campus.trading.entity.Product>().eq("spot_id", id));
        if (linkedProducts > 0) {
            throw new IllegalArgumentException("该交易点已有商品使用，不能删除");
        }
    }
}
