package com.aaroncoplan;

public class Book extends GraphQLObject {

  protected String getName() {
    return "Book";
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
