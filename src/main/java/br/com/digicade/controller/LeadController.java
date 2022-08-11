package br.com.digicade.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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
	public ResponseEntity<HashMap<String, Object>> criar(@RequestBody LeadDTO leads, @RequestParam String token) {
		HashMap<String, Object> map = new HashMap<>();
		if(token.equals(this.token)) {
			try {
				service.criar(leads);
				return ResponseEntity.status(HttpStatus.CREATED).body(null);
			}catch (Exception e) {
				map.put("Error", true);
				map.put("Message", e.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
			}
		}else {
			map.put("Error", true);
			map.put("Message", "Não Autorizado.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(map);
		}

	}
	
}
