package com.rmstudio.rmstudiofitness.controladores;

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
import java.util.List;

/**
 * API REST para Cidades
 *
 * Endpoints:
 *  GET    /api/cidades                     -> lista todas; filtros: ?q=nome&estadoId=1
 *  GET    /api/cidades/{id}                -> busca por id
 *  POST   /api/cidades                     -> cria (body: {nome, estadoId})
 *  PUT    /api/cidades/{id}                -> atualiza (body: {nome, estadoId})
 *  DELETE /api/cidades/{id}                -> remove
 */
@RestController
@RequestMapping("/api/cidades")
public class ControleCidade {

    @PersistenceContext
    private EntityManager em;

    // Payload simples para criação/atualização
    public record CidadePayload(String nome, Long estadoId) {}

    // LISTAR (com filtros opcionais)
    @GetMapping
    public List<Cidade> listar(@RequestParam(name = "q", required = false) String q,
                               @RequestParam(name = "estadoId", required = false) Long estadoId) {

        // Para retornar Estado junto no JSON, usamos JOIN FETCH
        StringBuilder jpql = new StringBuilder(
            "SELECT c FROM Cidade c JOIN FETCH c.estado e WHERE 1=1 ");

        if (q != null && !q.trim().isEmpty()) {
            jpql.append("AND LOWER(c.nome) LIKE :filtro ");
        }
        if (estadoId != null) {
            jpql.append("AND e.id = :estadoId ");
        }
        jpql.append("ORDER BY c.nome");

        TypedQuery<Cidade> query = em.createQuery(jpql.toString(), Cidade.class);

        if (q != null && !q.trim().isEmpty()) {
            query.setParameter("filtro", "%" + q.trim().toLowerCase() + "%");
        }
        if (estadoId != null) {
            query.setParameter("estadoId", estadoId);
        }

        return query.getResultList();
    }

    // BUSCAR POR ID (com estado)
    @GetMapping("/{id}")
    public ResponseEntity<Cidade> buscar(@PathVariable Long id) {
        List<Cidade> res = em.createQuery(
            "SELECT c FROM Cidade c JOIN FETCH c.estado WHERE c.id = :id", Cidade.class)
            .setParameter("id", id)
            .getResultList();
        if (res.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(res.get(0));
    }

    // BUSCAR CIDADES POR ESTADO
    @GetMapping("/por-estado/{estadoId}")
    public List<Cidade> getCidadesPorEstado(@PathVariable Long estadoId) {
        TypedQuery<Cidade> query = em.createQuery(
            "SELECT c FROM Cidade c JOIN FETCH c.estado WHERE c.estado.id = :estadoId ORDER BY c.nome", Cidade.class
        );
        query.setParameter("estadoId", estadoId);
        return query.getResultList();
    }

    // CRIAR
    @PostMapping
    @Transactional
    public ResponseEntity<?> criar(@RequestBody CidadePayload body) {
        String msg = validar(body, false);
        if (msg != null) return badRequest(msg);

        Estado estado = em.find(Estado.class, body.estadoId());
        if (estado == null) return badRequest("Estado não encontrado.");

        Cidade nova = new Cidade();
        nova.setNome(body.nome().trim());
        nova.setEstado(estado);

        em.persist(nova);
        em.flush();

        return ResponseEntity.created(URI.create("/api/cidades/" + nova.getId()))
                             .body(nova);
    }

    // ATUALIZAR
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody CidadePayload body) {
        Cidade existente = em.find(Cidade.class, id);
        if (existente == null) return ResponseEntity.notFound().build();

        String msg = validar(body, true);
        if (msg != null) return badRequest(msg);

        if (body.estadoId() != null) {
            Estado estado = em.find(Estado.class, body.estadoId());
            if (estado == null) return badRequest("Estado não encontrado.");
            existente.setEstado(estado);
        }
        if (body.nome() != null) {
            existente.setNome(body.nome().trim());
        }

        em.flush();
        return ResponseEntity.ok(existente);
    }

    // EXCLUIR
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Cidade c = em.find(Cidade.class, id);
        if (c == null) return ResponseEntity.notFound().build();
        em.remove(c);
        em.flush();
        return ResponseEntity.noContent().build();
    }

    // ===== Helpers =====

    private String validar(CidadePayload p, boolean isUpdate) {
        if (p == null) return "Corpo da requisição vazio.";

        if (!isUpdate) {
            if (isBlank(p.nome())) return "Informe o nome da cidade.";
            if (p.estadoId() == null) return "Informe o estadoId.";
        } else {
            // Em update, permitimos parciais, mas validamos quando enviados
            if (p.nome() != null && isBlank(p.nome())) return "Nome inválido.";
        }
        return null;
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
