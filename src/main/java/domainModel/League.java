package domainModel;

public class League {
    private final User admin;
    private final String name;
    private final NewsPaper newsPaper;
    private final String leagueCode;

    public League(User admin, String name, NewsPaper newsPaper, String leagueCode) {
        this.admin = admin;
        this.name = name;
        this.newsPaper = newsPaper;
        this.leagueCode = leagueCode;
    }
    // Getters
    public User getAdmin() {
        return admin;
    }

    public String getName() {
        return name;
    }

    public NewsPaper getTestata() {
        return newsPaper;
    }

    public String getLeagueCode() {
        return leagueCode;
    }
}
