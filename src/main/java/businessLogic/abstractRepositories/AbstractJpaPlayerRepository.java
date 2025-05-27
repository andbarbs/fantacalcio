package businessLogic.abstractRepositories;

import java.util.List;

import domainModel.Player;
import jakarta.persistence.EntityManager;

public interface AbstractJpaPlayerRepository {

	public List<Player> findAll(EntityManager session);
	
	public boolean addPlayer(EntityManager session, Player newPlayer);

	public List<Player> findBySurname(EntityManager session, String surname);
}
