package businessLogic.presenter;

import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import businessLogic.abstractDAL.repository.AbstractJpaPlayerRepository;
import businessLogic.abstractView.PlayerView;
import domainModel.Player;

public class PlayerListPresenterTest {

	PlayerListPresenter playerPresenter;
	PlayerView playerView;
	AbstractJpaPlayerRepository playerRepository;
	List<Player> players;

	@BeforeEach
	public void setup() {
		playerView = mock(PlayerView.class);
		playerRepository = mock(AbstractJpaPlayerRepository.class);
		playerPresenter = new PlayerListPresenter(playerView, playerRepository);
		players = new ArrayList<Player>();
	}

	@Test
	public void testAllPlayers() {
		when(playerRepository.findAll(playerPresenter.session)).thenReturn(players);// vedi playerListPresenter
		playerPresenter.showAllPlayers();
		verify(playerView).showAllPlayers(players);
	}

	@Test
	public void testShowPlayersWithSurname() {
		when(playerRepository.findBySurname(playerPresenter.session,"Rossi")).thenReturn(players); // vedi playerListPresenter
		playerPresenter.showPlayersWithSurname("Rossi");
		verify(playerView).showPlayersBySurname(players);
	}

}
