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

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class JpaGradeRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaGradeRepository gradeRepository;
	private EntityManager entityManager;
	private FantaUser admin;
	private League league;
	private MatchDay matchDay;
	
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
					.addAnnotatedClass(MatchDay.class)
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
			admin = new FantaUser("admin@example.com", "securePass");
			t.persist(admin);
			league = new League(admin, "Serie A", "code");
			t.persist(league);
            matchDay = new MatchDay("1 Giornata", 1, MatchDay.Status.FUTURE, league);
            t.persist(matchDay);
		});
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}
	
	@Nested
	@DisplayName("can look up all Grades for a given MatchDay from the database")
	class Retrieval {
		
		@Test
		@DisplayName("when no Grades for the given MatchDay exist in the database")
		public void testGetAllMatchGradesWhenNoGradesExist() {
			
			// GIVEN no Grades exist for a given MatchDay
			Player player1 = new Player.Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);			
			Grade voto1 = new Grade(player1, matchDay, 6.0);
			
			MatchDay gradeless = new MatchDay("1 Giornata", 2, MatchDay.Status.FUTURE, league);
			
			sessionFactory.inTransaction(session -> {
				session.persist(player1);
				session.persist(voto1);
				session.persist(gradeless);
			});
			
			// WHEN the SUT is used to retrieve Grades for that MatchDay
			entityManager.getTransaction().begin();
			List<Grade> retrieved = gradeRepository.getAllMatchGrades(gradeless);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN an empty Set is returned
			assertThat(retrieved).isEmpty();
		}
		
		@Test
		@DisplayName("when some Grades for the given MatchDay exist in the database")
		public void testGetAllMatchGradesWhenTwoGradesExist() {
			
			// GIVEN some Grades for a given MatchDay exist in the database
			Player player1 = new Player.Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
			Player player2 = new Player.Forward("Gigi", "Riva", Club.CAGLIARI);
			MatchDay matchDay2 = new MatchDay("1 Giornata", 2, MatchDay.Status.FUTURE, league);
			
			Grade voto1 = new Grade(player1, matchDay, 6.0);
			Grade voto2 = new Grade(player2, matchDay2, 8.0);			
			
			sessionFactory.inTransaction(session -> {
				session.persist(player1);
				session.persist(player2);
				session.persist(matchDay2);
				session.persist(voto1);
				session.persist(voto2);
			});
			
			// WHEN the SUT is used to retrieve Grades for that MatchDay
			entityManager.getTransaction().begin();
			List<Grade> retrieved = gradeRepository.getAllMatchGrades(matchDay);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN only the expected Grades are retrieved
			assertThat(retrieved).containsExactlyInAnyOrder(voto1);
		}
	}


	@Test
	@DisplayName("can persist a Grade to the database")
	void testSaveGradePersistsCorrectly() {

		// GIVEN a Grade's ancillary entities are manually persisted
		Player totti = new Player.Forward("Francesco", "Totti", Club.ROMA);
		sessionFactory.inTransaction(session -> session.persist(totti));

		// WHEN the SUT is used to persist a Grade to the database
		Grade grade = new Grade(totti, matchDay, 9.0);
		entityManager.getTransaction().begin();
		gradeRepository.saveGrade(grade);
		entityManager.getTransaction().commit();
	    entityManager.clear();

	    // THEN the Grade is present in the database
	    List<Grade> result = sessionFactory.fromTransaction((Session em) -> em
				.createQuery("FROM Grade g "
						+ "JOIN FETCH g.player "
						+ "JOIN FETCH g.matchDay md JOIN FETCH md.league l JOIN FETCH l.admin"
						, Grade.class)
				.getResultStream().toList());
	    
	    assertThat(result).containsExactly(grade);
	}
}
