package com.youngone.mediacontrol.config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
@Configuration public class SecurityConfig {
 @Bean SecurityFilterChain chain(HttpSecurity h)throws Exception{return h.csrf(c->c.ignoringRequestMatchers("/api/v1/agents/**")).authorizeHttpRequests(a->a.requestMatchers("/api/v1/agents/**","/actuator/health","/v3/api-docs/**","/swagger-ui/**").permitAll().anyRequest().authenticated()).httpBasic(Customizer.withDefaults()).build();}
}
