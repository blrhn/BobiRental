package org.bobirental.common.impl;

import org.bobirental.common.model.BaseEntity;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T extends BaseEntity> {
    private final BaseRepository<T> baseRepository;

    public BaseService(BaseRepository<T> baseRepository) {
        this.baseRepository = baseRepository;
    }

    public List<T> findAllEntities() {
        return baseRepository.findAll();
    }

    public T findEntityById(Integer id) {
        Optional<T> optionalClient = baseRepository.findById(id);

        return optionalClient.orElseThrow(
                () -> new IllegalArgumentException("Entity with id " + id + " does not exist"));

    }

    public T saveEntity(T entity) {
        return baseRepository.save(entity);
    }

    public T updateEntity(T entity) {
        if (baseRepository.existsById(entity.getId())) {
            return baseRepository.save(entity);
        }

        throw new IllegalArgumentException("Entity with id " + entity.getId() + " does not exist");
    }

    public void deleteEntityById(Integer id) {
        if (baseRepository.existsById(id)) {
            baseRepository.deleteById(id);
        }

        throw new IllegalArgumentException("Entity with id " + id + " does not exist");
    }
}
