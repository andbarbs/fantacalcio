package jpaRepositories;

import domainModel.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HibernateGradeRepositoryTest {

	private static SessionFactory sessionFactory;

	private JpaGradeRepository gradeRepository;

	private EntityManager entityManager;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(Grade.class)
					.addAnnotatedClass(Player.class)
					.addAnnotatedClass(Player.Goalkeeper.class)
					.addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(NewsPaper.class)
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

		// Instantiates the SUT using the static SessionFactory
		entityManager = sessionFactory.createEntityManager();
		gradeRepository = new JpaGradeRepository(entityManager);
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}
	
//	@Test
//	@DisplayName("getAllGrades() on an empty table")
//	public void testNoGradesExist(){
//		EntityManager repositorySession = sessionFactory.createEntityManager();
//		assertThat(gradeRepository.getAllMatchGrades()).isEmpty();
//		repositorySession.close();
//	}

	@Test
	@DisplayName("getAllGrades() when two grades have been persisted") public
	void testTwoGradesExist(){ MatchDaySerieA day1 = new
			MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12)); MatchDaySerieA
			day2 = new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 8, 12));
		NewsPaper gazzetta = new NewsPaper("Gazzetta"); Player buffon = new
				Player.Goalkeeper("Gigi", "Buffon");

		Grade voto1 = new Grade(buffon, day1, 6.0, 0, 1, gazzetta); Grade voto2 = new
				Grade(buffon, day2, 8.0, 2, 1, gazzetta);

		sessionFactory.inTransaction(session -> { session.persist(day1);
			session.persist(day2); session.persist(buffon); session.persist(gazzetta);
			session.persist(voto1); session.persist(voto2);});

		EntityManager repositorySession = sessionFactory.createEntityManager();
		/*assertThat(gradeRepository.getAllMatchGrades()).
				containsExactly(voto1, voto2); repositorySession.close(); */
	}

}
