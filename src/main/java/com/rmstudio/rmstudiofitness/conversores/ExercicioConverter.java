package com.rmstudio.rmstudiofitness.conversores;

import com.rmstudio.rmstudiofitness.entidades.Exercicio;
import com.rmstudio.rmstudiofitness.repositorios.ExercicioRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ExercicioConverter implements Converter<String, Exercicio> {

    private final ExercicioRepository repo;

    public ExercicioConverter(ExercicioRepository repo) {
        this.repo = repo;
    }

    @Override
    public Exercicio convert(String source) {
        if (source == null) return null;
        String s = source.trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        try {
            Long id = Long.valueOf(s);
            return repo.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            return null; // não é ID válido
        }
    }
}
