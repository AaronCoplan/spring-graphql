package com.aaroncoplan;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import java.util.List;

public abstract class GraphQLQuery<T> {

  protected abstract String getName();

  protected abstract FieldType getFieldType();

  protected abstract T execute();

  public final GraphQLFieldDefinition generateQueryField() {
    var executeMethod = List
      .of(this.getClass().getDeclaredMethods())
      .stream()
      .filter(method -> "execute".equals(method.getName()))
      .findFirst()
      .get();

    return GraphQLFieldDefinition
      .newFieldDefinition()
      .name(this.getName())
      .type(
        GraphQLTypeUtils.typeForFieldType(executeMethod, this.getFieldType())
      )
      .build();
  }

  public final GraphQLCodeRegistry generateDataFetchers() {
    return GraphQLCodeRegistry
      .newCodeRegistry()
      .dataFetcher(
        FieldCoordinates.coordinates("Query", this.getName()),
        (DataFetchingEnvironment dataFetchingEnvironment) -> {
          return this.execute();
        }
      )
      .build();
  }
}
