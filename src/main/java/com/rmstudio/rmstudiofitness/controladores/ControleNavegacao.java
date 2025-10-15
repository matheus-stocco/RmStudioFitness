package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.DiaSemana;
import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.entidades.PlanoAula;
import com.rmstudio.rmstudiofitness.repositorios.AvaliacaoFisicaRepository;
import com.rmstudio.rmstudiofitness.repositorios.EstadoRepository;
import com.rmstudio.rmstudiofitness.repositorios.ExercicioRepository;
import com.rmstudio.rmstudiofitness.repositorios.MensalidadeRepository;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import com.rmstudio.rmstudiofitness.repositorios.PlanoAulaRepository;
import com.rmstudio.rmstudiofitness.repositorios.TipoPlanoRepository;
import com.rmstudio.rmstudiofitness.servicos.PagamentoService;
import com.rmstudio.rmstudiofitness.entidades.Mensalidade;
import com.rmstudio.rmstudiofitness.entidades.TipoPlano;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ControleNavegacao {

    private static final Logger logger = LoggerFactory.getLogger(ControleNavegacao.class);
    
    private final PessoaRepository pessoaRepository;
    private final EstadoRepository estadoRepository;
    private final TipoPlanoRepository tipoPlanoRepository;
    private final AvaliacaoFisicaRepository avaliacaoFisicaRepository;
    private final MensalidadeRepository mensalidadeRepository;
    private final PlanoAulaRepository planoAulaRepository;
    private final ExercicioRepository exercicioRepository;

    @Autowired
    public ControleNavegacao(PessoaRepository pessoaRepository,
                             EstadoRepository estadoRepository,
                             TipoPlanoRepository tipoPlanoRepository,
                             PagamentoService pagamentoService,
                             AvaliacaoFisicaRepository avaliacaoFisicaRepository,
                             MensalidadeRepository mensalidadeRepository,
                             PlanoAulaRepository planoAulaRepository,
                             ExercicioRepository exercicioRepository) {
        this.pessoaRepository = pessoaRepository;
        this.estadoRepository = estadoRepository;
        this.tipoPlanoRepository = tipoPlanoRepository;
        this.avaliacaoFisicaRepository = avaliacaoFisicaRepository;
        this.mensalidadeRepository = mensalidadeRepository;
        this.planoAulaRepository = planoAulaRepository;
        this.exercicioRepository = exercicioRepository;
    }
    
    /**
     * Mapeia a URL /login para a página de login customizada.
     * @return O nome do template "login".
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Mapeia a URL /quem-somos para a página "Quem Somos".
     * @return O nome do template "quem-somos".
     */
    @GetMapping("/quem-somos")
    public String quemSomos() {
        return "quem-somos";
    }

    @GetMapping({"/", "/index.html"}) // Mapeia tanto a raiz quanto /index.html
    public String home() {
        return "index";
    }

    @GetMapping("/planos")
    public String planos(Model model, Authentication authentication) {
        List<TipoPlano> planos = tipoPlanoRepository.findAll();

        // Processa a descrição de cada plano para criar uma lista de benefícios
        planos.forEach(plano -> {
            if (plano.getDescricao() != null && !plano.getDescricao().isEmpty()) {
                List<String> beneficios = Arrays.stream(plano.getDescricao().split("\\r?\\n|;"))
                                                .map(String::trim)
                                                .filter(s -> !s.isEmpty())
                                                .collect(Collectors.toList());
                plano.setBeneficios(beneficios);
            } else {
                plano.setBeneficios(Collections.emptyList());
            }
        });
        
        model.addAttribute("planos", planos);

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            // A busca já carrega o planoAtivo por causa do JOIN FETCH no repositório
            Pessoa pessoa = pessoaRepository.findByUsuario(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
            model.addAttribute("planoAtivo", pessoa.getPlanoAtivo());
        } else {
            model.addAttribute("planoAtivo", null);
        }
        return "TiposDePlanos";
    }

    @GetMapping({"/membros", "/CadastroPessoas.html"})
    public String cadastroPessoas() {
        return "CadastroPessoas";
    }

    @GetMapping({"/autocadastro", "/FormularioAutoCadastro.html"})
    public String formularioAutoCadastro() {
        return "FormularioAutoCadastro";
    }

    @GetMapping({"/avaliacoes", "/CadastroAvaliacaoFisica.html"})
    public String cadastroAvaliacaoFisica() {
        return "CadastroAvaliacaoFisica";
    }

    @GetMapping({"/cidades", "/CadastroCidade.html"})
    public String cadastroCidade() {
        return "CadastroCidade";
    }

    @GetMapping({"/estados", "/CadastroEstado.html"})
    public String cadastroEstado() {
        return "CadastroEstado";
    }

    @GetMapping({"/exercicios", "/CadastroExercicios.html"})
    public String cadastroExercicios() {
        return "CadastroExercicios";
    }

    @GetMapping({"/itens-plano", "/CadastroItensPlano.html"})
    @Transactional(readOnly = true)
    public String cadastroItensPlano(@RequestParam("planoId") Long planoId, Model model) {
        PlanoAula planoAula = planoAulaRepository.findByIdWithItens(planoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano de Aula não encontrado"));

        model.addAttribute("planoAula", planoAula);
        model.addAttribute("exercicios", exercicioRepository.findAllByOrderByNome());
        model.addAttribute("gruposMusculares", exercicioRepository.findDistinctGruposMusculares());
        model.addAttribute("diasDaSemana", DiaSemana.values());

        return "CadastroItensPlano";
    }

    @GetMapping({"/planos-aula", "/CadastroPlanoAula.html"})
    public String cadastroPlanoAula() {
        return "CadastroPlanoAula";
    }

    @GetMapping({"/tipos-plano", "/CadastroTipoPlano.html"})
    public String cadastroTipoPlano() {
        return "CadastroTipoPlano";
    }

    /**
     * Mapeia a URL /minhas-mensalidades para a página de mensalidades do usuário.
     */
    @GetMapping("/minhas-mensalidades")
    @Transactional(readOnly = true)
    public String minhasMensalidades(Model model, Authentication authentication,
                                     @PageableDefault(size = 5) Pageable pageable) {
        logger.info("Iniciando carregamento de mensalidades para usuário: {}", authentication != null ? authentication.getName() : "null");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Usuário não autenticado, redirecionando para login");
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        logger.debug("Buscando pessoa com usuário: {}", username);
        
        try {
            Pessoa pessoa = pessoaRepository.findByUsuario(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
            
            logger.debug("Pessoa encontrada: ID={}, Nome={}", pessoa.getId(), pessoa.getNome());
            
            model.addAttribute("pessoa", pessoa);
            
            TipoPlano planoAtivo = pessoa.getPlanoAtivo();
            if (planoAtivo != null && planoAtivo.getDescricao() != null) {
                List<String> beneficios = Arrays.stream(planoAtivo.getDescricao().split("(?=[A-Z])|;"))
                                                .map(String::trim)
                                                .filter(s -> !s.isEmpty())
                                                .collect(Collectors.toList());
                planoAtivo.setBeneficios(beneficios);
            }
            model.addAttribute("planoAtivo", planoAtivo);
            
            // Etapa 1: Busca paginada apenas das entidades principais
            Page<Mensalidade> mensalidadesPage = mensalidadeRepository.findVisiveisByPessoaIdWithCustomSort(pessoa.getId(), pageable);
            
            // Etapa 2: Busca dos detalhes para as entidades da página atual
            List<Long> ids = mensalidadesPage.getContent().stream().map(Mensalidade::getId).collect(Collectors.toList());
            Page<Mensalidade> pageWithDetails;

            if (!ids.isEmpty()) {
                List<Mensalidade> mensalidadesComDetalhes = mensalidadeRepository.findAllWithDetailsByIds(ids);
                pageWithDetails = new PageImpl<>(mensalidadesComDetalhes, pageable, mensalidadesPage.getTotalElements());
            } else {
                pageWithDetails = Page.empty(pageable);
            }
            
            model.addAttribute("page", pageWithDetails);

            model.addAttribute("pageTitle", "Minhas Mensalidades");
            logger.info("Carregamento de mensalidades concluído com sucesso");
            return "minhas-mensalidades";
            
        } catch (Exception e) {
            logger.error("Erro ao carregar mensalidades para usuário {}: {}", username, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor ao carregar mensalidades");
        }
    }

    @GetMapping("/perfil")
    @Transactional(readOnly = true)
    public String perfil(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Pessoa principal = (Pessoa) authentication.getPrincipal();
            
            // Recarrega a pessoa do banco de dados com a cidade e o estado
            pessoaRepository.findByIdWithDetails(principal.getId())
                .ifPresent(pessoaCompleta -> model.addAttribute("pessoa", pessoaCompleta));
        }
        model.addAttribute("estados", estadoRepository.findAllByOrderByNome());
        return "perfil";
    }
 
    @GetMapping("/minhas-avaliacoes")
    public String minhasAvaliacoes(Model model, Authentication authentication,
                                   @PageableDefault(size = 1, sort = "dataAvaliacao") Pageable pageable) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            
            Pessoa pessoa = pessoaRepository.findByUsuario(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

            Page<com.rmstudio.rmstudiofitness.entidades.AvaliacaoFisica> page = avaliacaoFisicaRepository
                .findByPessoaIdOrderByDataAvaliacaoDesc(pessoa.getId(), pageable);
            
            model.addAttribute("page", page);
        }
        return "minhas-avaliacoes";
    }
 
    @GetMapping("/meus-planos-aula")
    @Transactional(readOnly = true)
    public String meusPlanosAula(Model model, Authentication authentication,
                                 @PageableDefault(size = 5, sort = "dataInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Pessoa pessoa = pessoaRepository.findByUsuario(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
            
            // Etapa 1: Busca paginada apenas dos Planos de Aula
            Page<PlanoAula> planosPage = planoAulaRepository.findByAlunoOrderByDataInicioDesc(pessoa, pageable);

            // Etapa 2: Busca dos detalhes (itens e exercícios) para os planos da página atual
            List<Long> ids = planosPage.getContent().stream().map(PlanoAula::getId).collect(Collectors.toList());
            Page<PlanoAula> pageWithDetails;

            if (!ids.isEmpty()) {
                List<PlanoAula> planosComDetalhes = planoAulaRepository.findAllWithDetailsByIds(ids);
                pageWithDetails = new PageImpl<>(planosComDetalhes, pageable, planosPage.getTotalElements());
            } else {
                pageWithDetails = Page.empty(pageable);
            }

            model.addAttribute("page", pageWithDetails);
        }
        return "meus-planos-aula";
    }
}
