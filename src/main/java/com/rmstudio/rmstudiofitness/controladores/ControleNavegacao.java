package com.rmstudio.rmstudiofitness.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControleNavegacao {
    
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
}
