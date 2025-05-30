package jpaRepositories;

import jakarta.persistence.EntityManager;

public class BaseJpaRepository {

	private final EntityManager em;


	public BaseJpaRepository(EntityManager em) {
		this.em = em;
	}


	public EntityManager getEntityManager() {
		return em;
	}

}