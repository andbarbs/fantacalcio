package concreteJpaRepositories;

import org.hibernate.SessionFactory;

public abstract class HibernateEntityRepository {

	private final SessionFactory sessionFactory;
	
	public HibernateEntityRepository(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

}