package com.aaroncoplan;

import javax.persistence.*;
import org.springframework.data.repository.CrudRepository;

@Entity
public class Author extends GraphQLObject {

  public Author() {}

  public Author(String name) {
    this.name = name;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long ID;

  private String name;

  protected String getName() {
    return "Author";
  }

  @Override
  protected Class<? extends CrudRepository> getRepository() {
    return AuthorRepository.class;
  }

  @GraphQLField(name = "id", type = FieldType.ID)
  public Long getID() {
    return this.ID;
  }

  @GraphQLField(name = "name", type = FieldType.STRING)
  public String getAuthorName() {
    return this.name;
  }
}
