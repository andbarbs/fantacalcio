package repository;

import java.util.List;

import model.Player;

public interface PlayerRepository {

	public List<Player> findAll();

	public Player findById(int id);
}
