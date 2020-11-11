package com.aaroncoplan.springgraphql;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;

public class GraphQLQueryDefinition {

  private final GraphQLFieldDefinition graphQLQuery;
  private final GraphQLCodeRegistry dataFetchers;

  public GraphQLQueryDefinition(
    GraphQLFieldDefinition graphQLQuery,
    GraphQLCodeRegistry dataFetchers
  ) {
    this.graphQLQuery = graphQLQuery;
    this.dataFetchers = dataFetchers;
  }

  public GraphQLFieldDefinition getGraphQLQuery() {
    return graphQLQuery;
  }

  public GraphQLCodeRegistry getDataFetchers() {
    return dataFetchers;
  }
}
