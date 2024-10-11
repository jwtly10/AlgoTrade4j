package dev.jwtly10.shared.exception;

import dev.jwtly10.shared.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class SharedGlobalExceptionHandler {


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String errorMessage = String.format("Missing required parameter: %s", ex.getParameterName());
        ErrorResponse errorResponse = new ErrorResponse(errorMessage, ErrorType.BAD_REQUEST);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeaders(MissingRequestHeaderException ex) {
        String errorMessage = String.format("Missing required header: %s", ex.getHeaderName());
        ErrorResponse errorResponse = new ErrorResponse(errorMessage, ErrorType.BAD_REQUEST);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String parameterName = ex.getName();
        String errorMessage = String.format("Invalid value for parameter '%s': '%s'", parameterName, ex.getValue());
//        log.error("Argument type mismatch: {}", errorMessage, ex);

        ErrorResponse errorResponse = new ErrorResponse(errorMessage, ErrorType.BAD_REQUEST);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ex.getErrorType());

        HttpStatus status = switch (ex.getErrorType()) {
            case BAD_REQUEST, VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Uncaught internal error", ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ErrorType.INTERNAL_ERROR);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse("Validation failed: " + errors, ErrorType.VALIDATION_ERROR);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResourceFoundException(NoResourceFoundException ex) {
        log.info("Handling invalid endpoint: /{}", ex.getResourcePath());
    }
}