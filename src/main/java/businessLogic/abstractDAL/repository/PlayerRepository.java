package businessLogic.abstractDAL.repository;

import java.util.List;

import domainModel.Player;

public interface PlayerRepository {

	public List<Player> findAll();

	public Player findById(int id);
}
