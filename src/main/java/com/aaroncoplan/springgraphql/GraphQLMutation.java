package com.aaroncoplan.springgraphql;

import graphql.schema.*;
import java.util.List;
import java.util.Map;

public abstract class GraphQLMutation<Input, Output> {

  protected abstract String getName();

  protected abstract FieldType getFieldType();

  protected abstract Output execute(Input input);

  public final GraphQLFieldDefinition generateMutationField() {
    var executeMethod = List
      .of(this.getClass().getDeclaredMethods())
      .stream()
      .filter(method -> "execute".equals(method.getName()))
      .findFirst()
      .get();

    var inputFields = GraphQLTypeUtils.inputFieldsForInputType(executeMethod);

    var inputType = GraphQLInputObjectType
      .newInputObject()
      .name(this.getName() + "Input")
      .fields(inputFields)
      .build();

    var argument = GraphQLArgument
      .newArgument()
      .name("input")
      .type(GraphQLNonNull.nonNull(inputType))
      .build();

    return GraphQLFieldDefinition
      .newFieldDefinition()
      .name(this.getName())
      .argument(argument)
      .type(
        GraphQLTypeUtils.typeForFieldType(executeMethod, this.getFieldType())
      )
      .build();
  }

  public final GraphQLCodeRegistry generateDataFetchers() {
    var executeMethod = List
      .of(this.getClass().getDeclaredMethods())
      .stream()
      .filter(method -> "execute".equals(method.getName()))
      .findFirst()
      .get();

    var inputParameterType = executeMethod.getParameterTypes()[0];

    return GraphQLCodeRegistry
      .newCodeRegistry()
      .dataFetcher(
        FieldCoordinates.coordinates("Mutation", this.getName()),
        (DataFetchingEnvironment dataFetchingEnvironment) -> {
          Map<String, Object> inputMap = dataFetchingEnvironment.getArgument(
            "input"
          );

          Input inputObject = (Input) inputParameterType
            .getConstructor()
            .newInstance();

          for (var field : inputParameterType.getDeclaredFields()) {
            var annotation = field.getAnnotation(GraphQLInputField.class);
            if (annotation == null) {
              continue;
            }

            var name = field.getName();
            var inputMapValue = inputMap.get(name);

            field.set(inputObject, inputMapValue);
          }

          return this.execute(inputObject);
        }
      )
      .build();
  }
}
