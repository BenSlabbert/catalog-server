package com.fluent.catalog.web.advice;

import com.fluent.catalog.web.dto.ErrorDto;
import com.fluent.catalog.web.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

  @ExceptionHandler
  public ResponseEntity<ErrorDto> handleNotFoundException(NotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(e.getMessage()));
  }
}
