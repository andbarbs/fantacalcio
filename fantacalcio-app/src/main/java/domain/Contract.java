package domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private FantaTeam team;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Player player;
    
    protected Contract() {}

    public Contract(FantaTeam team, Player player) {
        this.team = team;
        this.player = player;
    }

    // Getters
    public FantaTeam getTeam() {
        return team;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Contract contract = (Contract) o;
        return Objects.equals(team, contract.team) && Objects.equals(player, contract.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(team, player);
    }
}
