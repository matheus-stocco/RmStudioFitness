package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.repositorios.EstadoRepository;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import com.rmstudio.rmstudiofitness.repositorios.TipoPlanoRepository;
import com.rmstudio.rmstudiofitness.servicos.PagamentoService;
import com.rmstudio.rmstudiofitness.entidades.Mensalidade;
import com.rmstudio.rmstudiofitness.entidades.TipoPlano;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class ControleNavegacao {

    private final PessoaRepository pessoaRepository;
    private final EstadoRepository estadoRepository;
    private final TipoPlanoRepository tipoPlanoRepository;
    private final PagamentoService pagamentoService;

    @Autowired
    public ControleNavegacao(PessoaRepository pessoaRepository,
                             EstadoRepository estadoRepository,
                             TipoPlanoRepository tipoPlanoRepository,
                             PagamentoService pagamentoService) {
        this.pessoaRepository = pessoaRepository;
        this.estadoRepository = estadoRepository;
        this.tipoPlanoRepository = tipoPlanoRepository;
        this.pagamentoService = pagamentoService;
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
    public String cadastroItensPlano() {
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
    public String minhasMensalidades(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        Pessoa pessoa = pessoaRepository.findByUsuario(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        model.addAttribute("pessoa", pessoa);
        model.addAttribute("planoAtivo", pessoa.getPlanoAtivo());
        
        List<Mensalidade> mensalidades = pagamentoService.findMensalidadesByPessoaId(pessoa.getId());
        model.addAttribute("mensalidades", mensalidades);

        model.addAttribute("pageTitle", "Minhas Mensalidades");
        return "minhas-mensalidades";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Pessoa principal = (Pessoa) authentication.getPrincipal();
            
            // Recarrega a pessoa do banco de dados com a cidade e o estado
            pessoaRepository.findByIdWithCidadeAndEstado(principal.getId())
                .ifPresent(pessoaCompleta -> model.addAttribute("pessoa", pessoaCompleta));
        }
        model.addAttribute("estados", estadoRepository.findAllByOrderByNome());
        return "perfil";
    }
 
    @GetMapping("/minhas-avaliacoes")
    public String minhasAvaliacoes(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Em vez de usar o principal diretamente (que pode estar detached),
            // pegamos o nome de usuário e buscamos do banco novamente para garantir
            // que a sessão do Hibernate esteja ativa para carregar os dados.
            String username = authentication.getName();
            
            // Usa o novo método do repositório que busca pelo username e já carrega as avaliações
            pessoaRepository.findByUsuarioWithAvaliacoes(username).ifPresent(pessoaCompleta -> {
                model.addAttribute("avaliacoes", pessoaCompleta.getAvaliacoes());
            });
        }
        return "minhas-avaliacoes";
    }
 
    @GetMapping("/meus-planos-aula")
    public String meusPlanosAula(Model model, Authentication authentication) {
        model.addAttribute("planosDeAula", Collections.emptyList()); // Default to empty list
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            pessoaRepository.findByUsuarioWithPlanosDeAula(username).ifPresent(pessoaCompleta -> {
                model.addAttribute("planosDeAula", pessoaCompleta.getPlanosDeAula());
            });
        }
        return "meus-planos-aula";
    }
}
