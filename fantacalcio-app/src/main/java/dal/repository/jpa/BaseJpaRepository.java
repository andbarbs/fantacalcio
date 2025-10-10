package dal.repository.jpa;

import jakarta.persistence.EntityManager;

public abstract class BaseJpaRepository {

	private final EntityManager em;

	public BaseJpaRepository(EntityManager em) {
		this.em = em;
	}

	public EntityManager getEntityManager() {
		return em;
	}

}