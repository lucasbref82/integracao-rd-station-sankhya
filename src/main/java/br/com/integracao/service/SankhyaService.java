package br.com.integracao.service;

import br.com.integracao.exception.SankhyaException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class SankhyaService {

    private static final Logger log = LoggerFactory.getLogger(SankhyaService.class);

    @Getter
    private final String url;
    private final String usuario;
    private final String senha;
    @Setter
    private boolean senhaCriptografada;

    private static final String RESPONSE_BODY = "responseBody";
    private static final String JSESSION_ID = "jsessionid";

    /**
     * Chama um serviço no Sankhya (login, chamada e logout automáticos).
     */
    public JsonObject chamarServico(String nomeServico, String modulo, String corpo)
            throws IOException, SankhyaException {
        String jsessionid = efetuarLogin();
        HttpURLConnection conn = abrirConexao(nomeServico, modulo, jsessionid);
        JsonObject resposta = executarRequisicao(conn, corpo, nomeServico);
        try {
            efetuarLogout(jsessionid);
        } catch (Exception e) {
            log.warn("Falha no logout do Sankhya (jsessionid={}): {}", jsessionid, e.getMessage());
        }
        return resposta;
    }

    /**
     * Realiza login no Sankhya e retorna o jsessionid.
     */
    public String efetuarLogin() throws IOException, SankhyaException {
        HttpURLConnection conn = abrirConexao("MobileLoginSP.login", "mge", null);
        StringBuilder bodyBuf = new StringBuilder();

        String usuarioVal = retornarVazioOuNulo(this.usuario);
        if (usuarioVal != null) {
            bodyBuf.append(" \"NOMUSU\": { \"$\": \"").append(usuarioVal).append("\" }, ");
        } else {
            bodyBuf.append(" \"NOMUSU\": {}, ");
        }

        String interno = this.senhaCriptografada ? "\"INTERNO2\"" : "\"INTERNO\"";
        String senhaVal = retornarVazioOuNulo(this.senha);
        if (senhaVal != null) {
            bodyBuf.append(interno).append(": { \"$\": \"").append(senhaVal).append("\" }");
        } else {
            bodyBuf.append(interno).append(": {}");
        }

        JsonObject docResp = executarRequisicao(conn, bodyBuf.toString(), "MobileLoginSP.login");
        if (docResp.has(RESPONSE_BODY) && docResp.getAsJsonObject(RESPONSE_BODY).has(JSESSION_ID)) {
            try {
                return docResp.getAsJsonObject(RESPONSE_BODY).getAsJsonObject(JSESSION_ID).get("$")
                        .getAsString().trim();
            } catch (Exception e) {
                throw new SankhyaException("Resposta de login inválida: " + e.getMessage());
            }
        }
        throw new SankhyaException("Não foi possível obter jsessionid no login do Sankhya.");
    }

    /**
     * Realiza logout no Sankhya.
     */
    public void efetuarLogout(String jsessionid) throws IOException, SankhyaException {
        HttpURLConnection conn = abrirConexao("MobileLoginSP.logout", "mge", jsessionid);
        executarRequisicao(conn, null, "MobileLoginSP.logout");
    }

    /**
     * Executa a requisição HTTP (escreve body quando necessário e lê resposta JSON).
     */
    private JsonObject executarRequisicao(HttpURLConnection conn, String body, String nomeServico)
            throws IOException, SankhyaException {
        int responseCode = -1;
        try {
            conn.setDoOutput(true);
            conn.setDoInput(true);

            String requestBody = construirCorpoRequisicao(body, nomeServico);
            try (OutputStream out = conn.getOutputStream();
                 OutputStreamWriter wout = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                wout.write(requestBody);
                wout.flush();
            }

            responseCode = conn.getResponseCode();

            try (InputStream is = responseCode >= 200 && responseCode < 400 ? conn.getInputStream()
                    : conn.getErrorStream()) {
                JsonObject jsonObject = converterInputStreamParaJson(is);
                if (!jsonObject.has(RESPONSE_BODY)) {
                    String statusMessage = jsonObject.has("statusMessage") ?
                            jsonObject.get("statusMessage").getAsString() : "Resposta sem responseBody";
                    throw new SankhyaException(statusMessage);
                }
                return jsonObject;
            }
        } finally {
            // desconecta a conexão se possível
            try {
                conn.disconnect();
            } catch (Exception e) {
                log.debug("Erro ao desconectar: {}", e.getMessage());
            }
        }
    }

    /**
     * Monta o JSON do corpo da requisição esperado pelo Sankhya.
     */
    private String construirCorpoRequisicao(String body, String nomeServico) {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append("\"serviceName\": ").append('"').append(nomeServico).append('"').append(',');
        buf.append("\"requestBody\": {");
        if (body != null) {
            buf.append(body);
        }
        buf.append("}");
        buf.append("}");
        return buf.toString();
    }

    /**
     * Abre e configura a conexão HTTP para o serviço Sankhya.
     */
    private HttpURLConnection abrirConexao(String nomeServico, String modulo, String idSessao) throws IOException {
        StringBuilder buf = new StringBuilder();
        buf.append(this.url).append(this.url.endsWith("/") ? "" : "/")
                .append((modulo == null) ? "mge" : modulo).append("/service.sbr");
        buf.append("?serviceName=").append(nomeServico);
        if (idSessao != null) {
            buf.append("&mgeSession=").append(idSessao);
        }
        buf.append("&outputType=json");
        URL u = new URL(buf.toString());
        HttpURLConnection conexao = (HttpURLConnection) u.openConnection();
        conexao.setRequestMethod("POST");
        conexao.setRequestProperty("content-type", "application/json");
        if (idSessao != null) {
            conexao.setRequestProperty("Cookie", "JSESSIONID=" + idSessao);
        }
        // timeouts razoáveis (podem ser parametrizados)
        conexao.setConnectTimeout(10_000);
        conexao.setReadTimeout(60_000);
        return conexao;
    }

    private JsonObject converterInputStreamParaJson(InputStream is) throws IOException {
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    public static String retornarVazioOuNulo(String s) {
        if (s == null || s.isEmpty())
            return null;
        String trimed = s.trim();
        return (trimed.isEmpty()) ? null : trimed;
    }

}
