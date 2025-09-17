package lt.nsg.scroogebank.config;

import lt.nsg.scroogebank.auth.ApiKeyAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, proxyTargetClass = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationProvider authProvider, BasicAuthenticationFilter basicAuthFilter) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(registry ->
//                        registry.requestMatchers("/v3/**").permitAll()
//                                .requestMatchers("/swagger-ui/**").permitAll()
//                                .requestMatchers("/**").authenticated()
//                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .addFilterBefore(basicAuthFilter, AnonymousAuthenticationFilter.class)
                .build();
    }

    @Bean
    public BasicAuthenticationFilter basicAuthFilter(AuthenticationProvider authProvider) {
        BasicAuthenticationFilter basicAuthenticationFilter = new BasicAuthenticationFilter(new ProviderManager(authProvider));
        basicAuthenticationFilter.setAuthenticationConverter(authenticationConverter());
        return basicAuthenticationFilter;
    }

    @Bean
    ApiKeyAuthenticationConverter authenticationConverter() {
        return new ApiKeyAuthenticationConverter();
    }
}
