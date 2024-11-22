package com.example.tak.config.response.exception.handler;

import com.example.tak.config.response.code.BaseErrorCode;
import com.example.tak.config.response.exception.GeneralException;

public class GeneralHandler extends GeneralException {
    public GeneralHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
