package com.sparta.wildcard_newsfeed.exception;

import com.sparta.wildcard_newsfeed.domain.common.error.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> illegalArgumentException(IllegalArgumentException e) {
        log.error("예시", e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDto.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .message(e.getMessage())
                        .build());
    }

//    @ExceptionHandler(TokenNotFoundException.class)
//    public ResponseEntity<ErrorResponseDto> tokenNotFoundException(TokenNotFoundException e) {
//        log.error("Token 예외 발생 {} " ,e);
//        return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(ErrorResponseDto.builder()
//                        .statusCode(HttpStatus.NOT_FOUND.value())
//                        .message(e.getMessage())
//                        .build()
//                );
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errorMessageList = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(v -> errorMessageList.add(v.getDefaultMessage()));
        log.error(errorMessageList.toString());

        return ResponseEntity.badRequest()
                .body(ErrorResponseDto.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(errorMessageList)
                        .build());
    }
}