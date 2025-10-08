package dal.repository.jpa;

import java.util.Optional;

import business.ports.repository.FantaUserRepository;
import domain.FantaUser;
import domain.FantaUser_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaFantaUserRepository extends BaseJpaRepository implements FantaUserRepository {

	public JpaFantaUserRepository(EntityManager em) {
		super(em);
	}

	@Override
	public Optional<FantaUser> getUser(String email, String password) {
    	EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<FantaUser> query = cb.createQuery(FantaUser.class);
        Root<FantaUser> root = query.from(FantaUser.class);

        query.select(root).where(
                cb.equal(root.get(FantaUser_.email), email),
                cb.equal(root.get(FantaUser_.password), password)
        );

        return em.createQuery(query).getResultList().stream().findFirst();
	}

	@Override
	public void saveFantaUser(FantaUser fantaUser) {
		getEntityManager().persist(fantaUser);
	}

}
