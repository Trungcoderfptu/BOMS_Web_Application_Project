/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.ArrayList;
import java.util.List;
import model.Product;
import utility.ValidationException;

/**
 *
 * @author AD
 */
public class ProductDAO {

    private static List<Product> mockProductList = new ArrayList<>();

    static {
        mockProductList.add(new Product(1, 1, "Bá mía đè tem", 25000, "kem_tranchau.jpg", true));
        mockProductList.add(new Product(2, 1, "Khô gà loại 2", 15000, "kem_ocque.jpg", true));
        mockProductList.add(new Product(3, 2, "Nước rau má", 35000, "ts_duongden.jpg", true));
        mockProductList.add(new Product(4, 2, "Độ MIXI", 30000, "tra_oolongvai.jpg", true));

        mockProductList.add(new Product(5, 2, "Trà Đào Cam Sả", 35000, "tra_dao.jpg", false));
    }

    public List<Product> getAllProducts() throws ValidationException {
        return mockProductList;
    }

    public Product getProductById(int productId) {
        for (Product product : mockProductList) {
            if (product.getProductId() == productId) {
                return product;
            }
        }
        return null;
    }

    public synchronized void updateProductAvailability(int productId, boolean isAvailable) {
        Product product = getProductById(productId);
        if (product != null) {
            product.setAvailable(isAvailable);
        }
    }
}
