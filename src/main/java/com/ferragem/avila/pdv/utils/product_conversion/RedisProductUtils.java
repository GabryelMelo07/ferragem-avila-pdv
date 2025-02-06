package com.ferragem.avila.pdv.utils.product_conversion;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.ferragem.avila.pdv.utils.RedisUtils;
import com.ferragem.avila.pdv.utils.product_conversion.csv.ProdutosImportados;

@Component
public class RedisProductUtils {
	private final RedisUtils redisUtils;

	public RedisProductUtils(RedisUtils redisUtils) {
		this.redisUtils = redisUtils;
	}

	public void sendMessageOrStoreError(String channel, String key, String successMessage, String errorMessage, ProdutosImportados resultado) {
		if (resultado.getProdutosComErro().isEmpty()) {
			redisUtils.sendMessage(channel, successMessage);
		} else {
			this.storeValueAndSendMessage(key, resultado, 3, TimeUnit.HOURS, channel, errorMessage);
		}
	}

	public void storeValueAndSendMessage(String key, Object value, long timeout, TimeUnit timeUnit, String channel, String message) {
		redisUtils.storeValue(key, value, timeout, timeUnit);
		redisUtils.sendMessage(channel, message);
	}
	
}
