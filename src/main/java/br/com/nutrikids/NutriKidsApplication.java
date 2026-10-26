package br.com.nutrikids;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    exclude =
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
            .class)
public class NutriKidsApplication {
  public static void main(String[] args) {
    SpringApplication.run(NutriKidsApplication.class, args);
  }
}
