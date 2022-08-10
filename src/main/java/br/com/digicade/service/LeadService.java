package br.com.digicade.service;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Response;

import br.com.digicade.dto.LeadDTO;
import br.com.digicade.model.Lead;
import br.com.sankhya.service.*;

@Service
public class LeadService {

	private SWServiceInvoker serviceInvoker;
	
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
			+ "	\"NOMEPAP\","
			+ "	\"RAZAOSOCIAL\","
			+ "	\"CODVEND\","
			+ "	\"TIPPESSOA\","
			+ "	\"EMAIL\","
			+ "	\"CODTIPPARC\","
			+ "	\"AD_TCCNC\","
			+ "	\"TELEFONE\","
			+ "	\"AD_CADASTRADORDSTATION\","
			+ "	\"AD_IDRDSTATION\""
			+ "		],"
			+ "\"records\":["
			+ "	{"
			+ "		\"values\": {"
			+ "			\"0\": \"%s\","
			+ "			\"1\": \"%s\","
			+ "			\"2\": \"0\","
			+ "			\"3\": \"J\","
			+ "			\"4\": \"%s\","
			+ "			\"5\": \"0\","
			+ "			\"6\": \"9\","
			+ "			\"7\": \"%s\","
			+ "			\"8\": \"1\","
			+ "			\"9\": \"%d\""
			+ "				}"
			+ "	}"
			+ "]";
	
	private String bodyContato = "\"dataSetID\": \"01X\","
			+ "\"entityName\": \"ContatoProspect\","
			+ "\"standAlone\": false,"
			+ "\"fields\": ["
			+ "	\"NOMECONTATO\","
			+ "	\"EMAIL\","
			+ "	\"CELULAR\","
			+ "	\"CARGO\""
			+ "	],"
			+ "	\"records\": ["
			+ "		{"
			+ "			\"foreignKey\": {"
			+ "			\"CODPAP\": \"ULTCODPAP\""
			+ "			 },"
			+ "			 \"values\": {"
			+ "				\"0\": \"%s\","
			+ "				\"1\": \"%s\","
			+ "				\"2\": \"%s\","
			+ "			 	\"3\": \"%s\""
			+ "			 }"
			+ "		}"
			+ "	],"
			+ "\"ignoreListenerMethods\": \"\"";
	
	private final String bodyDbe = "\"sql\":\"SELECT MAX(CODPAP) FROM TCSPAP\"";
	
	public void criar (LeadDTO dto) throws Exception {
		for (Lead lead : dto.getLeads()) {
			callCreateProspect(lead);
		}
	}
	
	public void callCreateProspect(Lead lead) throws Exception{
		createProspect(this.url, this.usuario, this.senha, lead);
	}
	
	private void createProspect(String url, String usuario, String senha, Lead lead) throws Exception {
		String newBody =  String.format(body, lead.getName(), lead.getName(), lead.getEmail(), lead.getPersonal_phone(), lead.getId());
		System.out.println(newBody);
		serviceInvoker = new SWServiceInvoker(url, usuario, senha);
		JSONObject jsonObject = new JSONObject(serviceInvoker.callAsJson("DatasetSP.save", "mge", newBody).toString());		
		if(jsonObject.getInt("status") == 0) {
			  throw new IllegalStateException(jsonObject.getString("statusMessage"));
		}
		criaContato(retornaIdProspect(), lead);
	}
	
	private Integer retornaIdProspect() throws JSONException, Exception {
		JSONObject jsonObject = new JSONObject(serviceInvoker.callAsJson("DbExplorerSP.executeQuery", "mge", bodyDbe).toString());		
		if(jsonObject.getInt("status") == 0) {
			  throw new IllegalStateException(jsonObject.getString("statusMessage"));
		}
		JSONObject responseBody  = jsonObject.getJSONObject("responseBody");
		Integer idProspect = Integer.parseInt(new String(responseBody.getJSONArray("rows")
				.toString()
				.replace("[", "")
				.replace("]", "")));
		return idProspect;
	}
	
	private void criaContato(Integer idProspect, Lead lead) throws JSONException, Exception {
		String newBodyContato = String.format(bodyContato, lead.getName(), lead.getEmail(), lead.getPersonal_phone(), lead.getJob_title()).replace("ULTCODPAP", String.valueOf(idProspect));
		JSONObject jsonObject = new JSONObject(serviceInvoker.callAsJson("DatasetSP.save", "mge", newBodyContato).toString());		
		if(jsonObject.getInt("status") == 0) {
			  throw new IllegalStateException(jsonObject.getString("statusMessage"));
		}
	}
}
