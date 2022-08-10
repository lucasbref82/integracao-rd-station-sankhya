package br.com.digicade.utils;

import java.util.Base64;

public class Utilitarios {

	public static String DecodificadorBase64(String textoCodificado) throws Exception {
		String textoDeserializado = new String(Base64.getDecoder().decode(textoCodificado));
		return textoDeserializado;
	}

}
