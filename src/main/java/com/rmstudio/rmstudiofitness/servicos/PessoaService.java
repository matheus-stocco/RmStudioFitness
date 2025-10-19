package com.rmstudio.rmstudiofitness.servicos;

import com.rmstudio.rmstudiofitness.dtos.PessoaDto;
import com.rmstudio.rmstudiofitness.entidades.Perfil;
import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.repositorios.PerfilRepository;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Transactional(readOnly = true)
    public List<PessoaDto> buscarUsuariosPorNomeERole(String nome, String role) {
        List<Pessoa> pessoas;
        if (role != null && !role.isEmpty()) {
            pessoas = pessoaRepository.findByNomeContainingIgnoreCaseAndPerfis_Nome(nome, role);
        } else {
            pessoas = pessoaRepository.findByNomeContainingIgnoreCase(nome);
        }
        return pessoas.stream().map(PessoaDto::new).collect(Collectors.toList());
    }

    @Transactional
    public void adicionarRolePersonal(Long pessoaId) {
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Perfil personalPerfil = perfilRepository.findByNome("ROLE_PERSONAL")
                .orElseThrow(() -> new RuntimeException("Perfil PERSONAL não encontrado"));
        pessoa.getPerfis().add(personalPerfil);
        pessoaRepository.save(pessoa);
    }

    @Transactional
    public void removerRolePersonal(Long pessoaId) {
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Perfil personalPerfil = perfilRepository.findByNome("ROLE_PERSONAL")
                .orElseThrow(() -> new RuntimeException("Perfil PERSONAL não encontrado"));
        pessoa.getPerfis().remove(personalPerfil);
        pessoaRepository.save(pessoa);
    }
}
