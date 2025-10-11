package dal.repository.jpa;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import domain.Player;
import domain.Player.*;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Set;

class JpaPlayerRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaPlayerRepository playerRepository;
	private EntityManager entityManager;

//TODO aggiornare bootstrap di tutti i test hibernate
	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(Player.class)
					.addAnnotatedClass(Player.Goalkeeper.class).addAnnotatedClass(Player.Defender.class)
					.addAnnotatedClass(Player.Midfielder.class).addAnnotatedClass(Player.Forward.class)
					.getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void setup() {
		// ensures tests work on empty tables without having to recreate a
		// SessionFactory instance
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
	public void testFindAllWhenNoPlayersExist() {
        List<Player> players = playerRepository.findAll();
        entityManager.getTransaction().begin();
        assertThat(players).isEmpty();
        entityManager.getTransaction().commit();
        entityManager.clear();
	}

	@Test
	@DisplayName("findAll() when two players have been persisted")
	public void testFindAllTwoPlayersExist() {
		Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
		Player messi = new Forward("Lionel", "Messi", Club.PISA);

		sessionFactory.inTransaction(session -> {
			session.persist(buffon);
			session.persist(messi);
		});
        List<Player> players = playerRepository.findAll();
        entityManager.getTransaction().begin();
		assertThat(players).containsExactly(buffon, messi);
        entityManager.getTransaction().commit();
        entityManager.clear();
	}

	@Test
	@DisplayName("addPlayer() with a non-persisted player")
	public void testAddPlayerWithNonPersistedPlayer() {
		Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
        entityManager.getTransaction().begin();
        playerRepository.addPlayer(buffon);
        entityManager.getTransaction().commit();
        entityManager.clear();

		assertThat(entityManager.createQuery("from Player", Player.class).getResultList()).containsExactly(buffon);
	}

	@Test
	@DisplayName("addPlayer() does not add an already persisted player")
	public void testAddPlayerWithAlreadyPersistedPlayer() {
		Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);

		sessionFactory.inTransaction(session -> session.persist(buffon));
        entityManager.getTransaction().begin();
        Boolean result = playerRepository.addPlayer(buffon);
        entityManager.getTransaction().commit();
        entityManager.clear();
		assertFalse(result);
	}

	@Test
	@DisplayName("findBySurname when the player does not exist")
	public void testFindBySurnameWhenPlayerDoesNotExist() {
		Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
		Player messi = new Forward("Lionel", "Messi", Club.PISA);

		sessionFactory.inTransaction(session -> {
			session.persist(buffon);
			session.persist(messi);
		});
        entityManager.getTransaction().begin();
        List<Player> players = playerRepository.findBySurname("Thuram");
        entityManager.getTransaction().commit();
        entityManager.clear();
		assertThat(players).isEmpty();
	}

	@Test
	@DisplayName("findBySurname when the players exists")
	public void testFindBySurnameWhenPlayerExists() {
		Player marcus = new Forward("Marcus", "Thuram", Club.INTER);
		Player kephren = new Forward("Kephren", "Thuram", Club.JUVENTUS);
		Player eljif = new Forward("Eljif", "Elmas", Club.NAPOLI);

		sessionFactory.inTransaction(session -> {
			session.persist(marcus);
			session.persist(kephren);
			session.persist(eljif);
		});

        entityManager.getTransaction().begin();
        List<Player> players = playerRepository.findBySurname("Thuram");
        entityManager.getTransaction().commit();
        entityManager.clear();

		assertThat(players).containsExactlyInAnyOrder(marcus, kephren);
	}

	@Test
	@DisplayName("findByTeam when the player does not exist")
	public void testFindByTeamWhenPlayerDoesNotExist() {
		Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
		Player messi = new Forward("Lionel", "Messi", Club.PISA);

		sessionFactory.inTransaction(session -> {
			session.persist(buffon);
			session.persist(messi);
		});
        entityManager.getTransaction().begin();
        Set<Player> players = playerRepository.findByClub(Club.NAPOLI);
        entityManager.getTransaction().commit();
        entityManager.clear();
		assertThat(players).isEmpty();
	}

	@Test
	@DisplayName("findByTeam when the players exists")
	public void testFindByTeamWhenPlayerExists() {
		Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
		Player messi = new Forward("Lionel", "Messi", Club.MILAN);
		Player yamal = new Forward("Lamine", "Yamal", Club.MILAN);

		sessionFactory.inTransaction(session -> {
			session.persist(buffon);
			session.persist(messi);
			session.persist(yamal);
		});
        entityManager.getTransaction().begin();
        Set<Player> players = playerRepository.findByClub(Club.MILAN);
        entityManager.getTransaction().commit();
        entityManager.clear();

		assertThat(players).containsExactlyInAnyOrder(messi, yamal);
	}

}