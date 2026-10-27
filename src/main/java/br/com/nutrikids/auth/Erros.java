package br.com.nutrikids.auth;

import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class Erros {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validacao(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            Map.of(
                "mensagem",
                "Revise os campos informados",
                "campos",
                e.getBindingResult().getFieldErrors().stream()
                    .map(x -> x.getField() + ": " + x.getDefaultMessage())
                    .toList()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<?> argumento(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("mensagem", "Parâmetros inválidos"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> conflito(DataIntegrityViolationException e) {
    return ResponseEntity.status(409)
        .body(
            Map.of(
                "mensagem",
                "Conflito: cadastro duplicado, vínculo inválido ou horário indisponível"));
  }
}
