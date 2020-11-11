package com.aaroncoplan.springgraphql;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

public class GraphQLMutationDefinition {

  private final GraphQLFieldDefinition graphQLMutation;
  private final GraphQLCodeRegistry dataFetchers;

  public GraphQLMutationDefinition(
    GraphQLFieldDefinition graphQLMutation,
    GraphQLCodeRegistry dataFetchers
  ) {
    this.graphQLMutation = graphQLMutation;
    this.dataFetchers = dataFetchers;
  }

  public GraphQLFieldDefinition getGraphQLMutation() {
    return graphQLMutation;
  }

  public GraphQLCodeRegistry getDataFetchers() {
    return dataFetchers;
  }
}
