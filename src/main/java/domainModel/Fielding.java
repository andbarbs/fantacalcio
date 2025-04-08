package domainModel;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Fielding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic(optional = false)
    private boolean starter;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Player player;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private LineUp lineUp;

    public Fielding() {}
    public Fielding(boolean starter, Player player, LineUp lineUp) {
        this.starter = starter;
        this.player = player;
        this.lineUp = lineUp;
    }

    // Getters

    public boolean isStarter() {
        return starter;
    }

    public Player getPlayer() {
        return player;
    }

    public LineUp getLineUp() {
        return lineUp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Fielding fielding = (Fielding) o;
        return starter == fielding.starter && Objects.equals(player, fielding.player) && Objects.equals(lineUp, fielding.lineUp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(starter, player, lineUp);
    }
}

