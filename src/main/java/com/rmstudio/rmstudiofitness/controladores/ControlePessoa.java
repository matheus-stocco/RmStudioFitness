package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.entidades.Cidade;
import com.rmstudio.rmstudiofitness.entidades.Estado;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            Long   cidadeId
    ) {}

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
        String msg = validar(body, false);
        if (msg != null) return badRequest(msg);

        if (existeDuplicata(body.usuario(), body.email(), null)) {
            return badRequest("Usuário ou e-mail já cadastrado.");
        }

        Cidade cidade = em.find(Cidade.class, body.cidadeId());
        if (cidade == null) return badRequest("Cidade não encontrada.");

        Pessoa p = new Pessoa();
        p.setUsuario(body.usuario().trim());
        p.setSenha(body.senha()); // ideal: aplicar hash antes de persistir
        p.setNome(body.nome().trim());
        p.setEmail(body.email().trim());
        p.setTelefone(isBlank(body.telefone()) ? null : body.telefone().trim());
        p.setGenero(isBlank(body.genero()) ? null : body.genero().trim());
        p.setCidade(cidade);
        if (p.getDataCadastro() == null) {
            p.setDataCadastro(LocalDateTime.now());
        }

        em.persist(p);
        em.flush();

        return ResponseEntity.created(URI.create("/api/pessoas/" + p.getId())).body(p);
    }

    // ===== ATUALIZAR =====
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody PessoaPayload body) {
        Pessoa existente = em.find(Pessoa.class, id);
        if (existente == null) return ResponseEntity.notFound().build();

        String msg = validar(body, true);
        if (msg != null) return badRequest(msg);

        if (existeDuplicata(body.usuario(), body.email(), id)) {
            return badRequest("Usuário ou e-mail já cadastrado para outro registro.");
        }

        if (!isBlank(body.usuario())) existente.setUsuario(body.usuario().trim());
        if (!isBlank(body.senha()))   existente.setSenha(body.senha());
        if (!isBlank(body.nome()))    existente.setNome(body.nome().trim());
        if (!isBlank(body.email()))   existente.setEmail(body.email().trim());
        if (body.telefone() != null)  existente.setTelefone(isBlank(body.telefone()) ? null : body.telefone().trim());
        if (body.genero() != null)    existente.setGenero(isBlank(body.genero()) ? null : body.genero().trim());

        if (body.cidadeId() != null) {
            Cidade cidade = em.find(Cidade.class, body.cidadeId());
            if (cidade == null) return badRequest("Cidade não encontrada.");
            existente.setCidade(cidade);
        }

        em.flush();
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

    private String validar(PessoaPayload p, boolean update) {
        if (p == null) return "Corpo da requisição vazio.";

        if (!update) {
            if (isBlank(p.usuario())) return "Informe o usuário.";
            if (isBlank(p.senha()))   return "Informe a senha.";
            if (isBlank(p.nome()))    return "Informe o nome.";
            if (isBlank(p.email()))   return "Informe o e-mail.";
            if (p.cidadeId() == null) return "Informe a cidade (cidadeId).";
        } else {
            if (p.email() != null && isBlank(p.email())) return "E-mail inválido.";
            if (p.usuario() != null && isBlank(p.usuario())) return "Usuário inválido.";
        }
        return null;
    }

    private boolean existeDuplicata(String usuario, String email, Long ignorarId) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(p) FROM Pessoa p WHERE (1=1) ");
        if (!isBlank(usuario)) jpql.append("AND p.usuario = :usuario ");
        if (!isBlank(email))   jpql.append("AND p.email = :email ");
        if (ignorarId != null) jpql.append("AND p.id <> :id ");

        TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class);
        if (!isBlank(usuario)) q.setParameter("usuario", usuario.trim());
        if (!isBlank(email))   q.setParameter("email", email.trim());
        if (ignorarId != null) q.setParameter("id", ignorarId);

        Long count = q.getSingleResult();
        return count != null && count > 0;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private ResponseEntity<String> badRequest(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return badRequest(ex.getMessage());
    }
}
