package business;

import business.ports.repository.FantaUserRepository;
import business.ports.transaction.TransactionManager;
import domain.FantaUser;

public class LoginService {
	
	protected final TransactionManager transactionManager;

	public LoginService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	public void registerFantaUser(String mail, String password) {
		transactionManager.inTransaction((context) -> {
			FantaUserRepository fantaUserRepository = context.getFantaUserRepository();
			if(fantaUserRepository.getUser(mail, password).isEmpty())
				fantaUserRepository.saveFantaUser(new FantaUser(mail, password));
			else
				throw new UnsupportedOperationException("The user is already registered");
		});
	}

	public boolean loginFantaUser(String mail, String password) {
		return transactionManager.fromTransaction((context) -> context.getFantaUserRepository().getUser(mail, password).isPresent());
	}

}
