package com.api.pmtool.repository;


import java.util.UUID;


import org.springframework.data.jpa.repository.JpaRepository;


import com.api.pmtool.entity.Comments;

public interface CommentRepository extends JpaRepository<Comments, UUID> {
  // @EntityGraph(attributePaths = "uploads")
/*     @Query("SELECT c FROM Comments c LEFT JOIN FETCH c.uploads WHERE c.demand.id = :demandId")
    List<Comments> findByDemandId(UUID demandId); */
}
