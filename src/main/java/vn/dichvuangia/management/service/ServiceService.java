package vn.dichvuangia.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.dto.request.ServiceCreateRequest;
import vn.dichvuangia.management.dto.request.ServiceUpdateRequest;
import vn.dichvuangia.management.dto.response.ServiceResponse;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.ServiceRepository;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ServiceService {

    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public Page<ServiceResponse> getAll(Pageable pageable) {
        return serviceRepository.findAllByIsDeletedFalse(pageable).map(ServiceService::toResponse);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getById(Long id) {
        return toResponse(findActiveService(id));
    }

    @Transactional
    public ServiceResponse create(ServiceCreateRequest request) {
        vn.dichvuangia.management.entity.Service service = new vn.dichvuangia.management.entity.Service();
        service.setServiceCode(request.getServiceCode());
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setBasePrice(request.getBasePrice());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setIsDeleted(false);

        return toResponse(serviceRepository.save(service));
    }

    @Transactional
    public ServiceResponse update(Long id, ServiceUpdateRequest request) {
        vn.dichvuangia.management.entity.Service service = findActiveService(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            service.setName(request.getName());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getBasePrice() != null) {
            service.setBasePrice(request.getBasePrice());
        }
        if (request.getDurationMinutes() != null) {
            service.setDurationMinutes(request.getDurationMinutes());
        }

        return toResponse(serviceRepository.save(service));
    }

    @Transactional
    public void softDelete(Long id) {
        vn.dichvuangia.management.entity.Service service = findActiveService(id);
        service.setIsDeleted(true);
        serviceRepository.save(service);
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    private vn.dichvuangia.management.entity.Service findActiveService(Long id) {
        return serviceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));
    }

    static ServiceResponse toResponse(vn.dichvuangia.management.entity.Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .serviceCode(service.getServiceCode())
                .name(service.getName())
                .description(service.getDescription())
                .basePrice(service.getBasePrice())
                .durationMinutes(service.getDurationMinutes())
                .createdAt(service.getCreatedAt())
                .build();
    }
}
