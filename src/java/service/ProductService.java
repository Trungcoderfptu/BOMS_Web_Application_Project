/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.ProductDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Product;
import utility.ValidationException;

/**
 *
 * @author AD
 */
public class ProductService {

    private ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public List<Product> getAllProducts() throws ValidationException {
        return productDAO.getAllProducts();
    }

    public Product getProductById(int productId) {
        if (productId <= 0) {
            return null;
        }
        return productDAO.getProductById(productId);
    }

    public List<Map<String, Object>> getAllProductsForView() throws ValidationException {
        List<Product> products = productDAO.getAllProducts();
        return buildProductListMap(products);
    }

    //========== create respon pack to other service ==========
    private List<Map<String, Object>> buildProductListMap(List<Product> products) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (products != null) {
            for (Product p : products) {
                result.add(buildSingleProductMap(p)); // Tách tiếp việc ép 1 Product ra hàm nhỏ hơn
            }
        }
        return result;
    }

    private Map<String, Object> buildSingleProductMap(Product p) {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", p.getProductId());
        map.put("productName", p.getProductName());
        map.put("price", p.getPrice());
        map.put("imageUrl", p.getImageUrl());
        map.put("available", p.isAvailable());
        return map;
    }
}
