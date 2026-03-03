package com.coloranalysisbackend.repository;

import com.coloranalysisbackend.model.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, String> {
}
