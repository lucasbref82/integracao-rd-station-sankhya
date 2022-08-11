package br.com.digicade.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.digicade.dto.LeadDTO;
import br.com.digicade.service.LeadService;

@RestController
public class LeadController {
	
	@Autowired
	private LeadService service;
	
	@Value("${token}")
	private String token;
	
	@PostMapping(path = "/v1/criar")
	@ResponseStatus(code = HttpStatus.CREATED)
	public void criar(@RequestBody LeadDTO leads, @RequestParam String token) {
		if(token.equals(this.token)) {
			try {
				service.criar(leads);
			}catch (Exception e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
			}
		}else {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autorizado.");
		}

	}
	
}
