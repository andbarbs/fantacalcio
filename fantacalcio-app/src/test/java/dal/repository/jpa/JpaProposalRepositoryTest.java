package dal.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import domain.Contract;
import domain.FantaTeam;
import domain.FantaUser;
import domain.League;
import domain.Player;
import domain.Proposal;
import domain.Player.Club;
import jakarta.persistence.EntityManager;

class JpaProposalRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaProposalRepository proposalRepository;
	private EntityManager entityManager;
	
	// setup entities
	private FantaUser admin;
	private League league;
	private FantaUser user;
	private FantaTeam team;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(Proposal.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(Player.class)
					.addAnnotatedClass(Player.Forward.class)
					.addAnnotatedClass(Player.Midfielder.class)
					.addAnnotatedClass(Contract.class)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(FantaTeam.class)
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
			league = new League(admin, "lega", "1234");
			t.persist(league);
			user = new FantaUser("userMail", "userPswd");
			t.persist(user);
			team = new FantaTeam("team1", league, 0, admin, null);
			t.persist(team);
		});

	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}
	
	@Nested
	@DisplayName("can delete a Proposal instance from the database")
	class Deleting {	
		
		@Test
		@DisplayName("when the Proposal does not exist in the database")
		void testDeleteProposalWithoutProposal() {
			
			// GIVEN a Proposal's ancillary entities are manually persisted, but no Proposal is
			FantaTeam team2 = new FantaTeam("team2", league, 0, user, null);
			Player player1 = new Player.Forward("Francesco", "Totti", Club.ROMA);
			Player player2 = new Player.Midfielder("Kevin", "De Bruyne", Club.NAPOLI);
			Contract offeredContract = new Contract(team, player1);
			Contract requestedContract = new Contract(team2, player2);
			
			sessionFactory.inTransaction(session -> {
				session.persist(team2);
				session.persist(player1);
				session.persist(player2);
				session.persist(offeredContract);
				session.persist(requestedContract);
			});
			
			// WHEN the SUT is used to delete a Proposal that does not exist in the database
			entityManager.getTransaction().begin();
			boolean returned = proposalRepository.deleteProposal(
					new Proposal(offeredContract, requestedContract));
			entityManager.getTransaction().commit();
			entityManager.close();
			
			// THEN false is returned
			assertThat(returned).isFalse();
		}
		
		@Test
		@DisplayName("when the Proposal exists in the database")
		void testDeleteProposalWithExistingProposal() {
			
			// GIVEN a Proposal is manually persisted
			FantaTeam team2 = new FantaTeam("team2", league, 0, user, null);
			Player player1 = new Player.Forward("Francesco", "Totti", Club.ROMA);
			Player player2 = new Player.Midfielder("Kevin", "De Bruyne", Club.NAPOLI);
			Contract offeredContract = new Contract(team, player1);
			Contract requestedContract = new Contract(team2, player2);
			Proposal proposal = new Proposal(offeredContract, requestedContract);
			
			sessionFactory.inTransaction(session -> {
				session.persist(team2);
				session.persist(player1);
				session.persist(player2);
				session.persist(offeredContract);
				session.persist(requestedContract);
				session.persist(proposal);
			});
			
			// WHEN the SUT is used to delete a persisted Proposal
			entityManager.getTransaction().begin();
			boolean returned = proposalRepository.deleteProposal(proposal);
			entityManager.getTransaction().commit();
			entityManager.close();
			
			// THEN true is returned
			assertThat(returned).isTrue();
			
			// AND no Proposals exist in the Database
			assertThat(sessionFactory.fromTransaction(
					(Session em) -> em.createQuery("FROM Proposal", Proposal.class).getResultStream().toList())).isEmpty();
		}
	}

	@Nested
	@DisplayName("can retrieve all Proposal instances related to one Team")
	class LookupByTeam {	
		
		@Test
		@DisplayName("when no Proposal for that Team exists in the database")
		void testGetMyProposalsWithoutProposals() {
			
			// GIVEN no Proposals for team3 exist in the database
			FantaUser user2 = new FantaUser("userMail2", "userPswd3");
			FantaTeam team2 = new FantaTeam("team2", league, 0, user2, null);
			FantaUser user3 = new FantaUser("userMail3", "userPswd3");
			FantaTeam team3 = new FantaTeam("team3", league, 0, user3, null);
			Player player1 = new Player.Forward("Francesco", "Totti", Club.ROMA);
			Player player2 = new Player.Midfielder("Kevin", "De Bruyne", Club.NAPOLI);
			Player player3 = new Player.Midfielder("Luca", "Toni", Club.FIORENTINA);
			Contract contract1 = new Contract(team, player1);
			Contract contract2 = new Contract(team2, player2);
			Contract contract3 = new Contract(team3, player3);
			Proposal proposal12 = new Proposal(contract1, contract2);
			Proposal proposal21 = new Proposal(contract2, contract1);
			
			sessionFactory.inTransaction(session -> {
				session.persist(user2);
				session.persist(team2);
				session.persist(user3);
				session.persist(team3);
				session.persist(player1);
				session.persist(player2);
				session.persist(player3);
				session.persist(contract1);
				session.persist(contract2);
				session.persist(contract3);
				session.persist(proposal12);
				session.persist(proposal21);
			});
			
			// TODO change return to Set, or add Proposal ordering!
			// WHEN the SUT is used to retrieve proposals for team3		
			entityManager.getTransaction().begin();
			Set<Proposal> retrieved = proposalRepository.getProposalsFor(team3);
			entityManager.getTransaction().commit();
			entityManager.close();
			
			// THEN an empty Set is returned
			assertThat(retrieved).isEmpty();
		}
		
		@Test
		@DisplayName("when some Proposals for that Team exist in the database")
		void testGetMyProposalsWithSomeProposals() {
			
			// GIVEN two Proposals are manually persisted
			FantaUser user2 = new FantaUser("userMail2", "userPswd3");
			FantaTeam team2 = new FantaTeam("team2", league, 0, user2, null);
			FantaUser user3 = new FantaUser("userMail3", "userPswd3");
			FantaTeam team3 = new FantaTeam("team3", league, 0, user3, null);
			Player player1 = new Player.Forward("Francesco", "Totti", Club.ROMA);
			Player player2 = new Player.Midfielder("Kevin", "De Bruyne", Club.NAPOLI);
			Player player3 = new Player.Midfielder("Luca", "Toni", Club.FIORENTINA);
			Contract contract1 = new Contract(team, player1);
			Contract contract2 = new Contract(team2, player2);
			Contract contract3 = new Contract(team3, player3);
			Proposal proposal12 = new Proposal(contract1, contract2);
			Proposal proposal13 = new Proposal(contract1, contract3);
			Proposal toBeIgnored = new Proposal(contract2, contract3);
			
			sessionFactory.inTransaction(session -> {
				session.persist(user2);
				session.persist(team2);
				session.persist(user3);
				session.persist(team3);
				session.persist(player1);
				session.persist(player2);
				session.persist(player3);
				session.persist(contract1);
				session.persist(contract2);
				session.persist(contract3);
				session.persist(proposal12);
				session.persist(proposal13);
				session.persist(toBeIgnored);
			});
			
			// WHEN the SUT is used to retrieve Proposals for a given Team
			entityManager.getTransaction().begin();
			Set<Proposal> retrieved = proposalRepository.getProposalsFor(team);
			entityManager.getTransaction().commit();
			entityManager.close();
			
			// THEN only the expected Proposals are retrieved
			assertThat(retrieved).containsExactlyInAnyOrder(proposal12, proposal13);		
		}
	}

	@Test
	@DisplayName("can persist a Proposal instance to the database")
	void testSaveProposal() {
		
		// GIVEN a Proposal's ancillary entities are manually persisted
		FantaTeam team2 = new FantaTeam("team2", league, 0, user, null);
		Player player1 = new Player.Forward("Francesco", "Totti", Club.ROMA);
		Player player2 = new Player.Midfielder("Kevin", "De Bruyne", Club.NAPOLI);
		Contract offeredContract = new Contract(team, player1);
		Contract requestedContract = new Contract(team2, player2);
		
		sessionFactory.inTransaction(session -> {
			session.persist(team2);
			session.persist(player1);
			session.persist(player2);
			session.persist(offeredContract);
			session.persist(requestedContract);
		});
		
		// WHEN the SUT is used to persist a Proposal
		Proposal proposal = new Proposal(offeredContract, requestedContract);
		entityManager.getTransaction().begin();
		proposalRepository.saveProposal(proposal);
		entityManager.getTransaction().commit();
		entityManager.close();
		
		// THEN the Proposal is present in the database
		assertThat(sessionFactory.fromTransaction((Session em) -> em
				.createQuery("FROM Proposal p " 
						+ "JOIN FETCH p.offeredContract o JOIN FETCH p.requestedContract r "
						+ "JOIN FETCH o.player JOIN FETCH r.player " 
						+ "JOIN FETCH o.team ot JOIN FETCH r.team rt "
						+ "JOIN FETCH ot.fantaManager JOIN FETCH ot.league ol JOIN FETCH ol.admin "
						+ "JOIN FETCH rt.fantaManager JOIN FETCH rt.league rl JOIN FETCH rl.admin ", Proposal.class)
				.getResultStream().toList())).containsExactly(proposal);
	}
	

	@Nested
	@DisplayName("can retrieve a Proposal instance given its Contracts")
	class LookupByContracts {	
		
		@Test
		@DisplayName("when no Proposal for the given Contracts exists in the database")
		void testGetProposalWhenThereIsNoProposal() {
			
			// GIVEN two Proposals are manually persisted
			FantaUser user2 = new FantaUser("userMail2", "userPswd3");
			FantaTeam team2 = new FantaTeam("team2", league, 0, user2, null);
			FantaUser user3 = new FantaUser("userMail3", "userPswd3");
			FantaTeam team3 = new FantaTeam("team3", league, 0, user3, null);
			Player player1 = new Player.Forward("Francesco", "Totti", Club.ROMA);
			Player player2 = new Player.Midfielder("Kevin", "De Bruyne", Club.NAPOLI);
			Player player3 = new Player.Midfielder("Luca", "Toni", Club.FIORENTINA);
			Contract contract1 = new Contract(team, player1);
			Contract contract2 = new Contract(team2, player2);
			Contract contract3 = new Contract(team3, player3);
			Proposal proposal12 = new Proposal(contract1, contract2);
			Proposal proposal13 = new Proposal(contract1, contract3);
			
			sessionFactory.inTransaction(session -> {
				session.persist(user2);
				session.persist(team2);
				session.persist(user3);
				session.persist(team3);
				session.persist(player1);
				session.persist(player2);
				session.persist(player3);
				session.persist(contract1);
				session.persist(contract2);
				session.persist(contract3);
				session.persist(proposal12);
				session.persist(proposal13);
			});
			
			// WHEN the SUT is used to retrieve Proposals that do not exist in the database
			entityManager.getTransaction().begin();
			Optional<Proposal> nonExistentForContracts = proposalRepository.getProposalBy(contract2, contract3);
			Optional<Proposal> existsOtherWayRound = proposalRepository.getProposalBy(contract2, contract1);
			entityManager.getTransaction().commit();
			entityManager.close();
			
			// THEN empty Optionals are returned
			assertThat(nonExistentForContracts).isEmpty();
			assertThat(existsOtherWayRound).isEmpty();		
		}
		
		@Test
		@DisplayName("when a Proposal for the given Contracts exists in the database")
		void testGetProposalWhenContractsHaveProposal() {
			
			// GIVEN two Proposals are manually persisted
			FantaUser user2 = new FantaUser("userMail2", "userPswd3");
			FantaTeam team2 = new FantaTeam("team2", league, 0, user2, null);
			FantaUser user3 = new FantaUser("userMail3", "userPswd3");
			FantaTeam team3 = new FantaTeam("team3", league, 0, user3, null);
			Player player1 = new Player.Forward("Francesco", "Totti", Club.ROMA);
			Player player2 = new Player.Midfielder("Kevin", "De Bruyne", Club.NAPOLI);
			Player player3 = new Player.Midfielder("Luca", "Toni", Club.FIORENTINA);
			Contract contract1 = new Contract(team, player1);
			Contract contract2 = new Contract(team2, player2);
			Contract contract3 = new Contract(team3, player3);
			Proposal proposal12 = new Proposal(contract1, contract2);
			Proposal toBeIgnored = new Proposal(contract2, contract3);
			
			sessionFactory.inTransaction(session -> {
				session.persist(user2);
				session.persist(team2);
				session.persist(user3);
				session.persist(team3);
				session.persist(player1);
				session.persist(player2);
				session.persist(player3);
				session.persist(contract1);
				session.persist(contract2);
				session.persist(contract3);
				session.persist(proposal12);
				session.persist(toBeIgnored);
			});
			
			// WHEN the SUT is used to retrieve a Proposal that exists in the database
			entityManager.getTransaction().begin();
			Optional<Proposal> retrieved = proposalRepository.getProposalBy(contract1, contract2);
			entityManager.getTransaction().commit();
			entityManager.close();
			
			// THEN the expected Proposal is retrieved
			assertThat(retrieved).hasValue(proposal12);
		}
	}
}
