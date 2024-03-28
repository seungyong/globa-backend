package org.y2k2.globa.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private final int errorCode;
    private String message;

    public ErrorResponse(int errorCode, String message){
        this.errorCode = errorCode;
        this.message = message;
    }

}
