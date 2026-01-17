package org.bobirental.common.impl;

import org.bobirental.common.model.BaseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseController<T extends BaseEntity> {
    private final BaseService<T> baseService;

    public BaseController(BaseService<T> baseService) {
        this.baseService = baseService;
    }

    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @GetMapping
    public List<T> getAllEntities() {
        return baseService.findAllEntities();
    }

    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @GetMapping("/{id}")
    public T getEntity(@PathVariable Integer id) {
        return baseService.findEntityById(id);
    }

    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @PostMapping
    public T createEntity(@RequestBody T entity) {
        return baseService.saveEntity(entity);
    }

    @PreAuthorize("hasAnyRole('REGULAR_EMPLOYEE', 'WAREHOUSE_MANAGER')")
    @PutMapping("/{id}")
    public T updateEntity(@RequestBody T entity,  @PathVariable Integer id) {
        return baseService.updateEntity(entity);
    }

    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @DeleteMapping("/{id}")
    public void deleteEntity(@PathVariable Integer id) {
        baseService.deleteEntityById(id);
    }

}
