package com.aaroncoplan;

import graphql.GraphQL;
import graphql.schema.*;
import graphql.schema.idl.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;

@Component
public class GraphQLSchemaProvider {

    private GraphQL graphQL;

    @Bean
    public GraphQL graphQL() {
        return this.graphQL;
    }

    @PostConstruct
    public void init() {
        var bookTypeDefinition = GraphQLObjectType.newObject().name("Book")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("id").type(GraphQLID))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("name").type(GraphQLString).build())
                .field(GraphQLFieldDefinition.newFieldDefinition().name("author").type(GraphQLTypeReference.typeRef("Author")).build())
                .build();

        var authorTypeDefinition = GraphQLObjectType.newObject().name("Author")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("id").type(GraphQLID).build())
                .field(GraphQLFieldDefinition.newFieldDefinition().name("name").type(GraphQLString).build())
                .build();

        var queryTypeDefinition = GraphQLObjectType.newObject().name("Query")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("books")
                        .type(GraphQLList.list(GraphQLTypeReference.typeRef("Book"))).build()).build();

        var codeRegistry = GraphQLCodeRegistry.newCodeRegistry().dataFetcher(FieldCoordinates.coordinates("Query", "books"), (DataFetchingEnvironment dataFetchingEnvironment) -> {
            var data = new HashMap<String, Object>();
            data.put("id", 1);
            data.put("name", "Harry Potter");
            return new Object[]{data};
        }).build();

        var schema = GraphQLSchema.newSchema().query(queryTypeDefinition).additionalType(bookTypeDefinition).additionalType(authorTypeDefinition).codeRegistry(codeRegistry).build();
        this.graphQL = GraphQL.newGraphQL(schema).build();
    }
}
