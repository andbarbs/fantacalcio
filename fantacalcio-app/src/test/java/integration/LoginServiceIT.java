package integration;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import business.LoginService;
import dal.transaction.jpa.JpaTransactionManager;
import domain.FantaUser;

class LoginServiceIT {

	private static SessionFactory sessionFactory;
	private LoginService loginService;
	private JpaTransactionManager transactionManager;

	@BeforeAll
	static void initializeSessionFactory() {

		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(FantaUser.class)
					.getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void setUp() {
		// empties out database tables
		sessionFactory.getSchemaManager().truncateMappedObjects();

		// instantiates SUT
		transactionManager = new JpaTransactionManager(sessionFactory);
		loginService = new LoginService(transactionManager);
	}

	@Test
	void testRegisterAndLoginFantaUser() {

		// WHEN
		loginService.registerFantaUser("mail", "pswd");

		// THEN
		Optional<FantaUser> result = transactionManager
				.fromTransaction(context -> context.getFantaUserRepository().getUser("mail", "pswd"));
		assertThat(result).hasValue(new FantaUser("mail", "pswd"));

	}

	@Test
	void testLoginFantaUser() {

		// GIVEN
		FantaUser user = new FantaUser("mail", "pswd");
		transactionManager.inTransaction(context -> context.getFantaUserRepository().saveFantaUser(user));

		// WHEN
		boolean loginResult = loginService.loginFantaUser("mail", "pswd");

		// THEN
		assertThat(loginResult).isTrue();
	}

}
