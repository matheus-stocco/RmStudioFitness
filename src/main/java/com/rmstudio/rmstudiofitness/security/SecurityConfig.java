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
        return login -> {
            if (login.contains("@")) {
                return pessoaRepository.findByEmail(login)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário com e-mail " + login + " não encontrado."));
            }
            return pessoaRepository.findByUsuario(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário " + login + " não encontrado."));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/api/**") // Desabilita CSRF para todas as rotas de API
                )
                .authorizeHttpRequests(authorize -> authorize
                        // --- Permissões Públicas ---
                        .requestMatchers(
                                "/css/**", "/js/**", "/img/**", "/", "/login", "/autocadastro",
                                "/planos", "/quem-somos", "/error",
                                "/api/pagamentos/notificacao" // Webhook
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tipos-plano", "/api/estados/**", "/api/cidades/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pessoas").permitAll() // Autocadastro

                        // --- Permissões para Usuários Autenticados (Regras Específicas Primeiro) ---
                        .requestMatchers(HttpMethod.POST, "/planos/inscrever/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/pagamentos/gerar-pix/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pagamentos/pagar/**").authenticated()

                        // --- Permissões de Administrador (Regras Genéricas Depois) ---
                        .requestMatchers(
                            "/membros", "/tipos-plano", "/avaliacoes", "/cidades",
                            "/estados", "/exercicios", "/itens-plano", "/planos-aula", "/CadastroPessoas.html",
                            "/CadastroTipoPlano.html", "/CadastroAvaliacaoFisica.html", "/CadastroCidade.html",
                            "/CadastroEstado.html", "/CadastroExercicios.html", "/CadastroItensPlano.html",
                            "/CadastroPlanoAula.html"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        
                        // --- Permissões Gerais para Usuários Autenticados ---
                        .requestMatchers("/perfil", "/minhas-avaliacoes", "/meus-planos-aula", "/minhas-mensalidades").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginPage("/login") // Página de login customizada
                    .defaultSuccessUrl("/", true) // Redireciona para a home após o login
                    .permitAll()
                )
                .logout(logout ->
                    logout
                        .logoutSuccessUrl("/login?logout") // Redireciona para a página de login com parâmetro
                        .permitAll()
                )
                .rememberMe(rememberMe ->
                    rememberMe
                        .key("umaChaveMuitoSecreta") // Deve ser uma chave secreta e única
                        .tokenValiditySeconds(86400)  // Validade de 1 dia (em segundos)
                );

        return http.build();
    }
}
