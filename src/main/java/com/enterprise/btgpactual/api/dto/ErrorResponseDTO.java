package com.enterprise.btgpactual.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponseDTO {

    private int status;
    private String error;
    private String mensaje;
    private LocalDateTime timestamp;
}
