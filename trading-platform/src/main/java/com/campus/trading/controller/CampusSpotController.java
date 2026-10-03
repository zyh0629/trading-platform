package com.campus.trading.controller;

import com.campus.trading.entity.CampusSpot;
import com.campus.trading.security.UserPrincipal;
import com.campus.trading.service.impl.CampusSpotService;
import com.campus.trading.utils.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campus-spot")
public class CampusSpotController {
    private final CampusSpotService campusSpotService;

    public CampusSpotController(CampusSpotService campusSpotService) {
        this.campusSpotService = campusSpotService;
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<List<CampusSpot>> listAll(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(campusSpotService.listAll(principal.getUserId()));
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> add(@RequestBody CampusSpot spot) {
        validateSpot(spot);
        spot.setOwnerId(null);
        spot.setIsPublic(1);
        if (spot.getSortOrder() == null) {
            spot.setSortOrder(0);
        }
        return campusSpotService.add(spot)
                ? Result.success("交易点已添加", null)
                : Result.error("交易点添加失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> delete(@PathVariable Long id) {
        return campusSpotService.deletePublicById(id)
                ? Result.success("交易点已删除", null)
                : Result.error("交易点不存在");
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Result<List<CampusSpot>> listMy(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(campusSpotService.listMy(principal.getUserId()));
    }

    @PostMapping("/my")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<CampusSpot> addMy(@RequestBody CampusSpot spot,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        validateSpot(spot);
        validateCoordinates(spot);
        return Result.success(campusSpotService.addPrivate(spot, principal.getUserId()));
    }

    @DeleteMapping("/my/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Result<String> deleteMy(@PathVariable Long id,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return campusSpotService.deleteMyById(id, principal.getUserId())
                ? Result.success("私有交易点已删除", null)
                : Result.error(400, "私有交易点不存在或不属于当前用户");
    }

    private void validateSpot(CampusSpot spot) {
        if (spot == null || spot.getName() == null || spot.getName().isBlank()) {
            throw new IllegalArgumentException("交易点名称不能为空");
        }
        String name = spot.getName().trim();
        if (name.length() > 50
                || (spot.getDescription() != null && spot.getDescription().length() > 200)) {
            throw new IllegalArgumentException("交易点名称或描述超出长度限制");
        }
        spot.setName(name);
    }

    private void validateCoordinates(CampusSpot spot) {
        if (spot.getLatitude() == null || spot.getLatitude().abs().compareTo(new java.math.BigDecimal("90")) > 0
                || spot.getLongitude() == null
                || spot.getLongitude().abs().compareTo(new java.math.BigDecimal("180")) > 0) {
            throw new IllegalArgumentException("交易点经纬度无效，请在地图上选择位置");
        }
    }
}
