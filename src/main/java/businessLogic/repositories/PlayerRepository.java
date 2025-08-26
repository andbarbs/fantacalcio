package businessLogic.repositories;

import java.util.List;

import domainModel.Player;

public interface PlayerRepository {

	public List<Player> findAll();
	
	public boolean addPlayer(Player newPlayer);

	public List<Player> findBySurname(String surname);
	
	public List<Player> findByTeam(String team);
	
}
