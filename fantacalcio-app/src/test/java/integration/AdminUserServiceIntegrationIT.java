package integration;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.junit.jupiter.api.Test;

import business.AdminUserService;
import business.ports.repository.ContractRepository;
import business.ports.repository.FantaTeamRepository;
import business.ports.repository.FantaUserRepository;
import business.ports.repository.GradeRepository;
import business.ports.repository.LeagueRepository;
import business.ports.repository.MatchRepository;
import business.ports.repository.PlayerRepository;
import dal.repository.jpa.JpaContractRepository;
import dal.repository.jpa.JpaFantaTeamRepository;
import dal.repository.jpa.JpaFantaUserRepository;
import dal.repository.jpa.JpaGradeRepository;
import dal.repository.jpa.JpaLeagueRepository;
import dal.repository.jpa.JpaMatchRepository;
import dal.repository.jpa.JpaPlayerRepository;
import dal.transaction.jpa.JpaTransactionManager;
import domain.MatchDay;
import domain.Player.Club;
import domain.Player.Defender;
import domain.Player.Forward;
import domain.Player.Goalkeeper;
import domain.Player.Midfielder;
import jakarta.persistence.EntityManager;

class AdminUserServiceIntegrationIT {

	private static SessionFactory sessionFactory;
	private AdminUserService adminUserService;
	private JpaTransactionManager transactionManager;
	private EntityManager entityManager;

	private MatchRepository matchRepository;
	private GradeRepository gradeRepository;
//	private LineUpRepository lineUpRepository;
	private FantaTeamRepository fantaTeamRepository;
	private LeagueRepository leagueRepository;
	private PlayerRepository playerRepository;
	private ContractRepository contractRepository;
	private FantaUserRepository fantaUserRepository;

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
					.addAnnotatedClass(LineUp.class)
					.addAnnotatedClass(Result.class)
					.addAnnotatedClass(Grade.class)
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
		adminUserService = new AdminUserService(transactionManager);
		entityManager = sessionFactory.createEntityManager();

		fantaUserRepository = new JpaFantaUserRepository(entityManager);
		matchRepository = new JpaMatchRepository(entityManager);
		gradeRepository = new JpaGradeRepository(entityManager);
//		lineUpRepository = new JpaLineUpRepository(entityManager);
		fantaTeamRepository = new JpaFantaTeamRepository(entityManager);
		leagueRepository = new JpaLeagueRepository(entityManager);
		playerRepository = new JpaPlayerRepository(entityManager);
		contractRepository = new JpaContractRepository(entityManager);

	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}

	@Test
	void createLeague() {

		entityManager.getTransaction().begin();

		FantaUser admin = new FantaUser("mail", "pswd");
		fantaUserRepository.saveFantaUser(admin);

		entityManager.getTransaction().commit();

		adminUserService.createLeague("lega", admin, "1234");

		Optional<League> result = leagueRepository.getLeagueByCode("1234");

		assertThat(result).isPresent();
		League league = result.get();

		assertThat(league.getName()).isEqualTo("lega");
		assertThat(league.getAdmin()).isEqualTo(admin);
		assertThat(league.getLeagueCode()).isEqualTo("1234");
	}

// TODO in teoria è già testato
//	@Test
//	void createLeague_LeagueAlreadyExists() {
//
//		adminUserService.createLeague("lega", admin, newsPaper, "1234");
//		assertThatThrownBy(() -> adminUserService.createLeague("lega", admin, newsPaper, "1234"))
//				.isInstanceOf(IllegalArgumentException.class)
//				.hasMessage("A league with the same league code already exists");
//
//		transactionManager.inTransaction(context -> {
//			Optional<League> result = context.getLeagueRepository().getLeagueByCode("1234");
//
//			assertThat(result.isEmpty());
//		});
//	}

	@Test
	void generateCalendar() {

		entityManager.getTransaction().begin();

		FantaUser admin = new FantaUser("mail", "pswd");
		FantaUser user = new FantaUser("user2", "pswd2");
		fantaUserRepository.saveFantaUser(admin);
		fantaUserRepository.saveFantaUser(user);


		League league = new League(admin, "lega", "0000");
		leagueRepository.saveLeague(league);

		FantaTeam team1 = new FantaTeam("team1", league, 0, admin, new HashSet<Contract>());
		FantaTeam team2 = new FantaTeam("team2", league, 0, user, new HashSet<Contract>());

		fantaTeamRepository.saveTeam(team1);
		fantaTeamRepository.saveTeam(team2);

		List<MatchDay> matchDays = new ArrayList<MatchDay>();
		for (int i = 0; i < 20; i++) {
			matchDays.add(new MatchDay("Match " + i, 1, MatchDay.Status.FUTURE, league));
		}

		sessionFactory.inTransaction(t -> {
			for (MatchDay matchDay : matchDays) {
				t.persist(matchDay);
			}
		});

		entityManager.getTransaction().commit();

		adminUserService.generateCalendar(league);

		for (MatchDay matchDay : matchDays) {
			Match matchByMatchDay = matchRepository.getMatchByMatchDay(matchDay, league, team1);

			assertThat(matchByMatchDay.getMatchDaySerieA()).isEqualTo(matchDay);
			assertThat(matchByMatchDay.getTeam1().equals(team1) || matchByMatchDay.getTeam2().equals(team1)).isTrue();
		}
	}

	@Test
	void calculateGrades() {
		entityManager.getTransaction().begin();

		// Users
		FantaUser admin = new FantaUser("mail", "pswd");
		FantaUser user = new FantaUser("mail2", "pswd2");

		fantaUserRepository.saveFantaUser(admin);
		fantaUserRepository.saveFantaUser(user);


		League league = new League(admin, "lega", "0000");
		leagueRepository.saveLeague(league);

		// Teams
		FantaTeam team1 = new FantaTeam("team1", league, 0, admin, new HashSet<Contract>());
		FantaTeam team2 = new FantaTeam("team2", league, 0, user, new HashSet<Contract>());

		fantaTeamRepository.saveTeam(team1);
		fantaTeamRepository.saveTeam(team2);

		// MatchDays
		//TODO che senso ha creare 2 matchday?
		MatchDay prevDay = new MatchDay("Day1",1, MatchDay.Status.PAST,league);
		MatchDay dayToCalc = new MatchDay("Day2", 2, MatchDay.Status.PAST, league);

		entityManager.persist(prevDay);
		entityManager.persist(dayToCalc);

		// Match
		Match prevMatch = new Match(prevDay, team1, team2);
		Match match = new Match(dayToCalc, team1, team2);

		matchRepository.saveMatch(prevMatch);
		matchRepository.saveMatch(match);

		// Players for LineUp (formerly inline)
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

		for (Player player : players) {
			contractRepository.saveContract(new Contract(team1, player));
			contractRepository.saveContract(new Contract(team2, player));
		}
		
		// TODO se non servono le lineup, forse bastano molti meno players?

		// LineUp
//		LineUp lineup1 = LineUp.build()
//				.forTeam(team1)
//				.inMatch(match)
//				.withStarterLineUp(Scheme433.starterLineUp()
//						.withGoalkeeper(gk1)
//						.withDefenders(d1, d2, d3, d4)
//						.withMidfielders(m1, m2, m3)
//						.withForwards(f1, f2, f3))
//				.withSubstituteGoalkeepers(sgk1, sgk2, sgk3)
//				.withSubstituteDefenders(sd1, sd2, sd3)
//				.withSubstituteMidfielders(sm1, sm2, sm3)
//				.withSubstituteForwards(sf1, sf2, sf3);
//		
//		LineUp lineup2 = LineUp.build()
//				.forTeam(team1)
//				.inMatch(match)
//				.withStarterLineUp(Scheme433.starterLineUp()
//						.withGoalkeeper(gk1)
//						.withDefenders(d1, d2, d3, d4)
//						.withMidfielders(m1, m2, m3)
//						.withForwards(f1, f2, f3))
//				.withSubstituteGoalkeepers(sgk1, sgk2, sgk3)
//				.withSubstituteDefenders(sd1, sd2, sd3)
//				.withSubstituteMidfielders(sm1, sm2, sm3)
//				.withSubstituteForwards(sf1, sf2, sf3);
//
//		lineUpRepository.saveLineUp(lineup1);
//		lineUpRepository.saveLineUp(lineup2);

		// Grades
		Grade grade1 = new Grade(gk1, dayToCalc, 70.0);
		Grade grade2 = new Grade(d1, dayToCalc, 60.0);

		gradeRepository.saveGrade(grade1);
		gradeRepository.saveGrade(grade2);

		entityManager.getTransaction().commit();

        //TODO ma se lo sto testando perchè lo creo?
		AdminUserService service = new AdminUserService(transactionManager) {

		};

		service.calculateGrades(admin, league);

		List<Grade> allMatchGrades = gradeRepository.getAllMatchGrades(dayToCalc);
		assertThat(allMatchGrades.size()).isEqualTo(2);
		assertThat(allMatchGrades.get(0)).isEqualTo(grade1);
		assertThat(allMatchGrades.get(1)).isEqualTo(grade2);

	}

	@Test
	void setPlayersToTeam() {

		entityManager.getTransaction().begin();

		FantaUser admin = new FantaUser("mail", "pswd");
		fantaUserRepository.saveFantaUser(admin);

		League league = new League(admin, "lega", "1234");
		leagueRepository.saveLeague(league);

		FantaTeam team = new FantaTeam("", league, 0, admin, Set.of());
		fantaTeamRepository.saveTeam(team);

		Player.Forward player = new Player.Forward("Lionel", "Messi", Club.CREMONESE);
		Player.Goalkeeper player2 = new Player.Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
		playerRepository.addPlayer(player);
		playerRepository.addPlayer(player2);

		entityManager.getTransaction().commit();

		adminUserService.setPlayerToTeam(team, player);
		adminUserService.setPlayerToTeam(team, player2);

		Optional<Contract> contract = contractRepository.getContract(team, player);
		assertThat(contract).isPresent();
		Contract found = contract.get();
		assertThat(found.getPlayer()).isEqualTo(player);
		assertThat(found.getTeam()).isEqualTo(team);

		Optional<Contract> contract2 = contractRepository.getContract(team, player);
		assertThat(contract2).isPresent();
		Contract found2 = contract.get();
		assertThat(found2.getPlayer()).isEqualTo(player);
		assertThat(found2.getTeam()).isEqualTo(team);

	}

}
