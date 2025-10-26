# 🔑 Como Configurar o Token da PagHiper

## 📋 Passo a Passo

### 1. Acesse sua conta PagHiper
- Vá para: https://painel.paghiper.com
- Faça login com suas credenciais

### 2. Obtenha suas credenciais
- No painel, vá em **"Configurações"** ou **"API"**
- Copie seu **Token** e **API Key**

### 3. Configure no sistema

#### Opção A: Variável de Ambiente (Recomendado)
```bash
# Windows
set PAGHIPER_TOKEN=seu_token_real_aqui

# Linux/Mac
export PAGHIPER_TOKEN=seu_token_real_aqui
```

#### Opção B: Arquivo application.properties
Edite o arquivo `src/main/resources/application.properties`:
```properties
paghiper.api.token=seu_token_real_aqui
```

### 4. Teste o cancelamento

1. **Inicie a aplicação**
2. **Acesse "Minhas Mensalidades"**
3. **Clique em "Cancelar Plano"**
4. **Verifique os logs** no console

## 🔍 Logs Esperados

### ✅ Com Token Configurado:
```
🔄 Enviando requisição de cancelamento para PagHiper - TransactionId: abc123
✅ Transação abc123 cancelada na PagHiper com sucesso
🎯 === CANCELAMENTO DE PLANO CONCLUÍDO ===
👤 Pessoa: Nome do Usuário
📊 Total de mensalidades processadas: 1
🔄 Cancelamentos na PagHiper - Sucesso: 1, Falha: 0
✅ Todas as mensalidades foram canceladas localmente.
```

### ⚠️ Sem Token:
```
⚠️ CONFIGURAÇÃO PAGHIPER INVÁLIDA: Token não configurado
⚠️ PagHiper não configurado - cancelamentos feitos apenas localmente
💡 Para ativar cancelamento na PagHiper, configure: PAGHIPER_TOKEN=seu_token_real
```

## 🎯 Status Atual

- ✅ **Cancelamento local**: Sempre funciona
- ✅ **Logs detalhados**: Implementados
- ✅ **Tratamento de erros**: Robusto
- 🔄 **Cancelamento PagHiper**: Aguardando configuração do token

## 📞 Suporte

Se precisar de ajuda:
- **PagHiper**: atendimento@paghiper.com
- **Telefone**: (11) 3003-0462

## 🚀 Próximo Passo

**Configure o token real da PagHiper para ativar o cancelamento automático!**







