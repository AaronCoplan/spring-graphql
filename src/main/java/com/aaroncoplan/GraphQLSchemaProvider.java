package com.aaroncoplan;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;

import graphql.GraphQL;
import graphql.schema.*;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GraphQLSchemaProvider {

  private GraphQL graphQL;

  @Bean
  public GraphQL graphQL() {
    return this.graphQL;
  }

  @PostConstruct
  public void init() {
    var bookTypeDefinition = new Book().generateObjectType();
    var bookRootField = new Book().generateRootField();
    var bookDataFetchers = new Book().generateDataFetchers();

    var queryTypeDefinition = GraphQLObjectType
      .newObject()
      .name("Query")
      .field(bookRootField)
      .build();

    var codeRegistry = GraphQLCodeRegistry
      .newCodeRegistry()
      .dataFetchers(bookDataFetchers)
      .build();

    var schema = GraphQLSchema
      .newSchema()
      .query(queryTypeDefinition)
      .additionalType(bookTypeDefinition)
      .codeRegistry(codeRegistry)
      .build();
    this.graphQL = GraphQL.newGraphQL(schema).build();
  }
}
