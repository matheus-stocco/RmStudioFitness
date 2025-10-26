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

    @Value("${paghiper.api.token}")
    private String apiToken;

    @Value("${paghiper.api.cancel.url}")
    private String cancelUrl;

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

    public String getApiToken() {
        return apiToken;
    }

    /**
     * Verifica se a configuração da PagHiper está válida para cancelamento
     */
    public boolean isConfiguracaoValida() {
        return apiToken != null && 
               !apiToken.trim().isEmpty() && 
               !"seu_token_aqui".equals(apiToken) && 
               !"seu_token_paghiper_aqui".equals(apiToken) &&
               apiKey != null && 
               !apiKey.trim().isEmpty() &&
               cancelUrl != null && 
               !cancelUrl.trim().isEmpty();
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

    public String cancelarTransacao(String transactionId) {
        // Verifica se as configurações estão válidas
        if (apiToken == null || apiToken.trim().isEmpty() || 
            "seu_token_aqui".equals(apiToken) || "seu_token_paghiper_aqui".equals(apiToken)) {
            throw new RuntimeException("Token da PagHiper não configurado. Configure a variável PAGHIPER_TOKEN ou a propriedade paghiper.api.token com seu token real da PagHiper");
        }
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API Key da PagHiper não configurada");
        }
        
        // Usa a URL de cancelamento da PagHiper (endpoint correto)
        String cancelUrl = "https://pix.paghiper.com/invoice/cancel/";
        
        System.out.println("=== DEBUG PAGHIPER CANCEL ===");
        System.out.println("API Token: " + (apiToken != null ? "Configurado (" + apiToken.substring(0, Math.min(10, apiToken.length())) + "...)" : "NULL"));
        System.out.println("API Key: " + (apiKey != null ? "Configurado (" + apiKey.substring(0, Math.min(10, apiKey.length())) + "...)" : "NULL"));
        System.out.println("Cancel URL: " + cancelUrl);
        System.out.println("Transaction ID: " + transactionId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        
        try {
            // Cria o JSON manualmente para garantir o formato correto
            String requestJson = String.format(
                "{\"token\":\"%s\",\"apiKey\":\"%s\",\"status\":\"canceled\",\"transaction_id\":\"%s\"}",
                apiToken, apiKey, transactionId
            );
            
            System.out.println("Request JSON: " + requestJson);
            
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(cancelUrl, entity, String.class);
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                // Verifica se a resposta indica sucesso
                if (responseBody != null && responseBody.contains("success")) {
                    System.out.println("✅ Cancelamento confirmado na PagHiper!");
                    return responseBody;
                } else {
                    System.out.println("⚠️ Resposta da PagHiper não indica sucesso: " + responseBody);
                    return responseBody != null ? responseBody : "Resposta vazia";
                }
            } else {
                String errorBody = response.getBody();
                throw new RuntimeException("Erro HTTP ao cancelar transação: " + response.getStatusCode() + " - " + (errorBody != null ? errorBody : "Sem detalhes"));
            }
        } catch (Exception e) {
            System.out.println("❌ Erro na requisição: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha na comunicação com a API PagHiper para cancelamento", e);
        }
    }
}
