package br.com.geospot.api.exceptions;

import lombok.Getter;

@Getter
public class FlowException extends RuntimeException {

    private final ErrorCodeEnum code;

    public FlowException(ErrorCodeEnum code, String message) {
        super(message);
        this.code = code;
    }
}
