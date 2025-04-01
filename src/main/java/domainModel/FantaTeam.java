package domainModel;

public class FantaTeam {
    private final String name;
    private final League league;
    private int points;
    private final User fantaManager;

    public FantaTeam(String name, League league, int points, User fantaManager) {
        this.name = name;
        this.league = league;
        this.points = points;
        this.fantaManager = fantaManager;
    }

    // Getters
    public League getLeague() {
        return league;
    }

    public int getPoints() {
        return points;
    }

    public User getFantaManager() {
        return fantaManager;
    }

    public String getName() {
        return name;
    }
}

