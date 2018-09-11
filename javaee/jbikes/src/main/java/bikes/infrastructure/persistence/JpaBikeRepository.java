package bikes.infrastructure.persistence;

import bikes.domain.Bike;
import bikes.domain.BikeRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Stateless
public class JpaBikeRepository implements BikeRepository {

  @PersistenceContext(unitName = "bikePU")
  EntityManager em;

  @Override
  @SuppressWarnings("unchecked")
  public Collection<Bike> findAllBikes() {
    return Collections
        .unmodifiableCollection(em.createQuery("select b from Bike b").getResultList());
  }

  @Override
  public Optional<Bike> findBike(long id) {
    return Optional.ofNullable(em.find(Bike.class, id));
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.MANDATORY)
  public void addBike(Bike bike) {
    em.persist(bike);
  }

  @Override
  @TransactionAttribute(TransactionAttributeType.MANDATORY)
  public void removeBike(Bike bike) {
    em.remove(bike);
  }
}
