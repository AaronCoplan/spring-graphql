package com.aaroncoplan.springgraphql;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GraphQLTypeUtils {

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

    return null;
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
