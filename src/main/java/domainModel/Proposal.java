package domainModel;

public class Proposal {
    private FantaTeam ProposingTeam;
    private FantaTeam ReceivingTeam;
    private Player OfferedPlayer;
    private Player RequestedPlayer;

    // Getters
    public FantaTeam getProposingTeam() {
        return ProposingTeam;
    }

    public FantaTeam getReceivingTeam() {
        return ReceivingTeam;
    }

    public Player getGiocatoreProposto() {
        return OfferedPlayer;
    }

    public Player getGiocatoreRichiesto() {
        return RequestedPlayer;
    }
}

