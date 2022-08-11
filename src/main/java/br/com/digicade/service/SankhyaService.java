package br.com.digicade.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.digicade.exception.SankhyaException;

public class SankhyaService {
	private String url;

	private String usuario;

	private String senha;

	private boolean senhaCriptografada;

	private static final String RESPONSE_BODY = "responseBody";

	private static final String JSESSION_ID = "jsessionid";

	public SankhyaService(String url, String usuario, String senha) {
		this.url = url;
		this.usuario = usuario;
		this.senha = senha;
	}
	
	public JsonObject chamarServico(String serviceName, String module, String body) throws IOException, SankhyaException {
		String jsessionid = fazerLogin();
		URLConnection conn = abrirConexao(serviceName, module, jsessionid);
		JsonObject docResp = (JsonObject) chamarServico(conn, body, serviceName);
		fazerLogout(jsessionid);
		return docResp;
	}

	public String fazerLogin() throws IOException, SankhyaException {
		URLConnection conn = abrirConexao("MobileLoginSP.login", "mge", null);
		String session = null;
		StringBuffer bodyBuf = new StringBuffer();
		if (retornarVazioNulo(this.usuario) != null) {
			bodyBuf.append(" \"NOMUSU\": { \"$\": ").append("\"" + this.usuario + "\"").append(" }, ");
		} else {
			bodyBuf.append(" \"NOMUSU\": {}, ");
		}
		String interno = this.senhaCriptografada ? "\"INTERNO2\"" : "\"INTERNO\"";
		if (retornarVazioNulo(this.senha) != null) {
			bodyBuf.append(interno).append(": { \"$\": ").append("\"" + this.senha + "\"").append(" }");
		} else {
			bodyBuf.append(interno).append(": {}");
		}
		JsonObject docResp = (JsonObject) chamarServico(conn, bodyBuf.toString(), "MobileLoginSP.login");
		if (docResp.has(RESPONSE_BODY) && docResp.getAsJsonObject(RESPONSE_BODY).has(JSESSION_ID))
			session = docResp.getAsJsonObject(RESPONSE_BODY).getAsJsonObject(JSESSION_ID).get("$").getAsString().trim();
		return session;
	}

	public void fazerLogout(String jsessionid) throws IOException, SankhyaException {
		URLConnection conn = abrirConexao("MobileLoginSP.logout", "mge", jsessionid);
		chamarServico(conn, null, "MobileLoginSP.logout");
	}

	private Object chamarServico(URLConnection conn, String body, String serviceName) throws IOException, SankhyaException  {
		OutputStream out = null;
		InputStream inp = null;
		out = conn.getOutputStream();
		OutputStreamWriter wout = new OutputStreamWriter(out, "ISO-8859-1");
		String requestBody = null;
		requestBody = construirCorpoRequisicao(body, serviceName);
		wout.write(requestBody);
		wout.flush();
		inp = conn.getInputStream();
		JsonObject jsonObject = converterInputStreamEmJsonObject(inp);
		if (!jsonObject.has("responseBody"))
			throw new SankhyaException("Json de resposta npossui dados de resposta.");
		return jsonObject;
	}

	private String construirCorpoRequisicao(String body, String serviceName) {
		StringBuffer buf = new StringBuffer();
		buf.append(" {");
		buf.append("    \"serviceName\": ").append("\"" + serviceName + "\"").append(", ");
		buf.append("    \"requestBody\": {");
		buf.append(body);
		buf.append("    }");
		buf.append(" }");
		return buf.toString();
	}

	private URLConnection abrirConexao(String nomeServico, String modulo, String idSessao) throws IOException {
		StringBuffer buf = new StringBuffer();
		buf.append(this.url).append(this.url.endsWith("/") ? "" : "/").append((modulo == null) ? "mge" : modulo)
				.append("/service.sbr");
		buf.append("?serviceName=").append(nomeServico);
		if (idSessao != null) {
			buf.append("&mgeSession=").append(idSessao);
		}
		buf.append("&outputType=json");
		URL u = new URL(buf.toString());
		URLConnection uc = u.openConnection();
		HttpURLConnection conexao = (HttpURLConnection) uc;
		conexao.setDoOutput(true);
		conexao.setDoInput(true);
		conexao.setRequestMethod("POST");
		conexao.setRequestProperty("content-type", "application/json");
		if (idSessao != null) {
			conexao.setRequestProperty("Cookie", "JSESSIONID=" + idSessao);
		}
		return conexao;
	}

	public String getUrl() {
		return this.url;
	}

	public void setarSenhaCriptografada(boolean criptedPass) {
		this.senhaCriptografada = criptedPass;
	}

	private JsonObject converterInputStreamEmJsonObject(InputStream is) throws IOException {
		JsonObject jsonObject = null;
		try (Reader reader = new InputStreamReader(is, Charset.forName("UTF-8"));) {
			jsonObject = (JsonObject) JsonParser.parseReader(reader);
		} catch (Exception e) {
			if (!(e instanceof java.io.FileNotFoundException))
				e.printStackTrace();
		}
		return jsonObject;
	}

	public static String retornarVazioNulo(String s) {
		if (s == null || s.length() == 0)
			return null;
		String trimed = s.trim();
		return (trimed.length() == 0) ? null : trimed;
	}
}
