package com.aaroncoplan;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;

import graphql.GraphQL;
import graphql.schema.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;
import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

@Component
public class GraphQLSchemaProvider {

  private GraphQL graphQL;

  @Bean
  public GraphQL graphQL() {
    return this.graphQL;
  }

  private void generateGraphQLObjectDefinitions()
    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
      false
    );
    provider.addIncludeFilter(new AssignableTypeFilter(GraphQLObject.class));

    var results = provider.findCandidateComponents("com.aaroncoplan");
    for (var result : results) {
      var objectClass = (Class<? extends GraphQLObject>) Class.forName(
        result.getBeanClassName()
      );
      var objectInstance = objectClass.getConstructor().newInstance();

      var graphQLObjectType = objectInstance.generateObjectType();
      var graphQLRootField = objectInstance.generateRootField();
      var graphQLDataFetchers = objectInstance.generateDataFetchers();
    }
  }

  @PostConstruct
  public void init()
    throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    generateGraphQLObjectDefinitions();

    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
      false
    );
    provider.addIncludeFilter(new AssignableTypeFilter(GraphQLObject.class));

    var results = provider.findCandidateComponents("com.aaroncoplan");
    for (var result : results) {
      var objectClass = Class.forName(result.getBeanClassName());
      System.out.println(objectClass);
    }

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
