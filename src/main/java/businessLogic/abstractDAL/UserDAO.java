package businessLogic.abstractDAL;

import java.util.Optional;

public interface UserDAO {

    void saveUser(String username, String password);
    Optional<UserDAO> authenticate(String username, String password);


}
