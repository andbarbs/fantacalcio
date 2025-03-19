package hibernateDAL;



import static org.assertj.core.api.Assertions.*;

import java.util.EnumSet;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domainModel.Giocatore;

class HibernateGiocatoreRepositoryTest {

	private static StandardServiceRegistry serviceRegistry;
	private static Metadata metadata;
	private static SessionFactory sessionFactory;

	private HibernateGiocatoreRepository giocatoreRepository;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			serviceRegistry = new StandardServiceRegistryBuilder()
				.configure("hibernate-test.cfg.xml")
				.build();

			metadata = new MetadataSources(serviceRegistry)
			.addAnnotatedClass(Giocatore.class)
			.getMetadataBuilder()
			.build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

//	@BeforeEach
//	void setup() {
//		// H2-specific schema dropping to ensure tests work on a fresh database
//		// without having to recreate the SessionFactory instance
//		sessionFactory.inTransaction(
//				session -> session.createMutationQuery("TRUNCATE SCHEMA public AND COMMIT").executeUpdate());
//		
//		// Instantiates the SUT using the class-bound SessionFactory
//		giocatoreRepository = new HibernateGiocatoreRepository(sessionFactory);
//	}
	
	@BeforeEach
	void setup() {
		
		System.out.println("executing the beforeEach step");
		// Hibernate native schema dropping to ensure tests work on a fresh database
		// without having to recreate the SessionFactory instance
		SchemaExport schemaExport = new SchemaExport();

		// Drop the existing schema
		schemaExport.drop(EnumSet.of(TargetType.DATABASE), metadata);

		// Recreate the schema
		schemaExport.create(EnumSet.of(TargetType.DATABASE), metadata);

		// Instantiates the SUT using the class-bound SessionFactory
		giocatoreRepository = new HibernateGiocatoreRepository(sessionFactory);
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}
	
	@Test
	public void testNoPlayersExist(){
		System.out.println("actually executing test method!!!");
		assertThat(giocatoreRepository.getAllGiocatori()).isEmpty();
	}

	

}
