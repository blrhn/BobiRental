package org.bobirental.common.impl;

import org.bobirental.common.model.BaseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseController<T extends BaseEntity> {
    private final BaseService<T> baseService;

    public BaseController(BaseService<T> baseService) {
        this.baseService = baseService;
    }

    @GetMapping
    public List<T> getAllEntities() {
        return baseService.findAllEntities();
    }

    @GetMapping("/{id}")
    public T getEntity(@PathVariable Integer id) {
        return baseService.findEntityById(id);
    }

    @PostMapping
    public T createEntity(@RequestBody T entity) {
        return baseService.saveEntity(entity);
    }

    @PutMapping("/{id}")
    public T updateEntity(@RequestBody T entity,  @PathVariable Integer id) {
        return baseService.updateEntity(entity);
    }

    @DeleteMapping("/{id}")
    public void deleteEntity(@PathVariable Integer id) {
        baseService.deleteEntityById(id);
    }

}
