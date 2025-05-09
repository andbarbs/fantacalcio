package businessLogic;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.User;

import java.util.Objects;

public class SessionBean {
	private User user;
	private League league;
	private FantaTeam team;
	private boolean isAdmin;

	private static SessionBean instance = null;

	private SessionBean(User user, League league, FantaTeam team, boolean isAdmin) {
		this.user = user;
		this.league = league;
		this.team = team;
		this.isAdmin = isAdmin;
	}

	public static void initInstance(User user, League league, FantaTeam team, boolean isAdmin) {
		instance = new SessionBean(user, league, team, isAdmin);
	}

	public static SessionBean getInstance() {
		if (Objects.isNull(instance))
			throw new IllegalStateException();
		return instance;
	}

	// Getters
	public User getUser() {
		return user;
	}

	public League getLeague() {
		return league;
	}

	public FantaTeam getTeam() {
		return team;
	}

	public boolean getIsAdmin() {
		return isAdmin;
	}

}
