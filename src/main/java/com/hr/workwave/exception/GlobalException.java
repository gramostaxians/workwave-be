package com.hr.workwave.exception;

import com.hr.workwave.dto.ResponseExceptionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.math.BigInteger;

@ControllerAdvice
public class GlobalException {
//
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<ResponseExceptionDto> generalException(RuntimeException ex) {
//        ResponseExceptionDto response = new ResponseExceptionDto();
//        response.setCustomCode(BigInteger.valueOf(1000));
//        response.setMessage(ex.getMessage());
//
//        return ResponseEntity.ok(response);
//    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseExceptionDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        ResponseExceptionDto response = new ResponseExceptionDto();
        response.setCustomCode(BigInteger.valueOf(1002));
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(400).body(response);
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseExceptionDto> handleIllegalStateException(IllegalStateException ex) {
        ResponseExceptionDto response = new ResponseExceptionDto();
        response.setCustomCode(BigInteger.valueOf(1001));
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(400).body(response);
    }
}
