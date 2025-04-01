package businessLogic.abstractDAL.repository;

import java.util.List;

import domainModel.Player;

public interface PlayerRepository {

	public List<Player> findAll();

	public Player findById(int id);
	
	public boolean addPlayer(Player newPlayer);

	public List<Player> findBySurname(String surname);
}
