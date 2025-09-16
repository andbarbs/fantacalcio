package businessLogic.repositories;

import java.util.Optional;

import domainModel.FantaUser;

public interface FantaUserRepository {

	public Optional<FantaUser> getUser(String mail, String password);

	public void saveFantaUser(FantaUser fantaUser);

}
