package com.rmstudio.rmstudiofitness.entidades;

public enum DiaSemana {
    SEGUNDA("Segunda-feira"),
    TERCA("Terça-feira"),
    QUARTA("Quarta-feira"),
    QUINTA("Quinta-feira"),
    SEXTA("Sexta-feira"),
    SABADO("Sábado"),
    DOMINGO("Domingo");

    private final String label;

    DiaSemana(String label) { this.label = label; }

    public String getLabel() { return label; }
}
