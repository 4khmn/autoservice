package autoservice.model.repository;

import autoservice.model.entities.Master;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class MasterRepository extends HibernateAbstractDAO<Master, Long> {

    public MasterRepository() {
        super(Master.class);
    }

    public List<Master> mastersSortByName() {
        String hql = "FROM Master m ORDER BY m.name";
        return getSession().createQuery(hql, Master.class).getResultList();
    }


}