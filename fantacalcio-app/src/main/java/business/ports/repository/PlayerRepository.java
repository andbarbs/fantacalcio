package business.ports.repository;

import java.util.List;
import java.util.Set;

import domain.League;
import domain.Player;

public interface PlayerRepository {

	public Set<Player> findAll();
	
	public boolean addPlayer(Player newPlayer);

	public List<Player> findBySurname(String surname);
	
	public Set<Player> findByClub(Player.Club club);

	public Set<Player> getAllInLeague(League league);

}
