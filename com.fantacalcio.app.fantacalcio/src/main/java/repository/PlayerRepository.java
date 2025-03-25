package repository;

import java.util.List;

import model.FantaCoach;
import model.Player;
import model.Team;

public interface PlayerRepository {

	public List<Player> findAll();
	
	public Player findById(int id);
	
	public List<Player> findByTeam(Team team);

	public List<Player> findBySurname(String surname);

	public List<Player> findFantateam(FantaCoach fantaCoach);
}
