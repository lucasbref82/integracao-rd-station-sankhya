package br.com.digicade.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Lead {
	
	private Long id;
	private String email;
	private String name;
	@JsonProperty("personal_phone")
	private String personalPhone;
	@JsonProperty("job_title")
	private String jobTitle;
}
