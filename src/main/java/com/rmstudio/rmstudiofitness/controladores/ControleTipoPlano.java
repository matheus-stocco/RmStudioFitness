package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.TipoPlano; // ajuste se suas entidades estiverem em outro package
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
 * API REST para CRUD de TipoPlano.
 * Endpoints:
 *  GET    /api/tipos-plano               -> lista todos (ordenado por nome)
 *  GET    /api/tipos-plano/{id}          -> busca por id
 *  POST   /api/tipos-plano               -> cria
 *  PUT    /api/tipos-plano/{id}          -> atualiza
 *  DELETE /api/tipos-plano/{id}          -> remove
 */
@RestController
@RequestMapping("/api/tipos-plano")
public class ControleTipoPlano {

    @PersistenceContext
    private EntityManager em;

    // ---------- LISTAR ----------
    @GetMapping
    public List<TipoPlano> listar() {
        TypedQuery<TipoPlano> q = em.createQuery(
                "SELECT t FROM TipoPlano t ORDER BY t.nome", TipoPlano.class);
        return q.getResultList();
    }

    // ---------- BUSCAR POR ID ----------
    @GetMapping("/{id}")
    public ResponseEntity<TipoPlano> buscarPorId(@PathVariable Long id) {
        TipoPlano encontrado = em.find(TipoPlano.class, id);
        return (encontrado == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(encontrado);
    }

    // ---------- CRIAR ----------
    @PostMapping
    @Transactional
    public ResponseEntity<?> criar(@RequestBody TipoPlano tipoPlano) {
        validar(tipoPlano, false);

        em.persist(tipoPlano);
        em.flush(); // garante ID gerado

        return ResponseEntity
                .created(URI.create("/api/tipos-plano/" + tipoPlano.getId()))
                .body(tipoPlano);
    }

    // ---------- ATUALIZAR ----------
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody TipoPlano payload) {
        validar(payload, true);

        TipoPlano existente = em.find(TipoPlano.class, id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        existente.setNome(payload.getNome());
        existente.setDescricao(payload.getDescricao());
        existente.setValor(payload.getValor());

        // merge não é necessário se a entidade estiver gerenciada, mas é inofensivo:
        existente = em.merge(existente);
        em.flush();

        return ResponseEntity.ok(existente);
    }

    // ---------- EXCLUIR ----------
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        TipoPlano existente = em.find(TipoPlano.class, id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        em.remove(existente);
        em.flush();
        return ResponseEntity.noContent().build();
    }

    // ---------- VALIDAÇÃO BÁSICA ----------
    private void validar(TipoPlano t, boolean isUpdate) {
        if (t == null) {
            throw new IllegalArgumentException("Corpo da requisição vazio.");
        }
        if (t.getNome() == null || t.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do tipo de plano.");
        }
        if (t.getValor() == null) {
            throw new IllegalArgumentException("Informe o valor do tipo de plano.");
        }
        if (t.getValor().doubleValue() < 0) {
            throw new IllegalArgumentException("O valor deve ser maior ou igual a zero.");
        }
    }

    // ---------- TRATAMENTO DE ERROS SIMPLES ----------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
