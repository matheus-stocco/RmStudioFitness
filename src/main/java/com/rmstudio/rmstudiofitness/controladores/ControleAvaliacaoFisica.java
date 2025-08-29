package com.rmstudio.rmstudiofitness.controladores;

import com.rmstudio.rmstudiofitness.entidades.AvaliacaoFisica;
import com.rmstudio.rmstudiofitness.entidades.Pessoa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avaliacoes")
public class ControleAvaliacaoFisica {

    @PersistenceContext
    private EntityManager em;

    /** Payload para criação/atualização. */
    public record AvaliacaoPayload(
            Long pessoaId,
            Double peso,
            Double altura,
            Double gorduraCorporal,
            Double massaMagra,
            Double aguaCorporal,
            Double massaOssea,
            Double taxaBasal,          // opcional
            LocalDate dataAvaliacao    // opcional: ISO "yyyy-MM-dd"
    ) {}

    // ===== LISTAR =====
    @GetMapping
    public List<AvaliacaoFisica> listar(@RequestParam(name = "pessoaId", required = false) Long pessoaId) {
        String jpql = "SELECT a FROM AvaliacaoFisica a JOIN FETCH a.pessoa "
                    + (pessoaId != null ? "WHERE a.pessoa.id = :pid " : "")
                    + "ORDER BY a.dataAvaliacao DESC";

        TypedQuery<AvaliacaoFisica> q = em.createQuery(jpql, AvaliacaoFisica.class);
        if (pessoaId != null) q.setParameter("pid", pessoaId);
        return q.getResultList();
    }

    // ===== DETALHE =====
    @GetMapping("/{id}")
    public ResponseEntity<AvaliacaoFisica> buscar(@PathVariable Long id) {
        List<AvaliacaoFisica> res = em.createQuery(
                "SELECT a FROM AvaliacaoFisica a JOIN FETCH a.pessoa WHERE a.id = :id", AvaliacaoFisica.class)
                .setParameter("id", id)
                .getResultList();
        return res.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(res.get(0));
    }

    // ===== CRIAR =====
    @PostMapping
    @Transactional
    public ResponseEntity<?> criar(@RequestBody AvaliacaoPayload body) {
        String msg = validar(body);
        if (msg != null) return badRequest(msg);

        Pessoa pessoa = em.find(Pessoa.class, body.pessoaId());
        if (pessoa == null) return badRequest("Pessoa não encontrada.");

        AvaliacaoFisica a = new AvaliacaoFisica();
        a.setPessoa(pessoa);
        a.setDataAvaliacao(body.dataAvaliacao() != null ? body.dataAvaliacao() : LocalDate.now());

        if (body.peso()            != null) a.setPeso(BigDecimal.valueOf(body.peso()));
        if (body.altura()          != null) a.setAltura(BigDecimal.valueOf(body.altura()));
        if (body.gorduraCorporal() != null) a.setGorduraCorporal(BigDecimal.valueOf(body.gorduraCorporal()));
        if (body.massaMagra()      != null) a.setMassaMagra(BigDecimal.valueOf(body.massaMagra()));
        if (body.aguaCorporal()    != null) a.setAguaCorporal(BigDecimal.valueOf(body.aguaCorporal()));
        if (body.massaOssea()      != null) a.setMassaOssea(BigDecimal.valueOf(body.massaOssea()));
        // Se sua entidade tiver esse campo, descomente com o setter correto:
        // if (body.taxaBasal()       != null) a.setTaxaBasal(BigDecimal.valueOf(body.taxaBasal()));
        // ou: a.setTaxaMetabolicaBasal(BigDecimal.valueOf(body.taxaBasal()));

        em.persist(a);
        return ResponseEntity.created(URI.create("/api/avaliacoes/" + a.getId())).body(a);
    }

    // ===== ATUALIZAR =====
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody AvaliacaoPayload body) {
        AvaliacaoFisica a = em.find(AvaliacaoFisica.class, id);
        if (a == null) return ResponseEntity.notFound().build();

        if (body.pessoaId() != null) {
            Pessoa pessoa = em.find(Pessoa.class, body.pessoaId());
            if (pessoa == null) return badRequest("Pessoa não encontrada.");
            a.setPessoa(pessoa);
        }
        if (body.dataAvaliacao()   != null) a.setDataAvaliacao(body.dataAvaliacao());
        if (body.peso()            != null) a.setPeso(BigDecimal.valueOf(body.peso()));
        if (body.altura()          != null) a.setAltura(BigDecimal.valueOf(body.altura()));
        if (body.gorduraCorporal() != null) a.setGorduraCorporal(BigDecimal.valueOf(body.gorduraCorporal()));
        if (body.massaMagra()      != null) a.setMassaMagra(BigDecimal.valueOf(body.massaMagra()));
        if (body.aguaCorporal()    != null) a.setAguaCorporal(BigDecimal.valueOf(body.aguaCorporal()));
        if (body.massaOssea()      != null) a.setMassaOssea(BigDecimal.valueOf(body.massaOssea()));
        // if (body.taxaBasal()       != null) a.setTaxaBasal(BigDecimal.valueOf(body.taxaBasal()));
        // ou o nome correto do setter, se existir.

        return ResponseEntity.ok(a);
    }

    // ===== EXCLUIR =====
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        AvaliacaoFisica a = em.find(AvaliacaoFisica.class, id);
        if (a == null) return ResponseEntity.notFound().build();
        em.remove(a);
        return ResponseEntity.noContent().build();
    }

    // ===== IMC (por id) =====
    @GetMapping("/{id}/imc")
    public ResponseEntity<?> imcPorId(@PathVariable Long id) {
        AvaliacaoFisica a = em.find(AvaliacaoFisica.class, id);
        if (a == null) return ResponseEntity.notFound().build();
        Double peso = a.getPeso() != null ? a.getPeso().doubleValue() : null;
        Double altura = a.getAltura() != null ? a.getAltura().doubleValue() : null;
        if (peso == null || altura == null || altura <= 0) return badRequest("Registro sem peso/altura válidos.");
        double imc = peso / (altura * altura);
        return ResponseEntity.ok(Map.of(
                "id", id,
                "imc", round2(imc),
                "classificacao", classificarImc(imc)
        ));
    }

    // ===== IMC (direto por query) =====
    @GetMapping("/imc")
    public ResponseEntity<?> imcDireto(@RequestParam Double peso, @RequestParam Double altura) {
        if (peso == null || peso <= 0) return badRequest("peso inválido");
        if (altura == null || altura <= 0) return badRequest("altura inválida");
        double imc = peso / (altura * altura);
        return ResponseEntity.ok(Map.of(
                "imc", round2(imc),
                "classificacao", classificarImc(imc)
        ));
    }

    // ===== helpers =====
    private String validar(AvaliacaoPayload b) {
        if (b == null) return "Payload ausente";
        if (b.pessoaId() == null || b.pessoaId() <= 0) return "pessoaId obrigatório";
        if (b.peso() == null || b.peso() <= 0) return "peso obrigatório";
        if (b.altura() == null || b.altura() <= 0) return "altura obrigatória";
        return null;
    }

    private ResponseEntity<Map<String, Object>> badRequest(String msg) {
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String classificarImc(double imc) {
        if (imc < 18.5) return "Magreza";
        if (imc < 25)   return "Normal";
        if (imc < 30)   return "Sobrepeso";
        if (imc < 35)   return "Obesidade I";
        if (imc < 40)   return "Obesidade II";
        return "Obesidade III";
    }
}
