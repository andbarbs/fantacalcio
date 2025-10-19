package dal.repository.jpa;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import domain.Contract;
import domain.FantaTeam;
import domain.FantaUser;
import domain.League;
import domain.Player;
import domain.Player.*;
import jakarta.persistence.EntityManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class JpaPlayerRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaPlayerRepository playerRepository;
	private EntityManager entityManager;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(Player.class)
					.addAnnotatedClass(Player.Goalkeeper.class)
					.addAnnotatedClass(Player.Defender.class)
					.addAnnotatedClass(Player.Midfielder.class)
					.addAnnotatedClass(Player.Forward.class)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(Contract.class)
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
	
	

	@Nested
	@DisplayName("can add a Player to the system")
	class Persisting {	
		
		@Test
		@DisplayName("when the same Player has not been persisted yet")
		public void testAddPlayerWithNonPersistedPlayer() {
			
			// GIVEN no Player instances have been persisted
			
			// WHEN the SUT is used to persist a Player
			Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
			entityManager.getTransaction().begin();
			playerRepository.addPlayer(buffon);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN that Player is present in the database
			assertThat(sessionFactory
					.fromTransaction((Session em) -> em.createQuery("FROM Player", Player.class).getResultList()))
			.containsExactly(buffon);
		}
		
		@Test
		@DisplayName("when the same Player has already been persisted")
		public void testAddPlayerWithAlreadyPersistedPlayer() {
			
			// GIVEN a Player has been manually persisted to the database
			Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
			sessionFactory.inTransaction(session -> session.persist(buffon));
			
			// WHEN the SUT is used to persist the same Player
			entityManager.getTransaction().begin();
			Boolean result = playerRepository.addPlayer(buffon);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN false is returned
			assertFalse(result);
			
			// AND the database contains only one Player
			assertThat(sessionFactory
					.fromTransaction((Session em) -> em.createQuery("FROM Player", Player.class).getResultList()))
			.containsExactly(buffon);
		}
	}
	
	@Nested
	@DisplayName("can look up all Players in the system")
	class SpecializedRetrieval {
		
		@Nested
		@DisplayName("all Players")
		class UnspecializedRetrieval {		
			
			@Test
			@DisplayName("when no Players have been persisted")
			public void testFindAllWhenNoPlayersExist() {
				
				// GIVEN no Player has been persisted to the database
				
				// WHEN the SUT is used to retrieve all Players from the database
				entityManager.getTransaction().begin();
				Set<Player> players = playerRepository.findAll();
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN an empty Set is returned
				assertThat(players).isEmpty();
			}
			
			@Test
			@DisplayName("when some Players have been persisted")
			public void testFindAllTwoPlayersExist() {
				
				// GIVEN some Players have been persisted to the database
				Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
				Player messi = new Forward("Lionel", "Messi", Club.PISA);
				
				sessionFactory.inTransaction(session -> {
					session.persist(buffon);
					session.persist(messi);
				});
				
				// WHEN the SUT is used to retrieve all Players from the database
				entityManager.getTransaction().begin();
				Set<Player> players = playerRepository.findAll();
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN all Players are returned
				assertThat(players).containsExactlyInAnyOrder(buffon, messi);
			}
		}
		
		@Nested
		@DisplayName("by surname")
		class BySurname {				
			
			@Test
			@DisplayName("findBySurname when the player does not exist")
			public void testFindBySurnameWhenPlayerDoesNotExist() {
				
				// GIVEN one Player with a given surname has been manually persisted to the database
				Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
				Player messi = new Forward("Lionel", "Messi", Club.PISA);
				
				sessionFactory.inTransaction(session -> {
					session.persist(buffon);
					session.persist(messi);
				});
				
				// WHEN the SUT is used to retrieve Players by surname
				entityManager.getTransaction().begin();
				List<Player> players = playerRepository.findBySurname("Thuram");
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN only the appropriate Players are returned
				assertThat(players).isEmpty();
			}
			
			@Test
			@DisplayName("findBySurname when the players exists")
			public void testFindBySurnameWhenPlayerExists() {
				
				// GIVEN some Players with a given surname has been manually persisted to the database
				Player marcus = new Forward("Marcus", "Thuram", Club.INTER);
				Player kephren = new Forward("Kephren", "Thuram", Club.JUVENTUS);
				Player eljif = new Forward("Eljif", "Elmas", Club.NAPOLI);
				
				// WHEN the SUT is used to retrieve Players by surname
				sessionFactory.inTransaction(session -> {
					session.persist(marcus);
					session.persist(kephren);
					session.persist(eljif);
				});
				
				entityManager.getTransaction().begin();
				List<Player> players = playerRepository.findBySurname("Thuram");
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN only the appropriate Players are returned
				assertThat(players).containsExactlyInAnyOrder(marcus, kephren);
			}
		}
		
		@Nested
		@DisplayName("by Club")
		class ByClub {	
			
			@Test
			@DisplayName("findByTeam when the player does not exist")
			public void testFindByTeamWhenPlayerDoesNotExist() {
				
				// GIVEN no Players have been persisted for a given Club
				Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
				Player messi = new Forward("Lionel", "Messi", Club.PISA);
				
				sessionFactory.inTransaction(session -> {
					session.persist(buffon);
					session.persist(messi);
				});
				
				// WHEN the SUT is used to retrieve Players for the given Club
				entityManager.getTransaction().begin();
				Set<Player> players = playerRepository.findByClub(Club.NAPOLI);
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN an empty Set is returned
				assertThat(players).isEmpty();
			}
			
			@Test
			@DisplayName("findByTeam when the players exists")
			public void testFindByTeamWhenPlayerExists() {
				
				// GIVEN some Players have been persisted for a given Club
				Player buffon = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
				Player messi = new Forward("Lionel", "Messi", Club.MILAN);
				Player yamal = new Forward("Lamine", "Yamal", Club.MILAN);
				
				sessionFactory.inTransaction(session -> {
					session.persist(buffon);
					session.persist(messi);
					session.persist(yamal);
				});
				
				// WHEN the SUT is used to retrieve Players for the given Club
				entityManager.getTransaction().begin();
				Set<Player> players = playerRepository.findByClub(Club.MILAN);
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN only the appropriate Players are returned
				assertThat(players).containsExactlyInAnyOrder(messi, yamal);
			}
		}
	}
	
	@Nested
	@DisplayName("can retrieve Players competing in a League")
	class LookupInLeague {	
		
		@Test
		@DisplayName("when some Players compete in a League")
		public void testGetAllInLeague() {
			
			// GIVEN Contracts are persisted for Players under two Leagues
			Player player1 = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
			Player player2 = new Forward("Lionel", "Messi", Club.MILAN);
			Player player3 = new Forward("Lamine", "Yamal", Club.MILAN);
			
			FantaUser admin = new FantaUser("mail", "pswd");
			League league = new League(admin, "Lega", "codice");
			League otherLeague = new League(admin, "Lega", "codice");
			FantaTeam teamA = new FantaTeam("", league, 0, admin, new HashSet<>());
			teamA.getContracts().add(new Contract(teamA, player1));
			FantaTeam teamB = new FantaTeam("", league, 0, admin, new HashSet<>());
			teamB.getContracts().add(new Contract(teamB, player2));
			FantaTeam teamC = new FantaTeam("", otherLeague, 0, admin, new HashSet<>());
			teamC.getContracts().add(new Contract(teamC, player3));
			
			sessionFactory.inTransaction(session -> {
				session.persist(player1);
				session.persist(player2);
				session.persist(player3);
				session.persist(admin);
				session.persist(league);
				session.persist(otherLeague);
				session.persist(teamA);
				session.persist(teamB);
				session.persist(teamC);
			});
			
			// WHEN the SUT is used to retrieve Players in the given League
			entityManager.getTransaction().begin();
			Set<Player> players = playerRepository.getAllInLeague(league);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN only the appropriate Players are returned
			assertThat(players).containsExactlyInAnyOrder(player1, player2);
		}
	}
}