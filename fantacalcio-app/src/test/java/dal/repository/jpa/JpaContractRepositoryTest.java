package dal.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import domain.Contract;
import domain.FantaTeam;
import domain.FantaUser;
import domain.League;
import domain.Player;
import domain.Player.Club;
import domain.Player.Forward;
import jakarta.persistence.EntityManager;

class JpaContractRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaContractRepository contractRepository;
	private EntityManager entityManager;
	private FantaUser user;
	private FantaTeam team;
	private League league;
	private Forward player;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(Contract.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(Player.class)
					.addAnnotatedClass(Player.Forward.class)
					.addAnnotatedClass(Player.Defender.class)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(League.class)
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
		contractRepository = new JpaContractRepository(entityManager);

		sessionFactory.inTransaction(t -> {
			user = new FantaUser("manager@example.com", "securePass");
			t.persist(user);
			league = new League(user, "Lega", "1234");
			t.persist(league);
			team = new FantaTeam("Dream Team", league, 10, user, new HashSet<Contract>());
			t.persist(team);
			player = new Player.Forward("Lionel", "Messi", Club.PISA);
			t.persist(player);
		});

	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}
	
	@Nested
	@DisplayName("can look up a Contract from the database")
	class Retrieval {	
		
		@Test
		@DisplayName("when the Contract does not exist in the database")
		public void testGetContractWithNoContractExisting() {
			
			// GIVEN no Contract for test Player under test Team was persisted
			
			// WHEN the SUT is used to retrieve a Contract for test Player under test Team
			entityManager.getTransaction().begin();
			Optional<Contract> retrieved = contractRepository.getContract(team, player);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN an empty Optional is returned
			assertThat(retrieved).isEmpty();		
		}
		
		@Test
		@DisplayName("when the Contract exists in the database")
		public void testGetContractWithContractExisting() {
			
			// GIVEN two Contracts are instantiated
			Contract contract1 = new Contract(team, player);
			Player player2 = new Player.Defender("Giorgio", "Chiellini", Club.JUVENTUS);
			Contract contract2 = new Contract(team, player2);
			
			// AND are manually persisted
			sessionFactory.inTransaction(session -> {
				session.persist(player2);
				session.persist(contract1);
				session.persist(contract2);
			});
			
			// WHEN the SUT is used to retrieve the Contracts
			entityManager.getTransaction().begin();
			Optional<Contract> retrieved1 = contractRepository.getContract(team, player);
			Optional<Contract> retrieved2 = contractRepository.getContract(team, player2);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN Contracts are retrieved correctly
			assertThat(retrieved1).hasValue(contract1);
			assertThat(retrieved2).hasValue(contract2);	
		}
	}

	@Nested
	@DisplayName("can delete a Contract from the database")
	class Deletion {	
		
		@Test
		@DisplayName("when the Contract does not exist in the database")
		public void testDeleteContractWithNoContractExisting() {
			
			// GIVEN no Contract has been persisted
			
			// WHEN the SUT is used to delete a non-persisted Contract
			entityManager.getTransaction().begin();
			contractRepository.deleteContract(new Contract(team, player));
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN no Contracts exist in the database
			assertThat(sessionFactory.fromTransaction(
					(Session em) -> em.createQuery("FROM Contract", Contract.class).getResultStream().toList())).isEmpty();
		}
		
		@Test
		@DisplayName("when the Contract exists in the database")
		public void testDeleteContractWithContractExisting() {
			
			// GIVEN a Contract is persisted
			Contract contract = new Contract(team, player);		
			sessionFactory.inTransaction(em -> entityManager.persist(contract));
			
			// WHEN the SUT is used to delete the Contract
			entityManager.getTransaction().begin();
			contractRepository.deleteContract(contract);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN the Contract is removed from the db
			assertThat(sessionFactory.fromTransaction((Session em) -> em
					.createQuery("FROM Contract l WHERE l.player = :player AND l.team = :team", Contract.class)
					.setParameter("player", player).setParameter("team", team).getResultStream().toList())).isEmpty();
		}
	}
	

	@Test
	@DisplayName("can persist a Contract to the database")
	void testSaveContractPersistsCorrectly() {

		// GIVEN a Contract is instantiated
		Contract contract = new Contract(team, player);

		// WHEN the SUT is used to save it
		entityManager.getTransaction().begin();
		contractRepository.saveContract(contract);
		entityManager.getTransaction().commit();
		entityManager.clear();

		// THEN the Contract is persisted to the db
		assertThat(sessionFactory.fromTransaction((Session em) -> em
				.createQuery("FROM Contract c " + "JOIN FETCH c.player "
						+ "JOIN FETCH c.team ct JOIN FETCH ct.league ctl JOIN FETCH ctl.admin "
						+ "WHERE c.player = :player AND c.team = :team", Contract.class)
				.setParameter("player", player).setParameter("team", team).getResultStream().findFirst()))
				.hasValue(contract);
	}
}
