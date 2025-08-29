package com.rmstudio.rmstudiofitness.entidades;

public enum Perfil {
    ALUNO("Aluno"),
    PERSONAL("Personal"),
    GERENTE("Gerente");

    private final String label;

    Perfil(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
