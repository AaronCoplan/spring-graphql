package com.aaroncoplan;

import graphql.ExecutionInput;
import graphql.GraphQL;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GraphQLController {

  @Autowired
  private GraphQL graphQL;

  @PostMapping("/graphql")
  @ResponseBody
  public Map<String, Object> executeOperation(
    @RequestBody Map<String, Object> body
  ) {
    String query = (String) body.get("query");
    var castedVariables = (Map<String, Object>) body.get("variables");
    var variables = castedVariables != null
      ? castedVariables
      : new LinkedHashMap<String, Object>();

    var executionInput = ExecutionInput
      .newExecutionInput(query)
      .variables(variables);
    var executionResult = graphQL.execute(executionInput);

    var result = new LinkedHashMap<String, Object>();
    result.put("data", executionResult.getData());

    if (!executionResult.getErrors().isEmpty()) {
      result.put("errors", executionResult.getErrors());
    }

    return result;
  }
}
