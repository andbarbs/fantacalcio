package businessLogic.abstractDAL;

import domainModel.Player;

import java.util.List;

public interface PlayerRepository {
    List<Player> getAllPlayers();
}
