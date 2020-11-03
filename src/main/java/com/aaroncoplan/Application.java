package com.aaroncoplan;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public CommandLineRunner commandLineRunner(
    AuthorRepository authorRepository,
    BookRepository bookRepository
  ) {
    return args -> {
      System.out.println();

      var author = authorRepository.save(new Author("Suzanne Collins"));
      System.out.println("Author: " + author.getID());

      var book = bookRepository.save(new Book("Hunger Games", author));
      System.out.println("Book: " + book.getID());

      System.out.println();
    };
  }
}
