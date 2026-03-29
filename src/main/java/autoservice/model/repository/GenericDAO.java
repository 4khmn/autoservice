package autoservice.model.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface GenericDAO <T, PK extends Serializable> {

    PK save(T entity);

    Optional<T> findById(PK id);

    void delete(PK id);

    List<T> findAll();

    void update(T entity);

    Long count();



}
