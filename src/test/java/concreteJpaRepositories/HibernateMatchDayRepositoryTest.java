package concreteJpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

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

import domainModel.MatchDaySerieA;
import jakarta.persistence.EntityManager;

class HibernateMatchDayRepositoryTest {

	private static SessionFactory sessionFactory;

	private JpaMatchDayRepository matchDayRepository;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.configure("hibernate-test.cfg.xml")
				.build();

			Metadata metadata = new MetadataSources(serviceRegistry)
			.addAnnotatedClass(MatchDaySerieA.class)
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
		matchDayRepository = new JpaMatchDayRepository(sessionFactory);
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}
	
	@Test
	@DisplayName("getAllMatchDays() on an empty table")
	public void testNoMatchDaysExist(){
		EntityManager repositorySession = sessionFactory.createEntityManager();
		assertThat(matchDayRepository.getAllMatchDays(repositorySession)).isEmpty();
		repositorySession.close();	
	}
	
	@Test
	@DisplayName("getAllMatchDays() when two days have been persisted")
	public void testTwoMatchDaysExist(){		
		MatchDaySerieA day1 = new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12));
		MatchDaySerieA day2 = new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 8, 12));
		
		sessionFactory.inTransaction(session -> {
			session.persist(day1);
			session.persist(day2);});
		
		EntityManager repositorySession = sessionFactory.createEntityManager();
		assertThat(matchDayRepository.getAllMatchDays(repositorySession)).containsExactly(day1, day2);
		repositorySession.close();	
	}

}
