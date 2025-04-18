package uma.informatica.sii.gestor_productos.microservice_gestor_productos.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration{
    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(cs -> cs.disable())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) 
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2-console/**", "/auth/login").permitAll()
            .anyRequest().authenticated()
        )
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    
        http.addFilterAfter(jwtRequestFilter, LogoutFilter.class);
        return http.build();
    }
    

    

    public static Optional<UserDetails> getAuthenticatedUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(authentication -> (UserDetails) authentication.getPrincipal());
    }
}
