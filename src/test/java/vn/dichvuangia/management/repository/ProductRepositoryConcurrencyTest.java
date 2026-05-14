package vn.dichvuangia.management.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import vn.dichvuangia.management.common.enums.ProductType;
import vn.dichvuangia.management.entity.Product;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryConcurrencyTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("decrementStockIfEnough: update thành công khi đủ tồn kho")
    void decrementStockIfEnough_success() {
        Product product = new Product();
        product.setName("Pump A");
        product.setProductCode("PUMP-A-001");
        product.setProductType(ProductType.MACHINE);
        product.setPrice(new BigDecimal("1200000"));
        product.setStockQuantity(10);
        product = productRepository.saveAndFlush(product);

        int updated = productRepository.decrementStockIfEnough(product.getId(), 4);
        Product refreshed = productRepository.findById(product.getId()).orElseThrow();

        assertThat(updated).isEqualTo(1);
        assertThat(refreshed.getStockQuantity()).isEqualTo(6);
    }

    @Test
    @DisplayName("decrementStockIfEnough: không update khi tồn kho không đủ")
    void decrementStockIfEnough_insufficientStock() {
        Product product = new Product();
        product.setName("Pump B");
        product.setProductCode("PUMP-B-001");
        product.setProductType(ProductType.MACHINE);
        product.setPrice(new BigDecimal("2200000"));
        product.setStockQuantity(2);
        product = productRepository.saveAndFlush(product);

        int updated = productRepository.decrementStockIfEnough(product.getId(), 3);
        Product refreshed = productRepository.findById(product.getId()).orElseThrow();

        assertThat(updated).isEqualTo(0);
        assertThat(refreshed.getStockQuantity()).isEqualTo(2);
    }
}
