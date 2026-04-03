package vn.dichvuangia.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.dto.request.UserCreateRequest;
import vn.dichvuangia.management.dto.request.UserUpdateRequest;
import vn.dichvuangia.management.dto.response.UserResponse;
import vn.dichvuangia.management.entity.Role;
import vn.dichvuangia.management.entity.User;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.CustomerRepository;
import vn.dichvuangia.management.repository.RoleRepository;
import vn.dichvuangia.management.repository.UserRepository;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAllStaff(pageable).map(this::toResponseWithFullName);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(findUserById(id));
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }

        Role role = findRoleById(request.getRoleId());
        validateRoleAssignment(role);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setIsActive(true);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findUserById(id);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().isBlank() ? null : request.getFullName().trim());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleId() != null) {
            Role role = findRoleById(request.getRoleId());
            validateRoleAssignment(role);
            user.setRole(role);
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        return toResponseWithFullName(userRepository.save(user));
    }

    @Transactional
    public void lock(Long id) {
        User user = findUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void unlock(Long id) {
        User user = findUserById(id);
        user.setIsActive(true);
        userRepository.save(user);
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Role findRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
    }

    /**
     * MANAGEMENT không được phép gán role ADMIN.
     * Chỉ ADMIN mới được gán role ADMIN cho user khác.
     */
    private void validateRoleAssignment(Role targetRole) {
        if ("ADMIN".equals(targetRole.getName())) {
            Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String currentScope = jwt.getClaim("scope");
            if (!"ROLE_ADMIN".equals(currentScope)) {
                throw new AccessDeniedException("Chỉ ADMIN mới được gán vai trò ADMIN");
            }
        }
    }

    static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roleName(user.getRole().getName())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserResponse toResponseWithFullName(User user) {
        String fullName = user.getFullName(); // fullName trực tiếp trên User
        if (fullName == null && "CUSTOMER".equals(user.getRole().getName())) {
            // Fallback: lấy từ bảng Customer nếu là role CUSTOMER
            fullName = customerRepository.findByCreatedBy_Id(user.getId())
                    .map(c -> c.getFullName())
                    .orElse(null);
        }
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(fullName)
                .roleName(user.getRole().getName())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
