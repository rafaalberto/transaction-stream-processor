package io.rafaalberto.transaction_stream_processor.ingress.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

  @GetMapping("/hello")
  String hello() {
    return "Hello";
  }
}
