package com.api.pmtool.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "comment_type")
@Getter
@Setter
public class CommentTypeEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "comment_type_name", unique = true, nullable = false)
    private String commentTypeName;

    @Column(name = "can_upload_multiple_files", unique = false, nullable = false, length = 1)
    private String canUploadMultipleFiles;
}
