package domainModel;

public class Giocatore {
    public static enum Ruolo {PORTIERE, DIFENSORE, CENTROCAMPISTA, ATTACCANTE}
    private Ruolo ruolo;

    // Getter
    public Ruolo getRuolo() {
        return ruolo;
    }
}

