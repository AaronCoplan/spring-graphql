package com.aaroncoplan.springgraphql;

import graphql.schema.GraphQLFieldDefinition;
import java.lang.reflect.Method;

public class FieldDefinition {

  private final GraphQLFieldDefinition definition;
  private final Method dataGeneratorMethod;

  public FieldDefinition(
    GraphQLFieldDefinition definition,
    Method dataGeneratorMethod
  ) {
    this.definition = definition;
    this.dataGeneratorMethod = dataGeneratorMethod;
  }

  public GraphQLFieldDefinition getDefinition() {
    return definition;
  }

  public Method getDataGeneratorMethod() {
    return dataGeneratorMethod;
  }
}
