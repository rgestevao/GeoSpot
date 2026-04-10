package br.com.geospot.api.exceptions;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorInfo {

    private ErrorCodeEnum code;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;
}
