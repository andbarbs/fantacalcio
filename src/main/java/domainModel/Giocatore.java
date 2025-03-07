package domainModel;

public class Giocatore {
    public static enum Ruolo {PORTIERE, DIFENSORE, CENTROCAMPISTA, ATTACCANTE}
    private Ruolo ruolo;
    private String name;
    private String surname;

    public Giocatore(Ruolo ruolo, String name, String surname) {
        this.ruolo = ruolo;
        this.name = name;
        this.surname = surname;
    }

    public Giocatore() {}

    // Getter
    public Ruolo getRuolo() {
        return ruolo;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }
}

