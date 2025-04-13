package domainModel;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public abstract class Fielding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Player player;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private LineUp lineUp;

    Fielding() {}
    
    Fielding(Player player, LineUp lineUp) {
        this.player = player;
        this.lineUp = lineUp;
    }

    // Getters
    
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
        return Objects.equals(player, fielding.player) && Objects.equals(lineUp, fielding.lineUp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, lineUp);
    }
    
    @Entity
    public static abstract class StarterFielding extends Fielding {
    	
    	StarterFielding() {}
        
    	StarterFielding(Player player, LineUp lineUp) {
            super(player, lineUp);
        }
    }
    
    @Entity
    public static class SubstituteFielding extends Fielding {
    	
    	SubstituteFielding() {}
        
    	public SubstituteFielding(Player player, LineUp lineUp) {
            super(player, lineUp);
        }


    }
}

