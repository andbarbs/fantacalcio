package dal.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;

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

import domain.Contract;
import domain.FantaTeam;
import domain.FantaUser;
import domain.League;
import domain.Player;
import jakarta.persistence.EntityManager;

class JpaFantaTeamRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaFantaTeamRepository fantaTeamRepository;
	private EntityManager entityManager;
	private League league;
	private FantaUser admin;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(Contract.class)
					.addAnnotatedClass(Player.class)
					.getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void setup() {
		sessionFactory.getSchemaManager().truncateMappedObjects();
		entityManager = sessionFactory.createEntityManager();
		fantaTeamRepository = new JpaFantaTeamRepository(entityManager);

		sessionFactory.inTransaction(t -> {
			admin = new FantaUser("email", "pswd");
			t.persist(admin);
			league = new League(admin, "league", "1234");
			t.persist(league);
		});
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("getAllTeams() with no team in the league")
	public void testGetAllTeamsWithNoTeam() {

		entityManager.getTransaction().begin();
		List<FantaTeam> retrieved = fantaTeamRepository.getAllTeams(league);
		entityManager.getTransaction().commit();
	    entityManager.clear();		
		
		assertThat(retrieved).isEmpty();
	}

	@Test
	@DisplayName("getAllTeams() with some teams in the league")
	public void testGetAllTeamsWithSomeTeams() {

		// GIVEN two Teams are instantiated on a League
		FantaUser user1 = new FantaUser("mail1", "pswd1");
		FantaUser user2 = new FantaUser("mail2", "pswd2");

		FantaTeam team1 = new FantaTeam("team1", league, 0, user1, null);
		FantaTeam team2 = new FantaTeam("team2", league, 0, user2, null);

		// AND they are persisted manually
		sessionFactory.inTransaction(session -> {
			session.persist(user1);
			session.persist(user2);
			session.persist(team1);
			session.persist(team2);
		});

		// WHEN the SUT is used to retrieve all teams in the League
		entityManager.getTransaction().begin();
		List<FantaTeam> allTeams = fantaTeamRepository.getAllTeams(league);
		entityManager.getTransaction().commit();
	    entityManager.clear();
	    
	    // THEN exactly the supposed Teams are retrieved
		assertThat(allTeams).containsExactlyInAnyOrder(team1, team2);
	}

	@Test
	@DisplayName("saveTeam() should persist correctly")
	public void testSaveTeam() {
		
		//GIVEN a User is manually persisted
		FantaUser user = new FantaUser("mail1", "pswd1");
		sessionFactory.inTransaction(em -> em.persist(user));

		// GIVEN a Team exists for that user
		FantaTeam team = new FantaTeam("team1", league, 0, user, null);
		
		// WHEN the SUT is used to persist that Team
		entityManager.getTransaction().begin();
		fantaTeamRepository.saveTeam(team);
		entityManager.getTransaction().commit();
	    entityManager.clear();

		// THEN the Team is actually persisted to the database
	    FantaTeam result = sessionFactory.fromTransaction((Session em) -> em
	            .createQuery("FROM FantaTeam t JOIN FETCH t.fantaManager tfm JOIN FETCH t.league tl JOIN FETCH tl.admin "
	            		+ "WHERE t.league = :league AND t.fantaManager = :user", FantaTeam.class)
	            .setParameter("league", league).setParameter("user", user).getSingleResult());

		assertThat(result).isEqualTo(team);
	}

	@Test
	@DisplayName("getFantaTeamByUserAndLeague() when that team does not exist")
	public void testGetFantaTeamByUserAndLeagueWhenNotPresent() {

		assertThrows(jakarta.persistence.NoResultException.class,
				() -> fantaTeamRepository.getFantaTeamByUserAndLeague(league, admin));

	}

	@Test
	@DisplayName("getFantaTeamByUserAndLeague() when that team exists")
	public void testGetFantaTeamByUserAndLeagueWhenPresent() {

		FantaUser user = new FantaUser("mail", "pswd");
		FantaTeam team = new FantaTeam("team", league, 0, user, new HashSet<Contract>());

		sessionFactory.inTransaction(session -> {
			session.persist(user);
			session.persist(team);
		});

		assertThat(fantaTeamRepository.getFantaTeamByUserAndLeague(league, user)).isEqualTo(team);

	}

}
