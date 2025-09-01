package com.rmstudio.rmstudiofitness.conversores;

import com.rmstudio.rmstudiofitness.entidades.Cidade;
import com.rmstudio.rmstudiofitness.repositorios.CidadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToCidadeConverter implements Converter<String, Cidade> {

    @Autowired
    private CidadeRepository cidadeRepository;

    @Override
    public Cidade convert(@NonNull String source) {
        if (source != null && !source.isEmpty()) {
            try {
                Long id = Long.valueOf(source);
                return cidadeRepository.findById(id).orElse(null);
            } catch (NumberFormatException e) {
                // Lidar com a falha na conversão se necessário
                return null;
            }
        }
        return null;
    }
}
