package com.rmstudio.rmstudiofitness.servicos;

import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
public class AgendamentoService {

    public LocalDate calcularProximoVencimento() {
        LocalDate hoje = LocalDate.now();
        LocalDate proximoMes = hoje.plusMonths(1).withDayOfMonth(1);
        return encontrarQuintoDiaUtil(proximoMes);
    }

    private LocalDate encontrarQuintoDiaUtil(LocalDate primeiroDiaDoMes) {
        LocalDate data = primeiroDiaDoMes;
        int diasUteisContados = 0;
        while (diasUteisContados < 5) {
            if (isDiaUtil(data)) {
                diasUteisContados++;
            }
            if (diasUteisContados < 5) {
                data = data.plusDays(1);
            }
        }
        return data;
    }
    
    // Tornado público para uso em regras adicionais
    public LocalDate encontrarQuintoDiaUtilPublic(LocalDate primeiroDiaDoMes) {
        return encontrarQuintoDiaUtil(primeiroDiaDoMes);
    }

    // Ajusta uma data para o próximo dia útil caso caia em final de semana
    public LocalDate ajustarParaProximoDiaUtil(LocalDate data) {
        LocalDate d = data;
        while (!isDiaUtil(d)) {
            d = d.plusDays(1);
        }
        return d;
    }

    private boolean isDiaUtil(LocalDate data) {
        DayOfWeek diaDaSemana = data.getDayOfWeek();
        return diaDaSemana != DayOfWeek.SATURDAY && diaDaSemana != DayOfWeek.SUNDAY;
    }
}
