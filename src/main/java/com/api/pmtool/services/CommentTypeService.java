package com.api.pmtool.services;

import java.util.List;
import java.util.UUID;

import com.api.pmtool.entity.CommentTypeEntity;

public interface CommentTypeService {
    List<CommentTypeEntity> getAllCommentTypes();

    CommentTypeEntity getCommentTypeById(UUID id);

    CommentTypeEntity createCommentType(CommentTypeEntity commentType);

    CommentTypeEntity updateCommentType(UUID id, CommentTypeEntity commentTypeDetails);

    void deleteCommentType(UUID id);
}
