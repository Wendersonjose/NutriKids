package br.com.nutrikids.auth;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Paginacao {
  private Paginacao() {}

  public static Pageable descendente(int page, int size, String... ordem) {
    return ((PageRequest) de(page, size, ordem)).withSort(Sort.by(ordem).descending());
  }

  public static Pageable de(int page, int size, String... ordem) {
    if (page < 0 || page > 100000 || size < 1 || size > 100)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page: 0..100000; size: 1..100");
    return PageRequest.of(page, size, Sort.by(ordem));
  }
}
