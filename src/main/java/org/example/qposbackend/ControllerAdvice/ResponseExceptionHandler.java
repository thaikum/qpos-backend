package org.example.qposbackend.ControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;
import org.example.qposbackend.DTOs.MessageResponse;
import org.example.qposbackend.Exceptions.GenericExceptions;
import org.example.qposbackend.Exceptions.GenericRuntimeException;
import org.example.qposbackend.Exceptions.NotAcceptableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ResponseExceptionHandler {
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<MessageResponse> handleNoSuchElementException(NoSuchElementException ex) {
    return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<MessageResponse> handleAccessDeniedException(AccessDeniedException ex) {
    return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler({GenericRuntimeException.class, GenericExceptions.class})
  public ResponseEntity<MessageResponse> handleServerError(Exception ex) {
    ex.printStackTrace();
    return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_ACCEPTABLE);
  }

  @ExceptionHandler(NotAcceptableException.class)
  public ResponseEntity<MessageResponse> handleNotAcceptableException(NotAcceptableException ex) {
    return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_ACCEPTABLE);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<MessageResponse> handleAllOtherExceptions(Exception ex) {
    ex.printStackTrace();
    return new ResponseEntity<>(
        new MessageResponse(
            "An unexpected error occurred. Please contact your system administrator."),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
