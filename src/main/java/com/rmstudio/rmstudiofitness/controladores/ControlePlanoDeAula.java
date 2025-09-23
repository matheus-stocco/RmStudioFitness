package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.dtos.PlanoDeAulaDTO;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gerenciar os Planos de Aula.
 * Esta classe define os endpoints (URLs) da API para criar, ler, atualizar e deletar (CRUD)
 * os planos de treino dos alunos.
 * Responde às requisições que começam com /api/planoaulas.
 */
@RestController
@RequestMapping("/api/planoaulas")
public class ControlePlanoDeAula {

    @PersistenceContext
    private EntityManager em; // Gerenciador de Entidades do JPA para interagir com o banco de dados.

    // --- DTOs e Payloads ---
    public record ItemPlanoPayload(Long exercicioId, String diaSemana, Integer series, String repeticoes) {}
    public record PlanoDeAulaPayload(String nome, String descricao, LocalDateTime dataInicio, LocalDateTime dataFim, Long alunoId, List<ItemPlanoPayload> itens) {}
    
    // DTOs para respostas detalhadas foram movidos para a classe PlanoDeAulaDTO
    public record ItemPlanoDTO(Long id, Long exercicioId, String exercicioNome, Integer series, String repeticoes, String diaSemana) {}
    public record PlanoDeAulaDetalhadoDTO(Long id, String nome, String descricao, String dataInicio, String dataFim, PlanoDeAulaDTO.AlunoDTO aluno, List<ItemPlanoDTO> itens) {}


    /**
     * Endpoint para CRIAR um novo plano de aula.
     * Mapeado para requisições POST em /api/planoaulas.
     * @param payload O corpo da requisição contendo os dados do novo plano.
     * @return Uma resposta HTTP 201 (Created) com os dados do plano criado ou 400 (Bad Request) se houver erro.
     */
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
        plano.setNome(payload.nome());
        plano.setDescricao(payload.descricao());
        plano.setDataInicio(payload.dataInicio());
        plano.setDataFim(payload.dataFim());
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
                item.setSeries(itemPayload.series());
                item.setRepeticoes(itemPayload.repeticoes());
                plano.addItem(item);
            }
        }

        em.persist(plano);
        em.flush();
        
        PlanoDeAulaDTO dto = new PlanoDeAulaDTO(plano);

        return ResponseEntity.created(URI.create("/api/planoaulas/" + plano.getId())).body(dto);
    }

    /**
     * Endpoint para ATUALIZAR um plano de aula existente.
     * Mapeado para requisições PUT em /api/planoaulas/{id}.
     * @param id O ID do plano a ser atualizado.
     * @param payload O corpo da requisição com os novos dados do plano.
     * @return Uma resposta HTTP 200 (OK) com os dados do plano atualizado ou 404 (Not Found).
     */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizarPlano(@PathVariable Long id, @RequestBody PlanoDeAulaPayload payload) {
        PlanoDeAula plano = em.find(PlanoDeAula.class, id);
        if (plano == null) {
            return ResponseEntity.notFound().build();
        }

        Pessoa aluno = em.find(Pessoa.class, payload.alunoId());
        if (aluno == null) {
            return ResponseEntity.badRequest().body("Aluno não encontrado.");
        }

        plano.setNome(payload.nome());
        plano.setDescricao(payload.descricao());
        plano.setDataInicio(payload.dataInicio());
        plano.setDataFim(payload.dataFim());
        plano.setAluno(aluno);
        
        plano.getItens().clear();
        em.flush(); 
        if (payload.itens() != null && !payload.itens().isEmpty()) {
            for (ItemPlanoPayload itemPayload : payload.itens()) {
                Exercicio exercicio = em.find(Exercicio.class, itemPayload.exercicioId());
                if (exercicio == null) {
                    return ResponseEntity.badRequest().body("Exercício com id " + itemPayload.exercicioId() + " não encontrado.");
                }
                ItemPlanoDeAula item = new ItemPlanoDeAula();
                item.setExercicio(exercicio);
                item.setDiaSemana(itemPayload.diaSemana());
                item.setSeries(itemPayload.series());
                item.setRepeticoes(itemPayload.repeticoes());
                plano.addItem(item);
            }
        }
        
        em.flush();
        
        PlanoDeAulaDTO dto = new PlanoDeAulaDTO(plano);

        return ResponseEntity.ok(dto);
    }

    /**
     * Endpoint para ADICIONAR um novo item (exercício) a um plano existente.
     * Mapeado para POST em /api/planoaulas/{id}/itens
     */
    @PostMapping("/{id}/itens")
    @Transactional
    public ResponseEntity<?> adicionarItem(@PathVariable Long id, @RequestBody ItemPlanoPayload payload) {
        PlanoDeAula plano = em.find(PlanoDeAula.class, id);
        if (plano == null) {
            return ResponseEntity.notFound().build();
        }
        Exercicio exercicio = em.find(Exercicio.class, payload.exercicioId());
        if (exercicio == null) {
            return ResponseEntity.badRequest().body("Exercício não encontrado");
        }

        ItemPlanoDeAula item = new ItemPlanoDeAula();
        item.setExercicio(exercicio);
        item.setDiaSemana(payload.diaSemana());
        item.setSeries(payload.series());
        item.setRepeticoes(payload.repeticoes());
        
        plano.addItem(item);
        em.flush();

        ItemPlanoDTO itemDTO = new ItemPlanoDTO(item.getId(), exercicio.getId(), exercicio.getNome(), item.getSeries(), item.getRepeticoes(), item.getDiaSemana());
        
        return ResponseEntity.created(URI.create("/api/planoaulas/" + id + "/itens/" + item.getId())).body(itemDTO);
    }

    /**
     * Endpoint para DELETAR um item (exercício) de um plano.
     * Mapeado para DELETE em /api/planoaulas/{planoId}/itens/{itemId}
     */
    @DeleteMapping("/{planoId}/itens/{itemId}")
    @Transactional
    public ResponseEntity<?> removerItem(@PathVariable Long planoId, @PathVariable Long itemId) {
        ItemPlanoDeAula item = em.find(ItemPlanoDeAula.class, itemId);
        if (item == null || !item.getPlanoDeAula().getId().equals(planoId)) {
            return ResponseEntity.notFound().build();
        }
        em.remove(item);
        return ResponseEntity.noContent().build();
    }


    /**
     * Endpoint para BUSCAR um plano de aula detalhado pelo ID.
     * Mapeado para requisições GET em /api/planoaulas/{id}.
     * @param id O ID do plano a ser buscado.
     * @return Uma resposta HTTP 200 (OK) com o plano detalhado ou 404 (Not Found).
     */
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<PlanoDeAulaDetalhadoDTO> buscarPlanoPorId(@PathVariable Long id) {
        PlanoDeAula plano = em.find(PlanoDeAula.class, id);
        if (plano == null) {
            return ResponseEntity.notFound().build();
        }

        List<ItemPlanoDTO> itensDTO = plano.getItens().stream()
            .map(item -> new ItemPlanoDTO(
                item.getId(),
                item.getExercicio().getId(),
                item.getExercicio().getNome(),
                item.getSeries(),
                item.getRepeticoes(),
                item.getDiaSemana()
            ))
            .collect(Collectors.toList());
            
        PlanoDeAulaDTO planoDTO = new PlanoDeAulaDTO(plano);

        PlanoDeAulaDetalhadoDTO dto = new PlanoDeAulaDetalhadoDTO(
            plano.getId(),
            plano.getNome(),
            plano.getDescricao(),
            planoDTO.getDataInicio(),
            planoDTO.getDataFim(),
            planoDTO.getAluno(),
            itensDTO
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * Endpoint para LISTAR os planos de aula.
     * Mapeado para requisições GET em /api/planoaulas.
     * Pode opcionalmente filtrar por aluno através do parâmetro de URL 'alunoId'.
     * @param alunoId ID do aluno para filtrar os planos (opcional).
     * @return Uma lista de planos de aula (versão simplificada).
     */
    @GetMapping
    public ResponseEntity<List<PlanoDeAulaDTO>> listarPlanos(@RequestParam(name = "alunoId", required = false) Long alunoId) {
        String jpql = "SELECT p FROM PlanoDeAula p JOIN FETCH p.aluno";
        
        if (alunoId != null) {
            jpql += " WHERE p.aluno.id = :alunoId";
        }
        
        var query = em.createQuery(jpql, PlanoDeAula.class);
        
        if (alunoId != null) {
            query.setParameter("alunoId", alunoId);
        }
        
        List<PlanoDeAula> planos = query.getResultList();
        
        // Converte a lista de entidades para uma lista de DTOs
        List<PlanoDeAulaDTO> dtos = planos.stream()
                                          .map(PlanoDeAulaDTO::new)
                                          .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * Endpoint para DELETAR um plano de aula.
     * Mapeado para requisições DELETE em /api/planoaulas/{id}.
     * @param id O ID do plano a ser deletado.
     * @return Uma resposta HTTP 204 (No Content) em caso de sucesso.
     */
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
