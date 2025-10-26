# Configuração de Webhook do PagHiper

Este documento explica como configurar o webhook do PagHiper para receber notificações automáticas de pagamento.

## 📋 Pré-requisitos

- Conta ativa no PagHiper
- Sistema em produção com URL pública acessível
- Certificado SSL/HTTPS (obrigatório para webhooks)

## 🔧 Configuração no PagHiper

### Passo 1: Acessar as Configurações

1. Acesse o painel administrativo do PagHiper
2. Vá em **Configurações** → **Webhooks** ou **Notificações**

### Passo 2: Configurar URL do Webhook

A URL do webhook deve ser configurada como:

```
https://seu-dominio.com.br/api/pagamentos/notificacao
```

**Exemplo:**
```
https://rmstudiofitness.com.br/api/pagamentos/notificacao
```

### Passo 3: Configurar Eventos

Configure o webhook para enviar notificações para os seguintes eventos:

- ✅ Pagamento aprovado
- ✅ Pagamento confirmado
- ✅ Pagamento aguardando
- ✅ Pagamento cancelado
- ✅ Estorno/reembolso

### Passo 4: Salvar Configuração

Salve a configuração no painel do PagHiper.

## 🔍 Como Funciona

### Fluxo de Notificação

1. **Cliente paga via PIX** → Pagamento processado pelo PagHiper
2. **PagHiper envia webhook** → POST para `/api/pagamentos/notificacao`
3. **Sistema processa notificação** → `PagamentoService.processarNotificacao()`
4. **Status da mensalidade atualizado** → Automáticamente para "PAGO"

### Formato da Notificação

O PagHiper envia uma notificação no seguinte formato JSON:

```json
{
  "apiKey": "sua_api_key",
  "transaction_id": "ABC123XYZ",
  "notification_id": "NOT123456",
  "status": "paid",
  "order_id": "123"
}
```

### Mapeamento de Status

| Status PagHiper | Status Interno | Descrição |
|----------------|---------------|-----------|
| `paid`, `approved`, `completed` | `PAGO` | Pagamento confirmado |
| `pending`, `reserved`, `waiting_payment` | `PENDENTE` | Aguardando pagamento |
| `canceled`, `cancelled`, `refunded` | `CANCELADO` | Cancelado ou estornado |

## 🧪 Testando o Webhook

### Método 1: Usando Postman ou cURL

Envie uma requisição POST simulando a notificação do PagHiper:

```bash
curl -X POST https://seu-dominio.com.br/api/pagamentos/notificacao \
  -H "Content-Type: application/json" \
  -d '{
    "apiKey": "sua_api_key",
    "transaction_id": "ABC123XYZ",
    "notification_id": "TEST123",
    "status": "paid",
    "order_id": "123"
  }'
```

### Método 2: Verificar Logs

Verifique os logs do sistema para ver se a notificação foi recebida:

```log
=== INÍCIO PROCESSAMENTO NOTIFICAÇÃO PAGHIPER ===
Transaction ID: ABC123XYZ
Status recebido: paid
Notification ID: TEST123
Order ID: 123
Mensalidade encontrada - ID: 1, Status atual: PENDENTE
Atualizando mensalidade 1 de 'PENDENTE' para 'PAGO'
Data de pagamento definida para hoje: 2024-12-08
✅ Mensalidade 1 atualizada com sucesso para status 'PAGO'
=== FIM PROCESSAMENTO NOTIFICAÇÃO ===
```

## ⚠️ Solução de Problemas

### Webhook não está sendo recebido

1. **Verifique a URL:** A URL deve ser acessível publicamente
2. **Verifique HTTPS:** Webhooks exigem HTTPS válido
3. **Verifique Firewall:** Certifique-se de que não há bloqueio de IPs
4. **Verifique Logs:** Procure por erros no log da aplicação

### Status não está sendo atualizado

1. **Verifique Transaction ID:** Deve corresponder ao `transaction_id` da mensalidade
2. **Verifique Logs:** Procure por mensagens de erro no log
3. **Verifique Status:** Verifique se o status enviado pelo PagHiper está mapeado corretamente

### Como verificar se a mensalidade tem transaction_id

```sql
SELECT id, transaction_id, status, data_pagamento 
FROM mensalidade 
WHERE id = SEU_ID_DA_MENSALIDADE;
```

## 📝 Notas Importantes

1. **Segurança:** O PagHiper pode enviar notificações múltiplas para o mesmo pagamento. O sistema trata isso automaticamente, evitando processamento duplicado.

2. **Timeout:** O PagHiper espera uma resposta HTTP 200 OK dentro de 30 segundos. Certifique-se de que seu endpoint responde rapidamente.

3. **Idempotência:** O sistema é idempotente - se a mesma notificação for recebida múltiplas vezes, ela será processada apenas uma vez.

4. **Rollback:** Se houver erro no processamento, a transação será revertida automaticamente pelo Spring.

## 🔐 Segurança do Webhook

Para aumentar a segurança, você pode:

1. **Validar API Key:** Verificar se a `apiKey` corresponde à configuração
2. **Assinatura:** Validar assinatura HMAC se o PagHiper suportar (verifique a documentação oficial)
3. **Whitelist de IPs:** Configurar firewall para aceitar apenas IPs do PagHiper

## 📚 Documentação Adicional

- [Documentação Oficial do PagHiper](https://www.paghiper.com.br/atendimento/doc-api/)
- [Guia de Webhooks PagHiper](https://www.paghiper.com.br/atendimento/doc-api/#webhooks)
