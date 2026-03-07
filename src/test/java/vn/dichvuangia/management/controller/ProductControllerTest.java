package vn.dichvuangia.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.dichvuangia.management.common.enums.ProductType;
import vn.dichvuangia.management.dto.request.ProductCreateRequest;
import vn.dichvuangia.management.dto.request.ProductUpdateRequest;
import vn.dichvuangia.management.dto.response.ProductResponse;
import vn.dichvuangia.management.security.SecurityConfig;
import vn.dichvuangia.management.security.UserDetailsServiceImpl;
import vn.dichvuangia.management.service.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private ProductResponse sampleProduct() {
        return ProductResponse.builder()
                .id(1L)
                .productCode("SP001")
                .productType(ProductType.MACHINE)
                .brandId(1L)
                .brandName("Kangaroo")
                .name("Máy lọc nước KG100")
                .price(new BigDecimal("5000000"))
                .stockQuantity(10)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── GET /products ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /products - public → 200")
    void getAll_public_returns200() throws Exception {
        when(productService.getAll(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleProduct())));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].productCode").value("SP001"));
    }

    @Test
    @DisplayName("GET /products?type=MACHINE - lọc theo type → 200")
    void getAll_withTypeFilter_returns200() throws Exception {
        when(productService.getAll(eq(ProductType.MACHINE), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleProduct())));

        mockMvc.perform(get("/products").param("type", "MACHINE"))
                .andExpect(status().isOk());
    }

    // ── GET /products/{id} ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /products/1 - public → 200")
    void getById_public_returns200() throws Exception {
        when(productService.getById(1L)).thenReturn(sampleProduct());

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Máy lọc nước KG100"));
    }

    // ── POST /products ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /products - ADMIN → 201")
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns201() throws Exception {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setProductCode("SP001");
        req.setProductType(ProductType.MACHINE);
        req.setBrandId(1L);
        req.setName("Máy lọc nước KG100");
        req.setPrice(new BigDecimal("5000000"));

        when(productService.create(any())).thenReturn(sampleProduct());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.productCode").value("SP001"));
    }

    @Test
    @DisplayName("POST /products - MANAGEMENT → 201")
    @WithMockUser(roles = "MANAGEMENT")
    void create_asManagement_returns201() throws Exception {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setProductCode("SP001");
        req.setProductType(ProductType.FILTER);
        req.setBrandId(1L);
        req.setName("Lõi lọc");
        req.setPrice(new BigDecimal("200000"));

        when(productService.create(any())).thenReturn(sampleProduct());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /products - không auth → 401")
    void create_noAuth_returns401() throws Exception {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setProductCode("SP001");
        req.setProductType(ProductType.MACHINE);
        req.setBrandId(1L);
        req.setName("Máy lọc nước KG100");
        req.setPrice(new BigDecimal("5000000"));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /products - thiếu field bắt buộc → 400")
    @WithMockUser(roles = "ADMIN")
    void create_missingRequiredFields_returns400() throws Exception {
        ProductCreateRequest req = new ProductCreateRequest();
        // thiếu productCode, productType, brandId, name, price

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /products/{id} ──────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /products/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void update_asAdmin_returns200() throws Exception {
        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName("Máy lọc nước Updated");

        when(productService.update(eq(1L), any())).thenReturn(sampleProduct());

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    // ── DELETE /products/{id} ────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /products/1 - ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void delete_asAdmin_returns200() throws Exception {
        doNothing().when(productService).softDelete(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đã xóa sản phẩm"));
    }

    @Test
    @DisplayName("DELETE /products/1 - MANAGEMENT → 403")
    @WithMockUser(roles = "MANAGEMENT")
    void delete_asManagement_returns403() throws Exception {
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isForbidden());
    }
}
