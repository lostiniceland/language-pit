package infrastructure.persistence;

import domain.wife.ApprovalStatus;
import domain.wife.BikeApproval;
import domain.wife.WifeRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class JpaWifeRepository implements WifeRepository {

  @PersistenceContext(unitName = "wifePU")
  EntityManager em;


  @Override
  public Optional<BikeApproval> findBikeApproval(long bikeId) {
    Query query = em.createQuery("select a from BikeApproval a where a.bikeId = :bikeId ");
    query.setParameter("bikeId", bikeId);
    return Optional.ofNullable((BikeApproval) query.getSingleResult());
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<BikeApproval> findAllBikeApprovals() {
    return Collections
        .unmodifiableCollection(em.createQuery("select a from BikeApproval a").getResultList());
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<BikeApproval> findAllBikeAccepted() {
    Query query = em.createQuery("select a from BikeApproval a where a.approval != :accepted ");
    query.setParameter("accepted", ApprovalStatus.Accepted);
    return Collections.unmodifiableCollection(query.getResultList());
  }

  @Override
  public void addBikeApproval(BikeApproval approval) {
    em.persist(approval);
    em.flush();
  }

  @Override
  public void removeBikeApproval(BikeApproval approval) {
    em.remove(approval);
  }

  @Override
  public int countAllBikesOwned() {
    Query query = em.createQuery("select count(ba) from BikeApproval ba where ba.approval = :accepted");
    query.setParameter("accepted", ApprovalStatus.Accepted);
    return ((Number) query.getSingleResult()).intValue();
  }
}
