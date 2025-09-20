package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Exercicio;
import com.rmstudio.rmstudiofitness.entidades.ItemPlanoDeAula;
import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.entidades.PlanoDeAula;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/planoaulas")
public class ControlePlanoDeAula {

    @PersistenceContext
    private EntityManager em;

    public record ItemPlanoPayload(Long exercicioId, String diaSemana) {}
    public record PlanoDeAulaPayload(String descricao, Long alunoId, List<ItemPlanoPayload> itens) {}

    @PostMapping
    @Transactional
    public ResponseEntity<?> criarPlano(@RequestBody PlanoDeAulaPayload payload) {
        if (payload.alunoId() == null) {
            return ResponseEntity.badRequest().body("O aluno é obrigatório.");
        }
        Pessoa aluno = em.find(Pessoa.class, payload.alunoId());
        if (aluno == null) {
            return ResponseEntity.badRequest().body("Aluno não encontrado.");
        }

        PlanoDeAula plano = new PlanoDeAula();
        plano.setDescricao(payload.descricao());
        plano.setAluno(aluno);

        if (payload.itens() != null && !payload.itens().isEmpty()) {
            for (ItemPlanoPayload itemPayload : payload.itens()) {
                Exercicio exercicio = em.find(Exercicio.class, itemPayload.exercicioId());
                if (exercicio == null) {
                    return ResponseEntity.badRequest().body("Exercício com id " + itemPayload.exercicioId() + " não encontrado.");
                }
                ItemPlanoDeAula item = new ItemPlanoDeAula();
                item.setExercicio(exercicio);
                item.setDiaSemana(itemPayload.diaSemana());
                plano.addItem(item);
            }
        }

        em.persist(plano);
        em.flush();
        
        return ResponseEntity.created(URI.create("/api/planoaulas/" + plano.getId())).body(plano);
    }

    @GetMapping
    public ResponseEntity<List<Object>> listarPlanos() {
        List<PlanoDeAula> planos = em.createQuery("SELECT p FROM PlanoDeAula p LEFT JOIN FETCH p.aluno", PlanoDeAula.class).getResultList();
        
        List<Object> resultado = planos.stream().map(p -> {
            return new Object() {
                public Long id = p.getId();
                public String descricao = p.getDescricao();
                public Object aluno = new Object() {
                    public Long id = p.getAluno().getId();
                    public String nome = p.getAluno().getNome();
                };
            };
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }
    
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deletarPlano(@PathVariable Long id) {
        PlanoDeAula plano = em.find(PlanoDeAula.class, id);
        if (plano == null) {
            return ResponseEntity.notFound().build();
        }
        em.remove(plano);
        return ResponseEntity.noContent().build();
    }
}
