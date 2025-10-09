package dal.repository.jpa;

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
			player = new Player.Forward("Lionel", "Messi", Club.PISA);
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

		Player player2 = new Player.Defender("Giorgio", "Chiellini", Club.JUVENTUS);
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

		entityManager.close();

	}

	@Test
	@DisplayName("deleteContract() when contract exists")
	public void testDeleteContractWithContractExisting() {

		entityManager.getTransaction().begin();

		Contract contract = new Contract(team, player);

		entityManager.persist(contract);

		team.setContracts(Set.of(contract));
		
		entityManager.getTransaction().commit();
	    entityManager.clear();

	    entityManager.getTransaction().begin();
		contractRepository.deleteContract(contract);
		entityManager.getTransaction().commit();
	    entityManager.clear();

		Optional<Contract> result = entityManager
				.createQuery("FROM Contract l WHERE l.player = :player AND l.team = :team", Contract.class)
				.setParameter("player", player).setParameter("team", team).getResultStream().findFirst();

		assertThat(result).isEmpty();

		entityManager.close();

	}

	@Test
	@DisplayName("saveContract should persist correctly")
	void testSaveContractPersistsCorrectly() {

		entityManager.getTransaction().begin();
		
		Contract contract = new Contract(team, player);

		contractRepository.saveContract(contract);
		
		entityManager.getTransaction().commit();
		entityManager.clear();
		
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
