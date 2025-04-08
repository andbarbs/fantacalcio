package hibernateDAL;

import org.hibernate.SessionFactory;

public class HibernateEntityRepository {

	private final SessionFactory sessionFactory;
	
	public HibernateEntityRepository(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

}