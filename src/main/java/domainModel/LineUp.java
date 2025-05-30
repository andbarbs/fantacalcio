package domainModel;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.Set;

import domainModel.Fielding.StarterFielding;
import domainModel.Fielding.SubstituteFielding;
import domainModel.Player.Defender;
import domainModel.Player.Midfielder;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;

// coincides with ThreePositionLineUp by definition of the game

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {LineUp_.MATCH, LineUp_.TEAM}))
public abstract class LineUp {
    // public static enum Module{_343, _433, _352}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Match match;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private FantaTeam team;
    
    @OneToMany(mappedBy = Fielding_.LINE_UP)
    private Set<Fielding> fieldings;

    protected LineUp() {}
    
    LineUp(Match match, FantaTeam team, Set<Fielding> fieldings) {
		this.match = match;
		this.team = team;
		this.fieldings = fieldings;
	}

	// getters
    
    public Match getMatch() {
        return match;
    }
    
    public FantaTeam getTeam() {
    	return team;
    }

    Set<Fielding> getFieldings() {
    	return fieldings;
    }
    
    /*
	 * equals and hashCode do not include the fielding attribute to avoid infinite recursion
	 *   > this would feel better if there was a unique constraint on LineUp.(match, team)
	 *   > an alternative solution would be: requiring lineUp == other.lineUp in Fielding.equals(),
	 *     and excluding lineUp from Fielding.hashCode?
	 */

	@Override
	public int hashCode() {
		return Objects.hash(match);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LineUp other = (LineUp) obj;
		return Objects.equals(match, other.match);
	}
	
	/*
	 * these subtypes of Fielding express the kinds of fielding 
	 * that are significant to a three-position line-up
	 * 
	 *   > consider abolishing them as they merely mirror Player subtypes	 * 
	 */

	@Entity
    public static class GoalkeeperStarterFielding extends StarterFielding {

		protected GoalkeeperStarterFielding() {}
        
		public GoalkeeperStarterFielding(Goalkeeper player, LineUp lineUp) {
            super(player, lineUp);
        }
	}
	
	@Entity
    public static class DefenderStarterFielding extends StarterFielding {

		protected DefenderStarterFielding() {}
        
		public DefenderStarterFielding(Defender player, LineUp lineUp) {
            super(player, lineUp);
        }
	}
	
	@Entity
    public static class MidfielderStarterFielding extends StarterFielding {

		protected MidfielderStarterFielding() {}
        
		public MidfielderStarterFielding(Midfielder player, LineUp lineUp) {
            super(player, lineUp);
        }
	}
	
	@Entity
    public static class ForwardStarterFielding extends StarterFielding {

		protected ForwardStarterFielding() {}
        
		public ForwardStarterFielding(Forward player, LineUp lineUp) {
            super(player, lineUp);
        }
	}
	
	@Entity
    public static class GoalkeeperSubstituteFielding extends SubstituteFielding {

		protected GoalkeeperSubstituteFielding() {}
        
		public GoalkeeperSubstituteFielding(Goalkeeper player, LineUp lineUp, int benchPosition) {
            super(player, lineUp, benchPosition);
        }
	}
	
	@Entity
    public static class DefenderSubstituteFielding extends SubstituteFielding {

		protected DefenderSubstituteFielding() {}
        
		public DefenderSubstituteFielding(Defender player, LineUp lineUp, int benchPosition) {
            super(player, lineUp, benchPosition);
        }
	}
	
	@Entity
    public static class MidfielderSubstituteFielding extends SubstituteFielding {

		protected MidfielderSubstituteFielding() {}
        
		public MidfielderSubstituteFielding(Midfielder player, LineUp lineUp, int benchPosition) {
            super(player, lineUp, benchPosition);
        }
	}
	
	@Entity
    public static class ForwardSubstituteFielding extends SubstituteFielding {

		protected ForwardSubstituteFielding() {}
        
		public ForwardSubstituteFielding(Forward player, LineUp lineUp, int benchPosition) {
            super(player, lineUp, benchPosition);
        }
	}


    
    
}
