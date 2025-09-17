package lt.nsg.scroogebank.controller;

import lt.nsg.scroogebank.dto.ErrorResponse;
import lt.nsg.scroogebank.dto.ScroogeMoneyException;
import lt.nsg.scroogebank.service.ScroogeServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(ScroogeServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCustomException(ScroogeServiceException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(ScroogeMoneyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCustomException(ScroogeMoneyException ex) {
        return new ErrorResponse(ex.getMessage());
    }

}