package com.aaroncoplan.springgraphql;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.repository.CrudRepository;

public class RepositoryCache {

  private static RepositoryCache instance;
  private static Map<String, CrudRepository> cache;

  private RepositoryCache() {
    cache = new HashMap<>();
  }

  public static RepositoryCache getInstance() {
    if (instance == null) {
      instance = new RepositoryCache();
    }
    return instance;
  }

  public void insert(String objectName, CrudRepository repository) {
    cache.put(objectName, repository);
  }

  public CrudRepository lookup(String objectName) {
    return cache.get(objectName);
  }

  @Override
  public String toString() {
    return cache
      .keySet()
      .stream()
      .map(
        key -> {
          var value = lookup(key);
          return key + " ==> " + value;
        }
      )
      .collect(Collectors.joining());
  }
}
