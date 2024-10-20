package com.api.pmtool.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.pmtool.entity.ProjectEntity;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID>{

}
