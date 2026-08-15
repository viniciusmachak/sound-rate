package com.viniciusmcabral.sound_rate.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Map<String, Object>> handleNoSuchElementException(NoSuchElementException e,
			HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException e,
			HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
			HttpServletRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now());
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", "Validation Error");
		body.put("path", request.getRequestURI());

		Map<String, String> fieldErrors = new HashMap<>();
		for (FieldError f : e.getBindingResult().getFieldErrors()) {
			fieldErrors.put(f.getField(), f.getDefaultMessage());
		}
		body.put("errors", fieldErrors);

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex,
			HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", ex.getMessage(),
				request.getRequestURI());
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<Map<String, Object>> handleHandlerMethodValidationException(
			HandlerMethodValidationException ex, HttpServletRequest request) {
		Map<String, Object> body = buildErrorBody(HttpStatus.BAD_REQUEST, "Validation Error", request.getRequestURI());
		List<String> errors = new ArrayList<>();
		ex.getParameterValidationResults().forEach(result -> result.getResolvableErrors()
				.forEach(error -> errors.add(error.getDefaultMessage())));
		body.put("errors", errors);
		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex,
			HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex,
			HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex,
			HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(
			MissingServletRequestParameterException ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
			MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
		String message = "Invalid value for parameter '" + ex.getName() + "'.";
		return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", message, request.getRequestURI());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
				"An unexpected error occurred.", request.getRequestURI());
	}

	private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message,
			String path) {
		return new ResponseEntity<>(buildErrorBody(status, error, path, message), status);
	}

	private Map<String, Object> buildErrorBody(HttpStatus status, String error, String path) {
		return buildErrorBody(status, error, path, null);
	}

	private Map<String, Object> buildErrorBody(HttpStatus status, String error, String path, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now());
		body.put("status", status.value());
		body.put("error", error);
		if (message != null) {
			body.put("message", message);
		}
		body.put("path", path);
		return body;
	}
}
