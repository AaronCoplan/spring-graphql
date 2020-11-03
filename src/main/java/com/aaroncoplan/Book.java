package com.aaroncoplan;

import org.springframework.data.repository.CrudRepository;

public class Book extends GraphQLObject {

  protected String getName() {
    return "Book";
  }

  @Override
  protected Class<? extends CrudRepository> getRepository() {
    return null;
  }

  @GraphQLField(name = "id", type = FieldType.ID)
  public int getID() {
    return 1;
  }

  @GraphQLField(name = "title", type = FieldType.STRING)
  public String getTitle() {
    return "Hunger Games";
  }

  @GraphQLField(name = "author", type = FieldType.OBJECT)
  public Author getAuthor() {
    return new Author();
  }
}
