package com.viniciusmcabral.sound_rate.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;
	private GlobalExceptionHandler handler;

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		this.handler = new GlobalExceptionHandler();
		this.mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
				.setControllerAdvice(this.handler)
				.setValidator(validator)
				.build();
	}

	@Test
	void shouldReturnUnauthorizedForAuthenticationException() throws Exception {
		mockMvc.perform(get("/test/authentication"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Unauthorized"))
				.andExpect(jsonPath("$.message").value("Bad credentials"));
	}

	@Test
	void shouldReturnBadRequestForMissingRequestParameter() throws Exception {
		mockMvc.perform(get("/test/request-param"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void shouldReturnInternalServerErrorForUnexpectedException() throws Exception {
		mockMvc.perform(get("/test/runtime"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.error").value("Internal Server Error"))
				.andExpect(jsonPath("$.message").value("An unexpected error occurred."));
	}

	@Test
	void shouldReturnBadRequestForHandlerMethodValidationException() {
		HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
		ParameterValidationResult validationResult = mock(ParameterValidationResult.class);
		DefaultMessageSourceResolvable error = new DefaultMessageSourceResolvable(new String[] { "query" },
				null, "must not be blank");
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test/validated");

		when(exception.getParameterValidationResults()).thenReturn(List.of(validationResult));
		when(validationResult.getResolvableErrors()).thenReturn(List.of(error));

		ResponseEntity<java.util.Map<String, Object>> response = handler
				.handleHandlerMethodValidationException(exception, request);

		assertEquals(400, response.getStatusCode().value());
		assertEquals("Validation Error", response.getBody().get("error"));
		assertEquals(List.of("must not be blank"), response.getBody().get("errors"));
	}

	@Test
	void shouldReturnNotFoundForNoSuchElementException() {
		ResponseEntity<Map<String, Object>> response = handler.handleNoSuchElementException(
				new NoSuchElementException("Album not found"), new MockHttpServletRequest("GET", "/test/not-found"));

		assertEquals(404, response.getStatusCode().value());
		assertEquals("Not Found", response.getBody().get("error"));
		assertEquals("Album not found", response.getBody().get("message"));
	}

	@Test
	void shouldReturnConflictForIllegalStateException() {
		ResponseEntity<Map<String, Object>> response = handler.handleIllegalStateException(
				new IllegalStateException("Already exists"), new MockHttpServletRequest("POST", "/test/conflict"));

		assertEquals(409, response.getStatusCode().value());
		assertEquals("Conflict", response.getBody().get("error"));
		assertEquals("Already exists", response.getBody().get("message"));
	}

	@Test
	void shouldReturnBadRequestForMethodArgumentNotValidException() throws NoSuchMethodException {
		RequestPayload payload = new RequestPayload("");
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(payload, "requestPayload");
		bindingResult.addError(new FieldError("requestPayload", "name", "must not be blank"));
		Method method = TestController.class.getDeclaredMethod("requestBodyValidation", RequestPayload.class);
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
				new org.springframework.core.MethodParameter(method, 0), bindingResult);

		ResponseEntity<Map<String, Object>> response = handler.handleMethodArgumentNotValid(exception,
				new MockHttpServletRequest("POST", "/test/request-body-validation"));

		assertEquals(400, response.getStatusCode().value());
		assertEquals("Validation Error", response.getBody().get("error"));
		assertEquals(Map.of("name", "must not be blank"), response.getBody().get("errors"));
	}

	@Test
	void shouldReturnBadRequestForConstraintViolationException() {
		ConstraintViolation<?> violation = mock(ConstraintViolation.class);
		when(violation.toString()).thenReturn("query: must not be blank");

		ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolationException(
				new ConstraintViolationException("query: must not be blank", java.util.Set.of(violation)),
				new MockHttpServletRequest("GET", "/test/constraint-violation"));

		assertEquals(400, response.getStatusCode().value());
		assertEquals("Validation Error", response.getBody().get("error"));
		assertEquals("query: must not be blank", response.getBody().get("message"));
	}

	@Test
	void shouldReturnBadRequestForIllegalArgumentException() {
		ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgumentException(
				new IllegalArgumentException("Invalid input"), new MockHttpServletRequest("DELETE", "/test/bad-request"));

		assertEquals(400, response.getStatusCode().value());
		assertEquals("Bad Request", response.getBody().get("error"));
		assertEquals("Invalid input", response.getBody().get("message"));
	}

	@Test
	void shouldReturnForbiddenForAccessDeniedException() {
		ResponseEntity<Map<String, Object>> response = handler.handleAccessDeniedException(
				new AccessDeniedException("Forbidden action"), new MockHttpServletRequest("PUT", "/test/forbidden"));

		assertEquals(403, response.getStatusCode().value());
		assertEquals("Forbidden", response.getBody().get("error"));
		assertEquals("Forbidden action", response.getBody().get("message"));
	}

	@Test
	void shouldReturnBadRequestForMethodArgumentTypeMismatchException() {
		MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException("abc", Integer.class,
				"id", null, new IllegalArgumentException("invalid"));

		ResponseEntity<Map<String, Object>> response = handler.handleMethodArgumentTypeMismatchException(exception,
				new MockHttpServletRequest("GET", "/test/type-mismatch"));

		assertEquals(400, response.getStatusCode().value());
		assertEquals("Bad Request", response.getBody().get("error"));
		assertEquals("Invalid value for parameter 'id'.", response.getBody().get("message"));
	}

	@RestController
	@Validated
	static class TestController {

		@GetMapping("/test/authentication")
		ResponseEntity<Void> authentication() {
			throw new BadCredentialsException("Bad credentials");
		}

		@GetMapping("/test/request-param")
		ResponseEntity<Void> requestParam(@RequestParam String query) {
			return ResponseEntity.ok().build();
		}

		@GetMapping("/test/validated")
		ResponseEntity<Void> validated(@RequestParam @NotBlank String query) {
			return ResponseEntity.ok().build();
		}

		@PostMapping("/test/request-body-validation")
		ResponseEntity<Void> requestBodyValidation(@RequestBody @Valid RequestPayload payload) {
			return ResponseEntity.ok().build();
		}

		@GetMapping("/test/runtime")
		ResponseEntity<Void> runtime() {
			throw new RuntimeException("boom");
		}
	}

	record RequestPayload(@NotBlank String name) {
	}
}
