package hibernateDAL;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import domainModel.NewsPaper;
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

import domainModel.Grade;
import domainModel.MatchDaySerieA;
import domainModel.Player;

class HibernateGradeRepositoryTest {

	private static SessionFactory sessionFactory;

	private HibernateGradeRepository gradeRepository;

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
		gradeRepository = new HibernateGradeRepository(sessionFactory);
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}
	
	@Test
	@DisplayName("getAllGrades() on an empty table")
	public void testNoGradesExist(){
		assertThat(gradeRepository.getAllGrades()).isEmpty();
	}
	
	@Test
	@DisplayName("getAllGrades() when two grades have been persisted")
	public void testTwoGradesExist(){		
		MatchDaySerieA day1 = new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12));
		MatchDaySerieA day2 = new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 8, 12));
		NewsPaper gazzetta = new NewsPaper("Gazzetta");
		Player buffon = new Player.Goalkeeper("Gigi", "Buffon");
		
		Grade voto1 = new Grade(buffon, day1, 6.0, 0, 1, gazzetta);
		Grade voto2 = new Grade(buffon, day2, 8.0, 2, 1, gazzetta);
		
		
		sessionFactory.inTransaction(session -> {
			session.persist(day1);
			session.persist(day2);
			session.persist(buffon);
			session.persist(gazzetta);
			session.persist(voto1);
			session.persist(voto2);});
		
		assertThat(gradeRepository.getAllGrades()).containsExactly(voto1, voto2);
	}

}
