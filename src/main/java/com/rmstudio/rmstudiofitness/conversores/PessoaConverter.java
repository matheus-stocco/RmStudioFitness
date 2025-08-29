package com.rmstudio.rmstudiofitness.conversores;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PessoaConverter implements Converter<String, Pessoa> {

    private final PessoaRepository repo;

    public PessoaConverter(PessoaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Pessoa convert(String source) {
        if (source == null) return null;
        String s = source.trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        try {
            Long id = Long.valueOf(s);
            return repo.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            return null; // não veio ID válido
        }
    }
}
