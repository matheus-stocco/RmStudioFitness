package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.TipoPlano;
import com.rmstudio.rmstudiofitness.repositorios.TipoPlanoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * API REST para CRUD de TipoPlano.
 * Endpoints:
 *  GET    /api/tipos-plano               -> lista todos (ordenado por nome)
 *  GET    /api/tipos-plano/{id}          -> busca por id
 *  POST   /api/tipos-plano               -> cria
 *  PUT    /api/tipos-plano/{id}          -> atualiza
 *  DELETE /api/tipos-plano/{id}          -> remove
 */
@RestController
@RequestMapping("/api/tipos-plano")
public class ControleTipoPlano {

    private final TipoPlanoRepository tipoPlanoRepository;

    @Autowired
    public ControleTipoPlano(TipoPlanoRepository tipoPlanoRepository) {
        this.tipoPlanoRepository = tipoPlanoRepository;
    }

    // ---------- LISTAR ----------
    @GetMapping
    public List<TipoPlano> listar() {
        return tipoPlanoRepository.findAllByOrderByNome();
    }

    // ---------- BUSCAR POR ID ----------
    @GetMapping("/{id}")
    public ResponseEntity<TipoPlano> buscarPorId(@PathVariable Long id) {
        return tipoPlanoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------- CRIAR ----------
    @PostMapping
    @Transactional
    public ResponseEntity<TipoPlano> criar(@RequestBody TipoPlano tipoPlano) {
        validar(tipoPlano);
        TipoPlano novoTipoPlano = tipoPlanoRepository.save(tipoPlano);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(novoTipoPlano.getId()).toUri();
        return ResponseEntity.created(location).body(novoTipoPlano);
    }

    // ---------- ATUALIZAR ----------
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<TipoPlano> atualizar(@PathVariable Long id, @RequestBody TipoPlano payload) {
        validar(payload);
        
        return tipoPlanoRepository.findById(id)
            .map(existente -> {
                existente.setNome(payload.getNome());
                existente.setDescricao(payload.getDescricao());
                existente.setValor(payload.getValor());
                // Adicione outros campos para atualizar conforme necessário
                // existente.setDuracaoMeses(payload.getDuracaoMeses());
                // existente.setAtivo(payload.getAtivo());
                
                TipoPlano atualizado = tipoPlanoRepository.save(existente);
                return ResponseEntity.ok(atualizado);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // ---------- EXCLUIR ----------
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!tipoPlanoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        tipoPlanoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- VALIDAÇÃO BÁSICA ----------
    private void validar(TipoPlano t) {
        if (t == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corpo da requisição vazio.");
        }
        if (t.getNome() == null || t.getNome().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome do tipo de plano.");
        }
        if (t.getValor() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o valor do tipo de plano.");
        }
        if (t.getValor().doubleValue() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor deve ser maior ou igual a zero.");
        }
    }
}
