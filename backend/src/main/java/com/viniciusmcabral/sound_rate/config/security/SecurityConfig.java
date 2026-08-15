package com.viniciusmcabral.sound_rate.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.viniciusmcabral.sound_rate.config.security.filters.SecurityFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

	private final SecurityFilter securityFilter;
	private final CorsConfigurationSource corsConfigurationSource;
	private final boolean apiDocsEnabled;
	private final boolean swaggerUiEnabled;

	public SecurityConfig(SecurityFilter securityFilter, CorsConfigurationSource corsConfigurationSource,
			@Value("${springdoc.api-docs.enabled:false}") boolean apiDocsEnabled,
			@Value("${springdoc.swagger-ui.enabled:false}") boolean swaggerUiEnabled) {
		this.securityFilter = securityFilter;
		this.corsConfigurationSource = corsConfigurationSource;
		this.apiDocsEnabled = apiDocsEnabled;
		this.swaggerUiEnabled = swaggerUiEnabled;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource))
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.authorizeHttpRequests(req -> {
					req.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
					req.requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll();
					req.requestMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll();
					req.requestMatchers(HttpMethod.GET, "/api/v1/albums/**").permitAll();
					req.requestMatchers(HttpMethod.GET, "/api/v1/artists/**").permitAll();
					req.requestMatchers(HttpMethod.GET, "/api/v1/search").permitAll();
					if (apiDocsEnabled || swaggerUiEnabled) {
						req.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();
					}
					req.anyRequest().authenticated();
				}).addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class).build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); 
	}
}
