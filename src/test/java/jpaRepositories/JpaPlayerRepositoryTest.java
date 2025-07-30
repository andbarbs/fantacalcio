package jpaRepositories;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domainModel.Player;
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

import domainModel.Player.*;
import jakarta.persistence.EntityManager;

@DisplayName("tests for HibernatePlayerRepository")
class JpaPlayerRepositoryTest {

	private static SessionFactory sessionFactory;

	private JpaPlayerRepository playerRepository;

	private EntityManager entityManager;

//TODO aggiornare bootstrap di tutti i test hibernate
	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml")
					.build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(Player.class)
					.addAnnotatedClass(Player.Goalkeeper.class)
					.addAnnotatedClass(Player.Defender.class)
					.addAnnotatedClass(Player.Midfielder.class)
					.addAnnotatedClass(Player.Forward.class)
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
		entityManager = sessionFactory.createEntityManager();
		playerRepository = new JpaPlayerRepository(entityManager);
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("findAll() on an empty table")
	public void testFindAllWhenNoPlayersExist(){		
		assertThat(playerRepository.findAll()).isEmpty();
	}

	@Test
	@DisplayName("findAll() when two players have been persisted")
	public void testFindAllTwoPlayersExist(){
		Player buffon = new Goalkeeper("Gigi", "Buffon");
		Player messi = new Forward("Lionel", "Messi");

		sessionFactory.inTransaction(session -> {
			session.persist(buffon);
			session.persist(messi);});
		
		assertThat(playerRepository.findAll()).containsExactly(buffon, messi);
	}

	@Test
	@DisplayName("addPlayer() with a non-persisted player")
	public void testAddPlayerWithNonPersistedPlayer() {
		Player buffon = new Goalkeeper("Gigi", "Buffon");

		entityManager.getTransaction().begin(); // Start the transaction
		assertTrue(playerRepository.addPlayer(buffon));
		entityManager.getTransaction().commit(); // Commit the transaction to flush changes
		entityManager.close();

		assertThat(sessionFactory.fromTransaction((Session session) ->
				session.createSelectionQuery("from Player", Player.class).getResultList()))
				.containsExactly(buffon);
	}

	@Test
	@DisplayName("addPlayer() does not add an already persisted player")
	public void testAddPlayerWithAlreadyPersistedPlayer() {
		Player buffon = new Goalkeeper("Gigi", "Buffon");

		sessionFactory.inTransaction(session -> session.persist(buffon));

		assertFalse(playerRepository.addPlayer(buffon));
		entityManager.close();
	}

	@Test
	@DisplayName("findBySurname when the player does not exist")
	public void testFindBySurnameWhenPlayerDoesNotExist(){
		Player buffon = new Goalkeeper("Gigi", "Buffon");
		Player messi = new Forward("Lionel", "Messi");

		sessionFactory.inTransaction(session -> {
			session.persist(buffon);
			session.persist(messi);}
		);

		assertThat(playerRepository.findBySurname("Thuram")).isEmpty();
		entityManager.close();

	}

	@Test
	@DisplayName("findBySurname when the players exists")
	public void testFindBySurnameWhenPlayerExists(){
		Player marcus = new Forward("Marcus", "Thuram");
		Player kephren = new Forward("Kephren", "Thuram");
		Player eljif = new Forward("Eljif", "Elmas");

		sessionFactory.inTransaction(session -> {
			session.persist(marcus);
			session.persist(kephren);
			session.persist(eljif);});

		assertThat(playerRepository.findBySurname("Thuram")).containsExactlyInAnyOrder(marcus, kephren);
		entityManager.close();
	}


}