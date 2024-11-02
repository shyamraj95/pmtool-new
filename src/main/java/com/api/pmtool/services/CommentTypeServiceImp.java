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

    @Override
    public List<CommentTypeEntity> getAllCommentTypes() {
        return commentTypeRepository.findAll();
    }

    @Override
    public CommentTypeEntity getCommentTypeById(UUID id) {
        return commentTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("CommentType not found"));
    }

    @Override
    public CommentTypeEntity createCommentType(CommentTypeEntity commentType) {
        return commentTypeRepository.save(commentType);
    }

    @Override
    public CommentTypeEntity updateCommentType(UUID id, CommentTypeEntity commentTypeDetails) {
        return commentTypeRepository.findById(id).map(commentType -> {
            commentType.setCommentTypeName(commentTypeDetails.getCommentTypeName());
            return commentTypeRepository.save(commentType);
        }).orElseThrow(() -> new RuntimeException("CommentType not found"));
    }

    @Override
    public void deleteCommentType(UUID id) {
        commentTypeRepository.deleteById(id);
    }

}
