package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Estado;
import com.rmstudio.rmstudiofitness.entidades.Cidade;
import com.rmstudio.rmstudiofitness.repositorios.EstadoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.net.URI;
import java.util.List;

/**
 * API REST para Estados
 *
 * Endpoints:
 *  GET    /api/estados                 -> lista todos (ordenado por nome)
 *  GET    /api/estados/{id}            -> busca por id
 *  GET    /api/estados/{id}/cidades    -> cidades do estado (ordenadas por nome)
 *  POST   /api/estados                 -> cria
 *  PUT    /api/estados/{id}            -> atualiza
 *  DELETE /api/estados/{id}            -> remove (bloqueia se houver cidades vinculadas)
 */
@RestController
@RequestMapping("/api/estados")
public class ControleEstado {

    private final EstadoRepository estadoRepository;

    @PersistenceContext
    private EntityManager em;

    public ControleEstado(EstadoRepository estadoRepository) {
        this.estadoRepository = estadoRepository;
    }

    // LISTAR
    @GetMapping
    public Page<Estado> listar(@PageableDefault(sort = "nome") Pageable pageable) {
        return estadoRepository.findAllByOrderByNome(pageable);
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Estado> buscar(@PathVariable Long id) {
        Estado e = em.find(Estado.class, id);
        return e == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(e);
    }

    // LISTAR CIDADES DO ESTADO
    @GetMapping("/{id}/cidades")
    public ResponseEntity<List<Cidade>> cidadesDoEstado(@PathVariable Long id) {
        Estado e = em.find(Estado.class, id);
        if (e == null) return ResponseEntity.notFound().build();
        TypedQuery<Cidade> q = em.createQuery(
            "SELECT c FROM Cidade c WHERE c.estado.id = :id ORDER BY c.nome", Cidade.class);
        q.setParameter("id", id);
        return ResponseEntity.ok(q.getResultList());
    }

    // CRIAR
    @PostMapping
    @Transactional
    public ResponseEntity<?> criar(@RequestBody Estado body) {
        validar(body);
        Estado novo = new Estado();
        novo.setNome(body.getNome().trim());
        novo.setUf(body.getUf().trim().toUpperCase());

        em.persist(novo);
        em.flush();

        return ResponseEntity.created(URI.create("/api/estados/" + novo.getId()))
                             .body(novo);
    }

    // ATUALIZAR
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Estado body) {
        Estado existente = em.find(Estado.class, id);
        if (existente == null) return ResponseEntity.notFound().build();

        validar(body);
        existente.setNome(body.getNome().trim());
        existente.setUf(body.getUf().trim().toUpperCase());
        em.flush();

        return ResponseEntity.ok(existente);
    }

    // EXCLUIR (bloqueia se houver cidades vinculadas)
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Estado e = em.find(Estado.class, id);
        if (e == null) return ResponseEntity.notFound().build();

        Long dependentes = em.createQuery(
            "SELECT COUNT(c) FROM Cidade c WHERE c.estado.id = :id", Long.class)
            .setParameter("id", id)
            .getSingleResult();

        if (dependentes != null && dependentes > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Não é possível excluir: existem cidades vinculadas a este estado.");
        }

        em.remove(e);
        em.flush();
        return ResponseEntity.noContent().build();
    }

    // VALIDAÇÃO
    private void validar(Estado e) {
        if (e == null) throw new IllegalArgumentException("Corpo da requisição vazio.");
        if (isBlank(e.getNome())) throw new IllegalArgumentException("Informe o nome do estado.");
        if (isBlank(e.getUf())) throw new IllegalArgumentException("Informe a UF.");
        if (e.getUf().trim().length() != 2) throw new IllegalArgumentException("UF deve ter 2 letras.");
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    // 400 para validações
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
