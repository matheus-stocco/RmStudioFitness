package com.rmstudio.rmstudiofitness.conversores;

import com.rmstudio.rmstudiofitness.entidades.Estado;
import com.rmstudio.rmstudiofitness.controladores.ControlePessoa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EstadoConverter implements Converter<String, Estado> {

    @Autowired
    @Lazy
    private ControlePessoa controlePessoa;

    @Override
    public Estado convert(String source) {
        if (source == null) return null;
        String s = source.trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;

        List<Estado> estados = controlePessoa.getEstados();
        if (estados == null || estados.isEmpty()) return null;

        // 1) tenta por ID (numérico)
        try {
            Long id = Long.valueOf(s);
            for (Estado est : estados) {
                if (est != null && est.getId() != null && est.getId().equals(id)) {
                    return est;
                }
            }
        } catch (NumberFormatException ignore) { /* não numérico, tenta por UF/Nome */ }

        // 2) tenta por UF
        for (Estado est : estados) {
            if (est != null && est.getUf() != null && est.getUf().equalsIgnoreCase(s)) {
                return est;
            }
        }

        // 3) tenta por Nome
        for (Estado est : estados) {
            if (est != null && est.getNome() != null && est.getNome().equalsIgnoreCase(s)) {
                return est;
            }
        }

        return null;
    }

    /** Converte Estado -> String (ID) */
    public String estadoToString(Estado estado) {
        return (estado == null || estado.getId() == null) ? "" : String.valueOf(estado.getId());
    }

    /** Busca por ID na lista carregada pelo controle */
    public Estado buscarPorId(Long id) {
        if (id == null) return null;
        List<Estado> estados = controlePessoa.getEstados();
        if (estados == null) return null;
        for (Estado est : estados) {
            if (est != null && id.equals(est.getId())) return est;
        }
        return null;
    }

    /** Busca por UF na lista carregada pelo controle */
    public Estado buscarPorUf(String uf) {
        if (uf == null || uf.isEmpty()) return null;
        List<Estado> estados = controlePessoa.getEstados();
        if (estados == null) return null;
        for (Estado est : estados) {
            if (est != null && est.getUf() != null && est.getUf().equalsIgnoreCase(uf)) {
                return est;
            }
        }
        return null;
    }

    /** Busca por nome na lista carregada pelo controle */
    public Estado buscarPorNome(String nome) {
        if (nome == null || nome.isEmpty()) return null;
        List<Estado> estados = controlePessoa.getEstados();
        if (estados == null) return null;
        for (Estado est : estados) {
            if (est != null && est.getNome() != null && est.getNome().equalsIgnoreCase(nome)) {
                return est;
            }
        }
        return null;
    }
}
