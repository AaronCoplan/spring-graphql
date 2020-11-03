package com.aaroncoplan;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;

import graphql.GraphQL;
import graphql.schema.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
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

  private List<GraphQLObjectDefinition> generateGraphQLObjectDefinitions()
    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
      false
    );
    provider.addIncludeFilter(new AssignableTypeFilter(GraphQLObject.class));

    var objectDefinitions = new ArrayList<GraphQLObjectDefinition>();

    var results = provider.findCandidateComponents("com.aaroncoplan");
    for (var result : results) {
      var objectClass = (Class<? extends GraphQLObject>) Class.forName(
        result.getBeanClassName()
      );
      var objectInstance = objectClass.getConstructor().newInstance();

      var graphQLObjectType = objectInstance.generateObjectType();
      var graphQLRootField = objectInstance.generateRootField();
      var graphQLDataFetchers = objectInstance.generateDataFetchers();

      objectDefinitions.add(
        new GraphQLObjectDefinition(
          graphQLObjectType,
          graphQLRootField,
          graphQLDataFetchers
        )
      );
    }

    return objectDefinitions;
  }

  @PostConstruct
  public void init()
    throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    var graphQLObjectDefinitions = generateGraphQLObjectDefinitions();

    var queryTypeDefinition = GraphQLObjectType
      .newObject()
      .name("Query")
      .fields(
        graphQLObjectDefinitions
          .stream()
          .map(GraphQLObjectDefinition::getGraphQLRootField)
          .collect(Collectors.toList())
      )
      .build();

    GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

    for (var objectDefinition : graphQLObjectDefinitions) {
      codeRegistryBuilder =
        codeRegistryBuilder.dataFetchers(objectDefinition.getDataFetchers());
    }

    var schema = GraphQLSchema
      .newSchema()
      .query(queryTypeDefinition)
      .additionalTypes(
        graphQLObjectDefinitions
          .stream()
          .map(GraphQLObjectDefinition::getGraphQLObjectType)
          .collect(Collectors.toSet())
      )
      .codeRegistry(codeRegistryBuilder.build())
      .build();

    this.graphQL = GraphQL.newGraphQL(schema).build();
  }
}
