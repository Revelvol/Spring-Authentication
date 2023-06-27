package com.revelvol.JWT.exception.handler;

import com.revelvol.JWT.exception.UserAlreadyExistsException;
import com.revelvol.JWT.exception.UserNotFoundException;
import com.revelvol.JWT.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;


// im going to override all the Response Entity Handler
@ControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler({UserNotFoundException.class, TransactionSystemException.class, UserAlreadyExistsException.class, IllegalArgumentException.class})
    protected ResponseEntity<Object> handleDefaultException(Exception ex) {

        if (ex instanceof TransactionSystemException subEx) {
            return handleTransactionViolation(subEx);
        } else if (ex instanceof UserNotFoundException subEx) {
            return handleUserNotFoundException(subEx);

        } else if (ex instanceof IllegalArgumentException subEx) {
            return handleIllegalArgumentException(subEx);

        }

        else {
            ApiResponse response = new ApiResponse(HttpStatus.BAD_REQUEST.value(), "the request method is not valid");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Default handle Violation Exception
    protected ResponseEntity<Object> createDefaultResponse(Exception ex) {
        ApiResponse response = new ApiResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

    }

    private ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException subEx){
        return createDefaultResponse(subEx);
    }

    private ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException subEx) {

        String cause  = subEx.getMessage();
        HashMap<String,Object> errors  = new HashMap();
        errors.put("field",cause);

        ApiResponse response = new ApiResponse(HttpStatus.BAD_REQUEST.value(), "The request is not valid");
        response.setData(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }




    // Handle transaction violation and find out the root cause for that violation
    protected ResponseEntity<Object> handleTransactionViolation(TransactionSystemException ex) {
        if (ex.getRootCause() instanceof ConstraintViolationException subEx) {
            return handleConstraintViolationException(subEx);
        }

        return createDefaultResponse(ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException subEx) {

        Map<String, Object> errors = new HashMap<>();

        subEx.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });


        ApiResponse response = new ApiResponse(HttpStatus.BAD_REQUEST.value(), "The request is not valid");
        response.setData(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // override the handle method argument not valid on the default parent class exception
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, Object> errors = new HashMap<>();


        //get the error from the field
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        //create the response object
        ApiResponse response = new ApiResponse(HttpStatus.BAD_REQUEST.value(), "the request is not valid");
        response.setData(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


}
