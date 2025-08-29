package com.rmstudio.rmstudiofitness.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Encaminha rotas amigáveis (sem .html) para os arquivos em /static.
 * Ex.: GET /CadastroEstado  -> forward:/CadastroEstado.html
 */
@Controller
public class ControleHome {

    @GetMapping({"/", "/index"})
    public String index() { return "forward:/index.html"; }

    @GetMapping("/login")
    public String login() { return "forward:/login.html"; }

    @GetMapping("/CadastroEstado")
    public String cadastroEstado() { return "forward:/CadastroEstado.html"; }

    @GetMapping("/CadastroCidade")
    public String cadastroCidade() { return "forward:/CadastroCidade.html"; }

    @GetMapping("/CadastroPessoas")
    public String cadastroPessoas() { return "forward:/CadastroPessoas.html"; }

    @GetMapping("/CadastroExercicios")
    public String cadastroExercicios() { return "forward:/CadastroExercicios.html"; }

    @GetMapping("/CadastroPlanoAula")
    public String cadastroPlanoAula() { return "forward:/CadastroPlanoAula.html"; }

    @GetMapping("/CadastroItensPlano")
    public String cadastroItensPlano() { return "forward:/CadastroItensPlano.html"; }

    @GetMapping("/CadastroTipoPlano")
    public String cadastroTipoPlano() { return "forward:/CadastroTipoPlano.html"; }

    @GetMapping("/CadastroAvaliacaoFisica")
    public String cadastroAvaliacaoFisica() { return "forward:/CadastroAvaliacaoFisica.html"; }
}
