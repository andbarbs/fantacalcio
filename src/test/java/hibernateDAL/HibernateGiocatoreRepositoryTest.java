package hibernateDAL;

import static org.assertj.core.api.Assertions.*;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import domainModel.Giocatore;
import domainModel.Giocatore.Ruolo;

class HibernateGiocatoreRepositoryTest {

	private static SessionFactory sessionFactory;

	private HibernateGiocatoreRepository giocatoreRepository;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.configure("hibernate-test.cfg.xml")
				.build();

			Metadata metadata = new MetadataSources(serviceRegistry)
			.addAnnotatedClass(Giocatore.class)
			.getMetadataBuilder()
			.build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
	
	@BeforeEach
	void setup() {		
		// ensures tests work on empty tables without having to recreate a SessionFactory instance
		sessionFactory.getSchemaManager().truncateMappedObjects();

		// Instantiates the SUT using the static SessionFactory
		giocatoreRepository = new HibernateGiocatoreRepository(sessionFactory);
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}
	
	@Test
	@DisplayName("getAllGiocatori() on an empty table")
	public void testNoPlayersExist(){
		assertThat(giocatoreRepository.getAllGiocatori()).isEmpty();
	}
	
	@Test
	@DisplayName("getAllGiocatori() when two players have been persisted")
	public void testTwoPlayersExist(){		
		Giocatore buffon = new Giocatore(Ruolo.PORTIERE, "Gigi", "Buffon");
		Giocatore messi = new Giocatore(Ruolo.ATTACCANTE, "Lionel", "Messi");
		
		sessionFactory.inTransaction(session -> {
			session.persist(buffon);
			session.persist(messi);});
		
		assertThat(giocatoreRepository.getAllGiocatori()).containsExactly(buffon, messi);
	}

	

}
