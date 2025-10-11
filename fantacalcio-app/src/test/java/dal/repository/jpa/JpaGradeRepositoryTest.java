package dal.repository.jpa;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;

import domain.*;
import domain.Player.Club;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JpaGradeRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaGradeRepository gradeRepository;
	private EntityManager entityManager;
	private FantaUser manager;
	private League league;
	private MatchDaySerieA matchDay;
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

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(Grade.class)
					.addAnnotatedClass(Player.class)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(Match.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(Player.Goalkeeper.class)
					.addAnnotatedClass(Player.Forward.class)
					.addAnnotatedClass(Contract.class)
					.getMetadataBuilder().build();

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
			league = new League(manager, "Serie A", "code");
			t.persist(league);
			user1 = new FantaUser("mail1", "pswd1");
			t.persist(user1);
			user2 = new FantaUser("mail2", "pswd2");
			t.persist(user2);
			team1 = new FantaTeam("Team1", league, 32, user1, new HashSet<Contract>());
			t.persist(team1);
			team2 = new FantaTeam("Team2", league, 13, user2, new HashSet<Contract>());
			t.persist(team2);
            //TODO questo match day preso così non va bene vengono creati quando creo la lega i matchaday
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
        //TODO prende Matchday ora
		assertThat(gradeRepository.getAllMatchGrades(matchDay)).isEmpty();
	}

	@Test
	@DisplayName("getAllMatchGrades() when two grades have been persisted")
	public void testGetAllMatchGradesWhenTwoGradesExist() {
		Player player1 = new Player.Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
		Player player2 = new Player.Forward("Gigi", "Riva", Club.CAGLIARI);

		Grade voto1 = new Grade(player1, matchDay, 6.0);
		Grade voto2 = new Grade(player2, matchDay, 8.0);
		Contract contract1 = new Contract(team1, player1);
		Contract contract2 = new Contract(team1, player2);
		sessionFactory.inTransaction(session -> {
			session.persist(player1);
			session.persist(player2);
			session.persist(voto1);
			session.persist(voto2);
			session.persist(contract1);
			session.persist(contract2);
		});

        //TODO get allMatchGrades funziona in maniera diversa ora
		List<Grade> allMatchGrades = gradeRepository.getAllMatchGrades(matchDay);
		assertThat(allMatchGrades.size()).isEqualTo(2);
		assertThat(allMatchGrades.get(0).getMark() + allMatchGrades.get(1).getMark()).isEqualTo(6.0 + 8.0);
		
	}

	@Test
	@DisplayName("saveGrade should persist correctly")
	void testSaveGradePersistsCorrectly() {

		entityManager.getTransaction().begin();

		Player totti = new Player.Forward("Francesco", "Totti", Club.ROMA);
		Grade grade = new Grade(totti, matchDay, 9.0);

		entityManager.persist(totti);
		gradeRepository.saveGrade(grade);

		entityManager.getTransaction().commit();
	    entityManager.clear();

        //TODO ricontrollare cosa fa
		sessionFactory.inTransaction((Session em) -> {
			Optional<Grade> result = em
					.createQuery("SELECT g FROM Grade g " + "WHERE g.player = :player " + "AND g.matchDay = :matchDay "
							, Grade.class)
					.setParameter("player", totti).setParameter("matchDay", matchDay)
					.getResultStream().findFirst();

			assertThat(result).isPresent();
			Grade found = result.get();
			assertThat(found.getPlayer()).isEqualTo(totti);
		});

	}

}
