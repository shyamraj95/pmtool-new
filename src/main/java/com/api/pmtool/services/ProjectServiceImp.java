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
    
/**
 * Creates a new project based on the provided ProjectDto.
 *
 * This method initializes a ProjectEntity with data from the given ProjectDto
 * and saves it to the repository.
 *
 * @param project The Project Data Transfer Object containing project details.
 * @return The persisted ProjectEntity.
 */

    @Override
    public ProjectEntity createProject(ProjectDto project) {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setProjectName(project.getProjectName());
        projectEntity.setDescription(project.getDescription());
        return projectRepository.save(projectEntity);
    }
    /**
     * Retrieves a Project entity from the database by its ID.
     *
     * @param id The ID of the Project to be retrieved.
     * @return The Project entity.
     * @throws ResourceNotFoundException If the Project does not exist with the given ID.
     */
    @Override
    public ProjectEntity getProjectById(UUID id) {
        return projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id, null, id));
    }
    /**
     * Updates a Project entity with the given details.
     *
     * @param id The ID of the Project to be updated.
     * @param projectDetails The Project entity containing the updated details.
     * @return The updated Project entity.
     * @throws ResourceNotFoundException If the Project does not exist with the given ID.
     */
    @Override
    public ProjectEntity updateProject(UUID id, ProjectEntity projectDetails) {
        ProjectEntity project = getProjectById(id);
        project.setProjectName(projectDetails.getProjectName());
        project.setDescription(projectDetails.getDescription());
        return projectRepository.save(project);
    }
    /**
     * Deletes a Project entity by its ID.
     *
     * @param id The ID of the Project to be deleted.
     * @throws ResourceNotFoundException If the Project does not exist with the given ID.
     */
    @Override
    public void deleteProject(UUID id) {
        projectRepository.deleteById(id);
    }
    /**
     * Retrieves a list of all projects from the database.
     *
     * @return A list of Project entities.
     */
    @Override
    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }
}
