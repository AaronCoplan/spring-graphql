package com.aaroncoplan;

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeRuntimeWiring;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Component
public class GraphQLSchemaProvider {

    private GraphQL graphQL;

    @Bean
    public GraphQL graphQL() {
        return this.graphQL;
    }

    @PostConstruct
    public void init() {
        var bookTypeDefinition = "type Book { id: ID! \n name: String \n author: Author}";
        var authorTypeDefinition = "type Author {id: ID! \n name: String}";
        var queryTypeDefinition = "type Query { books: [Book!]! }";

        var schemaDefinitionLanguage = bookTypeDefinition + authorTypeDefinition + queryTypeDefinition;
        this.graphQL = GraphQL.newGraphQL(buildSchema(schemaDefinitionLanguage)).build();
    }

    private GraphQLSchema buildSchema(String schemaDefinitionLanguage) {
        var typeRegistry = new SchemaParser().parse(schemaDefinitionLanguage);
        var runtimeWiring = buildWiring();

        return new SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("books", (DataFetchingEnvironment dataFetchingEnvironment) -> {
                            var data = new HashMap<String, Object>();
                            data.put("id", 1);
                            data.put("name", "Harry Potter");
                            return new Object[]{data};
                        })).build();
    }
}
