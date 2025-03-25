package domainModel;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Player {
    public static enum Ruolo {PORTIERE, DIFENSORE, CENTROCAMPISTA, ATTACCANTE}
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    
    
    @Enumerated(EnumType.STRING)
    private Ruolo ruolo;
    
    private String name;
    private String surname;

    public Player(Ruolo ruolo, String name, String surname) {
        this.ruolo = ruolo;
        this.name = name;
        this.surname = surname;
    }

    public Player() {}

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

