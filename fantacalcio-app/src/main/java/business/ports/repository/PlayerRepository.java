package business.ports.repository;

import java.util.List;
import java.util.Set;

import domain.League;
import domain.Player;

public interface PlayerRepository {

	Set<Player> findAll();
	
	boolean addPlayer(Player newPlayer);

	List<Player> findBySurname(String surname);
	
	Set<Player> findByClub(Player.Club club);
	
	Set<Player> getAllInLeague(League league);
}
