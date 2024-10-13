package com.api.pmtool.entity;


import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.api.pmtool.enums.CommentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
@Table(name = "comments")
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
    @JsonIgnoreProperties("demand")
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Uploads> uploads; // One comment has many file uploads
    
    @Enumerated(EnumType.STRING)
    private CommentType type;
}
