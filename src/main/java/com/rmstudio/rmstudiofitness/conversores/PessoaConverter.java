package com.rmstudio.rmstudiofitness.conversores;

import com.rmstudio.rmstudiofitness.entidades.Pessoa;
import com.rmstudio.rmstudiofitness.repositorios.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PessoaConverter implements Converter<String, Pessoa> {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Override
    public Pessoa convert(@NonNull String source) {
        if (source != null && !source.isEmpty()) {
            try {
                Long id = Long.valueOf(source);
                return pessoaRepository.findById(id).orElse(null);
            } catch (NumberFormatException e) {
                return null; // ou lançar uma exceção
            }
        }
        return null;
    }
}
