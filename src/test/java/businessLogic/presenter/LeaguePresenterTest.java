package businessLogic.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import businessLogic.abstractDAL.repository.LeagueRepository;
import businessLogic.abstractView.LeagueView;
import domainModel.League;

class LeaguePresenterTest {

	LeaguePresenter leaguePresenter;
	LeagueView leagueView;
	LeagueRepository leagueRepository;
	League league;

	@BeforeEach
	public void setup() {
		leagueView = mock(LeagueView.class);
		leagueRepository = mock(LeagueRepository.class);
		leaguePresenter = new LeaguePresenter(leagueView, leagueRepository);
		league = new League(null, null, null, "codice123");
	}
	
	@Test
	public void testCreateLeagueWhenSpecifiedLeagueIsNew() {
		when(leagueRepository.add(league)).thenReturn(true);
		leaguePresenter.createLeague(league);
		verify(leagueView).newLeagueCreated(league);
	}

	@Test
	public void testCreateLeagueWhenSpecifiedLeagueAlreadyExists() {
		when(leagueRepository.add(league)).thenReturn(false);
		leaguePresenter.createLeague(league);
		verify(leagueView).showError(league.getName() + " già esistente");
	}
	
	@Test
	public void testJoinLeague() {
		when(leagueRepository.getLeagueByCode("codice123")).thenReturn(league);
		leaguePresenter.joinLeague("codice123");
		verify(leagueView).joinLeague(league);
	}

}
