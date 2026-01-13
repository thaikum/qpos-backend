package org.example.qposbackend.Exceptions;

public class EndOfDayException extends RuntimeException{
    public EndOfDayException(String message){
        super(message);
    }
}
