package businessLogic.presenter;

import businessLogic.abstractDAL.repository.AbstractJpaPlayerRepository;
import businessLogic.abstractView.PlayerView;

public class PlayerListPresenter {

	private PlayerView playerView;
	private AbstractJpaPlayerRepository playerRepository;

	public PlayerListPresenter(PlayerView playerView, AbstractJpaPlayerRepository playerRepository) {
		this.playerView = playerView;
		this.playerRepository = playerRepository;
	}

	public void showAllPlayers() {
		playerView.showAllPlayers(playerRepository.findAll());
	}

	public void showPlayersWithSurname(String surname) {
		playerView.showPlayersBySurname(playerRepository.findBySurname(surname));
	}

}
