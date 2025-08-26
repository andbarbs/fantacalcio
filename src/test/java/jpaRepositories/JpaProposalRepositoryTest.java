package jpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashSet;
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
import domainModel.Proposal;
import jakarta.persistence.EntityManager;

class JpaProposalRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaProposalRepository proposalRepository;
	private EntityManager entityManager;
	private FantaUser admin;
	private NewsPaper newspaper;
	private League league;
	private FantaTeam team;
	private FantaUser user;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(Proposal.class)
					.addAnnotatedClass(Proposal.PendingProposal.class).addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(Player.class).addAnnotatedClass(Player.Forward.class)
					.addAnnotatedClass(Player.Midfielder.class).addAnnotatedClass(Contract.class)
					.addAnnotatedClass(FantaUser.class).addAnnotatedClass(NewsPaper.class)
					.addAnnotatedClass(League.class).addAnnotatedClass(FantaTeam.class)
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
		proposalRepository = new JpaProposalRepository(entityManager);

		sessionFactory.inTransaction(t -> {
			admin = new FantaUser("adminMail", "adminPswd");
			t.persist(admin);
			newspaper = new NewsPaper("gazzetta");
			t.persist(newspaper);
			league = new League(admin, "lega", newspaper, "1234");
			t.persist(league);
			user = new FantaUser("userMail", "userPswd");
			t.persist(user);
			team = new FantaTeam("team1", league, 0, admin, new HashSet<Contract>());
			t.persist(team);
		});

	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}

	// TODO implement all tests
	@Test
	@DisplayName("deleteProposal() when the requested proposal is not present")
	void testDeleteProposalWithoutProposal() {

		entityManager.getTransaction().begin();

		FantaTeam team2 = new FantaTeam("team2", league, 0, user, new HashSet<Contract>());
		Player player1 = new Player.Forward("Francesco", "Totti", "Roma");
		Player player2 = new Player.Midfielder("Kevin", "De Bruyne", "Napoli");
		Contract offeredContract = new Contract(team, player1);
		Contract requestedContract = new Contract(team2, player2);

		sessionFactory.inTransaction(session -> {
			session.persist(team2);
			session.persist(player1);
			session.persist(player2);
			session.persist(offeredContract);
			session.persist(requestedContract);
		});

		Proposal.PendingProposal proposal = new Proposal.PendingProposal(offeredContract, requestedContract);

		assertThat(proposalRepository.deleteProposal(proposal)).isFalse();

		entityManager.close();

	}

	@Test
	@DisplayName("deleteProposal() when the requested proposal is present")
	void testDeleteProposalWithExistingProposal() {

		entityManager.getTransaction().begin();

		FantaTeam team2 = new FantaTeam("team2", league, 0, user, new HashSet<Contract>());
		Player player1 = new Player.Forward("Francesco", "Totti", "Roma");
		Player player2 = new Player.Midfielder("Kevin", "De Bruyne", "Napoli");
		Contract offeredContract = new Contract(team, player1);
		Contract requestedContract = new Contract(team2, player2);

		sessionFactory.inTransaction(session -> {
			session.persist(team2);
			session.persist(player1);
			session.persist(player2);
			session.persist(offeredContract);
			session.persist(requestedContract);
		});

		Proposal.PendingProposal proposal = new Proposal.PendingProposal(offeredContract, requestedContract);
		entityManager.persist(proposal);

		assertThat(proposalRepository.deleteProposal(proposal)).isTrue();

		entityManager.close();
	}

	@Test
	@DisplayName("getMyProposals() when there is no proposal for that team")
	void testGetMyProposalsWithoutProposals() {

		assertThat(proposalRepository.getMyProposals(league, team)).isEmpty();

	}

	@Test
	@DisplayName("getMyProposals() when there are some proposals for that team")
	void testGetMyProposalsWithSomeProposals() {

		FantaUser user2 = new FantaUser("userMail2", "userPswd3");
		FantaTeam team2 = new FantaTeam("team2", league, 0, user2, new HashSet<Contract>());
		FantaUser user3 = new FantaUser("userMail3", "userPswd3");
		FantaTeam team3 = new FantaTeam("team3", league, 0, user3, new HashSet<Contract>());
		Player player1 = new Player.Forward("Francesco", "Totti", "Roma");
		Player player2 = new Player.Midfielder("Kevin", "De Bruyne", "Napoli");
		Contract offeredContract = new Contract(team, player1);
		Contract requestedContract = new Contract(team2, player2);
		Contract offeredContract2 = new Contract(team, player1);
		Contract requestedContract2 = new Contract(team3, player2);
		Proposal.PendingProposal proposal1 = new Proposal.PendingProposal(offeredContract, requestedContract);
		Proposal.PendingProposal proposal2 = new Proposal.PendingProposal(offeredContract2, requestedContract2);
		
		sessionFactory.inTransaction(session -> {
			session.persist(user2);
			session.persist(team2);
			session.persist(user3);
			session.persist(team3);
			session.persist(player1);
			session.persist(player2);
			session.persist(offeredContract);
			session.persist(requestedContract);
			session.persist(offeredContract2);
			session.persist(requestedContract2);
			session.persist(proposal1);
			session.persist(proposal2);
		});

		assertThat(proposalRepository.getMyProposals(league, team)).containsExactlyInAnyOrder(proposal1, proposal2);
		
	}

	@Test
	@DisplayName("saveProposal() should persist correctly")
	void testSaveProposal() {

		entityManager.getTransaction().begin();

		FantaTeam team2 = new FantaTeam("team2", league, 0, user, new HashSet<Contract>());
		Player player1 = new Player.Forward("Francesco", "Totti", "Roma");
		Player player2 = new Player.Midfielder("Kevin", "De Bruyne", "Napoli");
		Contract offeredContract = new Contract(team, player1);
		Contract requestedContract = new Contract(team2, player2);

		sessionFactory.inTransaction(session -> {
			session.persist(team2);
			session.persist(player1);
			session.persist(player2);
			session.persist(offeredContract);
			session.persist(requestedContract);
		});

		Proposal proposal = new Proposal.PendingProposal(offeredContract, requestedContract);
		proposalRepository.saveProposal(proposal);

		entityManager.createQuery(
				"FROM Proposal p WHERE p.offeredContract = :offeredContract AND p.requestedContract = :requestedContract",
				Proposal.class).setParameter("offeredContract", offeredContract)
				.setParameter("requestedContract", requestedContract).getResultStream().findFirst();

		entityManager.close();

	}

	@Test
	@DisplayName("getProposal() with contracts that don't have a proposal")
	void testGetProposalWhenThereIsNoProposal() {
		
		FantaTeam team2 = new FantaTeam("team2", league, 0, user, new HashSet<Contract>());
		Player player1 = new Player.Forward("Francesco", "Totti", "Roma");
		Player player2 = new Player.Midfielder("Kevin", "De Bruyne", "Napoli");
		Contract offeredContract = new Contract(team, player1);
		Contract requestedContract = new Contract(team2, player2);

		sessionFactory.inTransaction(session -> {
			session.persist(team2);
			session.persist(player1);
			session.persist(player2);
			session.persist(offeredContract);
			session.persist(requestedContract);
		});
		
		assertThat(proposalRepository.getProposal(offeredContract, requestedContract)).isEmpty();
		
	}

	@Test
	@DisplayName("getProposal() with contracts that have a proposal")
	void testGetProposalWhenContractsHaveProposal() {

		FantaTeam team2 = new FantaTeam("team2", league, 0, user, new HashSet<Contract>());
		Player player1 = new Player.Forward("Francesco", "Totti", "Roma");
		Player player2 = new Player.Midfielder("Kevin", "De Bruyne", "Napoli");
		Contract offeredContract = new Contract(team, player1);
		Contract requestedContract = new Contract(team2, player2);
		Proposal.PendingProposal proposal = new Proposal.PendingProposal(offeredContract, requestedContract);

		sessionFactory.inTransaction(session -> {
			session.persist(team2);
			session.persist(player1);
			session.persist(player2);
			session.persist(offeredContract);
			session.persist(requestedContract);
			session.persist(proposal);
		});
		
		assertThat(proposalRepository.getProposal(offeredContract, requestedContract))
				.isPresent()
				.get().isEqualTo(proposal);

	}

}
