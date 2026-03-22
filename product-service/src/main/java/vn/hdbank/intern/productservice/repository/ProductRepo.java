package vn.hdbank.intern.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hdbank.intern.productservice.model.Product;

public interface ProductRepo extends JpaRepository<Product, String> {
}
