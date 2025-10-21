package com.rmstudio.rmstudiofitness.security;

import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PessoaRepository pessoaRepository) {
        return username -> pessoaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
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
                                "/api/pagamentos/notificacao", // Webhook
                                "/esqueceu-senha", "/recuperar-senha" // Recuperação de senha
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tipos-plano", "/api/estados/**", "/api/cidades/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pessoas").permitAll() // Autocadastro
                        .requestMatchers(HttpMethod.POST, "/esqueceu-senha", "/recuperar-senha").permitAll() // Recuperação de senha

                        // --- Permissões para Personal ---
                        .requestMatchers(
                            "/avaliacoes", "/planos-aula", "/exercicios", "/itens-plano",
                            "/CadastroAvaliacaoFisica.html", "/CadastroPlanoAula.html", "/CadastroExercicios.html",
                            "/CadastroItensPlano.html" // ItensPlano is related to PlanoAula
                        ).hasAnyRole("ADMIN", "PERSONAL")
                        .requestMatchers(HttpMethod.GET, "/api/pessoas", "/api/pessoas/alunos").hasAnyRole("ADMIN", "PERSONAL")
                        .requestMatchers(HttpMethod.GET, "/api/avaliacoes/**", "/api/planos-aula/**", "/api/exercicios/**").hasAnyRole("ADMIN", "PERSONAL")
                        .requestMatchers(HttpMethod.POST, "/api/avaliacoes/**", "/api/planos-aula/**", "/api/exercicios/**").hasAnyRole("ADMIN", "PERSONAL")
                        .requestMatchers(HttpMethod.PUT, "/api/avaliacoes/**", "/api/planos-aula/**", "/api/exercicios/**").hasAnyRole("ADMIN", "PERSONAL")
                        .requestMatchers(HttpMethod.DELETE, "/api/avaliacoes/**", "/api/planos-aula/**", "/api/exercicios/**").hasAnyRole("ADMIN", "PERSONAL")


                        // --- Permissões de Administrador (Regras Genéricas Depois) ---
                        .requestMatchers(
                            "/membros", "/tipos-plano", "/cidades",
                            "/estados", "/CadastroPessoas.html",
                            "/CadastroTipoPlano.html", "/CadastroCidade.html",
                            "/CadastroEstado.html", "/gerenciamento-roles"
                        ).hasRole("ADMIN")
                        .requestMatchers("/api/gerenciamento/**", "/api/pagamentos/**", "/api/tipos-plano/**", "/api/cidades/**", "/api/estados/**", "/api/pessoas/**").hasRole("ADMIN")

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
