package hibernateDAL;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.Session;
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
import jakarta.persistence.EntityManager;

@DisplayName("tests for HibernatePlayerRepository")
class HibernatePlayerRepositoryTest {

	private static SessionFactory sessionFactory;

	private JpaPlayerRepository playerRepository;

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
		playerRepository = new JpaPlayerRepository();
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}
	
	@Test
	@DisplayName("getAllGiocatori() on an empty table")
	public void testNoPlayersExist(){
		assertThat(playerRepository.findAll(sessionFactory.createEntityManager())).isEmpty();
	}
	
	@Test
	@DisplayName("getAllGiocatori() when two players have been persisted")
	public void testTwoPlayersExist(){		
		Player buffon = new Player(Role.GOALKEEPER, "Gigi", "Buffon");
		Player messi = new Player(Role.STRIKER, "Lionel", "Messi");
		
		sessionFactory.inTransaction(session -> {
			session.persist(buffon);
			session.persist(messi);});
		
		EntityManager repositorySession = sessionFactory.createEntityManager();
		assertThat(playerRepository.findAll(repositorySession)).containsExactly(buffon, messi);
		repositorySession.close();	
	}

	@Test
	@DisplayName("addPlayer() with a non-persisted player")
	public void testAddNonPersistedPlayer() {
		Player buffon = new Player(Role.GOALKEEPER, "Gigi", "Buffon");
		
		EntityManager repositorySession = sessionFactory.createEntityManager();
		repositorySession.getTransaction().begin(); // Start the transaction
		assertTrue(playerRepository.addPlayer(repositorySession, buffon));		
		repositorySession.getTransaction().commit(); // Commit the transaction to flush changes
		repositorySession.close();	
		
		assertThat(sessionFactory.fromTransaction((Session session) -> 
					session.createSelectionQuery("from Player", Player.class).getResultList()))
				.containsExactly(buffon);
	}
	
	@Test
	@DisplayName("addPlayer() does not add an already persisted player")
	public void testAddAlreadyPersistedPlayer() {
		Player buffon = new Player(Role.GOALKEEPER, "Gigi", "Buffon");
		
		sessionFactory.inTransaction(session -> session.persist(buffon));

		EntityManager repositorySession = sessionFactory.createEntityManager();
		assertFalse(playerRepository.addPlayer(repositorySession, buffon));
		repositorySession.close();	
	}

	@Test
	@DisplayName("findBySurname when the player does not exist")
	public void testFindBySurnameDoesNotExist(){
		Player buffon = new Player(Role.GOALKEEPER, "Gigi", "Buffon");
		Player messi = new Player(Role.STRIKER, "Lionel", "Messi");

		sessionFactory.inTransaction(session -> {
			session.persist(buffon);
			session.persist(messi);});

		EntityManager repositorySession = sessionFactory.createEntityManager();
		assertThat(playerRepository.findBySurname(repositorySession, "Thuram")).isEmpty();
		repositorySession.close();	
	}

	@Test
	@DisplayName("findBySurname when the players exist")
	public void testFindBySurnameExist(){
		Player marcus = new Player(Role.STRIKER, "Marcus", "Thuram");
		Player kephren = new Player(Role.MIDFIELDER, "Kephren", "Thuram");
		Player eljif = new Player(Role.MIDFIELDER, "Eljif", "Elmas");


		sessionFactory.inTransaction(session -> {
			session.persist(marcus);
			session.persist(kephren);
			session.persist(eljif);});

		EntityManager repositorySession = sessionFactory.createEntityManager();
		assertThat(playerRepository.findBySurname(repositorySession, "Thuram")).containsExactlyInAnyOrder(marcus, kephren);
		repositorySession.close();	
	}
	

}
