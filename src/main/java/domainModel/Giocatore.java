package domainModel;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Giocatore {
    public static enum Ruolo {PORTIERE, DIFENSORE, CENTROCAMPISTA, ATTACCANTE}
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    
    
    @Enumerated(EnumType.STRING)
    private Ruolo ruolo;
    
    private String name;
    private String surname;
    
    public Giocatore() {}

    public Giocatore(Ruolo ruolo, String name, String surname) {
        this.ruolo = ruolo;
        this.name = name;
        this.surname = surname;
    }

    // Getter
    
    public Long getId() {
		return id;
	}
    
    public Ruolo getRuolo() {
        return ruolo;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

	@Override
	public int hashCode() {
		return Objects.hash(name, ruolo, surname);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Giocatore other = (Giocatore) obj;
		return Objects.equals(name, other.name) && ruolo == other.ruolo && Objects.equals(surname, other.surname);
	}
   
}

