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

    protected Fielding() {}
    
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
    	
    	protected StarterFielding() {}
        
    	StarterFielding(Player player, LineUp lineUp) {
            super(player, lineUp);
        }
    }
    
    @Entity
    public static abstract class SubstituteFielding extends Fielding {    	
    	
    	private int benchPosition;

		protected SubstituteFielding() {}
        
    	SubstituteFielding(Player player, LineUp lineUp, int benchPosition) {
            super(player, lineUp);
			this.benchPosition = benchPosition;
        }

		public int getBenchPosition() {
			return benchPosition;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(benchPosition);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			SubstituteFielding other = (SubstituteFielding) obj;
			return benchPosition == other.benchPosition;
		}
		
    }
}

