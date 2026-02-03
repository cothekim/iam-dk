package com.iamdk.directory.security;

import com.iamdk.directory.entity.User;
import com.iamdk.directory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * JWT User Details Service
 */
@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLoginName(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.isLocked()) {
            throw new UsernameNotFoundException("Account is locked: " + username);
        }

        if (!user.getActive()) {
            throw new UsernameNotFoundException("Account is inactive: " + username);
        }

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getLoginName())
            .password(user.getPassword())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
            .build();
    }

    /**
     * Get user entity by username
     */
    public User getUserEntity(String username) {
        return userRepository.findByLoginName(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
