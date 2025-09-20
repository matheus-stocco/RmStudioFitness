package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.AvaliacaoFisica;
import com.rmstudio.rmstudiofitness.entidades.Pessoa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/avaliacoes")
public class ControleAvaliacaoFisica {

    @PersistenceContext
    private EntityManager em;

    public record AvaliacaoPayload(
            Long pessoaId,
            Double peso,
            Double altura,
            Double gorduraCorporal,
            Double massaMagra,
            Double massaMuscular,
            Double hidratacao,
            Double densidadeOssea,
            Double taxaMetabolismoBasal,
            Double gorduraVisceral
    ) {}

    private AvaliacaoFisica findAvaliacaoCompleta(Long id) {
        return em.createQuery(
            "SELECT a FROM AvaliacaoFisica a JOIN FETCH a.pessoa p JOIN FETCH p.cidade c JOIN FETCH c.estado WHERE a.id = :id", 
            AvaliacaoFisica.class)
            .setParameter("id", id)
            .getSingleResult();
    }

    @GetMapping
    public List<AvaliacaoFisica> listar(@RequestParam(name = "pessoaId", required = false) Long pessoaId) {
        String jpql = "SELECT a FROM AvaliacaoFisica a JOIN FETCH a.pessoa p JOIN FETCH p.cidade c JOIN FETCH c.estado "
                    + (pessoaId != null ? "WHERE a.pessoa.id = :pid " : "")
                    + "ORDER BY a.dataAvaliacao DESC";
        return em.createQuery(jpql, AvaliacaoFisica.class).getResultList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvaliacaoFisica> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(findAvaliacaoCompleta(id));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<AvaliacaoFisica> criar(@RequestBody AvaliacaoPayload body) {
        String msg = validar(body);
        if (msg != null) return ResponseEntity.badRequest().body(null);

        Pessoa pessoa = em.find(Pessoa.class, body.pessoaId());
        if (pessoa == null) return ResponseEntity.badRequest().body(null);

        AvaliacaoFisica a = new AvaliacaoFisica();
        a.setPessoa(pessoa);
        a.setDataAvaliacao(LocalDate.now());

        if (body.peso() != null) a.setPeso(BigDecimal.valueOf(body.peso()));
        if (body.altura() != null) a.setAltura(BigDecimal.valueOf(body.altura()));
        if (body.gorduraCorporal() != null) a.setGorduraCorporal(BigDecimal.valueOf(body.gorduraCorporal()));
        if (body.massaMagra() != null) a.setMassaMagra(BigDecimal.valueOf(body.massaMagra()));
        if (body.massaMuscular() != null) a.setMassaMuscular(BigDecimal.valueOf(body.massaMuscular()));
        if (body.hidratacao() != null) a.setHidratacao(BigDecimal.valueOf(body.hidratacao()));
        if (body.densidadeOssea() != null) a.setDensidadeOssea(BigDecimal.valueOf(body.densidadeOssea()));
        if (body.taxaMetabolismoBasal() != null) a.setTaxaMetabolismoBasal(BigDecimal.valueOf(body.taxaMetabolismoBasal()));
        if (body.gorduraVisceral() != null) a.setGorduraVisceral(BigDecimal.valueOf(body.gorduraVisceral()));
        
        em.persist(a);
        em.flush(); 

        AvaliacaoFisica avaliacaoSalva = findAvaliacaoCompleta(a.getId());
        return ResponseEntity.created(URI.create("/api/avaliacoes/" + avaliacaoSalva.getId())).body(avaliacaoSalva);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<AvaliacaoFisica> atualizar(@PathVariable Long id, @RequestBody AvaliacaoPayload body) {
        AvaliacaoFisica a = em.find(AvaliacaoFisica.class, id);
        if (a == null) return ResponseEntity.notFound().build();

        if (body.pessoaId() != null) {
            Pessoa pessoa = em.find(Pessoa.class, body.pessoaId());
            if (pessoa == null) return ResponseEntity.badRequest().body(null);
            a.setPessoa(pessoa);
        }
        
        if (body.peso() != null) a.setPeso(BigDecimal.valueOf(body.peso()));
        if (body.altura() != null) a.setAltura(BigDecimal.valueOf(body.altura()));
        if (body.gorduraCorporal() != null) a.setGorduraCorporal(BigDecimal.valueOf(body.gorduraCorporal()));
        if (body.massaMagra() != null) a.setMassaMagra(BigDecimal.valueOf(body.massaMagra()));
        if (body.massaMuscular() != null) a.setMassaMuscular(BigDecimal.valueOf(body.massaMuscular()));
        if (body.hidratacao() != null) a.setHidratacao(BigDecimal.valueOf(body.hidratacao()));
        if (body.densidadeOssea() != null) a.setDensidadeOssea(BigDecimal.valueOf(body.densidadeOssea()));
        if (body.taxaMetabolismoBasal() != null) a.setTaxaMetabolismoBasal(BigDecimal.valueOf(body.taxaMetabolismoBasal()));
        if (body.gorduraVisceral() != null) a.setGorduraVisceral(BigDecimal.valueOf(body.gorduraVisceral()));

        em.flush(); 

        return ResponseEntity.ok(findAvaliacaoCompleta(a.getId()));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        AvaliacaoFisica a = em.find(AvaliacaoFisica.class, id);
        if (a == null) return ResponseEntity.notFound().build();
        em.remove(a);
        return ResponseEntity.noContent().build();
    }

    private String validar(AvaliacaoPayload b) {
        if (b == null) return "Payload ausente";
        if (b.pessoaId() == null || b.pessoaId() <= 0) return "pessoaId obrigatório";
        if (b.peso() == null || b.peso() <= 0) return "peso obrigatório";
        if (b.altura() == null || b.altura() <= 0) return "altura obrigatória";
        return null;
    }
}
