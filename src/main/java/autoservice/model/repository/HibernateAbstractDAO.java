package autoservice.model.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public abstract class HibernateAbstractDAO<T, PK extends Serializable> implements GenericDAO<T, PK> {
    protected Class<T> type;

    @Autowired
    protected SessionFactory sessionFactory;

    protected HibernateAbstractDAO(Class<T> type) {
        this.type = type;
    }

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public PK save(T entity) {
        return (PK) getSession().save(entity);
    }

    @Override
    public Optional<T> findById(PK id) {
        return Optional.ofNullable(getSession().get(type, id));
    }

    @Override
    public void delete(PK id) {
        findById(id).ifPresent(entity -> getSession().delete(entity));
    }

    @Override
    public List<T> findAll() {
        return getSession()
                .createQuery("from " + type.getName(), type)
                .getResultList();
    }

    @Override
    public void update(T entity) {
        getSession().update(entity);
    }

    @Override
    public Long count() {
        return getSession().createQuery("select count(*) from " + type.getName(), Long.class).getSingleResult();
    }
}