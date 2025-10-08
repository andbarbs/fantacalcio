package business.ports.repository;

import java.util.Optional;

import domain.FantaUser;

public interface FantaUserRepository {

	public Optional<FantaUser> getUser(String mail, String password);

	public void saveFantaUser(FantaUser fantaUser);

}
