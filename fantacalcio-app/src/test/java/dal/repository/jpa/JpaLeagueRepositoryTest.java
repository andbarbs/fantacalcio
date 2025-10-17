package dal.repository.jpa;

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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import domain.Contract;
import domain.FantaTeam;
import domain.FantaUser;
import domain.League;
import domain.Player;
import domain.Player.Club;
import domain.Player.Forward;
import domain.Player.Goalkeeper;
import jakarta.persistence.EntityManager;

class JpaLeagueRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaLeagueRepository leagueRepository;
	private EntityManager entityManager;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(Player.class)
					.addAnnotatedClass(Player.Goalkeeper.class)
					.addAnnotatedClass(Player.Defender.class)
					.addAnnotatedClass(Player.Midfielder.class)
					.addAnnotatedClass(Player.Forward.class)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(Contract.class)
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
		leagueRepository = new JpaLeagueRepository(entityManager);
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}

	@Nested
	@DisplayName("can look up a League by its code")
	class LookupByCode {
		
		@Test
		@DisplayName("when no League with the given code exists in the database")
		void testGetLeagueByCodeWithNoLeague() {
			
			// GIVEN no League exists in the database with the given code
			FantaUser admin = new FantaUser("user", "pswd");
			League league = new League(admin, "lega", "exists");
			
			sessionFactory.inTransaction(session -> {
				session.persist(admin);
				session.persist(league);
			});
			
			// WHEN the SUT is used to retrieve a League with that code
			entityManager.getTransaction().begin();
			Optional<League> retrieved = leagueRepository.getLeagueByCode("does not exist");
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN an empty Optional is returned
			assertThat(retrieved).isEmpty();
		}
		
		@Test
		@DisplayName("when a League with the given code exists in the database")
		void testGetLeagueByCodeWhenLeagueExists() {
			
			// GIVEN a League is manually persisted to the database
			FantaUser admin = new FantaUser("user", "pswd");
			String leagueCode = "1234";
			League league = new League(admin, "lega", leagueCode);
			
			sessionFactory.inTransaction(session -> {
				session.persist(admin);
				session.persist(league);
			});
			
			// WHEN the SUT is used to look up that League
			entityManager.getTransaction().begin();
			Optional<League> result = leagueRepository.getLeagueByCode(leagueCode);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN only the expected League is retrieved
			assertThat(result).hasValue(league);
			
		}
	}	

	@Test
	@DisplayName("can persist a League to the database")
	void testSaveLeague() {

		// GIVEN a League's ancillary entities are manually persisted
		FantaUser admin = new FantaUser("user", "pswd");
		String leagueCode = "1234";
		League league = new League(admin, "lega", leagueCode);

		sessionFactory.inTransaction(session -> {
            session.persist(admin);
        });

		// WHEN the SUT is used to persist a League to the database
        entityManager.getTransaction().begin();
		leagueRepository.saveLeague(league);
        entityManager.getTransaction().commit();
        entityManager.clear();

		// THEN the database contains the League
		assertThat(sessionFactory.fromTransaction((Session em) -> em
				.createQuery("FROM League l JOIN FETCH l.admin", League.class).getResultStream().toList()))
				.containsExactly(league);
	}
	
	@Nested
	@DisplayName("can look up a League by a member")
	class LookupByMember {
		
		@Test
		@DisplayName("when no League with the given member exists in the database")
		void testGetLeaguesByUserWhenNoLeagueExists() {
			
			// GIVEN the database contains no League for a given User
			FantaUser admin = new FantaUser("admin1", "adminPswd1");
			League league1 = new League(admin, "lega1", "1234");
			League league2 = new League(admin, "lega2", "5678");
			
			FantaUser user = new FantaUser("user", "userPswd");
			FantaTeam fantaTeam1 = new FantaTeam("team1", league1, 0, user, new HashSet<Contract>());
			FantaTeam fantaTeam2 = new FantaTeam("team2", league2, 0, user, new HashSet<Contract>());
			
			FantaUser otherUser = new FantaUser("admin2", "adminPswd2");
			
			sessionFactory.inTransaction(session -> {
				session.persist(admin);
				session.persist(league1);
				session.persist(league2);
				session.persist(user);
				session.persist(fantaTeam1);
				session.persist(fantaTeam2);
				session.persist(otherUser);
			});
			
			// WHEN the SUT is used to look up Leagues where the user is admin
			entityManager.getTransaction().begin();
			Set<League> retrieved = leagueRepository.getLeaguesByMember(otherUser);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN an empty Set is returned
			assertThat(retrieved).isEmpty();
		}
		
		@Test
		@DisplayName("when a League with the given member exists in the database")
		void testGetLeaguesByUserWhenSomeLeaguesExist() {
			
			// GIVEN some Leagues with a given User as member exist in the database
			FantaUser admin = new FantaUser("admin1", "adminPswd1");
			League league1 = new League(admin, "lega1", "1234");
			League league2 = new League(admin, "lega2", "5678");
			
			FantaUser otherAdmin = new FantaUser("admin2", "adminPswd2");
			League otherLeague = new League(otherAdmin, "lega2", "5678");
			
			FantaUser user = new FantaUser("user", "userPswd");
			FantaTeam fantaTeam1 = new FantaTeam("team1", league1, 0, user, new HashSet<Contract>());
			FantaTeam fantaTeam2 = new FantaTeam("team2", league2, 0, user, new HashSet<Contract>());
			
			sessionFactory.inTransaction(session -> {
				session.persist(admin);
				session.persist(league1);
				session.persist(league2);
				session.persist(otherAdmin);
				session.persist(otherLeague);
				session.persist(user);
				session.persist(fantaTeam1);
				session.persist(fantaTeam2);
			});
			
			// WHEN the SUT is used to look up all Leagues the user is a member of
			entityManager.getTransaction().begin();
			Set<League> leagues = leagueRepository.getLeaguesByMember(user);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN only the expected Leagues are retrieved
			assertThat(leagues).containsExactlyInAnyOrder(league1, league2);
		}

        @Test
        @DisplayName("when no League with the given member exists in the database")
        void testGetLeaguesByJournalistWhenNoLeagueExists() {

            // GIVEN the database contains no League for a given User
            FantaUser admin = new FantaUser("admin1", "adminPswd1");
            League league1 = new League(admin, "lega1", "1234");
            League league2 = new League(admin, "lega2", "5678");

            FantaUser user = new FantaUser("user", "userPswd");
            FantaTeam fantaTeam1 = new FantaTeam("team1", league1, 0, user, new HashSet<Contract>());
            FantaTeam fantaTeam2 = new FantaTeam("team2", league2, 0, user, new HashSet<Contract>());

            FantaUser journalist = new FantaUser("journalist2", "journalistPswd2");

            sessionFactory.inTransaction(session -> {
                session.persist(admin);
                session.persist(league1);
                session.persist(league2);
                session.persist(user);
                session.persist(fantaTeam1);
                session.persist(fantaTeam2);
                session.persist(journalist);
            });

            // WHEN the SUT is used to look up Leagues where the user is admin
            entityManager.getTransaction().begin();
            Set<League> retrieved = leagueRepository.getLeaguesByJournalist(journalist);
            entityManager.getTransaction().commit();
            entityManager.clear();

            // THEN an empty Set is returned
            assertThat(retrieved).isEmpty();
        }

        @Test
        @DisplayName("when a League with the given member exists in the database")
        void testGetLeaguesByJournalistWhenSomeLeaguesExist() {

            // GIVEN some Leagues with a given User as member exist in the database
            FantaUser admin = new FantaUser("admin1", "adminPswd1");
            League league1 = new League(admin, "lega1", "1234");
            League league2 = new League(admin, "lega2", "5678");

            FantaUser otherAdmin = new FantaUser("admin2", "adminPswd2");
            League otherLeague = new League(otherAdmin, "lega2", "5678");

            FantaUser user = new FantaUser("user", "userPswd");
            FantaTeam fantaTeam1 = new FantaTeam("team1", league1, 0, user, new HashSet<Contract>());
            FantaTeam fantaTeam2 = new FantaTeam("team2", league2, 0, user, new HashSet<Contract>());
            FantaUser journalist = new FantaUser("journalist2", "journalistPswd2");
            league1.setNewsPaper(journalist);
            league2.setNewsPaper(journalist);

            sessionFactory.inTransaction(session -> {
                session.persist(journalist);
                session.persist(admin);
                session.persist(league1);
                session.persist(league2);
                session.persist(otherAdmin);
                session.persist(otherLeague);
                session.persist(user);
                session.persist(fantaTeam1);
                session.persist(fantaTeam2);
            });

            // WHEN the SUT is used to look up all Leagues the user is a member of
            entityManager.getTransaction().begin();
            Set<League> leagues = leagueRepository.getLeaguesByJournalist(journalist);
            entityManager.getTransaction().commit();
            entityManager.clear();

            // THEN only the expected Leagues are retrieved
            assertThat(leagues).containsExactlyInAnyOrder(league1, league2);
        }
	}

	@Nested
	@DisplayName("can retrieve all Teams belonging to a League")
	class Deletion {
		
		@Test
		@DisplayName("when some Teams belonging to a given League exist in the database")
		void testGetAllTeams() {
			
			// GIVEN some Teams are associated with a given League
			FantaUser admin = new FantaUser("admin", "pswd");
			League league1 = new League(admin, "lega1", "1234");
			FantaUser manager1 = new FantaUser("user1", "pswd1");
			FantaUser manager2 = new FantaUser("user2", "pswd2");
			FantaTeam team1 = new FantaTeam("team1", league1, 10, manager1, new HashSet<Contract>());
			FantaTeam team2 = new FantaTeam("team2", league1, 20, manager2, new HashSet<Contract>());
			
			FantaUser otherAdmin = new FantaUser("admin2", "pswd2");
			League otherLeague = new League(otherAdmin, "lega2", "5678");
			FantaUser otherManager = new FantaUser("other", "pswd3");
			FantaTeam otherTeam = new FantaTeam("otherTeam", otherLeague, 5, otherManager, new HashSet<Contract>());
			
			sessionFactory.inTransaction(session -> {
				session.persist(admin);
				session.persist(league1);
				session.persist(manager1);
				session.persist(manager2);
				session.persist(team1);
				session.persist(team2);
				session.persist(otherAdmin);
				session.persist(otherLeague);
				session.persist(otherManager);
				session.persist(otherTeam);
			});
			
			// WHEN the SUT is used to retrieve all Teams belonging to a given League
			entityManager.getTransaction().begin();
			List<FantaTeam> retrieved = leagueRepository.getAllTeams(league1);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN only the expected Teams are retrieved
			assertThat(retrieved).containsExactlyInAnyOrder(team1, team2).doesNotContain(otherTeam);
		}
		
		@Test
		@DisplayName("when no Teams belonging to a given League exist in the database")
		void testGetAllTeams_NoneExist() {
			
			// GIVEN no Teams are associated with a given League
			FantaUser admin = new FantaUser("admin", "pswd");
			League league1 = new League(admin, "lega1", "1234");
			FantaUser manager1 = new FantaUser("user1", "pswd1");
			FantaUser manager2 = new FantaUser("user2", "pswd2");
			FantaTeam team1 = new FantaTeam("team1", league1, 10, manager1, new HashSet<Contract>());
			FantaTeam team2 = new FantaTeam("team2", league1, 20, manager2, new HashSet<Contract>());
			
			FantaUser otherAdmin = new FantaUser("admin2", "pswd2");
			League otherLeague = new League(otherAdmin, "lega2", "5678");
			
			sessionFactory.inTransaction(session -> {
				session.persist(admin);
				session.persist(league1);
				session.persist(manager1);
				session.persist(manager2);
				session.persist(team1);
				session.persist(team2);
				session.persist(otherAdmin);
				session.persist(otherLeague);
			});
			
			// WHEN the SUT is used to retrieve all Teams belonging to a given League
			entityManager.getTransaction().begin();
			List<FantaTeam> retrieved = leagueRepository.getAllTeams(otherLeague);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN only the expected Teams are retrieved
			assertThat(retrieved).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("can retrieve Players competing in a League")
	class LookupInLeague {	
		
		@Test
		@DisplayName("")
		public void testGetAllInLeague() {
			
			// GIVEN Contracts are persisted for Players under two Leagues
			Player player1 = new Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
			Player player2 = new Forward("Lionel", "Messi", Club.MILAN);
			Player player3 = new Forward("Lamine", "Yamal", Club.MILAN);
			
			FantaUser admin = new FantaUser("mail", "pswd");
			League league = new League(admin, "Lega", "codice");
			League otherLeague = new League(admin, "Lega", "codice");
			FantaTeam teamA = new FantaTeam("", league, 0, admin, new HashSet<>());
			teamA.getContracts().add(new Contract(teamA, player1));
			FantaTeam teamB = new FantaTeam("", league, 0, admin, new HashSet<>());
			teamB.getContracts().add(new Contract(teamB, player2));
			FantaTeam teamC = new FantaTeam("", otherLeague, 0, admin, new HashSet<>());
			teamC.getContracts().add(new Contract(teamC, player3));
			
			sessionFactory.inTransaction(session -> {
				session.persist(player1);
				session.persist(player2);
				session.persist(player3);
				session.persist(admin);
				session.persist(league);
				session.persist(otherLeague);
				session.persist(teamA);
				session.persist(teamB);
				session.persist(teamC);
			});
			
			// WHEN the SUT is used to retrieve Players in the given League
			entityManager.getTransaction().begin();
			Set<Player> players = leagueRepository.getAllInLeague(league);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN only the appropriate Players are returned
			assertThat(players).containsExactlyInAnyOrder(player1, player2);
		}
	}
}
