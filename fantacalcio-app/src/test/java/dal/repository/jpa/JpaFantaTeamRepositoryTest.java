package dal.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;

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
import domain.NewsPaper;
import domain.Player;
import jakarta.persistence.EntityManager;

class JpaFantaTeamRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaFantaTeamRepository fantaTeamRepository;
	private EntityManager entityManager;
	private League league;
	private FantaUser admin;
	private NewsPaper newsPaper;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(FantaUser.class).addAnnotatedClass(NewsPaper.class)
					.addAnnotatedClass(League.class).addAnnotatedClass(Contract.class).addAnnotatedClass(Player.class)
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
			newsPaper = new NewsPaper("gazzetta");
			t.persist(newsPaper);
			league = new League(admin, "league", newsPaper, "1234");
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

		assertThat(fantaTeamRepository.getAllTeams(league)).isEmpty();

	}

	@Test
	@DisplayName("getAllTeams() with some teams in the league")
	public void testGetAllTeamsWithSomeTeams() {

		FantaUser user1 = new FantaUser("mail1", "pswd1");
		FantaUser user2 = new FantaUser("mail2", "pswd2");

		FantaTeam team1 = new FantaTeam("team1", league, 0, user1, new HashSet<Contract>());
		FantaTeam team2 = new FantaTeam("team2", league, 0, user2, new HashSet<Contract>());

		sessionFactory.inTransaction(session -> {
			session.persist(user1);
			session.persist(user2);
			session.persist(team1);
			session.persist(team2);
		});

		assertThat(fantaTeamRepository.getAllTeams(league)).containsExactlyInAnyOrder(team1, team2);

	}

	@Test
	@DisplayName("saveTeam() should persist correctly")
	public void testSaveTeam() {

		entityManager.getTransaction().begin();

		FantaUser user = new FantaUser("mail1", "pswd1");
		FantaTeam team = new FantaTeam("team1", league, 0, user, new HashSet<Contract>());

		entityManager.persist(user);

		fantaTeamRepository.saveTeam(team);

		FantaTeam result = entityManager
				.createQuery("FROM FantaTeam t WHERE t.league = :league AND t.fantaManager = :user", FantaTeam.class)
				.setParameter("league", league).setParameter("user", user).getSingleResult();

		assertThat(result).isEqualTo(team);

		entityManager.close();
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
