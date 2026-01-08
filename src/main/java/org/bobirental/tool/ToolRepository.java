package org.bobirental.tool;

import org.bobirental.common.impl.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolRepository extends BaseRepository<Tool> {
    @Query("SELECT t FROM Tool t WHERE t.id = :id AND t.toolAvailabilityStatus = 'AVAILABLE' ")
    Tool checkIfAvailableById(@Param("id") Integer id);

    @Query("SELECT t FROM Tool t WHERE t.toolAvailabilityStatus = 'AVAILABLE' " +
            "AND t.toolCategory = :category")
    List<Tool> findAvailableToolsByCategory(@Param("category") ToolCategory toolCategory);
}
