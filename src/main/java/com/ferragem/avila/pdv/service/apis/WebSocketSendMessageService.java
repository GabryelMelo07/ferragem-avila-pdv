package com.ferragem.avila.pdv.service.apis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.ferragem.avila.pdv.dto.WssMessageRequest;
import com.ferragem.avila.pdv.utils.OperationStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebSocketSendMessageService {

	private final String wssApiUrl;
	private final RestClient restClient;
	private static final int MAX_RETRIES = 3;
	private static final long RETRY_DELAY_MS = 2000;

	public WebSocketSendMessageService(@Value("${wss.api.url}") String wssApiUrl) {
		this.wssApiUrl = wssApiUrl;
		this.restClient = RestClient.create();
	}

	@Async
	public void sendMessage(OperationStatus status, String message) {
		int attempts = 0;
		boolean success = false;

		while (attempts < MAX_RETRIES && !success) {
			try {
				attempts++;
				log.info("Tentando enviar mensagem, tentativa {}/{}", attempts, MAX_RETRIES);

				ResponseEntity<String> response = restClient.post()
						.uri(wssApiUrl + "/message/send")
						.body(new WssMessageRequest(status, message))
						.retrieve()
						.toEntity(String.class);

				if (response.getStatusCode().is2xxSuccessful()) {
					log.info("Mensagem enviada com sucesso!");
					success = true;
				} else {
					log.error("Erro ao enviar mensagem. Status: {}", response.getStatusCode());
				}
			} catch (Exception e) {
				log.error("Erro na tentativa {}: {}", attempts, e.getMessage());
			}

			if (!success && attempts < MAX_RETRIES) {
				try {
					Thread.sleep(RETRY_DELAY_MS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		if (!success) {
			log.error("Falha ao enviar a mensagem após {} tentativas", MAX_RETRIES);
		}
	}

}
