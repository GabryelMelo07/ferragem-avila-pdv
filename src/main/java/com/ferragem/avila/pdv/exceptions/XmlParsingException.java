package com.ferragem.avila.pdv.exceptions;

public class XmlParsingException extends RuntimeException {
	public XmlParsingException(String message) {
		super(message);
	}

	public XmlParsingException() {
		super("Erro ao mapear o XML da Nota Fiscal.");
	}
	
}
