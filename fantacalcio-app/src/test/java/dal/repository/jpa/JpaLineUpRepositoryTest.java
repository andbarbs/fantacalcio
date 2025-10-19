package dal.repository.jpa;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;

import domain.*;
import domain.Player.*;
import domain.scheme.Scheme433;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * tests that a {@link JpaLineUpRepository} correctly
 * persists, retrieves and deletes {@link LineUp}s.
 */
class JpaLineUpRepositoryTest {

	private static SessionFactory sessionFactory;
	
	// the SUT reference
	private JpaLineUpRepository lineUpRepository;
	private EntityManager entityManager;  // the EntityManager the SUT is constructed on
	
	// references to setup entities managed in @BeforeEach
	private League league;
	private MatchDay matchDay;
	private FantaTeam opponent;
	private FantaUser manager;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(LineUp.class)
					.addAnnotatedClass(MatchDay.class)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(Match.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(Fielding.class)
					.addAnnotatedClass(Fielding.StarterFielding.class)
					.addAnnotatedClass(Fielding.SubstituteFielding.class)
					.addAnnotatedClass(Goalkeeper.class)
					.addAnnotatedClass(Defender.class)
					.addAnnotatedClass(Midfielder.class)
					.addAnnotatedClass(Forward.class)
					.addAnnotatedClass(Contract.class)
					.addAnnotatedClass(League.class)
					.getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void truncatorAndInstantiatorAndSetup() {
		
		// GIVEN the persistence unit is wiped clean
		sessionFactory.getSchemaManager().truncateMappedObjects();
		
		// AND a new SUT instance is constructed
		entityManager = sessionFactory.createEntityManager();
		lineUpRepository = new JpaLineUpRepository(entityManager);

		// AND common setup entities are assembled and persisted
		sessionFactory.inTransaction(em -> {
			manager = new FantaUser("manager@example.com", "securePass");
			em.persist(manager);
			league = new League(manager, "Serie A", "code");
			em.persist(league);
			opponent = new FantaTeam("Challengers", league, 25, manager, new HashSet<>());
			em.persist(opponent);
            matchDay = new MatchDay("1 giornata", 1, MatchDay.Status.FUTURE, league);
            em.persist(matchDay);
		});
	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}
	
	@Nested
	@DisplayName("given a LineUp instance is ready to be persisted")
	class LineUpReadyForPersisting {			
		
		private FantaTeam team;
		private Match match;
		private LineUp readyToBePersisted;

		@BeforeEach
		void prepareLineUpInstance() {
			
			// GIVEN a LineUp's ancillary entities are instantiated
			Goalkeeper gk1 = new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA);
			
			Defender d1 = new Defender("difensore1", "titolare", Player.Club.ATALANTA);
			Defender d2 = new Defender("difensore2", "titolare", Player.Club.ATALANTA);
			Defender d3 = new Defender("difensore3", "titolare", Player.Club.ATALANTA);
			Defender d4 = new Defender("difensore4", "titolare", Player.Club.ATALANTA);
			
			Midfielder m1 = new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA);
			Midfielder m2 = new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA);
			Midfielder m3 = new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA);
			
			Forward f1 = new Forward("attaccante1", "titolare", Player.Club.ATALANTA);
			Forward f2 = new Forward("attaccante2", "titolare", Player.Club.ATALANTA);
			Forward f3 = new Forward("attaccante3", "titolare", Player.Club.ATALANTA);
			
			Goalkeeper sgk1 = new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA);
			Goalkeeper sgk2 = new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA);
			Goalkeeper sgk3 = new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA);
			
			Defender sd1 = new Defender("difensore1", "panchina", Player.Club.ATALANTA);
			Defender sd2 = new Defender("difensore2", "panchina", Player.Club.ATALANTA);
			Defender sd3 = new Defender("difensore3", "panchina", Player.Club.ATALANTA);
			
			Midfielder sm1 = new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA);
			Midfielder sm2 = new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA);
			Midfielder sm3 = new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA);
			
			Forward sf1 = new Forward("attaccante1", "panchina", Player.Club.ATALANTA);
			Forward sf2 = new Forward("attaccante2", "panchina", Player.Club.ATALANTA);
			Forward sf3 = new Forward("attaccante3", "panchina", Player.Club.ATALANTA);
			
			List<Player> players = List.of(
					gk1, 
					d1, d2, d3, d4, 
					m1, m2, m3, 
					f1, f2, f3, 
					sgk1, sgk2, sgk3, 
					sd1, sd2, sd3,
					sm1, sm2, sm3,
					sf1, sf2, sf3);
			
			Set<Contract> contracts = new HashSet<Contract>();
			team = new FantaTeam("Dream Team", league, 30, manager, contracts);
			players.stream().map(p -> new Contract(team, p)).forEach(contracts::add);
			
			match = new Match(matchDay, team, opponent);
			
			// AND a LineUp's ancillary entities are persisted
			sessionFactory.inTransaction(em -> {
				players.forEach(em::persist);
				em.persist(team);  // relies on cascading for Contracts
				em.persist(match);
			});
			
			// AND a LineUp instance is assembled, ready to be persisted
			readyToBePersisted = LineUp.build()
					.forTeam(team)
					.inMatch(match)
					.withStarterLineUp(Scheme433.starterLineUp()
							.withGoalkeeper(gk1)
							.withDefenders(d1, d2, d3, d4)
							.withMidfielders(m1, m2, m3)
							.withForwards(f1, f2, f3))
					.withSubstituteGoalkeepers(sgk1, sgk2, sgk3)
					.withSubstituteDefenders(sd1, sd2, sd3)
					.withSubstituteMidfielders(sm1, sm2, sm3)
					.withSubstituteForwards(sf1, sf2, sf3);
		}

		@Test
		@DisplayName("deleteLineUp removes the LineUp by match and team")
		void testDeleteLineUpRemovesCorrectly() {
			
			// GIVEN a LineUp instance is persisted
			sessionFactory.inTransaction(em -> em.persist(readyToBePersisted));
			
			// WHEN the SUT is used to delete the LineUp
			entityManager.getTransaction().begin();
			lineUpRepository.deleteLineUp(readyToBePersisted);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN the LineUp is removed from the persistence unit
			assertThat(sessionFactory
					.fromTransaction((Session em) -> em.createQuery("FROM LineUp", LineUp.class).getResultList()))
					.isEmpty();
			
			// AND Fieldings are removed via cascading
			assertThat(sessionFactory
					.fromTransaction((Session em) -> em.createQuery("FROM Fielding", Fielding.class).getResultList()))
					.isEmpty();
		}


		@Test
		@DisplayName("saveLineUp persists a LineUp correctly")
		void testSaveLineUpPersistsCorrectly() {

			// GIVEN a LineUp instance is assembled
			
			// WHEN the SUT is used to persist it		
			entityManager.getTransaction().begin();
			lineUpRepository.saveLineUp(readyToBePersisted);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN the LineUp is actually saved to the persistence unit
			assertThat(
					sessionFactory.fromTransaction((Session em) -> em
							.createQuery(
									"SELECT DISTINCT l FROM LineUp l " + "JOIN FETCH l.match "
											+ "LEFT JOIN FETCH l.fieldings " + "WHERE l.match = :match AND l.team = :team",
											LineUp.class)
							.setParameter("match", match).setParameter("team", team).getResultStream().findFirst()))
			.isPresent().hasValueSatisfying(readyToBePersisted::recursiveEquals);
		}
		
		@Test
		@DisplayName("getLineUpByMatchAndTeam retrieves a saved LineUp correctly")
		void testGetLineUpByMatchAndTeamRetrievesCorrectly() {	
			
			// GIVEN a LineUp instance is persisted
			sessionFactory.inTransaction(em -> em.persist(readyToBePersisted));
			
			// WHEN the SUT is used to retrieve a LineUp
			entityManager.getTransaction().begin();
			Optional<LineUp> retrieved = lineUpRepository.getLineUpByMatchAndTeam(match, team);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN the retrieved LineUp is equal to that persisted
			assertThat(retrieved).isPresent().hasValueSatisfying(readyToBePersisted::recursiveEquals);
		}

		@Test
		@DisplayName("getLineUpByMatchAndTeam returns empty if no lineup exists")
		void testGetLineUpByMatchAndTeamEmpty() {		
			
			// GIVEN a LineUp instance is assembled, but not persisted
			
			// WHEN the SUT is used to retrieve a non-existent LineUp
			entityManager.getTransaction().begin();
			Optional<LineUp> result = lineUpRepository.getLineUpByMatchAndTeam(match, team);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN the SUT confirms the LineUp being looked up does not exist
			assertThat(result).isEmpty();
		}
	}
}
