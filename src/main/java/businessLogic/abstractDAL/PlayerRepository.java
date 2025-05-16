package businessLogic.abstractDAL;

import domainModel.FantaTeam;
import domainModel.Player;

import java.util.List;

public interface PlayerRepository {
	
    List<Player> getAllPlayers();

	List<Player> getPlayersByTeam(FantaTeam clickedTeam);
}
