package com.winestoreapp.wine.repository;

import com.winestoreapp.wine.model.Wine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WineRepository extends JpaRepository<Wine, Long> {
}
