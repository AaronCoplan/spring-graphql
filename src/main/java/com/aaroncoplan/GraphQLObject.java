package com.aaroncoplan;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;

import graphql.schema.*;
import java.util.*;
import java.util.stream.Collectors;

public abstract class GraphQLObject {

  protected abstract String getName();

  protected String getDescription() {
    return null;
  }

  public final GraphQLCodeRegistry generateDataFetchers() {
    var builder = GraphQLCodeRegistry
      .newCodeRegistry()
      .dataFetcher(
        FieldCoordinates.coordinates("Query", "load_Book"),
        (DataFetchingEnvironment dataFetchingEnvironment) -> new Book()
      );

    var fieldDefinitions = this.generateFieldDefinitions();
    for (FieldDefinition fieldDefinition : fieldDefinitions) {
      var definition = fieldDefinition.getDefinition();
      builder =
        builder.dataFetcher(
          FieldCoordinates.coordinates(this.getName(), definition.getName()),
          (DataFetchingEnvironment dataFetchingEnvironment) ->
            fieldDefinition
              .getDataGeneratorMethod()
              .invoke(dataFetchingEnvironment.getSource())
        );
    }

    return builder.build();
  }

  public final GraphQLFieldDefinition generateRootField() {
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

  public final GraphQLObjectType generateObjectType() {
    var fields =
      this.generateFieldDefinitions()
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

  private List<FieldDefinition> generateFieldDefinitions() {
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
