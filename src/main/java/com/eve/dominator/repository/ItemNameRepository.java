package com.eve.dominator.repository;

import com.eve.dominator.model.ItemName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemNameRepository extends JpaRepository<ItemName, Integer> {

    @Query("SELECT i FROM ItemName i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ItemName> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    Optional<ItemName> findByTypeId(Integer typeId);

    @Query("SELECT COUNT(i) FROM ItemName i")
    long countAll();
}
