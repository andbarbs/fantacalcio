package businessLogic;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import businessLogic.abstractDAL.repository.AbstarctJpaProposalRepository;
import businessLogic.abstractDAL.repository.AbstractJpaGradeRepository;
import businessLogic.abstractDAL.repository.AbstractJpaTeamRepository;
import domainModel.*;
import org.hibernate.SessionFactory;

import businessLogic.abstractDAL.repository.AbstractJpaMatchRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class Service {	
	
	private SessionFactory sessionFactory;
	private AbstractJpaMatchRepository matchRepository;
	private AbstractJpaTeamRepository teamRepository;
	private AbstractJpaGradeRepository gradeRepository;
	private AbstarctJpaProposalRepository proposalRepository;

	public Service(SessionFactory sessionFactory, AbstractJpaMatchRepository matchRepository, AbstractJpaTeamRepository teamRepository, AbstractJpaGradeRepository gradeRepository) {
		this.sessionFactory = sessionFactory;
		this.matchRepository = matchRepository;
		this.teamRepository = teamRepository;
		this.gradeRepository = gradeRepository;
	}

	public Map<MatchDaySerieA, Set<Match>> getAllMatches(League league) {
	    return fromSession(sessionFactory, em -> matchRepository.getAllMatches(em, league));
	}

	public Set<FantaTeam> getAllFantaTeams(League league) {
		return fromSession(sessionFactory, em -> teamRepository.getAllTeams(em, league));
	}

	public List<FantaTeam> getStandings(League league) {
		Set<FantaTeam> teams = getAllFantaTeams(league); // your existing method

		return teams.stream()
				.sorted(Comparator.comparing(FantaTeam::getPoints).reversed())
				.collect(Collectors.toList());
	}

	public Set<Grade> getAllMatchGrades(League league, Match match) {
		return fromSession(sessionFactory, em -> gradeRepository.getAllMatchGrades(em, match , league ));
	}

	public List<Proposal> getAllTeamProposals(League league, FantaTeam team) {
		return fromSession(sessionFactory, em -> proposalRepository.getMyProposals(em, league, team) );
	}

	public boolean acceptProposal(Proposal proposal) {
		return fromSession(sessionFactory, em -> proposalRepository.acceptedProposal(em, proposal));
	}

	public boolean rejectProposal(Proposal proposal) {
		return fromSession(sessionFactory, em -> proposalRepository.rejectedProposal(em, proposal));
	}

	
	static <T> T fromSession(EntityManagerFactory factory, Function<EntityManager, T> work) {
	    EntityManager em = factory.createEntityManager();
	    EntityTransaction transaction = em.getTransaction();
	    try {
	        transaction.begin();
	        T result = work.apply(em);
	        transaction.commit();
	        return result;
	    } catch (Exception e) {
	        if (transaction.isActive()) {
	            transaction.rollback();
	        }
	        throw e;
	    } finally {
	        em.close();
	    }
	}




}
