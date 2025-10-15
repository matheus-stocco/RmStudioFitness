package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Exercicio; // ajuste o pacote se suas entidades estiverem em outro namespace
import com.rmstudio.rmstudiofitness.repositorios.ExercicioRepository;

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
 * API REST para CRUD de Exercício.
 *
 * Endpoints:
 *  GET    /api/exercicios                 -> lista todos (ordenado por nome). Opcional: ?q=termo
 *  GET    /api/exercicios/{id}            -> busca por id
 *  POST   /api/exercicios                 -> cria
 *  PUT    /api/exercicios/{id}            -> atualiza
 *  DELETE /api/exercicios/{id}            -> remove
 */
@RestController
@RequestMapping("/api/exercicios")
public class ControleExercicio {

    private final ExercicioRepository exercicioRepository;
    @PersistenceContext
    private EntityManager em;

    public ControleExercicio(ExercicioRepository exercicioRepository) {
        this.exercicioRepository = exercicioRepository;
    }

    // ---------- LISTAR ----------
    @GetMapping
    public Page<Exercicio> listar(@RequestParam(name = "q", required = false) String q,
                                  @PageableDefault(sort = "nome") Pageable pageable) {
        if (q != null && !q.trim().isEmpty()) {
            // Busca tanto no nome quanto no grupo muscular
            String termo = q.trim();
            Page<Exercicio> porNome = exercicioRepository.findByNomeContainingIgnoreCaseOrderByNome(termo, pageable);
            if (porNome.hasContent()) {
                return porNome;
            }
            return exercicioRepository.findByGrupoMuscularContainingIgnoreCaseOrderByNome(termo, pageable);
        }
        return exercicioRepository.findAllByOrderByNome(pageable);
    }

    // ---------- LISTAR GRUPOS MUSCULARES ----------
    @GetMapping("/grupos-musculares")
    public List<String> listarGruposMusculares() {
        TypedQuery<String> query = em.createQuery(
            "SELECT DISTINCT e.grupoMuscular FROM Exercicio e ORDER BY e.grupoMuscular", String.class);
        return query.getResultList();
    }

    // ---------- BUSCAR POR ID ----------
    @GetMapping("/{id}")
    public ResponseEntity<Exercicio> buscarPorId(@PathVariable Long id) {
        Exercicio ex = em.find(Exercicio.class, id);
        return ex == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(ex);
    }

    // ---------- CRIAR ----------
    @PostMapping
    @Transactional
    public ResponseEntity<?> criar(@RequestBody Exercicio body) {
        validar(body);

        Exercicio novo = new Exercicio();
        novo.setNome(body.getNome().trim());
        novo.setDescricao(body.getDescricao());
        novo.setGrupoMuscular(body.getGrupoMuscular().trim());
        // Valores padrão para compatibilidade com BD existente
        novo.setSeries(1);
        novo.setRepeticoes(1);

        em.persist(novo);
        em.flush(); // garante ID

        return ResponseEntity.created(URI.create("/api/exercicios/" + novo.getId())).body(novo);
    }

    // ---------- ATUALIZAR ----------
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Exercicio body) {
        Exercicio existente = em.find(Exercicio.class, id);
        if (existente == null) return ResponseEntity.notFound().build();

        validar(body);

        existente.setNome(body.getNome().trim());
        existente.setDescricao(body.getDescricao());
        existente.setGrupoMuscular(body.getGrupoMuscular().trim());
        // Manter valores padrão se não foram fornecidos
        if (existente.getSeries() == null) existente.setSeries(1);
        if (existente.getRepeticoes() == null) existente.setRepeticoes(1);

        em.flush();
        return ResponseEntity.ok(existente);
    }

    // ---------- EXCLUIR ----------
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Exercicio existente = em.find(Exercicio.class, id);
        if (existente == null) return ResponseEntity.notFound().build();
        em.remove(existente);
        em.flush();
        return ResponseEntity.noContent().build();
    }

    // ---------- VALIDAÇÃO ----------
    private void validar(Exercicio e) {
        if (e == null) throw new IllegalArgumentException("Corpo da requisição vazio.");

        if (isBlank(e.getNome())) {
            throw new IllegalArgumentException("Informe o nome do exercício.");
        }
        if (isBlank(e.getGrupoMuscular())) {
            throw new IllegalArgumentException("Informe o grupo muscular.");
        }
       
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // ---------- TRATAMENTO DE ERROS ----------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
