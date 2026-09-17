package com.campus.trading.controller;

import com.campus.trading.entity.Product;
import com.campus.trading.service.impl.ProductService;
import com.campus.trading.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/add")
    public String addProduct(@RequestBody Product product) {
        return productService.addProduct(product) ? "发布成功" : "发布失败";
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Integer id) {
        return productService.getProductById(id);
    }

    @GetMapping("/list")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // 获取所有商品（包括下架的，用于统计）
    @GetMapping("/all")
    public List<Product> getAllProductsForStats() {
        return productService.getAllProductsForStats();
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

    @GetMapping("/category/{categoryId}")
    public List<Product> getByCategory(@PathVariable Integer categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @GetMapping("/my/{userId}")
    public List<Product> getMyProducts(@PathVariable Integer userId) {
        return productService.getProductsByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Integer id, @RequestParam Integer userId) {
        return productService.updateProductStatus(id, 2) ? "下架成功" : "下架失败";
    }

    // 编辑商品
    @PutMapping("/update")
    public Result<String> updateProduct(@RequestBody Product product) {
        // 检查商品是否存在
        Product existProduct = productService.getProductById(product.getId());
        if (existProduct == null) {
            return Result.error("商品不存在");
        }
        // 更新商品
        product.setUpdateTime(LocalDateTime.now());
        boolean success = productService.updateProduct(product);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }
}