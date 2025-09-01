package com.rmstudio.rmstudiofitness.controladores;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Converter para binding de parâmetros String -> LocalDateTime no Spring MVC.
 * Útil para receber datas em formulários/Ajax (query/form-data) sem JSF.
 *
 * Formatos aceitos:
 *  - "dd/MM/yyyy HH:mm" (pt-BR)
 *  - "yyyy-MM-dd'T'HH:mm" (ISO local datetime, típico de <input type="datetime-local">)
 */
@Component
public class LocalDataTempo implements Converter<String, LocalDateTime> {

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Override
    public LocalDateTime convert(@NonNull String source) {
        String s = source.trim();
        if (s.isEmpty()) return null;

        // tenta formato BR
        try {
            return LocalDateTime.parse(s, BR);
        } catch (DateTimeParseException ignore) { }

        // tenta ISO local (input datetime-local)
        try {
            return LocalDateTime.parse(s, ISO_LOCAL);
        } catch (DateTimeParseException ignore) { }

        throw new IllegalArgumentException(
            "Formato de data/hora inválido. Use 'dd/MM/yyyy HH:mm' ou 'yyyy-MM-ddTHH:mm'. Valor: " + s
        );
    }

    // ===== Utilitários opcionais =====

    /** Formata para padrão BR (dd/MM/yyyy HH:mm) */
    public static String toBr(LocalDateTime value) {
        return value == null ? "" : value.format(BR);
    }

    /** Formata para padrão ISO local (yyyy-MM-dd'T'HH:mm) */
    public static String toIsoLocal(LocalDateTime value) {
        return value == null ? "" : value.format(ISO_LOCAL);
    }

    /** Tenta validar se string é um datetime aceitável por este converter */
    public static boolean isValid(String s) {
        if (s == null || s.trim().isEmpty()) return false;
        try { LocalDateTime.parse(s.trim(), BR); return true; }
        catch (DateTimeParseException e1) {
            try { LocalDateTime.parse(s.trim(), ISO_LOCAL); return true; }
            catch (DateTimeParseException e2) { return false; }
        }
    }

    /** Retorna o padrão BR principal, útil em mensagens */
    public static String patternBr() { return "dd/MM/yyyy HH:mm"; }
}
