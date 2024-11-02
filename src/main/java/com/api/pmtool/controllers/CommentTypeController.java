package com.api.pmtool.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.pmtool.entity.CommentTypeEntity;
import com.api.pmtool.services.CommentTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/comment-types")
public class CommentTypeController {

    @Autowired
    private CommentTypeService commentTypeService;

    @GetMapping
    public List<CommentTypeEntity> getAllCommentTypes() {
        return commentTypeService.getAllCommentTypes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentTypeEntity> getCommentTypeById(@PathVariable UUID id) {
        CommentTypeEntity commentType = commentTypeService.getCommentTypeById(id);
        return ResponseEntity.ok().body(commentType);
    }

    @PostMapping
    public CommentTypeEntity createCommentType(@RequestBody CommentTypeEntity commentType) {
        return commentTypeService.createCommentType(commentType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentTypeEntity> updateCommentType(@PathVariable UUID id,
                                                       @RequestBody CommentTypeEntity commentTypeDetails) {
        try {
            CommentTypeEntity updatedCommentType = commentTypeService.updateCommentType(id, commentTypeDetails);
            return ResponseEntity.ok(updatedCommentType);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommentType(@PathVariable UUID id) {
        commentTypeService.deleteCommentType(id);
        return ResponseEntity.noContent().build();
    }
}
