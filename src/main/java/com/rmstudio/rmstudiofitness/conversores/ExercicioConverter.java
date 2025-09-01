package com.rmstudio.rmstudiofitness.conversores;

import com.rmstudio.rmstudiofitness.entidades.Exercicio;
import com.rmstudio.rmstudiofitness.repositorios.ExercicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ExercicioConverter implements Converter<String, Exercicio> {

    @Autowired
    private ExercicioRepository exercicioRepository;

    @Override
    public Exercicio convert(@NonNull String source) {
        if (source != null && !source.isEmpty()) {
            try {
                Long id = Long.valueOf(source);
                return exercicioRepository.findById(id).orElse(null);
            } catch (NumberFormatException e) {
                // Lidar com a falha na conversão de String para Long, se necessário
                return null;
            }
        }
        return null;
    }
}
