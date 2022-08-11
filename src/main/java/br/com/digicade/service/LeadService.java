package br.com.digicade.service;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.digicade.dto.LeadDTO;
import br.com.digicade.exception.SankhyaException;
import br.com.digicade.model.Lead;

@Service
public class LeadService {

	private SankhyaService serviceInvoker;
	
	@Value("${sankhya-url}")
	private String url;
	
	@Value("${sankhya-user}")
	private String usuario;
	
	@Value("${sankhya-password}")
	private String senha;
	
	private String body = "\"dataSetID\": \"00K\","
			+ "\"entityName\": \"ParceiroProspect\","
			+ "\"standAlone\": false,"
			+ "\"fields\":["
			+ "\"NOMEPAP\","
			+ "\"RAZAOSOCIAL\","
			+ "\"CODVEND\","
			+ "\"TIPPESSOA\","
			+ "\"EMAIL\","
			+ "\"CODTIPPARC\","
			+ "\"AD_TCCNC\","
			+ "\"AD_CADASTRADORDSTATION\","
			+ "\"OBSERVACAO\","
			+ "\"AD_IDRDSTATION\""
			+ "	],"
			+ "\"records\":["
			+ "{"
			+ "\"values\": {"
			+ "\"0\": \"%s\","
			+ "\"1\": \"%s\","
			+ "\"2\": \"0\","
			+ "\"3\": \"J\","
			+ "\"4\": \"%s\","
			+ "\"5\": \"0\","
			+ "\"6\": \"9\","
			+ "\"7\": \"1\","
			+ "\"8\": \"1\","
			+ "\"9\": \"%d\""
			+ "	}"
			+ "	}"
			+ "]";
	
	private String bodyContato = "\"dataSetID\": \"01X\","
			+ "\"entityName\": \"ContatoProspect\","
			+ "\"standAlone\": false,"
			+ "\"fields\": ["
			+ "\"NOMECONTATO\","
			+ "\"EMAIL\","
			+ "\"CELULAR\","
			+ "\"CARGO\""
			+ "],"
			+ "\"records\": ["
			+ "{"
			+ "\"foreignKey\": {"
			+ "\"CODPAP\": \"ULTCODPAP\""
			+ "},"
			+ "\"values\": {"
			+ "\"0\": \"%s\","
			+ "\"1\": \"%s\","
			+ "\"2\": \"%s\","
			+ "\"3\": \"%s\""
			+ "}"
			+ "}"
			+ "],"
			+ "\"ignoreListenerMethods\": \"\"";
	
	private static final String bodyDbe = "\"sql\":\"SELECT MAX(CODPAP) FROM TCSPAP\"";
	
	public void criar (LeadDTO dto) throws JSONException, IOException, SankhyaException {
		for (Lead leads : dto.getLeads()) {
			callCreateProspect(leads);
		}
	}
	
	public void callCreateProspect(Lead lead) throws JSONException, IOException, SankhyaException {
		createProspect(this.url, this.usuario, this.senha, lead);
	}
	
	private void createProspect(String url, String usuario, String senha, Lead lead) throws JSONException, IOException, SankhyaException {
		String newBody =  String.format(body, lead.getName(), lead.getName(), lead.getEmail(),lead.getId());
		serviceInvoker = new SankhyaService(url, usuario, senha);
		serviceInvoker.chamarServico("DatasetSP.save", "mge", newBody);		
		criaContato(retornaIdProspect(), lead);
	}
	
	private Integer retornaIdProspect() throws JSONException, IOException, SankhyaException {
		JSONObject jsonObject = new JSONObject(serviceInvoker.chamarServico("DbExplorerSP.executeQuery", "mge", bodyDbe).toString());		
		JSONObject responseBody  = jsonObject.getJSONObject("responseBody");
		String resposta = responseBody.getJSONArray("rows").toString().replace("[", "").replace("]", "");
		return Integer.parseInt(resposta);
	}
	
	private void criaContato(Integer idProspect, Lead lead) throws JSONException, IOException, SankhyaException {
		String newBodyContato = String.format(bodyContato, lead.getName(), lead.getEmail(), 
				lead.getPersonalPhone() != null ? lead.getPersonalPhone() : "", 
				lead.getJobTitle() != null ? lead.getJobTitle() : "").replace("ULTCODPAP", String.valueOf(idProspect));
		serviceInvoker.chamarServico("DatasetSP.save", "mge", newBodyContato);		
	}
}
