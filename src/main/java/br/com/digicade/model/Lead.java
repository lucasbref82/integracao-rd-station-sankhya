package br.com.digicade.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Lead {
	
	private Integer id;
	private String email;
	private String name;
	private String personal_phone;
	private String job_title;
}
