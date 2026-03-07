package vn.dichvuangia.management.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.dichvuangia.management.entity.User;
import vn.dichvuangia.management.repository.UserRepository;

import java.util.List;

/**
 * Implement UserDetailsService để Spring Security dùng khi xác thực login.
 * Được gọi bởi AuthenticationManager → authenticate().
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy tài khoản: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                // enabled = isActive → Spring Security tự ném DisabledException nếu false
                .disabled(!user.getIsActive())
                .authorities(List.of(new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().getName())))
                .build();
    }
}
