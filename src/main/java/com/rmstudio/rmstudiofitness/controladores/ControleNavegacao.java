package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.repositorios.EstadoRepository;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import java.util.Collections;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControleNavegacao {

    private final PessoaRepository pessoaRepository;
    private final EstadoRepository estadoRepository;

    public ControleNavegacao(PessoaRepository pessoaRepository, EstadoRepository estadoRepository) {
        this.pessoaRepository = pessoaRepository;
        this.estadoRepository = estadoRepository;
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

    @GetMapping({"/planos", "/TiposDePlanos.html"})
    public String planos() {
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
