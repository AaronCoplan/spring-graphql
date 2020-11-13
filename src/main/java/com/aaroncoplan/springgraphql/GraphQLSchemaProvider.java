package com.aaroncoplan.springgraphql;

import graphql.GraphQL;
import graphql.schema.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

@Component
public class GraphQLSchemaProvider {

  private GraphQL graphQL;

  private GraphQLSchema schema;

  @Value("${springgraphql.basePackage}")
  private String basePackage;

  @Bean
  public GraphQL graphQL() {
    return this.graphQL;
  }

  @Bean
  public GraphQLSchema graphQLSchema() {
    return this.schema;
  }

  @Autowired
  private ApplicationContext applicationContext;

  private Set<BeanDefinition> findExtensionsOfClass(
    ApplicationContext applicationContext,
    Class<?> targetClass,
    String basePackage
  ) {
    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
      false
    );
    provider.addIncludeFilter(new AssignableTypeFilter(targetClass));

    return provider.findCandidateComponents(basePackage);
  }

  private List<GraphQLMutationDefinition> generateGraphQLMutationDefinitions(
    ApplicationContext applicationContext
  )
    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    var graphQLMutationDefinitionClasses = findExtensionsOfClass(
      applicationContext,
      GraphQLMutation.class,
      basePackage
    );

    var mutationDefinitions = new ArrayList<GraphQLMutationDefinition>();

    for (var result : graphQLMutationDefinitionClasses) {
      var mutationClass = (Class<? extends GraphQLMutation>) Class.forName(
        result.getBeanClassName()
      );
      var mutationInstance = mutationClass.getConstructor().newInstance();

      var mutationDefinition = mutationInstance.generateMutationField();
      var dataFetchers = mutationInstance.generateDataFetchers();
      mutationDefinitions.add(
        new GraphQLMutationDefinition(mutationDefinition, dataFetchers)
      );
    }

    return mutationDefinitions;
  }

  private List<GraphQLQueryDefinition> generateGraphQLQueryDefinitions(
    ApplicationContext applicationContext
  )
    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    var graphQLQueryDefinitionClasses = findExtensionsOfClass(
      applicationContext,
      GraphQLQuery.class,
      basePackage
    );

    var queryDefinitions = new ArrayList<GraphQLQueryDefinition>();

    for (var result : graphQLQueryDefinitionClasses) {
      var queryClass = (Class<? extends GraphQLQuery>) Class.forName(
        result.getBeanClassName()
      );
      var queryInstance = queryClass.getConstructor().newInstance();

      var queryDefinition = queryInstance.generateQueryField();
      var dataFetchers = queryInstance.generateDataFetchers();
      queryDefinitions.add(
        new GraphQLQueryDefinition(queryDefinition, dataFetchers)
      );
    }

    return queryDefinitions;
  }

  private List<GraphQLObjectDefinition> generateGraphQLObjectDefinitions(
    ApplicationContext applicationContext
  )
    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    var graphQLObjectDefinitionClasses = findExtensionsOfClass(
      applicationContext,
      GraphQLObject.class,
      basePackage
    );

    var objectDefinitions = new ArrayList<GraphQLObjectDefinition>();
    var repositoryCache = RepositoryCache.getInstance();

    for (var result : graphQLObjectDefinitionClasses) {
      var objectClass = (Class<? extends GraphQLObject>) Class.forName(
        result.getBeanClassName()
      );
      var objectInstance = objectClass.getConstructor().newInstance();

      var repositoryClass = objectInstance.getRepository();
      var repositoryObject = repositoryClass != null
        ? applicationContext.getBean(repositoryClass)
        : null;
      if (repositoryObject != null) {
        repositoryCache.insert(objectInstance.getName(), repositoryObject);
      }

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
    var graphQLObjectDefinitions = generateGraphQLObjectDefinitions(
      applicationContext
    );

    var graphQLQueryDefinitions = generateGraphQLQueryDefinitions(
      applicationContext
    );

    var graphQLMutationDefinitions = generateGraphQLMutationDefinitions(
      applicationContext
    );

    var queryTypeDefinition = GraphQLObjectType
      .newObject()
      .name("Query")
      .fields(
        graphQLObjectDefinitions
          .stream()
          .map(GraphQLObjectDefinition::getGraphQLRootField)
          .collect(Collectors.toList())
      )
      .fields(
        graphQLQueryDefinitions
          .stream()
          .map(GraphQLQueryDefinition::getGraphQLQuery)
          .collect(Collectors.toList())
      )
      .build();

    var mutationTypeDefinition = GraphQLObjectType
      .newObject()
      .name("Mutation")
      .fields(
        graphQLMutationDefinitions
          .stream()
          .map(GraphQLMutationDefinition::getGraphQLMutation)
          .collect(Collectors.toList())
      )
      .build();

    GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

    for (var objectDefinition : graphQLObjectDefinitions) {
      codeRegistryBuilder =
        codeRegistryBuilder.dataFetchers(objectDefinition.getDataFetchers());
    }

    for (var queryDefinition : graphQLQueryDefinitions) {
      codeRegistryBuilder =
        codeRegistryBuilder.dataFetchers(queryDefinition.getDataFetchers());
    }

    for (var mutationDefinition : graphQLMutationDefinitions) {
      codeRegistryBuilder =
        codeRegistryBuilder.dataFetchers(mutationDefinition.getDataFetchers());
    }

    this.schema = GraphQLSchema
      .newSchema()
      .query(queryTypeDefinition)
      .mutation(mutationTypeDefinition)
      .additionalTypes(
        graphQLObjectDefinitions
          .stream()
          .map(GraphQLObjectDefinition::getGraphQLObjectType)
          .collect(Collectors.toSet())
      )
      .codeRegistry(codeRegistryBuilder.build())
      .build();

    this.graphQL = GraphQL.newGraphQL(this.schema).build();
  }
}
