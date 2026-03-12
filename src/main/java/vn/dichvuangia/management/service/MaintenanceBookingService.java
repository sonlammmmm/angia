package vn.dichvuangia.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.common.enums.BookingStatus;
import vn.dichvuangia.management.dto.request.BookingAssignRequest;
import vn.dichvuangia.management.dto.request.BookingCompleteRequest;
import vn.dichvuangia.management.dto.request.BookingCreateRequest;
import vn.dichvuangia.management.dto.response.BookingResponse;
import vn.dichvuangia.management.entity.Customer;
import vn.dichvuangia.management.entity.MaintenanceBooking;
import vn.dichvuangia.management.entity.User;
import vn.dichvuangia.management.exception.BookingAlreadyCompletedException;
import vn.dichvuangia.management.exception.InvalidStatusTransitionException;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.CustomerRepository;
import vn.dichvuangia.management.repository.MaintenanceBookingRepository;
import vn.dichvuangia.management.repository.ServiceRepository;
import vn.dichvuangia.management.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MaintenanceBookingService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Random RANDOM = new Random();

    private final MaintenanceBookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAll(BookingStatus status, LocalDateTime from,
                                        LocalDateTime to, Long customerId, Pageable pageable) {
        String scope = getCurrentScope();

        // TECHNICIAN: chỉ thấy lịch của mình
        if ("ROLE_TECHNICIAN".equals(scope)) {
            Long currentUserId = getCurrentUserId();
            return bookingRepository.findAllWithFilter(status, from, to, currentUserId, customerId, pageable)
                    .map(MaintenanceBookingService::toResponse);
        }

        // ADMIN / MANAGEMENT: thấy tất cả
        return bookingRepository.findAllWithFilter(status, from, to, null, customerId, pageable)
                .map(MaintenanceBookingService::toResponse);
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return toResponse(findBookingById(id));
    }

    /**
     * Tạo lịch: status = PENDING, technicianId = null.
     * Booking code = BK-{yyyyMMdd}-{5digits}.
     */
    @Transactional
    public BookingResponse create(BookingCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        vn.dichvuangia.management.entity.Service service = serviceRepository
                .findByIdAndIsDeletedFalse(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", request.getServiceId()));

        MaintenanceBooking booking = new MaintenanceBooking();
        booking.setBookingCode(generateBookingCode());
        booking.setCustomer(customer);
        booking.setService(service);
        booking.setBookingDate(request.getBookingDate());
        booking.setNotes(request.getNotes());
        booking.setStatus(BookingStatus.PENDING);

        return toResponse(bookingRepository.save(booking));
    }

    /**
     * Gán kỹ thuật viên: PENDING → CONFIRMED.
     * Chỉ ADMIN / MANAGEMENT gọi được (Controller enforce qua SecurityConfig).
     */
    @Transactional
    public BookingResponse assignTechnician(Long bookingId, BookingAssignRequest request) {
        MaintenanceBooking booking = findBookingById(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidStatusTransitionException(booking.getStatus().name(), BookingStatus.CONFIRMED.name());
        }

        User technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getTechnicianId()));

        booking.setTechnician(technician);
        booking.setStatus(BookingStatus.CONFIRMED);

        return toResponse(bookingRepository.save(booking));
    }

    /**
     * Hoàn thành lịch: CONFIRMED → COMPLETED.
     * Chỉ technician được gán mới được hoàn thành.
     */
    @Transactional
    public BookingResponse complete(Long bookingId, BookingCompleteRequest request) {
        MaintenanceBooking booking = findBookingById(bookingId);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BookingAlreadyCompletedException(bookingId);
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidStatusTransitionException(booking.getStatus().name(), BookingStatus.COMPLETED.name());
        }

        Long currentUserId = getCurrentUserId();
        if (booking.getTechnician() == null || !booking.getTechnician().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Chỉ kỹ thuật viên được gán mới có thể hoàn thành lịch này");
        }

        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            booking.setNotes(request.getNotes());
        }
        booking.setStatus(BookingStatus.COMPLETED);

        return toResponse(bookingRepository.save(booking));
    }

    /**
     * Hủy lịch: PENDING hoặc CONFIRMED → CANCELLED.
     * COMPLETED không thể hủy.
     */
    @Transactional
    public BookingResponse cancel(Long bookingId) {
        MaintenanceBooking booking = findBookingById(bookingId);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BookingAlreadyCompletedException(bookingId);
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(BookingStatus.CANCELLED.name(), BookingStatus.CANCELLED.name());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    private MaintenanceBooking findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceBooking", id));
    }

    private String generateBookingCode() {
        String date = LocalDateTime.now().format(DATE_FMT);
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = String.format("BK-%s-%05d", date, RANDOM.nextInt(100000));
            if (!bookingRepository.existsByBookingCode(code)) {
                return code;
            }
        }
        return "BK-" + date + "-" + System.currentTimeMillis() % 100000;
    }

    private Long getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("userId");
    }

    private String getCurrentScope() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("scope");
    }

    static BookingResponse toResponse(MaintenanceBooking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus())
                .bookingDate(booking.getBookingDate())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .customerId(booking.getCustomer().getId())
                .customerName(booking.getCustomer().getFullName())
                .customerPhone(booking.getCustomer().getPhone())
                .customerAddress(booking.getCustomer().getAddress())
                .serviceId(booking.getService().getId())
                .serviceName(booking.getService().getName())
                .serviceBasePrice(booking.getService().getBasePrice())
                .technicianId(booking.getTechnician() != null ? booking.getTechnician().getId() : null)
                .technicianUsername(booking.getTechnician() != null ? booking.getTechnician().getUsername() : null)
                .build();
    }
}
