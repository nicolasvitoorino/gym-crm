package br.com.unicuritiba.gymcrm.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MovimentacaoNaoEncontradaException.class)
	public ResponseEntity<Map<String, Object>> naoEncontrada(MovimentacaoNaoEncontradaException ex) {
		return erro(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
		Map<String, String> campos = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));
		Map<String, Object> corpo = corpo(HttpStatus.BAD_REQUEST, "Dados inválidos");
		corpo.put("campos", campos);
		return ResponseEntity.badRequest().body(corpo);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException ex) {
		return erro(HttpStatus.BAD_REQUEST,
				"JSON inválido. Use tipo RECEITA ou DESPESA e data no formato yyyy-MM-dd");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> parametroInvalido(MethodArgumentTypeMismatchException ex) {
		return erro(HttpStatus.BAD_REQUEST,
				"Parâmetro '" + ex.getName() + "' com valor inválido: " + ex.getValue());
	}

	private ResponseEntity<Map<String, Object>> erro(HttpStatus status, String mensagem) {
		return ResponseEntity.status(status).body(corpo(status, mensagem));
	}

	private Map<String, Object> corpo(HttpStatus status, String mensagem) {
		Map<String, Object> corpo = new LinkedHashMap<>();
		corpo.put("timestamp", Instant.now().toString());
		corpo.put("status", status.value());
		corpo.put("erro", status.getReasonPhrase());
		corpo.put("mensagem", mensagem);
		return corpo;
	}
}
