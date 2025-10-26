# 🔧 Configuração do Token PagHiper

## ⚠️ IMPORTANTE: Configure o Token Real

Para que o cancelamento funcione na PagHiper, você precisa configurar o token real da sua conta.

### 📋 Como Obter o Token da PagHiper

1. **Acesse sua conta PagHiper**: https://painel.paghiper.com
2. **Vá em Configurações > API**
3. **Copie o Token de Autenticação**
4. **Configure no sistema** (veja opções abaixo)

### 🔧 Opções de Configuração

#### **Opção 1: Variável de Ambiente (Recomendado)**
```bash
# Windows (PowerShell)
$env:PAGHIPER_TOKEN="seu_token_real_aqui"

# Windows (CMD)
set PAGHIPER_TOKEN=seu_token_real_aqui

# Linux/Mac
export PAGHIPER_TOKEN="seu_token_real_aqui"
```

#### **Opção 2: Arquivo application.properties**
Edite o arquivo `src/main/resources/application.properties`:
```properties
paghiper.api.token=seu_token_real_aqui
```

### 🧪 Como Testar

1. **Configure o token** usando uma das opções acima
2. **Inicie a aplicação**
3. **Acesse "Minhas Mensalidades"**
4. **Clique em "Cancelar Plano"**
5. **Verifique os logs** no console

### 📊 Logs Esperados

**Com Token Configurado:**
```
=== DEBUG PAGHIPER CANCEL ===
API Token: Configurado
API Key: Configurado
Cancel URL: https://api.paghiper.com/transaction/cancel/
Transaction ID: abc123
Request JSON: {"token":"seu_token","apiKey":"sua_key","status":"canceled","transaction_id":"abc123"}
Response Status: 200 OK
Response Body: {"cancellation_request":{"result":"success","response_message":"Transação cancelada"}}
```

**Sem Token (Fallback):**
```
Token da PagHiper não configurado. Configure a variável PAGHIPER_TOKEN ou a propriedade paghiper.api.token
Mensalidade 123 cancelada localmente.
```

### ✅ Verificação de Funcionamento

Após configurar o token, ao cancelar um plano você deve ver:

1. **Logs de debug** mostrando a requisição para PagHiper
2. **Resposta da PagHiper** confirmando o cancelamento
3. **Mensalidades canceladas** tanto na PagHiper quanto localmente
4. **Plano removido** do usuário

### 🚨 Problemas Comuns

#### **"Token não configurado"**
- Verifique se a variável de ambiente está definida
- Reinicie a aplicação após configurar

#### **"Erro na comunicação com PagHiper"**
- Verifique se o token está correto
- Confirme se a URL de cancelamento está correta
- Entre em contato com suporte PagHiper

#### **"Transação não encontrada"**
- A transação pode já ter sido cancelada
- Verifique se o transactionId está correto

### 📞 Suporte PagHiper

Se precisar de ajuda com a API:
- **Email**: atendimento@paghiper.com
- **Site**: https://atendimento.paghiper.com
- **Telefone**: (11) 3003-0462

### 🎯 Status Atual

- ✅ **Cancelamento local**: Funcionando
- ✅ **Logs detalhados**: Implementados
- ✅ **Tratamento de erros**: Robusto
- 🔄 **Cancelamento PagHiper**: Aguardando configuração do token

**Próximo passo**: Configure o token real da PagHiper para ativar o cancelamento automático!







