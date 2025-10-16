package com.rmstudio.rmstudiofitness.controladores;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gerenciamento")
public class GerenciamentoController {

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String gerenciarRoles() {
        return "gerenciamento-roles";
    }
}
