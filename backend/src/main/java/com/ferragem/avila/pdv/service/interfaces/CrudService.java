package com.ferragem.avila.pdv.service.interfaces;

import java.util.List;

public interface CrudService<T, DTO> {
    List<T> getAll();

    T getById(long id);

    T save(DTO dto);

    T update(long id, DTO dto);

    void delete(long id);
}
