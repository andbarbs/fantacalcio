package integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import domain.*;
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

import business.UserService;
import business.ports.transaction.TransactionManager.TransactionContext;
import dal.transaction.jpa.JpaTransactionManager;
import domain.Player.Club;
import domain.Player.Defender;
import domain.Player.Forward;
import domain.Player.Goalkeeper;
import domain.Player.Midfielder;
import domain.scheme.Scheme433;

/**
 * integrates {@link UserService} with {@link JpaTransactionManager} and,
 * consequently, JPA Entity Repositories
 */
@DisplayName("a UserService")
class UserServiceIT {

	private static SessionFactory sessionFactory;
	private UserService userService;
	private JpaTransactionManager transactionManager;

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
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(MatchDay.class)
					.addAnnotatedClass(Match.class)
					.addAnnotatedClass(Fielding.class)
					.addAnnotatedClass(Fielding.StarterFielding.class)
					.addAnnotatedClass(Fielding.SubstituteFielding.class)
					.addAnnotatedClass(LineUp.class)
					.addAnnotatedClass(Result.class)
					.addAnnotatedClass(Grade.class)
					.addAnnotatedClass(Proposal.class)
					.getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void setup() {
		// empties out database tables
		sessionFactory.getSchemaManager().truncateMappedObjects();
		
		// instantiates SUT
		transactionManager = new JpaTransactionManager(sessionFactory);
		userService = new UserService(transactionManager);
	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}
	
	@Test
	@DisplayName("can create a new League in the system")
	void createLeague() {
		
		// GIVEN
		FantaUser admin = new FantaUser("mail", "pswd");
		transactionManager.inTransaction(context -> context.getFantaUserRepository().saveFantaUser(admin));

		// WHEN
		userService.createLeague("lega", admin, "1234");

		// THEN
		Optional<League> persisted = transactionManager
				.fromTransaction(context -> context.getLeagueRepository().getLeagueByCode("1234"));

		assertThat(persisted).hasValue(new League(admin, "lega", "1234"));
	}

	@Test
	@DisplayName("can make a Team join a League (?)")
	void joinLeague() {

		// GIVEN
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "1234");
		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(user);
			context.getLeagueRepository().saveLeague(league);
		});

		// WHEN
		FantaTeam team = new FantaTeam("Team A", league, 0, user, null);
		userService.joinLeague(team, league);

		// THEN
		// TODO non capisco perche viene asserito questo
		Optional<League> result = transactionManager
				.fromTransaction(context -> context.getLeagueRepository().getLeagueByCode("1234"));
		assertThat(result).hasValue(new League(user, "Test League", "1234"));
	}

	@Test
	@DisplayName("can retrieve all Matches on a League's MatchDays")
	void testGetAllMatches() {

		// GIVEN
		FantaUser user1 = new FantaUser("user1@test.com", "pwd");
		FantaUser user2 = new FantaUser("user2@test.com", "pwd");
		League league = new League(user1, "Test League", "1234");
		FantaTeam team1 = new FantaTeam("Team A", league, 0, user1, new HashSet<Contract>());
		FantaTeam team2 = new FantaTeam("Team B", league, 0, user2, new HashSet<Contract>());
		MatchDay day1 = new MatchDay("MD1",1, MatchDay.Status.PAST, league);
		Match m1 = new Match(day1, team1, team2);
		
		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(user1);
			context.getFantaUserRepository().saveFantaUser(user2);
			context.getLeagueRepository().saveLeague(league);
			context.getTeamRepository().saveTeam(team1);
			context.getTeamRepository().saveTeam(team2);
			context.getMatchDayRepository().saveMatchDay(day1);
			context.getMatchRepository().saveMatch(m1);
		});

		// WHEN 
		Map<MatchDay, List<Match>> matchDayToMatches = userService.getAllMatches(league);

		// THEN
		assertThat(matchDayToMatches).containsExactlyEntriesOf(Map.of(day1, List.of(m1)));
	}

	@Test
	@DisplayName("can save a legitimate LineUp")
	void testSaveLineUp() {

		// GIVEN
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L003");
		MatchDay matchDay = new MatchDay("MD1",1, MatchDay.Status.FUTURE, league); // Monday
		
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
		
		HashSet<Contract> contracts = new HashSet<>();
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
		players.forEach(player -> contracts.add(new Contract(team, player)));
		Match match = new Match(matchDay, team, team);

		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(user);
			context.getLeagueRepository().saveLeague(league);
			context.getMatchDayRepository().saveMatchDay(matchDay);		
			players.forEach(context.getPlayerRepository()::addPlayer);
			context.getTeamRepository().saveTeam(team);  // relies on cascading for contracts
			context.getMatchRepository().saveMatch(match);
		});

		// WHEN
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

		// THEN
		Optional<LineUp> persisted = transactionManager.fromTransaction(context -> context.getLineUpRepository().getLineUpByMatchAndTeam(match, team));
		assertThat(persisted).hasValueSatisfying(lineUp::recursiveEquals);
	}

	@Test
	@DisplayName("can get a League's standings")
	void testGetStandings() {

		// WHEN
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L003");
		FantaTeam team1 = new FantaTeam("Team1", league, 10, user, null);
		FantaTeam team2 = new FantaTeam("Team2", league, 70, user, null);

		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(user);
			context.getLeagueRepository().saveLeague(league);
			context.getTeamRepository().saveTeam(team1);
			context.getTeamRepository().saveTeam(team2);
		});		

		// WHEN
		List<FantaTeam> standings = userService.getStandings(league);

		// THEN
		assertThat(standings).containsExactly(team2, team1);
	}

	@Test
	@DisplayName("can create a legitimate Proposal")
	void testCreateProposal() {

		// GIVEN
		FantaUser admin = new FantaUser("admin@test.com", "pwd");
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(admin, "Test League", "L003");
		FantaTeam myTeam = new FantaTeam("My Team", league, 0, admin, new HashSet<>());
		FantaTeam opponentTeam = new FantaTeam("Opponent", league, 0, user, new HashSet<>());
		Player offeredPlayer = new Player.Defender("Mario", "Rossi", Club.ATALANTA);
		Player requestedPlayer = new Player.Defender("Luigi", "Verdi", Club.BOLOGNA);
		Contract offeredContract = new Contract(myTeam, offeredPlayer);
		Contract requestedContract = new Contract(opponentTeam, requestedPlayer);
		myTeam.getContracts().add(offeredContract);
		opponentTeam.getContracts().add(requestedContract);

		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(admin);
			context.getFantaUserRepository().saveFantaUser(user);
			context.getLeagueRepository().saveLeague(league);
			context.getPlayerRepository().addPlayer(offeredPlayer);
			context.getPlayerRepository().addPlayer(requestedPlayer);
			context.getTeamRepository().saveTeam(myTeam);
			context.getTeamRepository().saveTeam(opponentTeam);
		});

		// WHEN
		boolean returned = userService.createProposal(requestedPlayer, offeredPlayer, myTeam, opponentTeam);

		// THEN
		assertThat(returned).isTrue();
		Optional<Proposal> result = transactionManager.fromTransaction(
				context -> context.getProposalRepository().getProposalBy(offeredContract, requestedContract));
		assertThat(result).hasValue(new Proposal(offeredContract, requestedContract));
	}

	@Test
	@DisplayName("can accept a legitimate Proposal")
	void testAcceptProposal() {

		// GIVEN
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L003");
		FantaTeam myTeam = new FantaTeam("My Team", league, 0, user, new HashSet<>());
		FantaTeam offeringTeam = new FantaTeam("Opponent", league, 0, user, new HashSet<>());
		Player requestedPlayer = new Player.Defender("Luigi", "Verdi", Club.BOLOGNA);
		Player offeredPlayer = new Player.Defender("Mario", "Rossi", Club.ATALANTA);
		Contract offeredContract = new Contract(offeringTeam, offeredPlayer);
		Contract requestedContract = new Contract(myTeam, requestedPlayer);
		offeringTeam.getContracts().add(offeredContract);
		myTeam.getContracts().add(requestedContract);
		Proposal proposal = new Proposal(offeredContract, requestedContract);

		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(user);
			context.getLeagueRepository().saveLeague(league);
			context.getPlayerRepository().addPlayer(requestedPlayer);
			context.getPlayerRepository().addPlayer(offeredPlayer);
			context.getTeamRepository().saveTeam(myTeam);
			context.getTeamRepository().saveTeam(offeringTeam);
			context.getProposalRepository().saveProposal(proposal);
		});

		// WHEN
		userService.acceptProposal(proposal, myTeam);

		// THEN
		assertThat(transactionManager.fromTransaction(
				(TransactionContext c) -> c.getContractRepository().getContract(offeringTeam, offeredPlayer)))
				.isEmpty();
		assertThat(transactionManager.fromTransaction(
				(TransactionContext c) -> c.getContractRepository().getContract(offeringTeam, requestedPlayer)))
				.isPresent();

		assertThat(transactionManager.fromTransaction(
				(TransactionContext c) -> c.getContractRepository().getContract(myTeam, requestedPlayer))).isEmpty();
		assertThat(transactionManager.fromTransaction(
				(TransactionContext c) -> c.getContractRepository().getContract(myTeam, offeredPlayer))).isPresent();
	}

	@Test
	@DisplayName("can get all Teams in a League")
	void testGetAllFantaTeams() {

		// GIVEN
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L003");
		FantaTeam team1 = new FantaTeam("Team1", league, 10, user, new HashSet<>());
		FantaTeam team2 = new FantaTeam("Team2", league, 70, user, new HashSet<>());

		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(user);
			context.getLeagueRepository().saveLeague(league);
			context.getTeamRepository().saveTeam(team1);
			context.getTeamRepository().saveTeam(team2);
		});

		// WHEN
		Set<FantaTeam> result = userService.getAllFantaTeams(league);
		
		// THEN
		assertThat(result).containsExactlyInAnyOrder(team1, team2);
	}

	@Test
	@DisplayName("can get all Players in the system")
	void testGetAllPlayers() {
		
		// GIVEN
		Player p1 = new Player.Defender("Mario", "Rossi", Club.ATALANTA);
		Player p2 = new Player.Defender("Luigi", "Verdi", Club.BOLOGNA);
		
		transactionManager.inTransaction(context -> {
			context.getPlayerRepository().addPlayer(p1);
			context.getPlayerRepository().addPlayer(p2);
		});

		// WHEN
		Set<Player> result = userService.getAllPlayers();
		
		// THEN
		assertThat(result).containsExactlyInAnyOrder(p1, p2);
	}
}
