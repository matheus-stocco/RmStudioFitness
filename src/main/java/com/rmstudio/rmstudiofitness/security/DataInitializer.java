package com.rmstudio.rmstudiofitness.security;

import com.rmstudio.rmstudiofitness.entidades.Perfil;
import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.repositorios.PerfilRepository;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import com.rmstudio.rmstudiofitness.repositorios.EstadoRepository;
import com.rmstudio.rmstudiofitness.repositorios.CidadeRepository;
import com.rmstudio.rmstudiofitness.entidades.Estado;
import com.rmstudio.rmstudiofitness.entidades.Cidade;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PessoaRepository pessoaRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final EstadoRepository estadoRepository;
    private final CidadeRepository cidadeRepository;

    public DataInitializer(PessoaRepository pessoaRepository, PerfilRepository perfilRepository, PasswordEncoder passwordEncoder, EstadoRepository estadoRepository, CidadeRepository cidadeRepository) {
        this.pessoaRepository = pessoaRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
        this.estadoRepository = estadoRepository;
        this.cidadeRepository = cidadeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (perfilRepository.count() == 0) {
            Perfil adminPerfil = new Perfil();
            adminPerfil.setNome("ROLE_ADMIN");
            perfilRepository.save(adminPerfil);

            Perfil userPerfil = new Perfil();
            userPerfil.setNome("ROLE_USER");
            perfilRepository.save(userPerfil);
        }

        if (pessoaRepository.count() == 0) {
            
            Estado estadoPadrao = estadoRepository.findByUf("XX").orElseGet(() -> {
                Estado novoEstado = new Estado();
                novoEstado.setNome("Padrão");
                novoEstado.setUf("XX");
                return estadoRepository.save(novoEstado);
            });

            Cidade cidadePadrao = cidadeRepository.findAll().stream()
                .filter(c -> "Cidade Padrão".equalsIgnoreCase(c.getNome()))
                .findFirst()
                .orElseGet(() -> {
                    Cidade novaCidade = new Cidade();
                    novaCidade.setNome("Cidade Padrão");
                    novaCidade.setEstado(estadoPadrao);
                    return cidadeRepository.save(novaCidade);
                });

            Perfil adminPerfil = perfilRepository.findAll().stream().filter(p -> p.getNome().equals("ROLE_ADMIN")).findFirst().orElseThrow();

            Pessoa admin = new Pessoa();
            admin.setUsuario("admin");
            admin.setSenha(passwordEncoder.encode("admin"));
            admin.setNome("Administrador");
            admin.setEmail("admin@rmstudio.com");
            admin.setAtivo(true);
            admin.setCidade(cidadePadrao);
            admin.setPerfis(Set.of(adminPerfil));
            
            pessoaRepository.save(admin);
        }
    }
}
