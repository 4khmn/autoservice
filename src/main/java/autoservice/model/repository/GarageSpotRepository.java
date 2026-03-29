package autoservice.model.repository;

import autoservice.model.entities.GarageSpot;
import org.springframework.stereotype.Repository;

@Repository
public class GarageSpotRepository extends HibernateAbstractDAO<GarageSpot, Long> {
    public GarageSpotRepository() {
        super(GarageSpot.class);
    }

}