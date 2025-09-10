package jpaRepositories;

import domainModel.*;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import domainModel.Player.Club;

class JpaGradeRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaGradeRepository gradeRepository;
	private EntityManager entityManager;
	private FantaUser manager;
	private NewsPaper newsPaper;
	private League league;
	private MatchDaySerieA matchDay;
	private FantaTeam player1;
	private FantaTeam player2;
	private FantaUser user1;
	private FantaUser user2;
	private FantaTeam team1;
	private FantaTeam team2;
	private Match match;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(Grade.class)
					.addAnnotatedClass(Player.class).addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(NewsPaper.class).addAnnotatedClass(League.class).addAnnotatedClass(Match.class)
					.addAnnotatedClass(FantaTeam.class).addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(Player.Goalkeeper.class).addAnnotatedClass(Player.Forward.class)
					.addAnnotatedClass(Contract.class).getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			ex.printStackTrace();
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void setup() {
		// ensures tests work on empty tables without having to recreate a
		// SessionFactory instance
		sessionFactory.getSchemaManager().truncateMappedObjects();

		// Instantiates the SUT using the static SessionFactory
		entityManager = sessionFactory.createEntityManager();
		gradeRepository = new JpaGradeRepository(entityManager);

		sessionFactory.inTransaction(t -> {
			manager = new FantaUser("manager@example.com", "securePass");
			t.persist(manager);
			newsPaper = new NewsPaper("Gazzetta");
			t.persist(newsPaper);
			league = new League(manager, "Serie A", newsPaper, "code");
			t.persist(league);
			matchDay = new MatchDaySerieA("Matchday 1", LocalDate.of(2025, 6, 19));
			t.persist(matchDay);
			player1 = new FantaTeam("Challengers", league, 25, manager, new HashSet<>());
			t.persist(player1);
			player2 = new FantaTeam("Challengers", league, 25, manager, new HashSet<>());
			t.persist(player2);
			user1 = new FantaUser("mail1", "pswd1");
			t.persist(user1);
			user2 = new FantaUser("mail2", "pswd2");
			t.persist(user2);
			team1 = new FantaTeam("Team1", league, 32, user1, new HashSet<Contract>());
			t.persist(team1);
			team2 = new FantaTeam("Team2", league, 13, user2, new HashSet<Contract>());
			t.persist(team2);
			match = new Match(matchDay, team1, team2);
			t.persist(match);
		});
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("getAllMatchGrades() on an empty table")
	public void testGetAllMatchGradesWhenNoGradesExist() {
		assertThat(gradeRepository.getAllMatchGrades(match)).isEmpty();
	}

	@Test
	@DisplayName("getAllMatchGrades() when two grades have been persisted")
	public void testGetAllMatchGradesWhenTwoGradesExist() {

		Player player1 = new Player.Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
		Player player2 = new Player.Forward("Gigi", "Riva", Club.CAGLIARI);

		Grade voto1 = new Grade(player1, matchDay, 6.0, newsPaper);
		Grade voto2 = new Grade(player1, matchDay, 8.0, newsPaper);

		sessionFactory.inTransaction(session -> {
			session.persist(player1);
			session.persist(player2);
			session.persist(voto1);
			session.persist(voto2);
		});

		EntityManager repositorySession = sessionFactory.createEntityManager();

		assertThat(gradeRepository.getAllMatchGrades(match)).containsExactly(voto1, voto2);
		repositorySession.close();
	}

	@Test
	@DisplayName("saveGrade should persist correctly")
	void testSaveGradePersistsCorrectly() {

		Player totti = new Player.Forward("Francesco", "Totti", Club.ROMA);
		Grade grade = new Grade(totti, matchDay, 9.0, newsPaper);

		sessionFactory.inTransaction(session -> {
			session.persist(totti);
			session.persist(grade);
		});

		sessionFactory.inTransaction((Session em) -> {
			Optional<Grade> result = em
					.createQuery("SELECT g FROM Grade g " + "WHERE g.player = :player " + "AND g.matchDay = :matchDay "
							+ "AND g.newsPaper = :newsPaper", Grade.class)
					.setParameter("player", totti).setParameter("matchDay", matchDay)
					.setParameter("newsPaper", newsPaper).getResultStream().findFirst();

			assertThat(result).isPresent();
			Grade found = result.get();
			assertThat(found.getPlayer()).isEqualTo(totti);
			assertThat(found.getNewsPaper()).isEqualTo(newsPaper);
		});

	}

}
