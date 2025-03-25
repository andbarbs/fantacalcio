package presenter;

import model.FantaCoach;
import model.Player;
import model.Team;
import repository.PlayerRepository;
import view.PlayerView;

public class PlayerListPresenter {

	private PlayerView playerView;
	private PlayerRepository playerRepository;
	private FantaCoach fantaCoach;

	public PlayerListPresenter(PlayerView playerView, PlayerRepository playerRepository, FantaCoach fantaCoach) {
		this.playerView = playerView;
		this.playerRepository = playerRepository;
		this.fantaCoach = fantaCoach;
	}

	public void allPlayers() {
		playerView.showPlayers(playerRepository.findAll());
	}

	public void filterPlayersByTeam(Team team) {
		playerView.showPlayers(playerRepository.findByTeam(team));
	}
	
	public void filterPlayersBySurname(String surname) {
		playerView.showPlayers(playerRepository.findBySurname(surname));
	}
	
	public void getMyPlayers() {
		playerView.showPlayers(playerRepository.findFantateam(fantaCoach));
	}
	
	public void getPlayerInfos(Player player) {
		playerView.showPlayerInfos(playerRepository.findById(Player.getId()));
	}
	

}
