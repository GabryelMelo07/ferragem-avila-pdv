package com.ferragem.avila.pdv.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.ferragem.avila.pdv.dto.NfeRequest;
import com.ferragem.avila.pdv.exceptions.XmlParsingException;
import com.ferragem.avila.pdv.utils.product_conversion.xml.Det;
import com.ferragem.avila.pdv.utils.product_conversion.xml.NfeProc;
import com.ferragem.avila.pdv.utils.product_conversion.xml.XmlParserUtil;

import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NfeApiService {

	private final String nfeApiUrl;

	private final RestClient restClient;

	public NfeApiService(@Value("${nfe.api.url}") String nfeApiUrl) {
		this.nfeApiUrl = nfeApiUrl;
		this.restClient = RestClient.create();
	}

	public List<Det> getNfeXml(String chaveAcesso) {
		String xmlResponse = restClient.post()
				.uri(nfeApiUrl)
				.contentType(MediaType.APPLICATION_JSON)
				.body(new NfeRequest(chaveAcesso))
				.retrieve()
				.body(String.class);

		NfeProc nfeProc = null;

		try {
			nfeProc = XmlParserUtil.parseXml(xmlResponse);
		} catch (JAXBException e) {
			log.error("Erro ao realizar o parse do XML: ", e);
			throw new XmlParsingException(e.getMessage());
		}

		return nfeProc.getNfe().getInfNfe().getDetList();
	}

}
