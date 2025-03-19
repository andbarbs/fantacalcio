package businessLogic.DAL;

import java.util.Optional;

public interface Utente {

    void saveUtente(String username, String password);
    Optional<Utente> authenticate(String username, String password);


}
