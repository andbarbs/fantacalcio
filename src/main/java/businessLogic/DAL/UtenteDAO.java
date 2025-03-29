package businessLogic.DAL;

import java.util.Optional;

import domainModel.Utente;

public interface UtenteDAO {

    void saveUtente(String username, String password);
    Optional<Utente> authenticate(String username, String password);


}
