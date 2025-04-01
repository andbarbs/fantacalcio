package businessLogic;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.User;

import java.time.LocalDateTime;

public class SessionBean {
    private User user;
    private League league;
    private FantaTeam team;
    private boolean isAdmin;
    private LocalDateTime date;

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

    public LocalDateTime getDate() {
        return date;
    }
}

