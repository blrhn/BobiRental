package org.bobirental.common.impl;

import org.bobirental.common.model.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Integer> {
}
