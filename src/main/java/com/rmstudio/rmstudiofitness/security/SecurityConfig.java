package com.rmstudio.rmstudiofitness.security;

import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PessoaRepository pessoaRepository) {
        return username -> pessoaRepository.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    // Recursos estáticos e páginas públicas
                    .requestMatchers("/", "/index.html", "/TiposDePlanos.html", "/planos").permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll() 
                    .requestMatchers("/login", "/error", "/autocadastro", "/FormularioAutoCadastro.html").permitAll()

                    // APIs públicas
                    .requestMatchers(HttpMethod.GET, "/api/tipos-plano").permitAll() // API de planos para a home
                    .requestMatchers(HttpMethod.POST, "/api/pessoas").permitAll() // Endpoint de autocadastro
                    .requestMatchers(HttpMethod.GET, "/api/estados/**", "/api/cidades/**").permitAll() // APIs para o formulário
                    
                    // Acesso ADMIN para APIs de escrita (garante que só admins modifiquem dados)
                    .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                    .anyRequest().authenticated() // Todas as outras requisições exigem autenticação
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login") // Página de login customizada
                    .defaultSuccessUrl("/perfil", true) // Redireciona para a home após o login
                    .permitAll()
            )
            .logout(logout ->
                logout
                    .logoutSuccessUrl("/login?logout") // Redireciona para a página de login com parâmetro
                    .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Desabilitar CSRF para simplificar, mas não recomendado em produção

        return http.build();
    }
}
