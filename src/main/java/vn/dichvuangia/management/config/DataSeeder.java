package vn.dichvuangia.management.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.entity.Brand;
import vn.dichvuangia.management.entity.Role;
import vn.dichvuangia.management.entity.User;
import vn.dichvuangia.management.entity.Service;
import vn.dichvuangia.management.repository.BrandRepository;
import vn.dichvuangia.management.repository.RoleRepository;
import vn.dichvuangia.management.repository.ServiceRepository;
import vn.dichvuangia.management.repository.UserRepository;

import java.math.BigDecimal;

/**
 * Seed dữ liệu khởi đầu nếu DB trống.
 * Chỉ chạy khi chưa có Role nào trong DB (idempotent).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final ServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (roleRepository.count() > 0) {
            return;
        }

        log.info("DataSeeder: Bắt đầu khởi tạo dữ liệu...");

        // ── Roles ────────────────────────────────────────────────────────────
        Role adminRole    = saveRole("ADMIN");
        Role mgmtRole     = saveRole("MANAGEMENT");
        Role saleRole     = saveRole("SALE");
        Role techRole     = saveRole("TECHNICIAN");
        Role customerRole = saveRole("CUSTOMER");  // dùng cho self-register
        // Suppress unused: customerRole được seed sẵn, AuthService sẽ findByName("CUSTOMER")
        assert customerRole != null;

        // ── Users ────────────────────────────────────────────────────────────
        String pwd = passwordEncoder.encode("admin123");

        saveUser("admin",     pwd, adminRole);
        saveUser("manager01", pwd, mgmtRole);
        saveUser("sale01",    pwd, saleRole);
        saveUser("tech01",    pwd, techRole);

        // ── Brands ───────────────────────────────────────────────────────────
        saveBrand("Aqua",     "Thương hiệu máy lọc nước Aqua Nhật Bản");
        saveBrand("Kangaroo", "Thương hiệu máy lọc nước Kangaroo Việt Nam");
        saveBrand("Karofi",   "Thương hiệu máy lọc nước Karofi Việt Nam");
        saveBrand("Sunhouse", "Thương hiệu đồ gia dụng Sunhouse");

        // ── Services ─────────────────────────────────────────────────────────
        saveService("Vệ sinh bộ lọc",        "Vệ sinh và thay lõi lọc định kỳ",         new BigDecimal("150000"), 60);
        saveService("Sửa chữa máy lọc nước", "Kiểm tra và sửa chữa hỏng hóc máy lọc",  new BigDecimal("300000"), 120);
        saveService("Lắp đặt máy mới",       "Lắp đặt và hướng dẫn sử dụng máy mới",   new BigDecimal("200000"), 90);
        saveService("Thay màng RO",          "Thay màng RO sau 2-3 năm sử dụng",        new BigDecimal("400000"), 90);
        saveService("Kiểm tra định kỳ",      "Kiểm tra tổng quát hệ thống lọc nước",    new BigDecimal("80000"),  30);

        log.info("DataSeeder: Hoàn thành! Đã tạo 5 roles, 4 users, 4 brands, 5 services.");
        log.info("DataSeeder: Role CUSTOMER có sẵn cho khách hàng tự đăng ký.");
        log.info("DataSeeder: Tài khoản mặc định — username: admin / password: admin123");
    }

    private Role saveRole(String name) {
        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }

    private void saveUser(String username, String pwd, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(pwd);
        user.setRole(role);
        user.setIsActive(true);
        userRepository.save(user);
    }

    private void saveBrand(String name, String description) {
        Brand brand = new Brand();
        brand.setName(name);
        brand.setDescription(description);
        brand.setIsDeleted(false);
        brandRepository.save(brand);
    }

    private void saveService(String name, String description,
                             BigDecimal basePrice, int durationMinutes) {
        Service service = new Service();
        service.setName(name);
        service.setDescription(description);
        service.setBasePrice(basePrice);
        service.setDurationMinutes(durationMinutes);
        service.setIsDeleted(false);
        serviceRepository.save(service);
    }
}
