package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.entidades.Cidade;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

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

    @PersistenceContext
    private EntityManager em;

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
    public List<Pessoa> listar(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cidadeId", required = false) Long cidadeId,
            @RequestParam(name = "estadoId", required = false) Long estadoId
    ) {
        StringBuilder jpql = new StringBuilder(
                "SELECT DISTINCT p FROM Pessoa p " +
                "LEFT JOIN FETCH p.cidade c " +
                "LEFT JOIN FETCH c.estado e " +
                "WHERE 1=1 "
        );

        if (!isBlank(q))         jpql.append("AND (LOWER(p.nome) LIKE :q OR LOWER(p.usuario) LIKE :q OR LOWER(p.email) LIKE :q) ");
        if (cidadeId != null)    jpql.append("AND c.id = :cidadeId ");
        if (estadoId != null)    jpql.append("AND e.id = :estadoId ");
        jpql.append("ORDER BY p.nome");

        TypedQuery<Pessoa> query = em.createQuery(jpql.toString(), Pessoa.class);
        if (!isBlank(q)) query.setParameter("q", "%" + q.trim().toLowerCase() + "%");
        if (cidadeId != null) query.setParameter("cidadeId", cidadeId);
        if (estadoId != null) query.setParameter("estadoId", estadoId);
        return query.getResultList();
    }

    // ===== DETALHE =====
    @GetMapping("/{id}")
    public ResponseEntity<Pessoa> buscar(@PathVariable Long id) {
        List<Pessoa> res = em.createQuery(
                "SELECT p FROM Pessoa p " +
                "LEFT JOIN FETCH p.cidade c " +
                "LEFT JOIN FETCH c.estado " +
                "WHERE p.id = :id", Pessoa.class)
                .setParameter("id", id)
                .getResultList();

        return res.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(res.get(0));
    }

    // ===== CRIAR =====
    @PostMapping
    @Transactional
    public ResponseEntity<?> criar(@RequestBody PessoaPayload body) {
        
        Cidade cidade = em.find(Cidade.class, body.cidade().id());
        if (cidade == null) return badRequest("Cidade não encontrada.");

        Pessoa p = new Pessoa();
        p.setUsuario(body.usuario().trim());
        p.setSenha(body.senha()); // ideal: aplicar hash antes de persistir
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
        if (p.getDataCadastro() == null) {
            p.setDataCadastro(LocalDateTime.now());
        }

        em.persist(p);
        em.flush();

        // Força a inicialização do proxy do Estado antes de fechar a sessão
        p.getCidade().getEstado().getUf(); 

        return ResponseEntity.created(URI.create("/api/pessoas/" + p.getId())).body(p);
    }

    // ===== ATUALIZAR =====
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody PessoaPayload body) {
        Pessoa existente = em.find(Pessoa.class, id);
        if (existente == null) return ResponseEntity.notFound().build();

        if (!isBlank(body.usuario())) existente.setUsuario(body.usuario().trim());
        if (!isBlank(body.senha()))   existente.setSenha(body.senha());
        if (!isBlank(body.nome()))    existente.setNome(body.nome().trim());
        if (!isBlank(body.email()))   existente.setEmail(body.email().trim());
        if (body.telefone() != null)  existente.setTelefone(isBlank(body.telefone()) ? null : body.telefone().trim());
        if (body.dataNascimento() != null) existente.setDataNascimento(body.dataNascimento());
        if (body.cpf() != null) existente.setCpf(isBlank(body.cpf()) ? null : body.cpf().trim());
        
        // Converte o gênero para o formato do banco de dados (primeira letra)
        if (body.genero() != null) {
            if (isBlank(body.genero())) {
                existente.setGenero(null);
            } else {
                existente.setGenero(body.genero().trim().substring(0, 1).toUpperCase());
            }
        }

        if (body.cidade() != null && body.cidade().id() != null) {
            Cidade cidade = em.find(Cidade.class, body.cidade().id());
            if (cidade == null) return badRequest("Cidade não encontrada.");
            existente.setCidade(cidade);
        }

        em.flush();
        
        // Força a inicialização do proxy do Estado antes de fechar a sessão
        existente.getCidade().getEstado().getUf();

        return ResponseEntity.ok(existente);
    }

    // ===== EXCLUIR =====
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Pessoa p = em.find(Pessoa.class, id);
        if (p == null) return ResponseEntity.notFound().build();
        em.remove(p);
        em.flush();
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
