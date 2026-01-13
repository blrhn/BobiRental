package org.bobirental.tool;

import org.bobirental.common.impl.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolEventRepository extends BaseRepository<ToolEvent> {
    @Query("SELECT te FROM ToolEvent te WHERE te.tool.id = :toolId ORDER BY te.eventDate DESC")
    List<ToolEvent> findToolEventByToolIdDesc(@Param("toolId") Integer toolId);

    @Query("SELECT te FROM ToolEvent te WHERE te.employee.id = :employeeId ORDER BY te.eventDate DESC")
    List<ToolEvent> findToolEventByEmployeeIdDesc(@Param("employeeId") Integer employeeId);
}
