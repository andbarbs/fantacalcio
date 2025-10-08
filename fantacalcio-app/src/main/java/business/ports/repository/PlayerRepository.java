package business.ports.repository;

import java.util.List;
import java.util.Set;

import domain.Player;

public interface PlayerRepository {

	public List<Player> findAll();
	
	public boolean addPlayer(Player newPlayer);

	public List<Player> findBySurname(String surname);
	
	public Set<Player> findByClub(Player.Club club);

}
