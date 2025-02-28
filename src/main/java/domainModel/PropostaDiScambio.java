package domainModel;

public class PropostaDiScambio {
    private FantaTeam teamProponente;
    private FantaTeam teamRicevente;
    private Giocatore giocatoreProposto;
    private Giocatore giocatoreRichiesto;

    // Getters
    public FantaTeam getTeamProponente() {
        return teamProponente;
    }

    public FantaTeam getTeamRicevente() {
        return teamRicevente;
    }

    public Giocatore getGiocatoreProposto() {
        return giocatoreProposto;
    }

    public Giocatore getGiocatoreRichiesto() {
        return giocatoreRichiesto;
    }
}

