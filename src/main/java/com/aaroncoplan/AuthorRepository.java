package com.aaroncoplan;

import org.springframework.data.repository.CrudRepository;

public interface AuthorRepository
  extends CrudRepository<Author, Long>, Repository<Author> {}
