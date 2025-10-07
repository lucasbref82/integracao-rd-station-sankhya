package br.com.integracao.controller;

import java.io.IOException;


import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.integracao.dto.LeadDTO;
import br.com.integracao.exception.SankhyaException;
import br.com.integracao.service.LeadService;

@RestController
@RequestMapping("/v1")
@Validated
public class LeadController {

    private static final Logger log = LoggerFactory.getLogger(LeadController.class);

    private final LeadService service;
    private final String expectedToken;

    public LeadController(LeadService service, @Value("${token}") String expectedToken) {
        this.service = service;
        this.expectedToken = expectedToken;
    }

    /**
     * Cria um lead.
     * @param lead payload do lead (validação com javax.validation)
     * @param apiToken token enviado no header X-Api-Token
     */
    @PostMapping(path = "/criar")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void criar(@RequestBody LeadDTO lead,
                      @RequestHeader(name = "X-Api-Token", required = true) String apiToken) {

        if (!StringUtils.hasText(apiToken) || !apiToken.equals(this.expectedToken)) {
            log.warn("Tentativa de acesso não autorizado ao endpoint /v1/criar");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autorizado.");
        }

        try {
            service.criar(lead);
            log.info("Lead criado com sucesso: {}", lead);
        } catch (SankhyaException e) {
            log.error("Erro interno no Sankhya ao criar lead: {}", lead, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro interno no Sankhya: " + e.getMessage(), e);
        } catch (JSONException | IOException e) {
            log.error("Erro ao processar requisição ao criar lead: {}", lead, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao processar requisição: " + e.getMessage(), e);
        }
    }
}
