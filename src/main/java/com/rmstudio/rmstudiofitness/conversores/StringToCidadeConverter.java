package com.rmstudio.rmstudiofitness.conversores;

import com.rmstudio.rmstudiofitness.entidades.Cidade;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class StringToCidadeConverter implements Converter<String, Cidade> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Cidade convert(String source) {
        if (source == null || source.isBlank()) return null;
        try {
            Long id = Long.valueOf(source);
            return em.find(Cidade.class, id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
