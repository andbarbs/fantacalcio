package jpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Optional;

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

import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.FantaUser;
import domainModel.League;
import domainModel.NewsPaper;
import domainModel.Player;
import jakarta.persistence.EntityManager;

class JpaLeagueRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaLeagueRepository leagueRepository;
	private EntityManager entityManager;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(NewsPaper.class).addAnnotatedClass(League.class)
					.addAnnotatedClass(FantaTeam.class).addAnnotatedClass(Contract.class)
					.addAnnotatedClass(Player.class).getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void setup() {
		sessionFactory.getSchemaManager().truncateMappedObjects();
		entityManager = sessionFactory.createEntityManager();
		leagueRepository = new JpaLeagueRepository(entityManager);
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("getLeagueByCode() when the league does not exist")
	void testGetLeagueByCodeWithNoLeague() {

		assertThat(leagueRepository.getLeagueByCode("1234")).isEmpty();

	}

	@Test
	@DisplayName("getLeagueByCode() when the league exists")
	void testGetLeagueByCodeWhenLeagueExists() {

		FantaUser admin = new FantaUser("user", "pswd");
		NewsPaper newsPaper = new NewsPaper("gazzetta");
		String leagueCode = "1234";
		League league = new League(admin, "lega", newsPaper, leagueCode);

		sessionFactory.inTransaction(session -> {
			session.persist(admin);
			session.persist(newsPaper);
			session.persist(league);
		});

		Optional<League> result = leagueRepository.getLeagueByCode(leagueCode);
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(league);

	}

	@Test
	@DisplayName("saveLeague() should persist the league correctly")
	void testSaveLeague() {

		entityManager.getTransaction().begin();

		FantaUser admin = new FantaUser("user", "pswd");
		NewsPaper newsPaper = new NewsPaper("gazzetta");
		String leagueCode = "1234";
		League league = new League(admin, "lega", newsPaper, leagueCode);

		entityManager.persist(admin);
		entityManager.persist(newsPaper);

		leagueRepository.saveLeague(league);

		assertThat(entityManager.createQuery("FROM League l WHERE l.leagueCode = :leagueCode", League.class)
				.setParameter("leagueCode", leagueCode).getSingleResult()).isEqualTo(league);

		entityManager.close();

	}

	@Test
	@DisplayName("getLeaguesByUser() when there is no league with the specified user")
	void testGetLeaguesByUserWhenNoLeagueExists() {

		FantaUser admin = new FantaUser("adminMail", "adminPswd");
		NewsPaper newsPaper = new NewsPaper("gazzetta");
		League league = new League(admin, "lega", newsPaper, "1234");
		FantaUser user = new FantaUser("mail", "pswd");


		sessionFactory.inTransaction(session -> {
			session.persist(admin);
			session.persist(newsPaper);
			session.persist(league);
			session.persist(user);
		});

		assertThat(leagueRepository.getLeaguesByUser(user)).isEmpty();

	}

	@Test
	@DisplayName("getLeaguesByUser() when there are some leagues with the specified user")
	void testGetLeaguesByUserWhenSomeLeaguesExist() {

		NewsPaper newsPaper = new NewsPaper("gazzetta");
		FantaUser user = new FantaUser("user", "userPswd");

		FantaUser admin1 = new FantaUser("admin1", "adminPswd1");
		League league1 = new League(admin1, "lega1", newsPaper, "1234");
		FantaTeam fantaTeam1 = new FantaTeam("team1", league1, 0, user, new HashSet<Contract>());

		FantaUser admin2 = new FantaUser("admin2", "adminPswd2");
		League league2 = new League(admin2, "lega2", newsPaper, "5678");
		FantaTeam fantaTeam2 = new FantaTeam("team2", league2, 0, user, new HashSet<Contract>());
		
		sessionFactory.inTransaction(session -> {
			session.persist(newsPaper);
			session.persist(user);
			session.persist(admin1);
			session.persist(league1);
			session.persist(fantaTeam1);
			session.persist(admin2);
			session.persist(league2);
			session.persist(fantaTeam2);
		});

		assertThat(leagueRepository.getLeaguesByUser(user)).containsExactlyInAnyOrder(league1, league2);

	}
	
	@Test
	@DisplayName("getAllTeams() should return all teams for the given league")
	void testGetAllTeams() {
	    NewsPaper newsPaper = new NewsPaper("gazzetta");
	    FantaUser admin = new FantaUser("admin", "pswd");
	    League league = new League(admin, "lega1", newsPaper, "1234");

	    FantaUser manager1 = new FantaUser("user1", "pswd1");
	    FantaUser manager2 = new FantaUser("user2", "pswd2");

	    FantaTeam team1 = new FantaTeam("team1", league, 10, manager1, new HashSet<Contract>());
	    FantaTeam team2 = new FantaTeam("team2", league, 20, manager2, new HashSet<Contract>());

	    // League unrelated to test
	    FantaUser admin2 = new FantaUser("admin2", "pswd2");
	    League otherLeague = new League(admin2, "lega2", newsPaper, "5678");
	    FantaUser otherManager = new FantaUser("other", "pswd3");
	    FantaTeam otherTeam = new FantaTeam("otherTeam", otherLeague, 5, otherManager, new HashSet<Contract>());

	    sessionFactory.inTransaction(session -> {
	        session.persist(newsPaper);
	        session.persist(admin);
	        session.persist(league);
	        session.persist(manager1);
	        session.persist(manager2);
	        session.persist(team1);
	        session.persist(team2);
	        session.persist(admin2);
	        session.persist(otherLeague);
	        session.persist(otherManager);
	        session.persist(otherTeam);
	    });

	    assertThat(leagueRepository.getAllTeams(league))
	        .containsExactlyInAnyOrder(team1, team2)
	        .doesNotContain(otherTeam);
	}

}
