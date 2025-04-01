package hibernateDAL;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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

import domainModel.Player;
import domainModel.Player.Role;

class HibernatePlayerRepositoryTest {

	private static SessionFactory sessionFactory;

	private HibernatePlayerRepository playerRepository;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.configure("hibernate-test.cfg.xml")
				.build();

			Metadata metadata = new MetadataSources(serviceRegistry)
			.addAnnotatedClass(Player.class)
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
		playerRepository = new HibernatePlayerRepository(sessionFactory);
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}
	
	@Test
	@DisplayName("getAllGiocatori() on an empty table")
	public void testNoPlayersExist(){
		assertThat(playerRepository.findAll()).isEmpty();
	}
	
	@Test
	@DisplayName("getAllGiocatori() when two players have been persisted")
	public void testTwoPlayersExist(){		
		Player buffon = new Player(Role.GOALKEEPER, "Gigi", "Buffon");
		Player messi = new Player(Role.STRIKER, "Lionel", "Messi");
		
		sessionFactory.inTransaction(session -> {
			session.persist(buffon);
			session.persist(messi);});
		
		assertThat(playerRepository.findAll()).containsExactly(buffon, messi);
	}

	@Test
	@DisplayName("addPlayer() with a legit new player")
	public void testAddPlayerSucceeds() {
		Player buffon = new Player(Role.GOALKEEPER, "Gigi", "Buffon");
//		Player messi = new Player(Role.STRIKER, "Lionel", "Messi");

		assertTrue(playerRepository.addPlayer(buffon));
		
		List<Player> result = sessionFactory.fromTransaction(
				session -> session.createSelectionQuery("from Player", Player.class).getResultList());
		
		assertThat(result).containsExactly(buffon);
	}
	
	@Test
	@DisplayName("addPlayer() with an already existing player")
	public void testAddPlayerFails() {
		Player buffon = new Player(Role.GOALKEEPER, "Gigi", "Buffon");
//		Player messi = new Player(Role.STRIKER, "Lionel", "Messi");
		
		sessionFactory.inTransaction(session -> session.persist(buffon));
		
		System.out.println("buffon nel db");

		assertFalse(playerRepository.addPlayer(buffon));
	}
	

}
