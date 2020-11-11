package com.aaroncoplan.springgraphql;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;

import graphql.schema.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GraphQLTypeUtils {

  public static List<GraphQLInputObjectField> inputFieldsForInputType(
    Method method
  ) {
    var inputParameterType = method.getParameterTypes()[0];

    return List
      .of(inputParameterType.getDeclaredFields())
      .stream()
      .map(
        field -> {
          var annotation = field.getAnnotation(GraphQLInputField.class);
          if (annotation == null) {
            return null;
          }

          var name = field.getName();

          switch (annotation.type()) {
            case ID:
              return GraphQLInputObjectField
                .newInputObjectField()
                .name(name)
                .type(GraphQLID)
                .build();
            case STRING:
              return GraphQLInputObjectField
                .newInputObjectField()
                .name(name)
                .type(GraphQLString)
                .build();
          }

          throw new RuntimeException("Unsupported Input Field Type");
        }
      )
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  public static GraphQLOutputType typeForFieldType(
    Method method,
    FieldType fieldType
  ) {
    switch (fieldType) {
      case ID:
        return GraphQLID;
      case STRING:
        return GraphQLString;
      case OBJECT:
        {
          var returnTypeClass = method.getReturnType();
          return typeReferenceForReturnTypeClass(returnTypeClass);
        }
      case LIST_OF_OBJECTS:
        {
          Type returnType = method.getGenericReturnType();
          ParameterizedType parameterizedType = (ParameterizedType) returnType;
          var returnTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];

          var typeReference = typeReferenceForReturnTypeClass(returnTypeClass);
          return GraphQLList.list(typeReference);
        }
    }

    throw new RuntimeException("Unrecognized Field Type");
  }

  private static GraphQLTypeReference typeReferenceForReturnTypeClass(
    Class<?> returnTypeClass
  ) {
    if (GraphQLObject.class.isAssignableFrom(returnTypeClass)) {
      try {
        var objectInstance =
          ((Class<? extends GraphQLObject>) returnTypeClass).getConstructor()
            .newInstance();
        return GraphQLTypeReference.typeRef(objectInstance.getName());
      } catch (
        InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e
      ) {
        throw new RuntimeException("Unknown Object type");
      }
    } else {
      throw new RuntimeException("Unknown Object type");
    }
  }
}
