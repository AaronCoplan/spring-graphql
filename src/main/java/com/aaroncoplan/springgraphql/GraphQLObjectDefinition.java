package com.aaroncoplan.springgraphql;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

public final class GraphQLObjectDefinition {

  private final GraphQLObjectType graphQLObjectType;
  private final GraphQLFieldDefinition graphQLRootField;
  private final GraphQLCodeRegistry dataFetchers;

  public GraphQLObjectDefinition(
    GraphQLObjectType graphQLObjectType,
    GraphQLFieldDefinition graphQLRootField,
    GraphQLCodeRegistry dataFetchers
  ) {
    this.graphQLObjectType = graphQLObjectType;
    this.graphQLRootField = graphQLRootField;
    this.dataFetchers = dataFetchers;
  }

  public GraphQLObjectType getGraphQLObjectType() {
    return graphQLObjectType;
  }

  public GraphQLFieldDefinition getGraphQLRootField() {
    return graphQLRootField;
  }

  public GraphQLCodeRegistry getDataFetchers() {
    return dataFetchers;
  }
}
