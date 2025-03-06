package com.ferragem.avila.pdv.utils.product_conversion;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.ferragem.avila.pdv.dto.WssMessageRequest;
import com.ferragem.avila.pdv.service.apis.WebSocketSendMessageService;
import com.ferragem.avila.pdv.utils.OperationInfo;
import com.ferragem.avila.pdv.utils.OperationStatus;
import com.ferragem.avila.pdv.utils.RedisUtils;
import com.ferragem.avila.pdv.utils.product_conversion.csv.ProdutosImportados;

@Component
public class RedisProductUtils {
	private final RedisUtils redisUtils;
	private final WebSocketSendMessageService webSocketSendMessageService;

	public RedisProductUtils(RedisUtils redisUtils, WebSocketSendMessageService webSocketSendMessageService) {
		this.redisUtils = redisUtils;
		this.webSocketSendMessageService = webSocketSendMessageService;
	}

	public void sendMessageOrStoreError(String key, String successMessage, String errorMessage, OperationInfo operationInfo, ProdutosImportados resultado) {
		if (resultado.getProdutosComErro().isEmpty()) {
			webSocketSendMessageService.sendMessage(new WssMessageRequest(OperationStatus.SUCCESS, operationInfo, successMessage));
		} else {
			this.storeValueAndSendMessage(key, resultado, 3, TimeUnit.HOURS, new WssMessageRequest(OperationStatus.ERROR,  operationInfo, errorMessage));
		}
	}

	public void storeValueAndSendMessage(String key, Object value, long timeout, TimeUnit timeUnit, WssMessageRequest request) {
		redisUtils.storeValue(key, value, timeout, timeUnit);
		webSocketSendMessageService.sendMessage(request);
	}
	
}
