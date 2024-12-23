package com.ferragem.avila.pdv.exceptions;

public class XlsxSizeLimitException extends RuntimeException {
        
    public XlsxSizeLimitException(String message) {
        super(message);
    }

    public XlsxSizeLimitException(int xlsxFileLimit) {
        super("Arquivos de formato .xlsx suportam somente " + xlsxFileLimit + " linhas.");
    }
    
}
