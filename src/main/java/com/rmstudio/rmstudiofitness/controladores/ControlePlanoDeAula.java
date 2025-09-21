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

    // --- DTOs (Data Transfer Objects) ---
    // DTOs são objetos simples usados para transferir dados entre o cliente (frontend) e o servidor (backend).
    // Eles ajudam a evitar expor a estrutura interna do banco de dados e a previnir erros de serialização.

    /** DTO para representar um item de exercício dentro de um plano detalhado. */
    public record ItemPlanoDTO(Long id, Long exercicioId, String exercicioNome, Integer series, String repeticoes, String diaSemana) {}
    
    /** DTO para a resposta de um plano de aula completo, incluindo todos os seus itens (exercícios). */
    public record PlanoDeAulaDetalhadoDTO(Long id, String nome, String descricao, LocalDateTime dataInicio, LocalDateTime dataFim, AlunoDTO aluno, List<ItemPlanoDTO> itens) {}
    
    /** DTO para a resposta de um plano de aula em uma lista (versão simplificada, sem os exercícios). */
    public record PlanoDeAulaDTO(Long id, String nome, String descricao, LocalDateTime dataInicio, LocalDateTime dataFim, AlunoDTO aluno) {}
    
    /** DTO para representar o aluno de forma simplificada. */
    public record AlunoDTO(Long id, String nome) {}
    
    // --- Payloads ---
    // Payloads são objetos que definem a estrutura dos dados que o backend espera receber do frontend.

    /** Payload para um item de exercício vindo na requisição de criação/atualização. */
    public record ItemPlanoPayload(Long exercicioId, String diaSemana, Integer series, String repeticoes) {}
    
    /** Payload para a criação ou atualização de um Plano de Aula completo. */
    public record PlanoDeAulaPayload(String nome, String descricao, LocalDateTime dataInicio, LocalDateTime dataFim, Long alunoId, List<ItemPlanoPayload> itens) {}

    /**
     * Endpoint para CRIAR um novo plano de aula.
     * Mapeado para requisições POST em /api/planoaulas.
     * @param payload O corpo da requisição contendo os dados do novo plano.
     * @return Uma resposta HTTP 201 (Created) com os dados do plano criado ou 400 (Bad Request) se houver erro.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> criarPlano(@RequestBody PlanoDeAulaPayload payload) {
        // Valida se o ID do aluno foi informado.
        if (payload.alunoId() == null) {
            return ResponseEntity.badRequest().body("O aluno é obrigatório.");
        }
        // Busca o aluno no banco de dados.
        Pessoa aluno = em.find(Pessoa.class, payload.alunoId());
        if (aluno == null) {
            return ResponseEntity.badRequest().body("Aluno não encontrado.");
        }

        // Cria uma nova instância da entidade PlanoDeAula.
        PlanoDeAula plano = new PlanoDeAula();
        plano.setNome(payload.nome());
        plano.setDescricao(payload.descricao());
        plano.setDataInicio(payload.dataInicio());
        plano.setDataFim(payload.dataFim());
        plano.setAluno(aluno);

        // Se houver itens (exercícios) no payload, percorre a lista e os adiciona ao plano.
        if (payload.itens() != null && !payload.itens().isEmpty()) {
            for (ItemPlanoPayload itemPayload : payload.itens()) {
                // Busca cada exercício pelo ID.
                Exercicio exercicio = em.find(Exercicio.class, itemPayload.exercicioId());
                if (exercicio == null) {
                    return ResponseEntity.badRequest().body("Exercício com id " + itemPayload.exercicioId() + " não encontrado.");
                }
                // Cria o ItemPlanoDeAula e preenche com os dados.
                ItemPlanoDeAula item = new ItemPlanoDeAula();
                item.setExercicio(exercicio);
                item.setDiaSemana(itemPayload.diaSemana());
                item.setSeries(itemPayload.series());
                item.setRepeticoes(itemPayload.repeticoes());
                plano.addItem(item); // Adiciona o item ao plano (o método addItem cuida da associação).
            }
        }

        // Persiste o plano e todos os seus itens no banco de dados.
        em.persist(plano);
        em.flush();
        
        // Cria um DTO de resposta para evitar problemas de serialização da entidade completa.
        PlanoDeAulaDTO dto = new PlanoDeAulaDTO(
            plano.getId(),
            plano.getNome(),
            plano.getDescricao(),
            plano.getDataInicio(),
            plano.getDataFim(),
            new AlunoDTO(aluno.getId(), aluno.getNome())
        );

        // Retorna a resposta de sucesso com o DTO do plano criado.
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
        // Busca o plano existente no banco.
        PlanoDeAula plano = em.find(PlanoDeAula.class, id);
        if (plano == null) {
            return ResponseEntity.notFound().build();
        }

        // Busca o aluno (pode ter sido alterado).
        Pessoa aluno = em.find(Pessoa.class, payload.alunoId());
        if (aluno == null) {
            return ResponseEntity.badRequest().body("Aluno não encontrado.");
        }

        // Atualiza os dados básicos do plano.
        plano.setNome(payload.nome());
        plano.setDescricao(payload.descricao());
        plano.setDataInicio(payload.dataInicio());
        plano.setDataFim(payload.dataFim());
        plano.setAluno(aluno);
        
        // A estratégia aqui é limpar os itens antigos e adicionar os novos que vieram na requisição.
        plano.getItens().clear();
        em.flush(); // Garante que a remoção seja executada antes da inserção.
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
        
        // Salva as alterações no banco.
        em.flush();
        
        // Cria o DTO de resposta.
        PlanoDeAulaDTO dto = new PlanoDeAulaDTO(
            plano.getId(),
            plano.getNome(),
            plano.getDescricao(),
            plano.getDataInicio(),
            plano.getDataFim(),
            new AlunoDTO(aluno.getId(), aluno.getNome())
        );

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
     * A anotação @Transactional é crucial aqui para permitir o carregamento
     * dos dados relacionados (lazy loading) que estão em outras tabelas.
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

        // Converte a lista de entidades ItemPlanoDeAula para uma lista de DTOs.
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

        // Cria o DTO de resposta com todos os detalhes.
        PlanoDeAulaDetalhadoDTO dto = new PlanoDeAulaDetalhadoDTO(
            plano.getId(),
            plano.getNome(),
            plano.getDescricao(),
            plano.getDataInicio(),
            plano.getDataFim(),
            new AlunoDTO(plano.getAluno().getId(), plano.getAluno().getNome()),
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
        // Esta é uma query JPQL que constrói o DTO diretamente no banco de dados.
        // É uma abordagem muito eficiente pois evita o tráfego excessivo de dados
        // e previne erros de lazy loading.
        String jpql = "SELECT new com.rmstudio.rmstudiofitness.controladores.ControlePlanoDeAula$PlanoDeAulaDTO(p.id, p.nome, p.descricao, p.dataInicio, p.dataFim, new com.rmstudio.rmstudiofitness.controladores.ControlePlanoDeAula$AlunoDTO(p.aluno.id, p.aluno.nome)) FROM PlanoDeAula p";
        
        // Se um ID de aluno for fornecido, adiciona a cláusula WHERE na query.
        if (alunoId != null) {
            jpql += " WHERE p.aluno.id = :alunoId";
        }
        
        // Cria a query.
        var query = em.createQuery(jpql, PlanoDeAulaDTO.class);
        
        // Se o filtro de aluno existir, define o parâmetro na query.
        if (alunoId != null) {
            query.setParameter("alunoId", alunoId);
        }
        
        // Executa a query e retorna o resultado.
        List<PlanoDeAulaDTO> resultado = query.getResultList();
        return ResponseEntity.ok(resultado);
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
        em.remove(plano); // Remove a entidade do banco.
        return ResponseEntity.noContent().build();
    }
}
