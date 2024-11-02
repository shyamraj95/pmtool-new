package com.api.pmtool.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.api.pmtool.entity.Comments;

public interface CommentRepository extends JpaRepository<Comments, UUID> {

  @Query("SELECT c FROM Comments c LEFT JOIN FETCH c.uploads WHERE c.commentType.id = :commentTypeId AND c.demand.id = :demandId")
  List<Comments> findByTypeIdAndDemandId(@Param("commentTypeId") UUID commentTypeId,
      @Param("demandId") UUID demandId);
}
