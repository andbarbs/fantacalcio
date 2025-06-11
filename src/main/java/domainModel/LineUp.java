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

}
