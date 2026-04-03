package vn.dichvuangia.management.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.common.enums.ProductType;
import vn.dichvuangia.management.dto.request.ProductCreateRequest;
import vn.dichvuangia.management.dto.request.ProductUpdateRequest;
import vn.dichvuangia.management.dto.response.ProductImageResponse;
import vn.dichvuangia.management.dto.response.ProductResponse;
import vn.dichvuangia.management.entity.Brand;
import vn.dichvuangia.management.entity.Product;
import vn.dichvuangia.management.entity.ProductImage;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.BrandRepository;
import vn.dichvuangia.management.repository.ProductImageRepository;
import vn.dichvuangia.management.repository.ProductRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final BrandRepository brandRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(ProductType type, Long brandId, String q, Pageable pageable) {
        String searchTerm = (q != null && !q.isBlank()) ? q.trim() : null;
        return productRepository.findAllWithFilter(type, brandId, searchTerm, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return toResponse(findActiveProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalArgumentException("Mã sản phẩm '" + request.getProductCode() + "' đã tồn tại");
        }

        Brand brand = findActiveBrand(request.getBrandId());

        Product product = new Product();
        product.setProductCode(request.getProductCode());
        product.setProductType(request.getProductType());
        product.setBrand(brand);
        product.setName(request.getName());
        product.setModel(request.getModel());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        product.setWarrantyMonths(request.getWarrantyMonths());
        product.setLifespanMonths(request.getLifespanMonths());
        product.setIsDeleted(false);
        product.setSpecsJson(serializeSpecs(request.getSpecsJson()));

        Product saved = productRepository.save(product);

        // Lưu ảnh sản phẩm nếu có
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            saveProductImages(saved, request.getImageUrls());
        }

        return toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = findActiveProduct(id);

        if (request.getBrandId() != null) {
            product.setBrand(findActiveBrand(request.getBrandId()));
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getModel() != null) {
            product.setModel(request.getModel());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getWarrantyMonths() != null) {
            product.setWarrantyMonths(request.getWarrantyMonths());
        }
        if (request.getLifespanMonths() != null) {
            product.setLifespanMonths(request.getLifespanMonths());
        }
        if (request.getSpecsJson() != null) {
            product.setSpecsJson(serializeSpecs(request.getSpecsJson()));
        }

        // Cập nhật ảnh: xóa ảnh cũ, thêm ảnh mới
        if (request.getImageUrls() != null) {
            productImageRepository.deleteAll(product.getImages());
            product.getImages().clear();
            if (!request.getImageUrls().isEmpty()) {
                saveProductImages(product, request.getImageUrls());
            }
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void softDelete(Long id) {
        Product product = findActiveProduct(id);
        product.setIsDeleted(true);
        productRepository.save(product);
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    /**
     * Lưu danh sách URL ảnh cho sản phẩm. Phần tử đầu tiên = ảnh chính (isMain=true).
     */
    private void saveProductImages(Product product, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage img = new ProductImage();
            img.setProduct(product);
            img.setImageUrl(imageUrls.get(i));
            img.setIsMain(i == 0);
            productImageRepository.save(img);
            product.getImages().add(img);
        }
    }

    private Product findActiveProduct(Long id) {
        return productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private Brand findActiveBrand(Long brandId) {
        return brandRepository.findByIdAndIsDeletedFalse(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", brandId));
    }

    /**
     * Map → JSON String để lưu vào cột specs_json.
     * null input → null DB value (theo Q4=A).
     */
    private String serializeSpecs(Map<String, Object> specs) {
        if (specs == null) return null;
        try {
            return objectMapper.writeValueAsString(specs);
        } catch (Exception e) {
            log.warn("Không thể serialize specsJson, lưu null: {}", e.getMessage());
            return null;
        }
    }

    /**
     * JSON String → Map để trả về response.
     * null/lỗi → trả empty map.
     */
    private Map<String, Object> deserializeSpecs(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Không thể deserialize specsJson: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private ProductResponse toResponse(Product product) {
        List<ProductImageResponse> images = product.getImages().stream()
                .map(img -> ProductImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .isMain(img.getIsMain())
                        .build())
                .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .productType(product.getProductType())
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .name(product.getName())
                .model(product.getModel())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .warrantyMonths(product.getWarrantyMonths())
                .lifespanMonths(product.getLifespanMonths())
                .specsJson(deserializeSpecs(product.getSpecsJson()))
                .images(images)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
