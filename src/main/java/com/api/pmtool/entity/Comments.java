package com.api.pmtool.entity;


import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Index;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "comments", 
indexes = {
    @Index(name = "idx_comments_demand_id", columnList = "demand_id"),
    @Index(name = "idx_comments_task_id", columnList = "task_id"),
    @Index(name = "idx_comments_comment_type_id", columnList = "comment_type_id")
})
public class Comments extends Auditable<UUID>{

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition="UUID")
    private UUID id;

    //@JsonIgnoreProperties("comments")  // Prevent recursion without blocking uploads
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "demand_id", nullable = false)
    private Demand demand; // Each comment belongs to one demand
    
    private String comment;

    //@JsonBackReference // Prevents infinite recursion in JSON serialization
    @JsonIgnoreProperties({"demand", "task"})
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Uploads> uploads; // One comment has many file uploads
    
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = true)
    private TasksEntity task; // Nullable, can be associated with a task

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comment_type_id", nullable = false)
 //   @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CommentTypeEntity commentType;
}
