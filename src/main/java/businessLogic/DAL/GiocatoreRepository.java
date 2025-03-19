package businessLogic.DAL;

import domainModel.Giocatore;

import java.util.List;

public interface GiocatoreRepository {
    List<Giocatore> getAllGiocatori();
}
