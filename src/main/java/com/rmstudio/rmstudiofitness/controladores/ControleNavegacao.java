package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControleNavegacao {

    private final PessoaRepository pessoaRepository;

    public ControleNavegacao(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
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
        return "perfil";
    }
}
