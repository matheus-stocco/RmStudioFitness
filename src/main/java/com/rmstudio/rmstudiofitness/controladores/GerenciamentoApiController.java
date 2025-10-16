package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.dtos.PessoaDto;
import com.rmstudio.rmstudiofitness.servicos.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gerenciamento")
public class GerenciamentoApiController {

    @Autowired
    private PessoaService pessoaService;

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<PessoaDto>> listarUsuarios(
            @RequestParam(required = false, defaultValue = "") String nome,
            @RequestParam(required = false, defaultValue = "") String role) {
        List<PessoaDto> usuarios = pessoaService.buscarUsuariosPorNomeERole(nome, role);
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping("/usuarios/{id}/adicionar-role-personal")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> adicionarRolePersonal(@PathVariable Long id) {
        pessoaService.adicionarRolePersonal(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/usuarios/{id}/remover-role-personal")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> removerRolePersonal(@PathVariable Long id) {
        pessoaService.removerRolePersonal(id);
        return ResponseEntity.ok().build();
    }
}
