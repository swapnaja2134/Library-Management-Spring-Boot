
package com.example.librarymgnt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;


@Configuration
public class SecurityConfig {

	@Value("${app.cors.origins}")
	private String allowedOrigins;
    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
        	.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required or token invalid\",\"path\":\"" + request.getRequestURI() + "\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied: insufficient permissions\",\"path\":\"" + request.getRequestURI() + "\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/user/register",
                        "/user/login"
                )
                .permitAll()

                // Book viewing: accessible by all authenticated roles
                .requestMatchers(HttpMethod.GET, "/book/**")
                .hasAnyRole("ADMIN", "LIBRARIAN", "MEMBER")

                // Book creation & updates: ADMIN or LIBRARIAN
                .requestMatchers(HttpMethod.POST, "/book/**")
                .hasAnyRole("ADMIN", "LIBRARIAN")

                .requestMatchers(HttpMethod.PUT, "/book/**")
                .hasAnyRole("ADMIN", "LIBRARIAN")

                // Book deletion: ADMIN only
                .requestMatchers(HttpMethod.DELETE, "/book/**")
                .hasRole("ADMIN")

                // Borrow operations
                .requestMatchers(HttpMethod.POST, "/borrow/**")
                .hasRole("MEMBER")

                .requestMatchers(HttpMethod.PUT, "/borrow/**")
                .hasAnyRole("MEMBER", "LIBRARIAN", "ADMIN")

                .requestMatchers(HttpMethod.GET, "/borrow/my")
                .hasRole("MEMBER")

                .requestMatchers(HttpMethod.GET, "/borrow/all")
                .hasAnyRole("ADMIN", "LIBRARIAN")

                // Admin operations
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                .anyRequest()
                .authenticated()
            );

        http.addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}