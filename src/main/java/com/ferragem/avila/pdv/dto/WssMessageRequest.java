package com.ferragem.avila.pdv.dto;

import com.ferragem.avila.pdv.utils.OperationStatus;

public record WssMessageRequest(OperationStatus status, String content) {
}
