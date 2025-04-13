package domainModel;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.Set;

import domainModel.Fielding.StarterFielding;
import domainModel.Player.Defender;
import domainModel.Player.Midfielder;
import domainModel.Player.Striker;

@Entity
public abstract class LineUp {
    // public static enum Module{_343, _433, _352}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Match match;
    
    @OneToMany(mappedBy = Fielding_.LINE_UP)
    private Set<Fielding> fieldings;

    LineUp() {}
    
    LineUp(Match match, Set<Fielding> fieldings) {
		this.match = match;
		this.fieldings = fieldings;
	}

	// getters
    
    public Match getMatch() {
        return match;
    }

    public Set<Fielding> getFieldings() {
    	return fieldings;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LineUp lineUp = (LineUp) o;
        return Objects.equals(match, lineUp.match);
    }

    @Override
    public int hashCode() {
        return Objects.hash(match);
    }
    

	@Entity
    public static abstract class ThreePositionLineUp extends LineUp {
    	
    	@Entity
        public static class DefenderFielding extends StarterFielding {

    		DefenderFielding() {}
            
    		public DefenderFielding(Defender player, LineUp lineUp) {
                super(player, lineUp);
            }
    	}
    	
    	@Entity
        public static class MidfielderFielding extends StarterFielding {

    		MidfielderFielding() {}
            
    		public MidfielderFielding(Midfielder player, LineUp lineUp) {
                super(player, lineUp);
            }
    	}
    	
    	@Entity
        public static class ForwardFielding extends StarterFielding {

    		ForwardFielding() {}
            
    		public ForwardFielding(Striker player, LineUp lineUp) {
                super(player, lineUp);
            }
    	}
    	
    	ThreePositionLineUp() {}
    	
    	ThreePositionLineUp(Match match, Set<Fielding> fieldings) {
    		super(match, fieldings);
    	}
    	
    }
    
    @Entity
    public static abstract class FourPositionLineUp extends LineUp {
    	
    	@Entity
        public static class DefenderFielding extends StarterFielding {
    		
    		DefenderFielding() {}
            
    		public DefenderFielding(Player player, LineUp lineUp) {
                super(player, lineUp);
            }
    	}
    	
    	@Entity
        public static class DefensiveMidfielderFielding extends StarterFielding {}
    	
    	
    	@Entity
        public static class AttackingMidfielderFielding extends StarterFielding {}
    	
    	@Entity
        public static class ForwardFielding extends StarterFielding {}
    	
    	FourPositionLineUp() {}
    	
    	FourPositionLineUp(Match match, Set<Fielding> fieldings) {
    		super(match, fieldings);
    	}
    	
    }
}
