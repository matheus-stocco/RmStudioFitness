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
                    // ===============================================================================================
                    // REGRAS PÚBLICAS (EXECUTADAS PRIMEIRO)
                    // Liberar acesso a recursos estáticos (CSS, JS, Imagens) para todos.
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                    // Liberar acesso às páginas públicas principais.
                    .requestMatchers("/", "/index.html", "/planos", "/TiposDePlanos.html").permitAll()
                    // Liberar acesso às páginas de login e autocadastro.
                    .requestMatchers("/login", "/error", "/autocadastro", "/FormularioAutoCadastro.html").permitAll()
                    // Liberar acesso às APIs públicas necessárias para o frontend.
                    .requestMatchers(HttpMethod.GET, "/api/tipos-plano", "/api/estados/**", "/api/cidades/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/pessoas").permitAll() // API de autocadastro
 
                    // ===============================================================================================
                    // REGRAS DE ADMIN (Páginas e APIs de escrita)
                    // Apenas usuários com perfil ADMIN podem acessar estas páginas de gerenciamento.
                    .requestMatchers(
                        "/membros", "/tipos-plano", "/avaliacoes", "/cidades",
                        "/estados", "/exercicios", "/itens-plano", "/planos-aula"
                    ).hasRole("ADMIN")
                    // Apenas ADMINs podem realizar operações de escrita (POST, PUT, DELETE) na API.
                    .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
 
                    // ===============================================================================================
                    // REGRAS PARA USUÁRIOS AUTENTICADOS
                    // Qualquer outra requisição que não se encaixou nas regras acima exige que o usuário esteja autenticado.
                    // Isso inclui páginas como /perfil e /minhas-avaliacoes.
                    .anyRequest().authenticated()
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login") // Página de login customizada
                    .defaultSuccessUrl("/", true) // Redireciona para a home após o login
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
