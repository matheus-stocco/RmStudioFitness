package com.rmstudio.rmstudiofitness.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Atalhos de navegação por redirect (úteis para botões/links).
 * Ex.: GET /navegacao/pessoas -> redirect:/CadastroPessoas
 */
@Controller
@RequestMapping("/navegacao")
public class ControleNavegacao {

    @GetMapping("/home")
    public String home() { return "redirect:/"; }

    @GetMapping("/login")
    public String toLogin() { return "redirect:/login"; }

    @GetMapping("/estado")
    public String toEstado() { return "redirect:/CadastroEstado"; }

    @GetMapping("/cidade")
    public String toCidade() { return "redirect:/CadastroCidade"; }

    @GetMapping("/pessoas")
    public String toPessoas() { return "redirect:/CadastroPessoas"; }

    @GetMapping("/exercicios")
    public String toExercicios() { return "redirect:/CadastroExercicios"; }

    @GetMapping("/planoaula")
    public String toPlanoAula() { return "redirect:/CadastroPlanoAula"; }

    @GetMapping("/itensplano")
    public String toItensPlano() { return "redirect:/CadastroItensPlano"; }

    @GetMapping("/tipoplano")
    public String toTipoPlano() { return "redirect:/CadastroTipoPlano"; }

    @GetMapping("/avaliacoes")
    public String toAvaliacoes() { return "redirect:/CadastroAvaliacaoFisica"; }
}
