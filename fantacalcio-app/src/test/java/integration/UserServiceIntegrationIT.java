package integration;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import business.UserService;
import business.ports.repository.ContractRepository;
import business.ports.repository.FantaTeamRepository;
import business.ports.repository.FantaUserRepository;
import business.ports.repository.LeagueRepository;
import business.ports.repository.LineUpRepository;
import business.ports.repository.MatchDayRepository;
import business.ports.repository.MatchRepository;
import business.ports.repository.NewsPaperRepository;
import business.ports.repository.PlayerRepository;
import business.ports.repository.ProposalRepository;
import business.ports.repository.ResultsRepository;
import dal.repository.jpa.JpaContractRepository;
import dal.repository.jpa.JpaFantaTeamRepository;
import dal.repository.jpa.JpaFantaUserRepository;
import dal.repository.jpa.JpaLeagueRepository;
import dal.repository.jpa.JpaLineUpRepository;
import dal.repository.jpa.JpaMatchDayRepository;
import dal.repository.jpa.JpaMatchRepository;
import dal.repository.jpa.JpaNewsPaperRepository;
import dal.repository.jpa.JpaPlayerRepository;
import dal.repository.jpa.JpaProposalRepository;
import dal.repository.jpa.JpaResultsRepository;
import dal.transaction.jpa.JpaTransactionManager;
import domain.Contract;
import domain.FantaTeam;
import domain.FantaUser;
import domain.Fielding;
import domain.Grade;
import domain.League;
import domain.LineUp;
import domain.Match;
import domain.MatchDaySerieA;
import domain.NewsPaper;
import domain.Player;
import domain.Proposal;
import domain.Result;
import domain.Player.Club;
import domain.Player.Defender;
import domain.Player.Forward;
import domain.Player.Goalkeeper;
import domain.Player.Midfielder;
import domain.Proposal.PendingProposal;
import domain.scheme.Scheme433;
import jakarta.persistence.EntityManager;

class UserServiceIntegrationIT {

	private static SessionFactory sessionFactory;
	private UserService userService;
	private JpaTransactionManager transactionManager;
	private EntityManager entityManager;

	private MatchRepository matchRepository;
	private MatchDayRepository matchDayRepository;
	// private GradeRepository gradeRepository;
	private LineUpRepository lineUpRepository;
	private FantaTeamRepository fantaTeamRepository;
	private LeagueRepository leagueRepository;
	private PlayerRepository playerRepository;
	private ContractRepository contractRepository;
	private NewsPaperRepository newspaperRepository;
	private FantaUserRepository fantaUserRepository;
	private ResultsRepository resultsRepository;
	private ProposalRepository proposalRepository;

	@BeforeAll
	static void initializeSessionFactory() {

		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(Contract.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(Player.class)
					.addAnnotatedClass(Player.Goalkeeper.class)
					.addAnnotatedClass(Player.Defender.class)
					.addAnnotatedClass(Player.Midfielder.class)
					.addAnnotatedClass(Player.Forward.class)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(NewsPaper.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(Match.class)
					.addAnnotatedClass(Fielding.class)
					.addAnnotatedClass(Fielding.StarterFielding.class)
					.addAnnotatedClass(Fielding.SubstituteFielding.class)
					.addAnnotatedClass(LineUp.class)
					.addAnnotatedClass(Result.class)
					.addAnnotatedClass(Grade.class)
					.addAnnotatedClass(Proposal.class)
					.addAnnotatedClass(Proposal.PendingProposal.class)
					.addAnnotatedClass(Proposal.RejectedProposal.class)
					.getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void setup() {

		sessionFactory.getSchemaManager().truncateMappedObjects();
		transactionManager = new JpaTransactionManager(sessionFactory);
		userService = new UserService(transactionManager);
		entityManager = sessionFactory.createEntityManager();

		fantaUserRepository = new JpaFantaUserRepository(entityManager);
		matchRepository = new JpaMatchRepository(entityManager);
		matchDayRepository = new JpaMatchDayRepository(entityManager);
		// gradeRepository = new JpaGradeRepository(entityManager);
		lineUpRepository = new JpaLineUpRepository(entityManager);
		fantaTeamRepository = new JpaFantaTeamRepository(entityManager);
		leagueRepository = new JpaLeagueRepository(entityManager);
		playerRepository = new JpaPlayerRepository(entityManager);
		contractRepository = new JpaContractRepository(entityManager);
		newspaperRepository = new JpaNewsPaperRepository(entityManager);
		resultsRepository = new JpaResultsRepository(entityManager);
		proposalRepository = new JpaProposalRepository(entityManager);
	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}

	@Test
	void joinLeague() {

		entityManager.getTransaction().begin();

		FantaUser user = new FantaUser("user@test.com", "pwd");
		fantaUserRepository.saveFantaUser(user);

		NewsPaper newsPaper = new NewsPaper("Gazzetta");
		newspaperRepository.saveNewsPaper(newsPaper);

		League league = new League(user, "Test League", newsPaper, "1234");
		leagueRepository.saveLeague(league);

		FantaTeam team = new FantaTeam("Team A", league, 0, user, new HashSet<Contract>());

		entityManager.getTransaction().commit();

		userService.joinLeague(team, league);

		Optional<League> result = leagueRepository.getLeagueByCode("1234");
		assertThat(result).isPresent();

		League resultLeague = result.get();
		assertThat(resultLeague.getAdmin()).isEqualTo(user);
		assertThat(resultLeague.getLeagueCode()).isEqualTo("1234");
		assertThat(resultLeague.getName()).isEqualTo("Test League");
		assertThat(resultLeague.getNewsPaper()).isEqualTo(newsPaper);
	}

	@Test
	void testGetAllMatches() {

		entityManager.getTransaction().begin();

		FantaUser user1 = new FantaUser("user1@test.com", "pwd");
		fantaUserRepository.saveFantaUser(user1);
		FantaUser user2 = new FantaUser("user2@test.com", "pwd");
		fantaUserRepository.saveFantaUser(user2);

		NewsPaper newsPaper = new NewsPaper("Gazzetta");
		newspaperRepository.saveNewsPaper(newsPaper);

		League league = new League(user1, "Test League", newsPaper, "1234");
		leagueRepository.saveLeague(league);

		FantaTeam team1 = new FantaTeam("Team A", league, 0, user1, new HashSet<Contract>());
		fantaTeamRepository.saveTeam(team1);
		FantaTeam team2 = new FantaTeam("Team B", league, 0, user2, new HashSet<Contract>());
		fantaTeamRepository.saveTeam(team2);

		MatchDaySerieA day1 = new MatchDaySerieA("MD1", LocalDate.of(2025, 9, 7));
		matchDayRepository.saveMatchDay(day1);

		Match m1 = new Match(day1, team1, team2);
		matchRepository.saveMatch(m1);

		entityManager.getTransaction().commit();

		Map<MatchDaySerieA, List<Match>> result = userService.getAllMatches(league);

		assertThat(result.get(day1).size()).isEqualTo(1);
		Match resultMatch = result.get(day1).get(0);

		assertThat(resultMatch.getMatchDaySerieA().getName()).isEqualTo("MD1");
	}

	@Test
	void testSaveLineUp() {

		entityManager.getTransaction().begin();

		FantaUser user = new FantaUser("user@test.com", "pwd");
		fantaUserRepository.saveFantaUser(user);

		NewsPaper newsPaper = new NewsPaper("Gazzetta");
		newspaperRepository.saveNewsPaper(newsPaper);

		League league = new League(user, "Test League", newsPaper, "L003");
		leagueRepository.saveLeague(league);

		MatchDaySerieA matchDay = new MatchDaySerieA("MD1", LocalDate.now().plusWeeks(1)); // Monday
		matchDayRepository.saveMatchDay(matchDay);		

		// Players for LineUp
		Goalkeeper gk1 = new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA);

		Defender d1 = new Defender("difensore1", "titolare", Player.Club.ATALANTA);
		Defender d2 = new Defender("difensore2", "titolare", Player.Club.ATALANTA);
		Defender d3 = new Defender("difensore3", "titolare", Player.Club.ATALANTA);
		Defender d4 = new Defender("difensore4", "titolare", Player.Club.ATALANTA);

		Midfielder m1 = new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA);
		Midfielder m2 = new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA);
		Midfielder m3 = new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA);

		Forward f1 = new Forward("attaccante1", "titolare", Player.Club.ATALANTA);
		Forward f2 = new Forward("attaccante2", "titolare", Player.Club.ATALANTA);
		Forward f3 = new Forward("attaccante3", "titolare", Player.Club.ATALANTA);

		Goalkeeper sgk1 = new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA);
		Goalkeeper sgk2 = new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA);
		Goalkeeper sgk3 = new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA);

		Defender sd1 = new Defender("difensore1", "panchina", Player.Club.ATALANTA);
		Defender sd2 = new Defender("difensore2", "panchina", Player.Club.ATALANTA);
		Defender sd3 = new Defender("difensore3", "panchina", Player.Club.ATALANTA);

		Midfielder sm1 = new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA);
		Midfielder sm2 = new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA);
		Midfielder sm3 = new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA);

		Forward sf1 = new Forward("attaccante1", "panchina", Player.Club.ATALANTA);
		Forward sf2 = new Forward("attaccante2", "panchina", Player.Club.ATALANTA);
		Forward sf3 = new Forward("attaccante3", "panchina", Player.Club.ATALANTA);

		List<Player> players = List.of(
				gk1, 
				d1, d2, d3, d4, 
				m1, m2, m3, 
				f1, f2, f3, 
				sgk1, sgk2, sgk3, 
				sd1, sd2, sd3,
				sm1, sm2, sm3,
				sf1, sf2, sf3);
		
		players.forEach(playerRepository::addPlayer);
		
		// team & contracts
		HashSet<Contract> contracts = new HashSet<>();
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
		players.forEach(player -> contracts.add(new Contract(team, player)));
		fantaTeamRepository.saveTeam(team);  // relies on cascading for contracts

		// match
		Match match = new Match(matchDay, team, team);
		matchRepository.saveMatch(match);		
		
		entityManager.getTransaction().commit();
		entityManager.clear();

		// LineUp
		LineUp lineUp = LineUp.build()
				.forTeam(team)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(gk1)
						.withDefenders(d1, d2, d3, d4)
						.withMidfielders(m1, m2, m3)
						.withForwards(f1, f2, f3))
				.withSubstituteGoalkeepers(sgk1, sgk2, sgk3)
				.withSubstituteDefenders(sd1, sd2, sd3)
				.withSubstituteMidfielders(sm1, sm2, sm3)
				.withSubstituteForwards(sf1, sf2, sf3);

		userService.saveLineUp(lineUp);

		assertThat(lineUpRepository.getLineUpByMatchAndTeam(match, team)).isPresent()
				.hasValueSatisfying(lineUp::recursiveEquals);
	}

	@Test
	void testGetNextMatch() {

		entityManager.getTransaction().begin();

		FantaUser user = new FantaUser("user@test.com", "pwd");
		fantaUserRepository.saveFantaUser(user);

		NewsPaper newsPaper = new NewsPaper("Gazzetta");
		newspaperRepository.saveNewsPaper(newsPaper);

		League league = new League(user, "Test League", newsPaper, "L003");
		leagueRepository.saveLeague(league);

		FantaTeam team = new FantaTeam("Team", league, 30, user, new HashSet<>());
		FantaTeam team2 = new FantaTeam("Team2", league, 40, user, new HashSet<>());
		fantaTeamRepository.saveTeam(team);
		fantaTeamRepository.saveTeam(team2);

		MatchDaySerieA prevMatchDay = new MatchDaySerieA("MD1", LocalDate.now().minusWeeks(1));
		MatchDaySerieA nextMatchDay = new MatchDaySerieA("MD2", LocalDate.now().plusWeeks(1));
		matchDayRepository.saveMatchDay(prevMatchDay);
		matchDayRepository.saveMatchDay(nextMatchDay);

		Match prevMatch = new Match(prevMatchDay, team, team2);
		Match nextMatch = new Match(nextMatchDay, team, team2);
		matchRepository.saveMatch(prevMatch);
		matchRepository.saveMatch(nextMatch);

		Result prevResults = new Result(20, 50, 1, 2, prevMatch);
		resultsRepository.saveResult(prevResults);

		entityManager.getTransaction().commit();

		Match result = userService.getNextMatch(league, team, LocalDate.now());
		assertThat(result.getMatchDaySerieA().getName()).isEqualTo("MD2");
	}

	@Test
	void testGetStandings() {

		entityManager.getTransaction().begin();

		FantaUser user = new FantaUser("user@test.com", "pwd");
		fantaUserRepository.saveFantaUser(user);

		NewsPaper newsPaper = new NewsPaper("Gazzetta");
		newspaperRepository.saveNewsPaper(newsPaper);

		League league = new League(user, "Test League", newsPaper, "L003");
		leagueRepository.saveLeague(league);

		FantaTeam team1 = new FantaTeam("Team1", league, 10, user, new HashSet<>());
		FantaTeam team2 = new FantaTeam("Team2", league, 70, user, new HashSet<>());
		fantaTeamRepository.saveTeam(team1);
		fantaTeamRepository.saveTeam(team2);

		entityManager.getTransaction().commit();

		List<FantaTeam> standings = userService.getStandings(league);

		assertThat(standings.get(0).getName()).isEqualTo("Team2");
		assertThat(standings.get(1).getName()).isEqualTo("Team1");
	}

	@Test
	void testCreateProposal() {

		entityManager.getTransaction().begin();

		FantaUser user = new FantaUser("user@test.com", "pwd");
		fantaUserRepository.saveFantaUser(user);

		NewsPaper newsPaper = new NewsPaper("Gazzetta");
		newspaperRepository.saveNewsPaper(newsPaper);

		League league = new League(user, "Test League", newsPaper, "L003");
		leagueRepository.saveLeague(league);

		FantaTeam myTeam = new FantaTeam("My Team", league, 0, user, new HashSet<>());
		FantaTeam opponentTeam = new FantaTeam("Opponent", league, 0, user, new HashSet<>());
		fantaTeamRepository.saveTeam(myTeam);
		fantaTeamRepository.saveTeam(opponentTeam);

		Player offeredPlayer = new Player.Defender("Mario", "Rossi", Club.ATALANTA);
		Player requestedPlayer = new Player.Defender("Luigi", "Verdi", Club.BOLOGNA);
		playerRepository.addPlayer(offeredPlayer);
		playerRepository.addPlayer(requestedPlayer);

		Contract offeredContract = new Contract(myTeam, offeredPlayer);
		Contract requestedContract = new Contract(opponentTeam, requestedPlayer);
		myTeam.getContracts().add(offeredContract);
		opponentTeam.getContracts().add(requestedContract);
		contractRepository.saveContract(offeredContract);
		contractRepository.saveContract(requestedContract);

		entityManager.getTransaction().commit();

		assertThat(userService.createProposal(requestedPlayer, offeredPlayer, myTeam, opponentTeam)).isTrue();

		Optional<Proposal> result = proposalRepository.getProposal(offeredContract, requestedContract);
		assertThat(result).isPresent();
		Proposal resultProposal = result.get();
		assertThat(resultProposal.getOfferedContract()).isEqualTo(offeredContract);
		assertThat(resultProposal.getRequestedContract()).isEqualTo(requestedContract);
	}

	@Test
	void testAcceptProposal() {

		entityManager.getTransaction().begin();

		FantaUser user = new FantaUser("user@test.com", "pwd");
		fantaUserRepository.saveFantaUser(user);

		NewsPaper newsPaper = new NewsPaper("Gazzetta");
		newspaperRepository.saveNewsPaper(newsPaper);

		League league = new League(user, "Test League", newsPaper, "L003");
		leagueRepository.saveLeague(league);

		FantaTeam myTeam = new FantaTeam("My Team", league, 0, user, new HashSet<>());
		FantaTeam offeringTeam = new FantaTeam("Opponent", league, 0, user, new HashSet<>());
		fantaTeamRepository.saveTeam(myTeam);
		fantaTeamRepository.saveTeam(offeringTeam);

		Player requestedPlayer = new Player.Defender("Luigi", "Verdi", Club.BOLOGNA);
		Player offeredPlayer = new Player.Defender("Mario", "Rossi", Club.ATALANTA);
		playerRepository.addPlayer(requestedPlayer);
		playerRepository.addPlayer(offeredPlayer);

		Contract offeredContract = new Contract(offeringTeam, offeredPlayer);
		Contract requestedContract = new Contract(myTeam, requestedPlayer);
		offeringTeam.getContracts().add(offeredContract);
		myTeam.getContracts().add(requestedContract);
		contractRepository.saveContract(offeredContract);
		contractRepository.saveContract(requestedContract);

		Proposal.PendingProposal proposal = new PendingProposal(offeredContract, requestedContract);
		proposalRepository.saveProposal(proposal);

		entityManager.getTransaction().commit();

		userService.acceptProposal(proposal, myTeam);

		assertThat(contractRepository.getContract(offeringTeam, offeredPlayer)).isEmpty();
		assertThat(contractRepository.getContract(offeringTeam, requestedPlayer)).isPresent();

		assertThat(contractRepository.getContract(myTeam, requestedPlayer)).isEmpty();
		assertThat(contractRepository.getContract(myTeam, offeredPlayer)).isPresent();
	}

	@Test
	void testGetAllFantaTeams() {

		entityManager.getTransaction().begin();

		FantaUser user = new FantaUser("user@test.com", "pwd");
		fantaUserRepository.saveFantaUser(user);

		NewsPaper newsPaper = new NewsPaper("Gazzetta");
		newspaperRepository.saveNewsPaper(newsPaper);

		League league = new League(user, "Test League", newsPaper, "L003");
		leagueRepository.saveLeague(league);

		FantaTeam team1 = new FantaTeam("Team1", league, 10, user, new HashSet<>());
		FantaTeam team2 = new FantaTeam("Team2", league, 70, user, new HashSet<>());
		fantaTeamRepository.saveTeam(team1);
		fantaTeamRepository.saveTeam(team2);

		entityManager.getTransaction().commit();

		List<FantaTeam> result = userService.getAllFantaTeams(league);

		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0).getLeague()).isEqualTo(result.get(1).getLeague());
		assertThat(result.get(0).getName()).isIn("Team1", "Team2");
		assertThat(result.get(1).getName()).isIn("Team1", "Team2");
		assertThat(result.get(0).getPoints()).isIn(10, 70);
		assertThat(result.get(1).getPoints()).isIn(10, 70);
	}

	@Test
	void testGetAllPlayers() {

		entityManager.getTransaction().begin();

		Player p1 = new Player.Defender("Mario", "Rossi", Club.ATALANTA);
		Player p2 = new Player.Defender("Luigi", "Verdi", Club.BOLOGNA);
		playerRepository.addPlayer(p1);
		playerRepository.addPlayer(p2);

		entityManager.getTransaction().commit();

		List<Player> result = userService.getAllPlayers();
		assertThat(result).containsExactly(p1, p2);
	}

}
