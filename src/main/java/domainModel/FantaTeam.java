package domainModel;

public class FantaTeam {
    private final String name;
    private final League lega;
    private int punti;
    private final Utente proprietario;

    public FantaTeam(String name, League lega, int punti, Utente proprietario) {
        this.name = name;
        this.lega = lega;
        this.punti = punti;
        this.proprietario = proprietario;
    }

    // Getters
    public League getLega() {
        return lega;
    }

    public int getPunti() {
        return punti;
    }

    public Utente getProprietario() {
        return proprietario;
    }

    public String getName() {
        return name;
    }
}

