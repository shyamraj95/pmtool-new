package com.api.pmtool.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonBackReference;


import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
public class Uploads extends Auditable<UUID> {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition="UUID")
    private UUID id; // UUID for unique identification

    //@JsonIgnoreProperties("uploads")  // Prevent infinite recursion by ignoring the 'uploads' field of the comment
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false) // Specify the foreign key column for the relationship
    private Comments comment;  // This establishes the many-to-one relationship

    private String fileName;
    private String filePath;
}
