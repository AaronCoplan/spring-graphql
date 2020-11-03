package com.aaroncoplan;

public interface Repository<T> {
  T findByID(long id);
}
