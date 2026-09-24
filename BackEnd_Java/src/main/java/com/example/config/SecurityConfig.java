package com.example.config;

import com.example.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
// import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

@Configuration
public class SecurityConfig {

        @Autowired
        private JwtAuthFilter jwtFilter;

        @Autowired
        private OAuth2SuccessHandler oAuth2SuccessHandler;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http.cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .formLogin(form -> form.disable()) // form login disable temp
                                .httpBasic(basic -> basic.disable()) // basic disable
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**", "/oauth2/**", "/login/**").permitAll()
                                                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/tours/**", "/api/categories/**",
                                                                "/api/Search/**", "/images/**")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated()
                                ).oauth2Login(oauth -> oauth
                                                .successHandler(oAuth2SuccessHandler))
                                .exceptionHandling(ex -> ex
                                                .defaultAuthenticationEntryPointFor(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                                                request -> request.getRequestURI().startsWith("/api/")))

                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                return http.build();
        }

        // @Bean
        // public AuthenticationManager authenticationManager() {
        // return authentication -> authentication;
        // }
}
