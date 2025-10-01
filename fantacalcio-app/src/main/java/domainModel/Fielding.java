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
    

	public interface FieldingVisitor {
		void visitStarterFielding(StarterFielding starterFielding);
		void visitSubstituteFielding(SubstituteFielding substituteFielding);		
	}
	
	public static class FieldingVisitorAdapter implements FieldingVisitor {

		@Override
		public void visitStarterFielding(StarterFielding starterFielding) {}

		@Override
		public void visitSubstituteFielding(SubstituteFielding substituteFielding) {}
		
	}
    
    public abstract void accept(FieldingVisitor visitor);
    
    @Entity
    public static class StarterFielding extends Fielding {
    	
    	protected StarterFielding() {}
        
    	public StarterFielding(Player player, LineUp lineUp) {
            super(player, lineUp);
        }

		@Override
		public void accept(FieldingVisitor visitor) {
			visitor.visitStarterFielding(this);
		}    	
    }
    
    @Entity
    public static class SubstituteFielding extends Fielding {
    	
    	private int benchPosition;

		protected SubstituteFielding() {}
        
    	public SubstituteFielding(Player player, LineUp lineUp, int benchPosition) {
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

		@Override
		public void accept(FieldingVisitor visitor) {
			visitor.visitSubstituteFielding(this);
		}
		
    }
}

