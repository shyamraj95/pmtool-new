package com.api.pmtool.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.pmtool.dtos.ProjectDto;
import com.api.pmtool.entity.ProjectEntity;
import com.api.pmtool.exception.ResourceNotFoundException;
import com.api.pmtool.repository.ProjectRepository;

@Service
public class ProjectServiceImp implements ProjectService {

@Autowired
    private ProjectRepository projectRepository;
    
    @Override
    public ProjectEntity createProject(ProjectDto project) {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setProjectName(project.getProjectName());
        projectEntity.setDescription(project.getDescription());
        return projectRepository.save(projectEntity);
    }
    @Override
    public ProjectEntity getProjectById(UUID id) {
        return projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id, null, id));
    }
    @Override
    public ProjectEntity updateProject(UUID id, ProjectEntity projectDetails) {
        ProjectEntity project = getProjectById(id);
        project.setProjectName(projectDetails.getProjectName());
        project.setDescription(projectDetails.getDescription());
        return projectRepository.save(project);
    }
    @Override
    public void deleteProject(UUID id) {
        projectRepository.deleteById(id);
    }
    @Override
    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }
}
