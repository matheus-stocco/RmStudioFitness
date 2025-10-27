# 🔑 Token Automático da PagHiper

## 📋 Como Funciona

O sistema agora captura automaticamente o token da resposta da PagHiper quando um PIX é criado e usa esse token para cancelamentos futuros.

### 🔄 Fluxo Automático:

1. **Criação do PIX**: Sistema envia requisição para PagHiper
2. **Captura da Resposta**: Armazena a resposta completa da PagHiper
3. **Extração do Token**: Extrai automaticamente o token da resposta
4. **Cancelamento**: Usa o token capturado para cancelar transações

## 🧪 Como Testar

### 1. **Teste de Configuração**
```
http://localhost:8080/api/pagamentos/testar-paghiper
```

### 2. **Teste de Token da Resposta**
```
http://localhost:8080/api/pagamentos/testar-token-resposta
```

### 3. **Teste Completo**

#### Passo 1: Gere um PIX
1. Acesse "Minhas Mensalidades"
2. Clique "Gerar PIX" em uma mensalidade pendente
3. Verifique os logs para ver a resposta da PagHiper

#### Passo 2: Teste o Token
1. Acesse `/api/pagamentos/testar-token-resposta`
2. Verifique se o token foi extraído da resposta

#### Passo 3: Teste o Cancelamento
1. Clique "Cancelar Plano"
2. Verifique os logs para ver se o token foi usado

## 📊 Logs Esperados

### ✅ Com Token Capturado:
```
PIX criado com sucesso - TransactionId: 10GRZNTFZBPANA25, Resposta completa: {"pix_create_request":{"result":"success","transaction_id":"10GRZNTFZBPANA25","token":"abc123..."}}
🔑 Token extraído da resposta PagHiper para cancelamento
🔄 Enviando requisição de cancelamento para PagHiper - TransactionId: 10GRZNTFZBPANA25
✅ Transação 10GRZNTFZBPANA25 cancelada na PagHiper com sucesso
```

### ⚠️ Sem Token na Resposta:
```
PIX criado com sucesso - TransactionId: 10GRZNTFZBPANA25, Resposta completa: {"pix_create_request":{"result":"success","transaction_id":"10GRZNTFZBPANA25"}}
ℹ️ Resposta PagHiper encontrada, mas sem token
🔄 Enviando requisição de cancelamento para PagHiper - TransactionId: 10GRZNTFZBPANA25
```

## 🔍 Verificação na PagHiper

Após o cancelamento, verifique no painel:
1. Acesse: https://painel.paghiper.com
2. Vá em "Busca por Transação"
3. Procure pela transação
4. O status deve mudar de "Aguardando" para "Cancelado"

## 📁 Campos Adicionados

### Entidade Mensalidade:
- `paghiperResponse`: Armazena a resposta completa da PagHiper (JSON)
- `extrairTokenDaResposta()`: Método para extrair token da resposta

### Endpoints de Teste:
- `/api/pagamentos/testar-paghiper`: Testa configuração
- `/api/pagamentos/testar-token-resposta`: Testa extração de token

## 🎯 Vantagens

- ✅ **Automático**: Não precisa configurar token manualmente
- ✅ **Dinâmico**: Usa o token específico de cada transação
- ✅ **Robusto**: Fallback para configuração manual se necessário
- ✅ **Transparente**: Logs detalhados de todo o processo

## 🚀 Próximos Passos

1. **Gere um PIX** para capturar a resposta
2. **Teste a extração** do token
3. **Execute um cancelamento** real
4. **Verifique na PagHiper** se foi cancelado
5. **Monitore os logs** para confirmar funcionamento

## 🔧 Debug

Para debug detalhado, adicione no `application.properties`:
```properties
logging.level.com.rmstudio.rmstudiofitness.servicos=DEBUG
logging.level.org.springframework.web.client=DEBUG
```

## 📞 Suporte

Se o token não for extraído automaticamente:
1. Verifique se a resposta da PagHiper contém um campo "token"
2. Entre em contato com suporte PagHiper para confirmar formato da resposta
3. Use a configuração manual como fallback









