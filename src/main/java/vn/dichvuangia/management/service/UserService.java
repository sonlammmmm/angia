package vn.dichvuangia.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.dto.request.UserCreateRequest;
import vn.dichvuangia.management.dto.request.UserUpdateRequest;
import vn.dichvuangia.management.dto.response.UserResponse;
import vn.dichvuangia.management.entity.Role;
import vn.dichvuangia.management.entity.User;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.RoleRepository;
import vn.dichvuangia.management.repository.UserRepository;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserService::toResponse);
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

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleId() != null) {
            user.setRole(findRoleById(request.getRoleId()));
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        return toResponse(userRepository.save(user));
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

    static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roleName(user.getRole().getName())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
