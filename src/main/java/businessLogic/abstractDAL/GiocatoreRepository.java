package businessLogic.abstractDAL;

import domainModel.Player;

import java.util.List;

public interface GiocatoreRepository {
    List<Player> getAllGiocatori();
}
