package domainModel;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;

import com.fantacalcio.app.generator.api.GenerateScheme;
import com.fantacalcio.app.generator.api.GenerateSchemes;

// coincides with ThreePositionLineUp by definition of the game

@GenerateSchemes({
	@GenerateScheme(className = "Scheme433", defenders = 4, midfielders = 3, forwards = 3),
    @GenerateScheme(className = "Scheme343", defenders = 3, midfielders = 4, forwards = 3),
    @GenerateScheme(className = "Scheme532", defenders = 5, midfielders = 3, forwards = 2)
})
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {LineUp_.MATCH, LineUp_.TEAM}))
public abstract class LineUp {
    // public static enum Module{_343, _433, _352}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = LineUp_.MATCH)
	private Match match;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = LineUp_.TEAM)
	private FantaTeam team;
    
	@OneToMany(mappedBy = Fielding_.LINE_UP, cascade = CascadeType.ALL, orphanRemoval = true)
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
	
	// entry point for strongly-typed Player lookup
	public LineUpViewer extract() {
		return new LineUpViewer(this);
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
