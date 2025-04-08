package domainModel;

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
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    private String name;
    private String surname;

    public Player(Role role, String name, String surname) {
        this.role = role;
        this.name = name;
        this.surname = surname;
    }

    public Player() {}

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
}

