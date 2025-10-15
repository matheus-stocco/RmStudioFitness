package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.entidades.Cidade;
import com.rmstudio.rmstudiofitness.entidades.Perfil;
import com.rmstudio.rmstudiofitness.repositorios.CidadeRepository;
import com.rmstudio.rmstudiofitness.repositorios.PerfilRepository;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.server.ResponseStatusException;

/**
 * API REST para Pessoas.
 *
 * Endpoints:
 *  GET    /api/pessoas                           -> lista (filtros: ?q=&cidadeId=&estadoId=)
 *  GET    /api/pessoas/{id}                      -> detalhe (com cidade/estado)
 *  POST   /api/pessoas                           -> cria
 *  PUT    /api/pessoas/{id}                      -> atualiza
 *  DELETE /api/pessoas/{id}                      -> exclui
 */
@RestController
@RequestMapping("/api/pessoas")
public class ControlePessoa {

    private final PasswordEncoder passwordEncoder;
    private final PerfilRepository perfilRepository;
    private final PessoaRepository pessoaRepository;
    private final CidadeRepository cidadeRepository;

    public ControlePessoa(PasswordEncoder passwordEncoder, PerfilRepository perfilRepository, PessoaRepository pessoaRepository, CidadeRepository cidadeRepository) {
        this.passwordEncoder = passwordEncoder;
        this.perfilRepository = perfilRepository;
        this.pessoaRepository = pessoaRepository;
        this.cidadeRepository = cidadeRepository;
    }

    /** Payload para criação/atualização. */
    public record PessoaPayload(
            String usuario,
            String senha,
            String nome,
            String email,
            String telefone,
            String genero,
            LocalDate dataNascimento,
            String cpf,
            CidadePayload cidade
    ) {
        public record CidadePayload(Long id){}
    }

    // ===== LISTAR (com filtros opcionais) =====
    @GetMapping
    public Page<Pessoa> listar(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cidadeId", required = false) Long cidadeId,
            @RequestParam(name = "estadoId", required = false) Long estadoId,
            Pageable pageable
    ) {
        // Esta é uma implementação simplificada usando repositórios.
        // Para filtros complexos, uma abordagem com Criteria API ou Querydsl seria mais robusta.
        if (cidadeId != null) {
            return pessoaRepository.findByCidadeIdOrderByNome(cidadeId, pageable);
        }
        if (estadoId != null) {
            return pessoaRepository.findByEstadoId(estadoId, pageable);
        }
        if (q != null && !q.trim().isEmpty()){
            return pessoaRepository.findByNomeContainingIgnoreCase(q.trim(), pageable);
        }
        return pessoaRepository.findAllByOrderByNome(pageable);
    }

    // ===== DETALHE =====
    @GetMapping("/{id}")
    public ResponseEntity<Pessoa> buscar(@PathVariable Long id) {
        return pessoaRepository.findByIdWithCidadeAndEstado(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===== CRIAR =====
    @PostMapping
    @Transactional
    public ResponseEntity<?> criar(@RequestBody PessoaPayload body) {

        if (pessoaRepository.existsByUsuario(body.usuario().trim())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Este nome de usuário já está em uso.");
        }
        if (body.email() != null && pessoaRepository.existsByEmail(body.email().trim())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Este e-mail já está cadastrado.");
        }
        if (!isBlank(body.cpf()) && pessoaRepository.existsByCpf(body.cpf().trim())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Este CPF já está cadastrado.");
        }

        Cidade cidade = cidadeRepository.findById(body.cidade().id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cidade não encontrada."));

        Perfil perfilUsuario = perfilRepository.findAll().stream()
            .filter(p -> p.getNome().equals("ROLE_USER"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Perfil ROLE_USER não encontrado."));

        Pessoa p = new Pessoa();
        p.setUsuario(body.usuario().trim());
        p.setSenha(passwordEncoder.encode(body.senha())); // Criptografa a senha
        p.setNome(body.nome().trim());
        p.setEmail(body.email().trim());
        p.setTelefone(isBlank(body.telefone()) ? null : body.telefone().trim());
        p.setDataNascimento(body.dataNascimento());
        p.setCpf(isBlank(body.cpf()) ? null : body.cpf().trim());

        // Converte o gênero para o formato do banco de dados (primeira letra)
        if (!isBlank(body.genero())) {
            p.setGenero(body.genero().trim().substring(0, 1).toUpperCase());
        } else {
            p.setGenero(null);
        }

        p.setCidade(cidade);

        Set<Perfil> perfis = new HashSet<>();
        perfis.add(perfilUsuario);
        p.setPerfis(perfis);

        if (p.getDataCadastro() == null) {
            p.setDataCadastro(LocalDateTime.now());
        }

        Pessoa pessoaSalva = pessoaRepository.save(p);

        return ResponseEntity.created(URI.create("/api/pessoas/" + pessoaSalva.getId())).body(pessoaSalva);
    }

    // ===== ATUALIZAR =====
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody PessoaPayload body) {
        Pessoa existente = pessoaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa não encontrada"));

        if (!isBlank(body.usuario())) existente.setUsuario(body.usuario().trim());
        if (!isBlank(body.senha())) existente.setSenha(passwordEncoder.encode(body.senha())); // Senha é criptografada
        if (!isBlank(body.nome())) existente.setNome(body.nome().trim());
        if (!isBlank(body.email())) existente.setEmail(body.email().trim());
        if (body.telefone() != null) existente.setTelefone(isBlank(body.telefone()) ? null : body.telefone().trim());
        if (body.dataNascimento() != null) existente.setDataNascimento(body.dataNascimento());
        if (body.cpf() != null) existente.setCpf(isBlank(body.cpf()) ? null : body.cpf().trim());

        // Converte o gênero
        if (body.genero() != null) {
            if (isBlank(body.genero())) {
                existente.setGenero(null);
            } else {
                existente.setGenero(body.genero().trim().substring(0, 1).toUpperCase());
            }
        }

        if (body.cidade() != null && body.cidade().id() != null) {
            Cidade cidade = cidadeRepository.findById(body.cidade().id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cidade não encontrada."));
            existente.setCidade(cidade);
        }

        Pessoa pessoaAtualizada = pessoaRepository.save(existente);
        return ResponseEntity.ok(pessoaAtualizada);
    }

    // ===== EXCLUIR =====
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!pessoaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        pessoaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Helpers =====

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private ResponseEntity<String> badRequest(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return badRequest(ex.getMessage());
    }
}
