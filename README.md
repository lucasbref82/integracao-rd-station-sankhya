# Integração RD Station → Sankhya

Esta aplicação tem como objetivo **receber leads enviados pelo RD Station** e **registrá-los automaticamente no sistema Sankhya**.

## Funcionalidade

* Recebe um `LeadDTO` via endpoint REST.
* Valida o token de autenticação.
* Envia os dados do lead para o Sankhya através dos serviços internos.

## Endpoint principal

```
POST /v1/criar?token=SEU_TOKEN
```

Body esperado (exemplo simplificado):

```json
{
  "leads": [
    {
      "id": 123,
      "name": "João Silva",
      "email": "joao@email.com"
    }
  ]
}
```

## Objetivo

Automatizar o processo de integração entre o RD Station e o Sankhya, garantindo que todos os leads capturados sejam cadastrados corretamente no ERP.
