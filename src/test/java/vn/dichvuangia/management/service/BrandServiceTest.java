package vn.dichvuangia.management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import vn.dichvuangia.management.dto.request.BrandCreateRequest;
import vn.dichvuangia.management.dto.request.BrandUpdateRequest;
import vn.dichvuangia.management.dto.response.BrandResponse;
import vn.dichvuangia.management.entity.Brand;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.BrandRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    private Brand brand;

    @BeforeEach
    void setUp() {
        brand = new Brand();
        brand.setId(1L);
        brand.setName("Kangaroo");
        brand.setDescription("Thuong hieu Viet");
        brand.setLogoUrl("http://logo.png");
        brand.setIsDeleted(false);
        brand.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAll - happy path")
    void getAll_happyPath() {
        Pageable pageable = PageRequest.of(0, 10);
        when(brandRepository.findAllByIsDeletedFalse(pageable))
                .thenReturn(new PageImpl<>(List.of(brand)));

        var page = brandService.getAll(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Kangaroo");
        verify(brandRepository).findAllByIsDeletedFalse(pageable);
    }

    @Test
    @DisplayName("getById - happy path")
    void getById_happyPath() {
        when(brandRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(brand));

        BrandResponse res = brandService.getById(1L);

        assertThat(res.getId()).isEqualTo(1L);
        assertThat(res.getName()).isEqualTo("Kangaroo");
        verify(brandRepository).findByIdAndIsDeletedFalse(1L);
    }

    @Test
    @DisplayName("getById - not found")
    void getById_notFound() {
        when(brandRepository.findByIdAndIsDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> brandService.getById(99L));
    }

    @Test
    @DisplayName("create - happy path")
    void create_happyPath() {
        BrandCreateRequest req = new BrandCreateRequest();
        req.setName("Kangaroo");
        req.setDescription("Thuong hieu Viet");
        req.setLogoUrl("http://logo.png");

        when(brandRepository.save(any(Brand.class))).thenReturn(brand);

        BrandResponse res = brandService.create(req);

        assertThat(res.getName()).isEqualTo("Kangaroo");
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    @DisplayName("update - happy path")
    void update_happyPath() {
        BrandUpdateRequest req = new BrandUpdateRequest();
        req.setName("Updated Name");
        req.setDescription("Updated Desc");
        req.setLogoUrl("http://new.png");

        when(brandRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(brand));
        when(brandRepository.save(any(Brand.class))).thenAnswer(inv -> inv.getArgument(0));

        BrandResponse res = brandService.update(1L, req);

        assertThat(res.getName()).isEqualTo("Updated Name");
        assertThat(res.getDescription()).isEqualTo("Updated Desc");
        assertThat(res.getLogoUrl()).isEqualTo("http://new.png");
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    @DisplayName("update - not found")
    void update_notFound() {
        BrandUpdateRequest req = new BrandUpdateRequest();
        req.setName("Updated Name");

        when(brandRepository.findByIdAndIsDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> brandService.update(99L, req));
    }

    @Test
    @DisplayName("softDelete - happy path")
    void softDelete_happyPath() {
        when(brandRepository.findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(brand));
        when(brandRepository.save(any(Brand.class))).thenAnswer(inv -> inv.getArgument(0));

        brandService.softDelete(1L);

        ArgumentCaptor<Brand> captor = ArgumentCaptor.forClass(Brand.class);
        verify(brandRepository).save(captor.capture());
        assertThat(captor.getValue().getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("softDelete - not found")
    void softDelete_notFound() {
        when(brandRepository.findByIdAndIsDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> brandService.softDelete(99L));
    }
}
