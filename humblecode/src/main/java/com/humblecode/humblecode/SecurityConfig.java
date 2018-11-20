package com.humblecode.humblecode;

import com.humblecode.humblecode.data.UserRepository;
import com.humblecode.humblecode.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.ArrayList;
import java.util.List;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange()
                .pathMatchers("/api/**", "/css/**", "/js/**", "/images/**", "/").permitAll()
                .pathMatchers("/user/**").hasAuthority("user")
                .and()
                .csrf()
                .and()
                .formLogin();
        return http.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(@Autowired UserRepository userRepository) {
        List<UserDetails> userDetails = new ArrayList<>();
        userDetails.addAll(userRepository.findAll().collectList().block());
        if (userDetails.isEmpty()) { // here for tests to work
            userDetails.add(new User("user1", "password"));
        }
        return new MapReactiveUserDetailsService(userDetails);
    }

    @Bean
    public PasswordEncoder myPasswordEncoder() {
        // never do this in production of course
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence charSequence) {
                return charSequence.toString();
            }
            @Override
            public boolean matches(CharSequence charSequence, String s) {
                return charSequence.equals(s);
            }
        };
    }

}
