package com.aaroncoplan;

import javax.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;

@Entity
public class Author extends GraphQLObject {

  @Transient
  @Autowired
  private AuthorRepository authorRepository;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long ID;

  private String name;

  protected String getName() {
    return "Author";
  }

  @Override
  protected Repository getRepository() {
    return authorRepository;
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
