package com.campus.trading.service.impl;

import com.campus.trading.entity.Product;
import java.util.List;

public interface ProductService {
    boolean addProduct(Product product);
    Product getProductById(Integer id);
    List<Product> getAllProducts();
    List<Product> searchProducts(String keyword);
    List<Product> getProductsByCategory(Integer categoryId);
    List<Product> getProductsByUserId(Integer userId);
    List<Product> getAllProductsForStats();
    boolean deleteProduct(Integer id, Integer userId);
    boolean updateProductStatus(Integer id, Integer status);
    boolean updateProduct(Product product);
}