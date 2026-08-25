package com.example.bookstore.errorhandling;

import com.example.bookstore.exception.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<ErrorItem> errorItems = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String errorCode = "E400_INVALID_" + fieldError.getField().toUpperCase();
            errorItems.add(new ErrorItem(errorCode, fieldError.getDefaultMessage()));
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("The request contains invalid or missing data. Please correct the errors and try again.")
                .path(request.getDescription(false).replace("uri=", ""))
                .errorItems(errorItems)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private ResponseEntity<Object> createErrorResponse(Exception e, HttpStatus status, String summaryMessage, String errorCode, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(summaryMessage)
                .path(request.getDescription(false).replace("uri=", ""))
                .errorItems(List.of(new ErrorItem(errorCode, e.getMessage())))
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Object> handleBookNotFoundException(BookNotFoundException e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.NOT_FOUND,
                "The requested book could not be located in the catalog.",
                "E404_BOOK_NOT_FOUND", request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.NOT_FOUND,
                "The specified user profile does not exist.",
                "E404_USER_NOT_FOUND", request);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Object> handleCategoryNotFoundException(CategoryNotFoundException e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.NOT_FOUND,
                "The specified category could not be located.",
                "E404_CATEGORY_NOT_FOUND", request);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Object> handleOrderNotFoundException(OrderNotFoundException e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.NOT_FOUND,
                "The requested order record could not be found.",
                "E404_ORDER_NOT_FOUND", request);
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<Object> handleInvalidOrderException(InvalidOrderException e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.BAD_REQUEST,
                "The provided order payload is malformed or violates business rules.",
                "E400_INVALID_ORDER", request);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Object> handleInsufficientStockException(InsufficientStockException e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.UNPROCESSABLE_CONTENT,
                "The order cannot be fulfilled due to insufficient inventory.",
                "E422_INSUFFICIENT_STOCK", request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> handleDuplicateResourceException(DuplicateResourceException e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.CONFLICT,
                "A data conflict occurred. The resource you are trying to create already exists.",
                "E409_DUPLICATE_RESOURCE", request);
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<Object> handleOrderException(OrderException e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred while processing the order transaction.",
                "E500_ORDER_PROCESSING_FAILED", request);
    }

    @ExceptionHandler(KeycloakUserCreationException.class)
    public ResponseEntity<Object> handleKeycloakUserCreationException(KeycloakUserCreationException e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.BAD_REQUEST,
                "Failed to register the user with the identity provider.",
                "E400_KEYCLOAK_CREATION_FAILED", request);
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Object> handleCartNotFoundException(CartNotFoundException e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.NOT_FOUND,
                "The requested cart could not be found.",
                "E404_CART_NOT_FOUND", request);
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<Object> handleCartItemNotFoundException(CartItemNotFoundException e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.NOT_FOUND,
                "The requested cart item could not be found.",
                "E404_CART_ITEM_NOT_FOUND", request);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        return createErrorResponse(e,HttpStatus.NOT_FOUND,
                "The requested resource could not be found.",
                "E404_RESOURCE_NOT_FOUND", request
                );
    }
    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<Object> handleReviewNotFoundException(ReviewNotFoundException e, WebRequest request) {
        return createErrorResponse(e,HttpStatus.NOT_FOUND,
                "The review could not be found.",
                "E404_REVIEW_NOT_FOUND", request
        );
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception e, WebRequest request) {
        return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected system error occurred.",
                "E500_INTERNAL_ERROR", request);
    }
}