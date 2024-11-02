package com.api.pmtool.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.pmtool.entity.CommentTypeEntity;

public interface CommentTypeRepository extends JpaRepository<CommentTypeEntity, UUID> {

}
