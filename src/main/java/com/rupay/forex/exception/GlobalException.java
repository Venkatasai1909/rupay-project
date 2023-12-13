package com.rupay.forex.exception;

import com.rupay.forex.dto.ExceptionResponse;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalException {
    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleFileAlreadyExistsException(FileAlreadyExistsException fileAlreadyExistsException) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder().
                message(fileAlreadyExistsException.getMessage()).
                errorCode("FILE_ALREADY_EXISTS").localDateTime(LocalDateTime.now()).
                build();

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ExceptionResponse> handleCustomException(CustomException customException) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder().
                message(customException.getMessage()).
                errorCode(customException.errorCode).localDateTime(LocalDateTime.now()).build();

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}
