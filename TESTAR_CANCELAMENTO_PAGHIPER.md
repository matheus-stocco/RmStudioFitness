# 🧪 Teste de Cancelamento PagHiper

## 🔍 Como Testar se o Cancelamento está Funcionando

### 1. **Teste da Configuração**
Acesse no navegador:
```
http://localhost:8080/api/pagamentos/testar-paghiper
```

**Resposta esperada:**
- ✅ `Configuração da PagHiper está válida!` - Token configurado
- ❌ `Configuração da PagHiper inválida` - Token não configurado

### 2. **Teste de Cancelamento Real**

#### Passo 1: Configure o Token
```bash
# Windows
set PAGHIPER_TOKEN=seu_token_real_da_paghiper

# Linux/Mac
export PAGHIPER_TOKEN=seu_token_real_da_paghiper
```

#### Passo 2: Inicie a Aplicação
```bash
mvn spring-boot:run
```

#### Passo 3: Teste o Cancelamento
1. Acesse: `http://localhost:8080/minhas-mensalidades`
2. Clique em **"Cancelar Plano"**
3. Verifique os logs no console

### 3. **Logs Esperados**

#### ✅ Com Token Configurado:
```
=== DEBUG PAGHIPER CANCEL ===
API Token: Configurado (abc1234567...)
API Key: Configurado (apk_492182...)
Cancel URL: https://pix.paghiper.com/invoice/cancel/
Transaction ID: 10GRZNTFZBPANA25
Request JSON: {"token":"seu_token","apiKey":"sua_key","status":"canceled","transaction_id":"10GRZNTFZBPANA25"}
Response Status: 200 OK
Response Body: {"cancellation_request":{"result":"success","response_message":"O Pix foi cancelado com Sucesso"}}
✅ Cancelamento confirmado na PagHiper!
```

#### ❌ Sem Token:
```
⚠️ CONFIGURAÇÃO PAGHIPER INVÁLIDA: Token não configurado
⚠️ PagHiper não configurado - cancelamentos feitos apenas localmente
💡 Para ativar cancelamento na PagHiper, configure: PAGHIPER_TOKEN=seu_token_real
```

### 4. **Verificação na PagHiper**

Após o cancelamento, verifique no painel da PagHiper:
1. Acesse: https://painel.paghiper.com
2. Vá em "Busca por Transação"
3. Procure pela transação `10GRZNTFZBPANA25`
4. O status deve mudar de "Aguardando" para "Cancelado"

### 5. **Problemas Comuns**

#### **"Token não configurado"**
- Verifique se a variável `PAGHIPER_TOKEN` está definida
- Reinicie a aplicação após configurar

#### **"Erro HTTP 401/403"**
- Token ou API Key incorretos
- Verifique as credenciais no painel PagHiper

#### **"Erro HTTP 404"**
- URL de cancelamento incorreta
- Verifique se a URL está correta

#### **"Transação não encontrada"**
- Transaction ID incorreto
- Transação já cancelada

### 6. **Debug Avançado**

Para debug detalhado, adicione no `application.properties`:
```properties
logging.level.com.rmstudio.rmstudiofitness.servicos=DEBUG
logging.level.org.springframework.web.client=DEBUG
```

### 7. **Status da Transação**

- **Aguardando**: Transação ativa, pode ser cancelada
- **Pago**: Transação paga, não pode ser cancelada
- **Cancelado**: Transação cancelada com sucesso
- **Expirado**: Transação expirada

### 8. **Próximos Passos**

1. **Configure o token real** da PagHiper
2. **Teste o endpoint** `/api/pagamentos/testar-paghiper`
3. **Execute um cancelamento** real
4. **Verifique na PagHiper** se a transação foi cancelada
5. **Monitore os logs** para identificar problemas

## 🎯 Resultado Esperado

Após configurar corretamente, ao cancelar um plano:
- ✅ Transação cancelada na PagHiper
- ✅ Status muda para "Cancelado" no painel
- ✅ Mensalidade cancelada localmente
- ✅ Plano removido do usuário






