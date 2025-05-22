package org.example.qposbackend.ControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.Exceptions.GenericExceptions;
import org.example.qposbackend.Exceptions.GenericRuntimeException;
import org.example.qposbackend.Exceptions.NotAcceptableException;
import org.hibernate.TransientPropertyValueException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLSyntaxErrorException;
import java.util.NoSuchElementException;

@ControllerAdvice
public class ResponseExceptionHandler {
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<MessageResponse> handleNoSuchElementException(NoSuchElementException ex) {
    return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({
    TransientPropertyValueException.class,
    NullPointerException.class,
    SQLSyntaxErrorException.class,
    HttpMessageNotReadableException.class,
    GenericRuntimeException.class,
    GenericExceptions.class
  })
  public ResponseEntity<MessageResponse> handleServerError(Exception ex) {
    return new ResponseEntity<>(
        new MessageResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(NotAcceptableException.class)
  public ResponseEntity<MessageResponse> handleNotAcceptableException(NotAcceptableException ex) {
    return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_ACCEPTABLE);
  }

  @ExceptionHandler({TypeMismatchException.class, InvalidDefinitionException.class})
  public ResponseEntity<MessageResponse> handleNotAcceptableException(Exception ex) {
    return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
  }
}
