package com.api.pmtool.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.pmtool.entity.CommentTypeEntity;
import com.api.pmtool.repository.CommentTypeRepository;
@Service
public class CommentTypeServiceImp implements CommentTypeService {

    @Autowired
    private CommentTypeRepository commentTypeRepository;

    /**
     * Gets all comment types.
     * 
     * @return A list of all comment types.
     */
    @Override
    public List<CommentTypeEntity> getAllCommentTypes() {
        return commentTypeRepository.findAll();
    }

    /**
     * Gets a comment type by its ID.
     * 
     * @param id The ID of the comment type to retrieve.
     * @return The comment type with the given ID, or a RuntimeException if no
     *         comment type with the given ID was found.
     */
    @Override
    public CommentTypeEntity getCommentTypeById(UUID id) {
        return commentTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("CommentType not found"));
    }

    /**
     * Creates a new comment type.
     * 
     * @param commentType The comment type to create.
     * @return The newly created comment type.
     */
    @Override
    public CommentTypeEntity createCommentType(CommentTypeEntity commentType) {
        return commentTypeRepository.save(commentType);
    }

    /**
     * Updates an existing comment type.
     * 
     * @param id The ID of the comment type to update.
     * @param commentTypeDetails The comment type details to update with.
     * @return The updated comment type.
     * @throws RuntimeException If a comment type with the given ID was not found.
     */
    @Override
    public CommentTypeEntity updateCommentType(UUID id, CommentTypeEntity commentTypeDetails) {
        return commentTypeRepository.findById(id).map(commentType -> {
            commentType.setCommentTypeName(commentTypeDetails.getCommentTypeName());
            return commentTypeRepository.save(commentType);
        }).orElseThrow(() -> new RuntimeException("CommentType not found"));
    }

    /**
     * Deletes a comment type by its ID.
     * 
     * @param id The ID of the comment type to delete.
     */

    @Override
    public void deleteCommentType(UUID id) {
        commentTypeRepository.deleteById(id);
    }

}
