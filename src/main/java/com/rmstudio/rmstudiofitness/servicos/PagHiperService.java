package com.rmstudio.rmstudiofitness.servicos;

import com.rmstudio.rmstudiofitness.paghiper.dto.PagHiperRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PagHiperService {

    @Value("${paghiper.api.key}")
    private String apiKey;

    @Value("${paghiper.api.url}")
    private String apiUrl;

    @Value("${paghiper.notification.url}")
    private String notificationUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PagHiperService(RestTemplateBuilder restTemplateBuilder) {
        // Usa o RestTemplateBuilder para construir um RestTemplate já configurado
        // com os conversores de mensagem padrão (incluindo JSON).
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public String criarPix(PagHiperRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        
        try {
            String requestJson = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                // Lidar com respostas de erro da API
                throw new RuntimeException("Erro ao criar PIX: " + response.getBody());
            }
        } catch (Exception e) {
            // Lidar com erros de conexão ou outros problemas
            throw new RuntimeException("Falha na comunicação com a API PagHiper", e);
        }
    }
}
