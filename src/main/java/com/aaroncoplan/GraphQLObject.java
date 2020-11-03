package com.aaroncoplan;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;

import graphql.GraphQL;
import graphql.execution.batched.BatchedDataFetcherFactory;
import graphql.execution.nextgen.BatchedDataFetcher;
import graphql.schema.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class GraphQLObject {

  protected abstract String getName();

  protected String getDescription() {
    return null;
  }

  public GraphQLCodeRegistry generateDataFetchers() {
    var fieldDefinitions = this.generateFields();

    return GraphQLCodeRegistry
      .newCodeRegistry()
      .dataFetcher(
        FieldCoordinates.coordinates("Query", "load_Book"),
        (DataFetchingEnvironment dataFetchingEnvironment) -> {
          return new Book();
        }
      )
      .dataFetcher(
        FieldCoordinates.coordinates("Book", "id"),
        (DataFetchingEnvironment dataFetchingEnvironment) -> {
          var book = (Book) dataFetchingEnvironment.getSource();
          return book.getID();
        }
      )
      .dataFetcher(
        FieldCoordinates.coordinates("Book", "title"),
        (DataFetchingEnvironment dataFetchingEnvironment) -> {
          var book = (Book) dataFetchingEnvironment.getSource();
          return book.getTitle();
        }
      )
      .build();
  }

  public GraphQLFieldDefinition generateRootField() {
    return GraphQLFieldDefinition
      .newFieldDefinition()
      .name("load_" + this.getName())
      .type(GraphQLTypeReference.typeRef(this.getName()))
      .argument(
        GraphQLArgument
          .newArgument()
          .name("id")
          .type(GraphQLNonNull.nonNull(GraphQLID))
          .build()
      )
      .build();
  }

  public GraphQLObjectType generateObjectType() {
    var fields =
      this.generateFields()
        .stream()
        .map(FieldDefinition::getDefinition)
        .collect(Collectors.toList());

    return GraphQLObjectType
      .newObject()
      .name(this.getName())
      .description(this.getDescription())
      .fields(fields)
      .build();
  }

  private List<FieldDefinition> generateFields() {
    return List
      .of(this.getClass().getDeclaredMethods())
      .stream()
      .map(
        method -> {
          var annotation = method.getAnnotation(GraphQLField.class);
          if (annotation == null) {
            return null;
          }

          var name = annotation.name();
          var type = typeForFieldType(annotation.type());

          return new FieldDefinition(
            GraphQLFieldDefinition
              .newFieldDefinition()
              .name(name)
              .type(type)
              .build(),
            method
          );
        }
      )
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private GraphQLOutputType typeForFieldType(FieldType fieldType) {
    switch (fieldType) {
      case ID:
        return GraphQLID;
      case STRING:
        return GraphQLString;
    }

    return null;
  }
}
