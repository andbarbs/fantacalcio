package businessLogic.abstractRepositories;

import java.util.List;

import domainModel.FantaTeam;
import domainModel.Player;

public interface PlayerRepository {

	public List<Player> findAll();
	
	public boolean addPlayer(Player newPlayer);

	public List<Player> findBySurname(String surname);
	
	public List<Player> findByTeam(FantaTeam team);
}
