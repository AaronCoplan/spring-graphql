package com.aaroncoplan;

public class Author extends GraphQLObject {

  protected String getName() {
    return "Author";
  }

  @GraphQLField(name = "id", type = FieldType.ID)
  public int getID() {
    return 2;
  }

  @GraphQLField(name = "name", type = FieldType.STRING)
  public String getAuthorName() {
    return "Suzanne Collins";
  }
}
