package br.com.integracao.dto;

import java.util.List;

import br.com.integracao.model.Lead;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeadDTO {
	private List<Lead> leads;
}
