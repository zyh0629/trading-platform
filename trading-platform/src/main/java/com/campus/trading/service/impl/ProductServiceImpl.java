package com.campus.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trading.entity.Product;
import com.campus.trading.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public boolean addProduct(Product product) {
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
        }
        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.orderByDesc("create_time");
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.like("title", keyword);
        wrapper.orderByDesc("create_time");
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> getProductsByCategory(Integer categoryId) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.eq("category_id", categoryId);
        wrapper.orderByDesc("create_time");
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> getProductsByUserId(Integer userId) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> getAllProductsForStats() {
        return productMapper.selectList(null);
    }

    @Override
    public boolean updateProduct(Product product) {
        product.setUpdateTime(LocalDateTime.now());
        return productMapper.updateById(product) > 0;
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