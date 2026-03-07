package vn.dichvuangia.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.dto.request.BrandCreateRequest;
import vn.dichvuangia.management.dto.request.BrandUpdateRequest;
import vn.dichvuangia.management.dto.response.BrandResponse;
import vn.dichvuangia.management.entity.Brand;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.BrandRepository;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public Page<BrandResponse> getAll(Pageable pageable) {
        return brandRepository.findAllByIsDeletedFalse(pageable).map(BrandService::toResponse);
    }

    @Transactional(readOnly = true)
    public BrandResponse getById(Long id) {
        return toResponse(findActiveBrand(id));
    }

    @Transactional
    public BrandResponse create(BrandCreateRequest request) {
        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setIsDeleted(false);

        return toResponse(brandRepository.save(brand));
    }

    @Transactional
    public BrandResponse update(Long id, BrandUpdateRequest request) {
        Brand brand = findActiveBrand(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            brand.setName(request.getName());
        }
        if (request.getDescription() != null) {
            brand.setDescription(request.getDescription());
        }
        if (request.getLogoUrl() != null) {
            brand.setLogoUrl(request.getLogoUrl());
        }

        return toResponse(brandRepository.save(brand));
    }

    @Transactional
    public void softDelete(Long id) {
        Brand brand = findActiveBrand(id);
        brand.setIsDeleted(true);
        brandRepository.save(brand);
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    private Brand findActiveBrand(Long id) {
        return brandRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", id));
    }

    static BrandResponse toResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .createdAt(brand.getCreatedAt())
                .build();
    }
}
