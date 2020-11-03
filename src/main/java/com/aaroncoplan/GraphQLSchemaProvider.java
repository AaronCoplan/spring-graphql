package com.aaroncoplan;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;

import graphql.GraphQL;
import graphql.schema.*;
import java.util.HashMap;
import java.util.Map;
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
    var bookTypeDefinition = GraphQLObjectType
      .newObject()
      .name("Book")
      .field(
        GraphQLFieldDefinition
          .newFieldDefinition()
          .name("id")
          .type(GraphQLNonNull.nonNull(GraphQLID))
      )
      .field(
        GraphQLFieldDefinition
          .newFieldDefinition()
          .name("name")
          .type(GraphQLString)
          .build()
      )
      .field(
        GraphQLFieldDefinition
          .newFieldDefinition()
          .name("author")
          .type(GraphQLTypeReference.typeRef("Author"))
          .build()
      )
      .build();

    var authorTypeDefinition = GraphQLObjectType
      .newObject()
      .name("Author")
      .field(
        GraphQLFieldDefinition
          .newFieldDefinition()
          .name("id")
          .type(GraphQLNonNull.nonNull(GraphQLID))
          .build()
      )
      .field(
        GraphQLFieldDefinition
          .newFieldDefinition()
          .name("name")
          .type(GraphQLString)
          .build()
      )
      .build();

    var queryTypeDefinition = GraphQLObjectType
      .newObject()
      .name("Query")
      .field(
        GraphQLFieldDefinition
          .newFieldDefinition()
          .name("books")
          .type(
            GraphQLNonNull.nonNull(
              GraphQLList.list(GraphQLTypeReference.typeRef("Book"))
            )
          )
          .build()
      )
      .field(
        GraphQLFieldDefinition
          .newFieldDefinition()
          .name("authors")
          .type(
            GraphQLNonNull.nonNull(
              GraphQLList.list(GraphQLTypeReference.typeRef("Author"))
            )
          )
          .build()
      )
      .build();

    var codeRegistry = GraphQLCodeRegistry
      .newCodeRegistry()
      .dataFetcher(
        FieldCoordinates.coordinates("Book", "author"),
        (DataFetchingEnvironment dataFetchingEnvironment) -> {
          var source = (Map<String, Object>) dataFetchingEnvironment.getSource();
          var id = (Integer) source.get("id");
          if (id != 1) {
            return null;
          }

          var data = new HashMap<String, Object>();
          data.put("id", 2);
          data.put("name", "Suzanne Collins");
          return data;
        }
      )
      .dataFetcher(
        FieldCoordinates.coordinates("Query", "books"),
        (DataFetchingEnvironment dataFetchingEnvironment) -> {
          var data = new HashMap<String, Object>();
          data.put("id", 1);
          data.put("name", "Hunger Games");
          return new Object[] { data };
        }
      )
      .dataFetcher(
        FieldCoordinates.coordinates("Query", "authors"),
        (DataFetchingEnvironment dataFetchingEnvironment) -> {
          var data = new HashMap<String, Object>();
          data.put("id", 2);
          data.put("name", "Suzanne Collins");
          return new Object[] { data };
        }
      )
      .build();

    var schema = GraphQLSchema
      .newSchema()
      .query(queryTypeDefinition)
      .additionalType(bookTypeDefinition)
      .additionalType(authorTypeDefinition)
      .codeRegistry(codeRegistry)
      .build();
    this.graphQL = GraphQL.newGraphQL(schema).build();
  }
}
