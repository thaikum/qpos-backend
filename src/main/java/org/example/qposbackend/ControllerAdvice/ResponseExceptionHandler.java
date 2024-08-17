package org.example.qposbackend.ControllerAdvice;

import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.Exceptions.NotAcceptableException;
import org.hibernate.TransientPropertyValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLSyntaxErrorException;
import java.util.NoSuchElementException;

@ControllerAdvice
public class ResponseExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<MessageResponse> handleNoSuchElementException(NoSuchElementException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({TransientPropertyValueException.class, NullPointerException.class, SQLSyntaxErrorException.class})
    public ResponseEntity<MessageResponse> handleServerError(Exception ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotAcceptableException.class)
    public ResponseEntity<MessageResponse> handleNotAcceptableException(NotAcceptableException ex) {
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_ACCEPTABLE);
    }
}
