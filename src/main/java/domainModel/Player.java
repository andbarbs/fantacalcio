package domainModel;

import java.util.Objects;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Player {
    public static enum Role {GOALKEEPER, DEFENDER, MIDFIELDER, STRIKER}
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    
    
    @Basic(optional=false)
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Basic(optional=false)
    private String name;
    
    @Basic(optional=false)
    private String surname;
    
    public Player() {}

    public Player(Role role, String name, String surname) {
        this.role = role;
        this.name = name;
        this.surname = surname;
    }

    // Getter
    public Role getRuolo() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

	@Override
	public int hashCode() {
		return Objects.hash(name, role, surname);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		return Objects.equals(name, other.name) && role == other.role && Objects.equals(surname, other.surname);
	}
    
    
}

