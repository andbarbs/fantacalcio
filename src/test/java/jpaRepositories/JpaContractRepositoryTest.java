package jpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
import org.junit.jupiter.api.Test;

import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.FantaUser;
import domainModel.League;
import domainModel.NewsPaper;
import domainModel.Player;
import domainModel.Player.Forward;
import jakarta.persistence.EntityManager;

@DisplayName("tests for HibernateContractRepository")
class JpaContractRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaContractRepository contractRepository;
	private EntityManager entityManager;
	private FantaUser user;
	private NewsPaper newspaper;
	private FantaTeam team;
	private League league;
	private Forward player;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(Contract.class)
					.addAnnotatedClass(FantaTeam.class).addAnnotatedClass(Player.class)
					.addAnnotatedClass(Player.Forward.class).addAnnotatedClass(Player.Defender.class)
					.addAnnotatedClass(FantaUser.class).addAnnotatedClass(NewsPaper.class)
					.addAnnotatedClass(League.class).getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	// TODO I test ci mettono molto rispetto ad altre classi
	@BeforeEach
	void setup() {
		sessionFactory.getSchemaManager().truncateMappedObjects();
		entityManager = sessionFactory.createEntityManager();
		contractRepository = new JpaContractRepository(entityManager);
		
		sessionFactory.inTransaction(t -> {
			user = new FantaUser("manager@example.com", "securePass");
			t.persist(user);
			newspaper = new NewsPaper("Gazzetta");
			t.persist(newspaper);
			league = new League(user, "Lega", newspaper, "1234");
			t.persist(league);
			team = new FantaTeam("Dream Team", league, 10, user, new HashSet<Contract>());
			t.persist(team);
			player = new Player.Forward("Lionel", "Messi");
			t.persist(player);
		});
		
	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("getContract() when contract doesn't exist")
	public void testGetContractWithNoContractExisting() {

		assertThat(contractRepository.getContract(team, player)).isEmpty();
	}

	@Test
	@DisplayName("getContract() when contract exists")
	public void testGetContractWithContractExisting() {


		Player player2 = new Player.Defender("Giorgio", "Chiellini");
		Contract contract1 = new Contract(team, player);
		Contract contract2 = new Contract(team, player2);

		sessionFactory.inTransaction(session -> {
			session.persist(player2);
			session.persist(contract1);
			session.persist(contract2);
		});

		team.setContracts(Set.of(contract1, contract2));

		assertThat(contractRepository.getContract(team, player).get()).isEqualTo(contract1);
		assertThat(contractRepository.getContract(team, player2).get()).isEqualTo(contract2);
	}

	@Test
	@DisplayName("deleteContract() when contract doesn't exist")
	public void testDeleteContractWithNoContractExisting() {

		entityManager.getTransaction().begin();

		contractRepository.deleteContract(new Contract(team, player));

		Optional<Contract> result = entityManager
				.createQuery("FROM Contract l WHERE l.player = :player AND l.team = :team", Contract.class)
				.setParameter("player", player).setParameter("team", team).getResultStream().findFirst();

		assertThat(result).isEmpty();
		
		entityManager.clear();

	}

	@Test
	@DisplayName("deleteContract() when contract exists")
	public void testDeleteContractWithContractExisting() {

		entityManager.getTransaction().begin();

		Contract contract = new Contract(team, player);

		entityManager.persist(contract);

		team.setContracts(Set.of(contract));

		contractRepository.deleteContract(contract);

		Optional<Contract> result = entityManager
				.createQuery("FROM Contract l WHERE l.player = :player AND l.team = :team", Contract.class)
				.setParameter("player", player).setParameter("team", team).getResultStream().findFirst();

		assertThat(result).isEmpty();

		entityManager.clear();
		
	}

	@Test
	@DisplayName("saveContract should persist correctly")
	void testSaveContractPersistsCorrectly() {

		Contract contract = new Contract(team, player);

		sessionFactory.inTransaction(session -> {
			session.persist(contract);
		});

		sessionFactory.inTransaction((Session em) -> {
			Optional<Contract> result = em
					.createQuery("FROM Contract l WHERE l.player = :player AND l.team = :team", Contract.class)
					.setParameter("player", player).setParameter("team", team).getResultStream().findFirst();

			assertThat(result).isPresent();
			Contract found = result.get();
			assertThat(found.getTeam()).isEqualTo(team);
			assertThat(found.getPlayer()).isEqualTo(player);
		});

	}

}
