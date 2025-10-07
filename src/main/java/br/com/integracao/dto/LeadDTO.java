package br.com.digicade.dto;

import java.util.List;

import br.com.digicade.model.Lead;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeadDTO {
	private List<Lead> leads;
}
