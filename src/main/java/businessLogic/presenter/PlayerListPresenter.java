package businessLogic.presenter;

import businessLogic.DAL.repository.PlayerRepository;
import view.PlayerView;

public class PlayerListPresenter {

	private PlayerView playerView;
	private PlayerRepository playerRepository;

	public PlayerListPresenter(PlayerView playerView, PlayerRepository playerRepository) {
		this.playerView = playerView;
		this.playerRepository = playerRepository;
	}

	public void allPlayers() {
		playerView.showAllPlayers(playerRepository.findAll());
	}

	/*
	public void showPlayersWithId(int id) {
		playerView.showPlayerById(playerRepository.findById(id));
	}
	*/

}
