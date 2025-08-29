package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.PlanoAula;
import com.rmstudio.rmstudiofitness.entidades.ItemPlanoAula;
import com.rmstudio.rmstudiofitness.entidades.Exercicio;
import com.rmstudio.rmstudiofitness.entidades.DiaSemana;

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
 * API REST para Plano de Aula e seus Itens.
 *
 * Endpoints principais:
 *  GET    /api/planos-aula                        -> lista planos
 *  GET    /api/planos-aula/{id}                   -> plano com itens
 *  POST   /api/planos-aula                        -> cria plano
 *  PUT    /api/planos-aula/{id}                   -> atualiza nome
 *  DELETE /api/planos-aula/{id}                   -> remove plano
 *
 *  GET    /api/planos-aula/dias                   -> lista de DiaSemana
 *  GET    /api/planos-aula/exercicios             -> lista de Exercicios (para combos)
 *
 *  GET    /api/planos-aula/{id}/itens             -> lista itens (opcional ?dia=SEGUNDA)
 *  POST   /api/planos-aula/{id}/itens             -> adiciona item (body: {exercicioId,dia,series,repeticoes})
 *  PUT    /api/planos-aula/{id}/itens/{itemId}    -> atualiza item
 *  DELETE /api/planos-aula/{id}/itens/{itemId}    -> remove item
 */
@RestController
@RequestMapping("/api/planos-aula")
public class ControlePlanoAula {

    @PersistenceContext
    private EntityManager em;

    // ======== Planos ========

    /** Lista todos os planos ordenados por nome (sem join nos itens para ficar leve). */
    @GetMapping
    public List<PlanoAula> listarPlanos() {
        TypedQuery<PlanoAula> q = em.createQuery(
                "SELECT p FROM PlanoAula p ORDER BY p.nome", PlanoAula.class);
        return q.getResultList();
    }

    /** Busca plano por id carregando itens (JOIN FETCH). */
    @GetMapping("/{id}")
    public ResponseEntity<PlanoAula> buscarPlano(@PathVariable Long id) {
        List<PlanoAula> res = em.createQuery(
                "SELECT p FROM PlanoAula p LEFT JOIN FETCH p.itens WHERE p.id = :id", PlanoAula.class)
                .setParameter("id", id)
                .getResultList();
        if (res.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(res.get(0));
    }

    /** Cria um novo plano. Body JSON mínimo: { "nome": "..." } */
    @PostMapping
    @Transactional
    public ResponseEntity<?> criarPlano(@RequestBody PlanoAula payload) {
        if (payload == null || isBlank(payload.getNome())) {
            return badRequest("Informe o nome do plano.");
        }
        PlanoAula novo = new PlanoAula();
        novo.setNome(payload.getNome().trim());
        em.persist(novo);
        em.flush();
        return ResponseEntity.created(URI.create("/api/planos-aula/" + novo.getId())).body(novo);
    }

    /** Atualiza nome do plano. */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizarPlano(@PathVariable Long id, @RequestBody PlanoAula payload) {
        PlanoAula plano = em.find(PlanoAula.class, id);
        if (plano == null) return ResponseEntity.notFound().build();
        if (payload == null || isBlank(payload.getNome())) {
            return badRequest("Informe o nome do plano.");
        }
        plano.setNome(payload.getNome().trim());
        em.flush();
        return ResponseEntity.ok(plano);
    }

    /** Remove um plano e (por cascade correto do mapeamento) seus itens. */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> removerPlano(@PathVariable Long id) {
        PlanoAula plano = em.find(PlanoAula.class, id);
        if (plano == null) return ResponseEntity.notFound().build();
        em.remove(plano);
        em.flush();
        return ResponseEntity.noContent().build();
    }

    // ======== Suportes (dias e exercícios) ========

    /** Fornece os valores do enum DiaSemana para popular combobox. */
    @GetMapping("/dias")
    public DiaSemana[] listarDias() {
        return DiaSemana.values();
    }

    /** Lista exercícios (para combos no front). */
    @GetMapping("/exercicios")
    public List<Exercicio> listarExercicios() {
        return em.createQuery("SELECT e FROM Exercicio e ORDER BY e.nome", Exercicio.class)
                 .getResultList();
    }

    // ======== Itens de Plano ========

    /** Lista itens de um plano; pode filtrar por dia (?dia=SEGUNDA). */
    @GetMapping("/{id}/itens")
    public ResponseEntity<?> listarItens(
            @PathVariable Long id,
            @RequestParam(name = "dia", required = false) DiaSemana dia) {

        PlanoAula plano = em.find(PlanoAula.class, id);
        if (plano == null) return ResponseEntity.notFound().build();

        if (dia == null) {
            List<ItemPlanoAula> itens = em.createQuery(
                    "SELECT i FROM ItemPlanoAula i JOIN FETCH i.exercicio WHERE i.planoAula.id = :id ORDER BY i.id",
                    ItemPlanoAula.class)
                    .setParameter("id", id)
                    .getResultList();
            return ResponseEntity.ok(itens);
        } else {
            List<ItemPlanoAula> itens = em.createQuery(
                    "SELECT i FROM ItemPlanoAula i JOIN FETCH i.exercicio " +
                    "WHERE i.planoAula.id = :id AND i.dia = :dia ORDER BY i.id",
                    ItemPlanoAula.class)
                    .setParameter("id", id)
                    .setParameter("dia", dia)
                    .getResultList();
            return ResponseEntity.ok(itens);
        }
    }

    /** Body para criação/atualização de item. */
    public record ItemPayload(Long exercicioId, DiaSemana dia, Integer series, Integer repeticoes) {}

    /** Adiciona item ao plano. */
    @PostMapping("/{id}/itens")
    @Transactional
    public ResponseEntity<?> adicionarItem(@PathVariable Long id, @RequestBody ItemPayload body) {
        PlanoAula plano = em.find(PlanoAula.class, id);
        if (plano == null) return ResponseEntity.notFound().build();

        String msg = validarItem(body);
        if (msg != null) return badRequest(msg);

        Exercicio ex = em.find(Exercicio.class, body.exercicioId());
        if (ex == null) return badRequest("Exercício não encontrado.");

        ItemPlanoAula item = new ItemPlanoAula();
        item.setPlanoAula(plano);
        item.setDia(body.dia());
        item.setExercicio(ex);
        item.setSeries(body.series());
        item.setRepeticoes(body.repeticoes());

        // Se o mapeamento da entidade PlanoAula tiver método utilitário, use-o:
        // plano.addItem(item);
        em.persist(item);
        em.flush();

        return ResponseEntity.created(URI.create("/api/planos-aula/" + id + "/itens/" + item.getId()))
                             .body(item);
    }

    /** Atualiza um item existente. */
    @PutMapping("/{id}/itens/{itemId}")
    @Transactional
    public ResponseEntity<?> atualizarItem(@PathVariable Long id,
                                           @PathVariable Long itemId,
                                           @RequestBody ItemPayload body) {
        PlanoAula plano = em.find(PlanoAula.class, id);
        if (plano == null) return ResponseEntity.notFound().build();

        ItemPlanoAula item = em.find(ItemPlanoAula.class, itemId);
        if (item == null || !item.getPlanoAula().getId().equals(id)) {
            return ResponseEntity.notFound().build();
        }

        String msg = validarItem(body);
        if (msg != null) return badRequest(msg);

        if (body.exercicioId() != null) {
            Exercicio ex = em.find(Exercicio.class, body.exercicioId());
            if (ex == null) return badRequest("Exercício não encontrado.");
            item.setExercicio(ex);
        }
        item.setDia(body.dia());
        item.setSeries(body.series());
        item.setRepeticoes(body.repeticoes());

        em.flush();
        return ResponseEntity.ok(item);
    }

    /** Remove um item do plano. */
    @DeleteMapping("/{id}/itens/{itemId}")
    @Transactional
    public ResponseEntity<?> removerItem(@PathVariable Long id, @PathVariable Long itemId) {
        PlanoAula plano = em.find(PlanoAula.class, id);
        if (plano == null) return ResponseEntity.notFound().build();

        ItemPlanoAula item = em.find(ItemPlanoAula.class, itemId);
        if (item == null || !item.getPlanoAula().getId().equals(id)) {
            return ResponseEntity.notFound().build();
        }

        em.remove(item);
        em.flush();
        return ResponseEntity.noContent().build();
    }

    // ======== Helpers ========

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Validações simples de item. Retorna mensagem de erro ou null se ok. */
    private String validarItem(ItemPayload p) {
        if (p == null) return "Corpo da requisição vazio.";
        if (p.exercicioId() == null) return "Informe o exercicioId.";
        if (p.dia() == null) return "Informe o dia.";
        if (p.series() == null || p.series() <= 0) return "Séries deve ser > 0.";
        if (p.repeticoes() == null || p.repeticoes() <= 0) return "Repetições deve ser > 0.";
        return null;
    }

    private ResponseEntity<String> badRequest(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    }

    // Tratamento simples de IllegalArgument para respostas 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> onIllegalArgument(IllegalArgumentException ex) {
        return badRequest(ex.getMessage());
    }
}
