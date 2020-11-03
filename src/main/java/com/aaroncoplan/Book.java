package com.aaroncoplan;

import javax.persistence.*;
import org.springframework.data.repository.CrudRepository;

@Entity
public class Book extends GraphQLObject {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long ID;

  private String title;

  @OneToOne
  private Author author;

  public Book() {}

  public Book(String title, Author author) {
    this.title = title;
    this.author = author;
  }

  protected String getName() {
    return "Book";
  }

  @Override
  protected Class<? extends CrudRepository> getRepository() {
    return BookRepository.class;
  }

  @GraphQLField(name = "id", type = FieldType.ID)
  public Long getID() {
    return this.ID;
  }

  @GraphQLField(name = "title", type = FieldType.STRING)
  public String getTitle() {
    return this.title;
  }

  @GraphQLField(name = "author", type = FieldType.OBJECT)
  public Author getAuthor() {
    return this.author;
  }
}
