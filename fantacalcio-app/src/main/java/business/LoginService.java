package business;

import business.ports.repository.FantaUserRepository;
import business.ports.repository.NewsPaperRepository;
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
	
	public void registerNewsPaper(String name) {
		transactionManager.inTransaction((context) -> {
			NewsPaperRepository newsPaperRepository = context.getNewspaperRepository();
			if(newsPaperRepository.getNewspaper(name).isEmpty())
				newsPaperRepository.saveNewsPaper(new NewsPaper(name));
			else
				throw new UnsupportedOperationException("The newspaper is already registered");
		});
	}
	
	public boolean loginFantaUser(String mail, String password) {
		return transactionManager.fromTransaction((context) -> {
			return context.getFantaUserRepository().getUser(mail, password).isPresent();
		});
	}
	
	public boolean loginNewsPaper(String name) {
		return transactionManager.fromTransaction((context) -> {
			return context.getNewspaperRepository().getNewspaper(name).isPresent();
		});
	}

}
