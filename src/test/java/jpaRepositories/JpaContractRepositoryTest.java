package jpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
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
import domainModel.LineUp;
import domainModel.Match;
import domainModel.NewsPaper;
import domainModel.Player;
import domainModel._433LineUp;
import domainModel.Player.Defender;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel.Player.Midfielder;
import jakarta.persistence.EntityManager;

@DisplayName("tests for HibernateContractRepository")
class JpaContractRepositoryTest {

	private static SessionFactory sessionFactory;

	private JpaContractRepository contractRepository;

	private EntityManager entityManager;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(Contract.class).addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(Player.class).addAnnotatedClass(Player.Forward.class)
					.addAnnotatedClass(Player.Defender.class).addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(NewsPaper.class).addAnnotatedClass(League.class)
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
	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}
	
	@Test
	@DisplayName("getContract() when contract doesn't exist")
	public void testGetContractWithNoContractExisting(){
		
		FantaUser user = new FantaUser("email", "pswd");
		NewsPaper newspaper = new NewsPaper("Gazzetta");
		League league = new League(user, "Lega", newspaper, "1234");
		FantaTeam team = new FantaTeam("Dream Team", league, 10, user, new HashSet<Contract>());
		Player player = new Player.Forward("Lionel", "Messi");
		
		sessionFactory.inTransaction(session -> {
			session.persist(user);
			session.persist(newspaper);
			session.persist(league);
			session.persist(team);
			session.persist(player);
		});
		
		assertThat(contractRepository.getContract(team, player)).isEmpty();
	}
	
	@Test
	@DisplayName("getContract() when contract exists")
	public void testGetContractWithContractExisting(){
		
		FantaUser user = new FantaUser("email", "pswd");
		NewsPaper newspaper = new NewsPaper("Gazzetta");
		League league = new League(user, "Lega", newspaper, "1234");
		Player player1 = new Player.Forward("Lionel", "Messi");
		Player player2 = new Player.Defender("Giorgio", "Chiellini");
		FantaTeam team = new FantaTeam("Dream Team", league, 10, user, new HashSet<Contract>());
		Contract contract1 = new Contract(team, player1);
		Contract contract2 = new Contract(team, player2);
				
		sessionFactory.inTransaction(session -> {
			session.persist(user);
			session.persist(newspaper);
			session.persist(league);
			session.persist(player1);
			session.persist(player2);
			session.persist(team);
			session.persist(contract1);
			session.persist(contract2);
		});
		
		team.setContracts(Set.of(contract1, contract2));

		assertThat(contractRepository.getContract(team, player1).get()).isEqualTo(contract1);
		assertThat(contractRepository.getContract(team, player2).get()).isEqualTo(contract2);
	}
	
	// TODO Errore: transaction required, executing an update/delete query?????
	@Test
	@DisplayName("deleteContract() when contract doesn't exist")
	public void testDeleteContractWithNoContractExisting(){
		
		FantaUser user = new FantaUser("email", "pswd");
		NewsPaper newspaper = new NewsPaper("Gazzetta");
		League league = new League(user, "Lega", newspaper, "1234");
		FantaTeam team = new FantaTeam("Dream Team", league, 10, user, new HashSet<Contract>());
		Player player = new Player.Forward("Lionel", "Messi");
		
		sessionFactory.inTransaction(session -> {
			session.persist(user);
			session.persist(newspaper);
			session.persist(league);
			session.persist(team);
			session.persist(player);
		});
		
		sessionFactory.inTransaction((Session em) -> {
			contractRepository.deleteContract(new Contract(team, player));
			
			Optional<Contract> result = em
					.createQuery("FROM Contract l WHERE l.player = :player AND l.team = :team", Contract.class)
					.setParameter("player", player).setParameter("team", team).getResultStream().findFirst();

			assertThat(result).isEmpty();
		});
		
	}
	
	// TODO Errore: transaction required, executing an update/delete query?????
	@Test
	@DisplayName("deleteContract() when contract exists")
	public void testDeleteContractWithContractExisting(){
		
		FantaUser user = new FantaUser("email", "pswd");
		NewsPaper newspaper = new NewsPaper("Gazzetta");
		League league = new League(user, "Lega", newspaper, "1234");
		Player player = new Player.Forward("Lionel", "Messi");
		FantaTeam team = new FantaTeam("Dream Team", league, 10, user, new HashSet<Contract>());
		Contract contract = new Contract(team, player);
				
		sessionFactory.inTransaction(session -> {
			session.persist(user);
			session.persist(newspaper);
			session.persist(league);
			session.persist(player);
			session.persist(team);
			session.persist(contract);
		});
		
		team.setContracts(Set.of(contract));
		
		sessionFactory.inTransaction((Session em) -> {
			contractRepository.deleteContract(contract);
			
			Optional<Contract> result = em
					.createQuery("FROM Contract l WHERE l.player = :player AND l.team = :team", Contract.class)
					.setParameter("player", player).setParameter("team", team).getResultStream().findFirst();

			assertThat(result).isEmpty();
		});
	}
	
	@Test
	@DisplayName("saveContract should persist correctly")
	void testSaveContractPersistsCorrectly() {
		
		FantaUser user = new FantaUser("email", "pswd");
		NewsPaper newspaper = new NewsPaper("Gazzetta");
		League league = new League(user, "Lega", newspaper, "1234");
		Player player = new Player.Forward("Lionel", "Messi");
		FantaTeam team = new FantaTeam("Dream Team", league, 10, user, new HashSet<Contract>());
		Contract contract = new Contract(team, player);
				
		sessionFactory.inTransaction(session -> {
			session.persist(user);
			session.persist(newspaper);
			session.persist(league);
			session.persist(player);
			session.persist(team);
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
