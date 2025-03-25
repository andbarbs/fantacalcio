package domainModel;

public class Proposal {
    private FantaTeam teamProponente;
    private FantaTeam teamRicevente;
    private Player playerProposto;
    private Player playerRichiesto;

    // Getters
    public FantaTeam getTeamProponente() {
        return teamProponente;
    }

    public FantaTeam getTeamRicevente() {
        return teamRicevente;
    }

    public Player getGiocatoreProposto() {
        return playerProposto;
    }

    public Player getGiocatoreRichiesto() {
        return playerRichiesto;
    }
}

