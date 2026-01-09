package com.test.repository;

import com.test.model.ClassificationPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassificationRepo extends JpaRepository<ClassificationPlan, Long> {
    boolean existsByName(String name);
}
