package br.com.nutrikids.auth;

import java.util.*;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class Erros {
  public record ErroApi(String mensagem, List<String> campos) {}

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErroApi> validacao(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            new ErroApi(
                "Revise os campos informados",
                e.getBindingResult().getFieldErrors().stream()
                    .map(x -> x.getField() + ": " + x.getDefaultMessage())
                    .toList()));
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    OptimisticLockingFailureException.class
  })
  ResponseEntity<ErroApi> conflito(Exception e) {
    return resposta(
        409,
        "Conflito: cadastro duplicado, vínculo inválido, versão concorrente ou horário"
            + " indisponível");
  }

  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<ErroApi> negocio(ResponseStatusException e) {
    return resposta(
        e.getStatusCode().value(),
        e.getReason() == null ? "Operação não permitida" : e.getReason());
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  ResponseEntity<ErroApi> entrada(Exception e) {
    return resposta(400, "Parâmetro ou JSON inválido");
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<ErroApi> tamanho(Exception e) {
    return resposta(413, "Upload excede 5 MB");
  }

  private ResponseEntity<ErroApi> resposta(int status, String mensagem) {
    return ResponseEntity.status(status).body(new ErroApi(mensagem, List.of()));
  }
}
