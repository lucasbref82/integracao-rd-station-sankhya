package br.com.integracao.service;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.integracao.dto.LeadDTO;
import br.com.integracao.exception.SankhyaException;
import br.com.integracao.model.Lead;

@Service
public class LeadService {

    private static final Logger log = LoggerFactory.getLogger(LeadService.class);

    private final SankhyaService sankhyaService;

    public LeadService(@Value("${sankhya-url}") String url,
                       @Value("${sankhya-user}") String usuario,
                       @Value("${sankhya-password}") String senha) {
        this.sankhyaService = new SankhyaService(url, usuario, senha);
    }

    public void criar(LeadDTO dto) throws JSONException, IOException, SankhyaException {
        if (dto == null || dto.getLeads() == null || dto.getLeads().isEmpty()) {
            log.info("Nenhum lead recebido para criação.");
            return;
        }

        for (Lead lead : dto.getLeads()) {
            try {
                callCreateProspect(sankhyaService, lead);
            } catch (JSONException | IOException | SankhyaException e) {
                log.error("Falha ao processar lead id={} nome={}: {}", lead.getId(), lead.getName(), e.getMessage(), e);
                // relança para ser tratado em camadas superiores (Controller/Advice)
                throw e;
            }
        }
    }

    private void callCreateProspect(SankhyaService sankhyaService, Lead lead) throws JSONException, IOException, SankhyaException {
        if (lead == null) {
            return;
        }
        if (leadExists(sankhyaService, lead.getId())) {
            log.info("Lead já existe no Sankhya (id={}), pulando.", lead.getId());
            return;
        }
        
        createProspect(sankhyaService, lead);
    }

    /**
     * Verifica se o lead já existe no Sankhya.
     * Retorna true se EXISTE, false caso contrário.
     */
    private boolean leadExists(SankhyaService sankhyaService, Long idLead) throws JSONException, IOException, SankhyaException {
        if (idLead == null) {
            return false;
        }

        String sql = String.format("SELECT AD_IDRDSTATION FROM TCSPAP WHERE AD_IDRDSTATION = %d", idLead);
        JSONObject jsonObject = new JSONObject(sankhyaService.chamarServico("DbExplorerSP.executeQuery", "mge", buildSqlPayload(sql)).toString());
        JSONObject responseBody = jsonObject.optJSONObject("responseBody");
        if (responseBody == null) {
            return false;
        }

        JSONArray rows = responseBody.optJSONArray("rows");
        return rows != null && !rows.isEmpty();
    }

    private void createProspect(SankhyaService sankhyaService, Lead lead) throws JSONException, IOException, SankhyaException {
        String payload = buildProspectPayload(lead).toString();
        sankhyaService.chamarServico("DatasetSP.save", "mge", payload);

        Integer idProspect = retornaIdProspect(sankhyaService);
        if (idProspect != null) {
            criaContato(sankhyaService, idProspect, lead);
        } else {
            throw new SankhyaException("Não foi possível obter ID do prospect após criação.");
        }
    }

    private Integer retornaIdProspect(SankhyaService sankhyaService) throws JSONException, IOException, SankhyaException {
        JSONObject jsonObject = new JSONObject(sankhyaService.chamarServico("DbExplorerSP.executeQuery", "mge", buildSqlPayload("SELECT MAX(CODPAP) FROM TCSPAP")).toString());
        JSONObject responseBody = jsonObject.optJSONObject("responseBody");
        if (responseBody == null) {
            throw new SankhyaException("Resposta inválida do Sankhya ao recuperar ID do prospect.");
        }

        JSONArray rows = responseBody.optJSONArray("rows");
        if (rows == null || rows.isEmpty()) {
            throw new SankhyaException("Nenhuma linha retornada ao recuperar ID do prospect.");
        }

        Object first = rows.get(0);
        try {
            if (first instanceof JSONArray) {
                return ((JSONArray) first).getInt(0);
            } else {
                String s = first.toString().replaceAll("[^0-9-]", "");
                return Integer.parseInt(s);
            }
        } catch (Exception e) {
            throw new SankhyaException("Erro ao parsear ID do prospect: " + e.getMessage());
        }
    }

    private void criaContato(SankhyaService sankhyaService, Integer idProspect, Lead lead) throws JSONException, IOException, SankhyaException {
        JSONObject payload = buildContatoPayload(idProspect, lead);
        sankhyaService.chamarServico("DatasetSP.save", "mge", payload.toString());
    }

    private JSONObject buildProspectPayload(Lead lead) {
        JSONObject root = new JSONObject();
        root.put("dataSetID", "00K");
        root.put("entityName", "ParceiroProspect");
        root.put("standAlone", false);

        JSONArray fields = new JSONArray();
        fields.put("NOMEPAP");
        fields.put("RAZAOSOCIAL");
        fields.put("CODVEND");
        fields.put("TIPPESSOA");
        fields.put("EMAIL");
        fields.put("CODTIPPARC");
        fields.put("AD_TCCNC");
        fields.put("AD_CADASTRADORDSTATION");
        fields.put("OBSERVACAO");
        fields.put("AD_IDRDSTATION");
        root.put("fields", fields);

        JSONArray records = buildJson(lead);

        root.put("records", records);

        return root;
    }

    private JSONArray buildJson(Lead lead) {
        JSONObject valores = new JSONObject();
        valores.put("0", nonNullString(lead.getName()));
        valores.put("1", nonNullString(lead.getName()));
        valores.put("2", "0");
        valores.put("3", "J");
        valores.put("4", nonNullString(lead.getEmail()));
        valores.put("5", "0");
        valores.put("6", "9");
        valores.put("7", "1");
        valores.put("8", "1");
        valores.put("9", lead.getId() != null ? lead.getId().intValue() : 0);

        JSONObject registo = new JSONObject();
        registo.put("values", valores);

        JSONArray records = new JSONArray();
        records.put(registo);
        return records;
    }

    private JSONObject buildContatoPayload(Integer idProspect, Lead lead) {
        JSONObject root = new JSONObject();
        root.put("dataSetID", "01X");
        root.put("entityName", "ContatoProspect");
        root.put("standAlone", false);

        JSONArray fields = new JSONArray();
        fields.put("NOMECONTATO");
        fields.put("EMAIL");
        fields.put("CELULAR");
        fields.put("CARGO");
        root.put("fields", fields);

        JSONObject foreignKey = new JSONObject();
        foreignKey.put("CODPAP", "ULTCODPAP");

        JSONObject values = new JSONObject();
        values.put("0", nonNullString(lead.getName()));
        values.put("1", nonNullString(lead.getEmail()));
        values.put("2", nonNullString(lead.getPersonalPhone() != null ? trataCampoTelefone(lead.getPersonalPhone()) : ""));
        values.put("3", nonNullString(lead.getJobTitle() != null ? lead.getJobTitle() : ""));

        JSONObject registro = new JSONObject();
        registro.put("foreignKey", foreignKey);
        registro.put("values", values);

        JSONArray registros = new JSONArray();
        registros.put(registro);

        root.put("records", registros);
        root.put("ignoreListenerMethods", "");
        
        String asString = root.toString().replace("ULTCODPAP", String.valueOf(idProspect));
        return new JSONObject(asString);
    }

    private String buildSqlPayload(String sql) {
        JSONObject obj = new JSONObject();
        obj.put("sql", sql);
        return obj.toString();
    }

    private String nonNullString(String s) {
        return s == null ? "" : s;
    }

    public String trataCampoTelefone(String telefone) {
        if (telefone == null) {
            return "";
        }
        return telefone.replaceAll("[^0-9]+", "");
    }

}

