package com.api.pmtool.services;

import java.util.List;
import java.util.UUID;

import com.api.pmtool.dtos.ProjectDto;
import com.api.pmtool.entity.ProjectEntity;

public interface ProjectService {

    ProjectEntity createProject(ProjectDto project);

    ProjectEntity getProjectById(UUID id);

    ProjectEntity updateProject(UUID id, ProjectEntity projectDetails);

    void deleteProject(UUID id);

    List<ProjectEntity> getAllProjects();

}
