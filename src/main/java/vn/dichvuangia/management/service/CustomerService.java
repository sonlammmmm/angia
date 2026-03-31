package vn.dichvuangia.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.dto.request.CustomerCreateRequest;
import vn.dichvuangia.management.dto.request.CustomerUpdateRequest;
import vn.dichvuangia.management.dto.response.BookingResponse;
import vn.dichvuangia.management.dto.response.CustomerResponse;
import vn.dichvuangia.management.entity.Customer;
import vn.dichvuangia.management.entity.Role;
import vn.dichvuangia.management.entity.User;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.CustomerRepository;
import vn.dichvuangia.management.repository.MaintenanceBookingRepository;
import vn.dichvuangia.management.repository.RoleRepository;
import vn.dichvuangia.management.repository.UserRepository;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final MaintenanceBookingRepository bookingRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * ADMIN / MANAGEMENT → thấy tất cả khách hàng.
     * SALE → chỉ thấy khách do mình tạo (data-level security).
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAll(Pageable pageable) {
        String scope = getCurrentScope();

        if ("ROLE_SALE".equals(scope)) {
            Long currentUserId = getCurrentUserId();
            return customerRepository.findAllByCreatedBy_Id(currentUserId, pageable)
                    .map(CustomerService::toResponse);
        }

        return customerRepository.findAll(pageable).map(CustomerService::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return toResponse(findCustomerById(id));
    }

    @Transactional
    public CustomerResponse create(CustomerCreateRequest request) {
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại '" + request.getPhone() + "' đã tồn tại");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã tồn tại");
        }

        Long currentUserId = getCurrentUserId();
        User createdBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

        // Nếu có username/password → tạo tài khoản User cho khách hàng
        User customerUser = null;
        if (request.getUsername() != null && !request.getUsername().isBlank()
                && request.getPassword() != null && !request.getPassword().isBlank()) {

            // Validate confirm password
            if (request.getConfirmPassword() == null
                    || !request.getPassword().equals(request.getConfirmPassword())) {
                throw new IllegalArgumentException("Mật khẩu và xác nhận mật khẩu không khớp");
            }

            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
            }

            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER chưa được khởi tạo"));

            customerUser = new User();
            customerUser.setUsername(request.getUsername());
            customerUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            customerUser.setRole(customerRole);
            customerUser.setIsActive(true);
            customerUser = userRepository.save(customerUser);
        }

        Customer customer = new Customer();
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setEmail(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail() : null);
        // createdBy = tài khoản khách hàng nếu có, ngược lại = nhân viên tạo
        customer.setCreatedBy(customerUser != null ? customerUser : createdBy);

        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerUpdateRequest request) {
        Customer customer = findCustomerById(id);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            customer.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (!request.getPhone().equals(customer.getPhone())
                    && customerRepository.existsByPhone(request.getPhone())) {
                throw new IllegalArgumentException("Số điện thoại '" + request.getPhone() + "' đã tồn tại");
            }
            customer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getEmail() != null) {
            if (!request.getEmail().isBlank()
                    && !request.getEmail().equals(customer.getEmail())
                    && customerRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã tồn tại");
            }
            customer.setEmail(request.getEmail().isBlank() ? null : request.getEmail());
        }

        return toResponse(customerRepository.save(customer));
    }

    // ─── JWT context helpers ────────────────────────────────────────────────────

    /**
     * Khách hàng (CUSTOMER) xem hồ sơ của chính mình — GET /customers/me
     */
    @Transactional(readOnly = true)
    public CustomerResponse getMyProfile() {
        Long currentUserId = getCurrentUserId();
        Customer customer = customerRepository.findByCreatedBy_Id(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ khách hàng cho tài khoản hiện tại"));
        return toResponse(customer);
    }

    /**
     * Khách hàng (CUSTOMER) cập nhật hồ sơ của chính mình — PUT /customers/me
     */
    @Transactional
    public CustomerResponse updateMyProfile(CustomerUpdateRequest request) {
        Long currentUserId = getCurrentUserId();
        Customer customer = customerRepository.findByCreatedBy_Id(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ khách hàng cho tài khoản hiện tại"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            customer.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (!request.getPhone().equals(customer.getPhone())
                    && customerRepository.existsByPhone(request.getPhone())) {
                throw new IllegalArgumentException("Số điện thoại '" + request.getPhone() + "' đã tồn tại");
            }
            customer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getEmail() != null) {
            if (!request.getEmail().isBlank()
                    && !request.getEmail().equals(customer.getEmail())
                    && customerRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã tồn tại");
            }
            customer.setEmail(request.getEmail().isBlank() ? null : request.getEmail());
        }

        return toResponse(customerRepository.save(customer));
    }

    /** Lấy userId từ claim "userId" trong Access Token. */
    private Long getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("userId");
    }

    /**
     * Lấy scope từ claim "scope" (ví dụ: "ROLE_SALE", "ROLE_ADMIN").
     * Spring OAuth2 Resource Server tự set Principal = Jwt.
     */
    private String getCurrentScope() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("scope");
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    /** Lịch sử đặt lịch bảo trì của một khách hàng — GET /customers/{id}/bookings */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookings(Long customerId, Pageable pageable) {
        // Đảm bảo customer tồn tại trước
        findCustomerById(customerId);
        return bookingRepository.findAllByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(MaintenanceBookingService::toResponse);
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    static CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .createdAt(customer.getCreatedAt())
                .createdById(customer.getCreatedBy() != null ? customer.getCreatedBy().getId() : null)
                .createdByUsername(customer.getCreatedBy() != null ? customer.getCreatedBy().getUsername() : null)
                .build();
    }
}
