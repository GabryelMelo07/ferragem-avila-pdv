package com.ferragem.avila.pdv.dto;

import com.ferragem.avila.pdv.utils.OperationInfo;
import com.ferragem.avila.pdv.utils.OperationStatus;

public record WssMessageRequest(OperationStatus status, OperationInfo operation, String content) {
}
