package businessLogic.presenter;

import businessLogic.abstractDAL.repository.AbstractJpaPlayerRepository;
import businessLogic.abstractView.PlayerView;
import jakarta.persistence.EntityManager;

public class PlayerListPresenter {

	private PlayerView playerView;
	private AbstractJpaPlayerRepository playerRepository;
	public EntityManager session;// aggiunto perchè mi dava problemi tanto dovrebbe sparire sta classe

	public PlayerListPresenter(PlayerView playerView, AbstractJpaPlayerRepository playerRepository) {
		this.playerView = playerView;
		this.playerRepository = playerRepository;
	}

	public void showAllPlayers() {
		playerView.showAllPlayers(playerRepository.findAll(session));
	}

	public void showPlayersWithSurname(String surname) {
		playerView.showPlayersBySurname(playerRepository.findBySurname(session,surname));
	}

}
