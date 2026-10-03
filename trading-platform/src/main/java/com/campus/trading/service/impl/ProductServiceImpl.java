package com.campus.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.ai.AiAssistantService;
import com.campus.trading.entity.CampusSpot;
import com.campus.trading.entity.Product;
import com.campus.trading.mapper.ProductMapper;
import com.campus.trading.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AiAssistantService aiAssistantService;

    @Autowired
    private CampusSpotService campusSpotService;

    @Override
    public boolean addProduct(Product product, Integer currentUserId) {
        validateSpot(product.getSpotId(), currentUserId);
        auditProduct(product);
        product.setStatus(0);
        product.setViews(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        return productMapper.insert(product) > 0;
    }

    @Override
    public Product getProductById(Integer id) {
        Product product = productMapper.selectById(id);
        if (product != null) {
            product.setViews(product.getViews() + 1);
            productMapper.updateById(product);
            attachSpot(product);
        }
        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        return getAllProducts(null);
    }

    @Override
    public List<Product> getAllProducts(Long spotId) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        if (spotId != null) {
            wrapper.eq("spot_id", spotId);
        }
        wrapper.orderByDesc("create_time");
        return attachSpots(productMapper.selectList(wrapper));
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.like("title", keyword);
        wrapper.orderByDesc("create_time");
        return attachSpots(productMapper.selectList(wrapper));
    }

    @Override
    public List<Product> getProductsByCategory(Integer categoryId) {
        return getProductsByCategory(categoryId, null);
    }

    @Override
    public List<Product> getProductsByCategory(Integer categoryId, Long spotId) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.eq("category_id", categoryId);
        if (spotId != null) {
            wrapper.eq("spot_id", spotId);
        }
        wrapper.orderByDesc("create_time");
        return attachSpots(productMapper.selectList(wrapper));
    }

    @Override
    public List<Product> getProductsByUserId(Integer userId) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return attachSpots(productMapper.selectList(wrapper));
    }

    @Override
    public List<Product> getAllProductsForStats() {
        return attachSpots(productMapper.selectList(null));
    }

    @Override
    public boolean updateProduct(Product product, Integer currentUserId) {
        validateSpot(product.getSpotId(), currentUserId);
        auditProduct(product);
        product.setUpdateTime(LocalDateTime.now());
        return productMapper.updateById(product) > 0;
    }

    private void auditProduct(Product product) {
        AiAssistantService.AuditResult auditResult =
                aiAssistantService.auditProduct(product.getTitle(), product.getDescription());
        if (!auditResult.passed()) {
            throw new IllegalArgumentException("商品未通过审核：" + auditResult.reason());
        }
    }

    private void validateSpot(Long spotId, Integer currentUserId) {
        if (!campusSpotService.isAvailableToUser(spotId, currentUserId)) {
            throw new IllegalArgumentException("请选择公共交易点或属于自己的私有交易点");
        }
    }

    private List<Product> attachSpots(List<Product> products) {
        if (products.isEmpty()) {
            return products;
        }
        Map<Long, CampusSpot> spotById = campusSpotService.listAll(authenticatedUserId()).stream()
                .collect(Collectors.toMap(CampusSpot::getId, Function.identity()));
        for (Product product : products) {
            CampusSpot spot = spotById.get(product.getSpotId());
            if (spot != null) {
                product.setSpotName(spot.getName());
                product.setSpotDescription(spot.getDescription());
            }
        }
        return products;
    }

    private void attachSpot(Product product) {
        if (!campusSpotService.isAvailableToUser(product.getSpotId(), authenticatedUserId())) {
            return;
        }
        CampusSpot spot = campusSpotService.getById(product.getSpotId());
        if (spot != null) {
            product.setSpotName(spot.getName());
            product.setSpotDescription(spot.getDescription());
        }
    }

    private Integer authenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUserId();
        }
        return null;
    }

    @Override
    public boolean deleteProduct(Integer id, Integer userId) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
        return productMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean updateProductStatus(Integer id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        return productMapper.updateById(product) > 0;
    }
}