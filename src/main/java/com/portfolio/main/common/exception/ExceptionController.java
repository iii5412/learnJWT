package com.portfolio.main.common.exception;

import com.portfolio.main.presentation.rest.response.ErrorResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class ExceptionController {
    @ExceptionHandler(BusiException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> busiException(BusiException e) {
        final int statusCode = e.getStatusCode();
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .code(String.valueOf(statusCode))
                .message(e.getMessage())
                .validation(e.getValidation())
                .build();

        return ResponseEntity.status(statusCode).body(errorResponse);
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> dataAccessException(DataAccessException e) {
        final int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .code(String.valueOf(statusCode))
                .message("데이터를 가져오는데 문제가 발생했습니다.")
                .build();

        return ResponseEntity.status(statusCode).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> accessDeniedException(AccessDeniedException e) {
        final int statusCode = HttpStatus.FORBIDDEN.value();
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .code(String.valueOf(statusCode))
                .message("접근 권한이 없습니다.")
                .build();

        return ResponseEntity.status(statusCode).body(errorResponse);
    }


}
