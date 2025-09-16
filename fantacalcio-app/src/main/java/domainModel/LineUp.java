package domainModel;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.fantacalcio.app.generator.api.GenerateScheme;
import com.fantacalcio.app.generator.api.GenerateSchemes;

import domainModel.LineUp.LineUpBuilderSteps.ReadyForStarterLineUp;
import domainModel.LineUp.LineUpBuilderSteps.ReadyForSubstituteDefenders;
import domainModel.LineUp.LineUpBuilderSteps.ReadyForSubstituteForwards;
import domainModel.LineUp.LineUpBuilderSteps.ReadyForSubstituteGoalkeepers;
import domainModel.LineUp.LineUpBuilderSteps.ReadyForSubstituteMidfielders;
import domainModel.LineUp.LineUpBuilderSteps.StarterLineUp;
import domainModel.Player.Defender;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel.Player.Midfielder;

// coincides with ThreePositionLineUp by definition of the game

@GenerateSchemes({
	@GenerateScheme(defenders = 4, midfielders = 3, forwards = 3),
    @GenerateScheme(defenders = 3, midfielders = 4, forwards = 3),
    @GenerateScheme(defenders = 5, midfielders = 3, forwards = 2)
})
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {LineUp_.MATCH, LineUp_.TEAM}))
public class LineUp {
    // public static enum Module{_343, _433, _352}
	
	private Scheme scheme;

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
    
    LineUp(Scheme scheme, Match match, FantaTeam team, Set<Fielding> fieldings) {
		this.scheme = scheme;
		this.match = match;
		this.team = team;
		this.fieldings = fieldings;
	}

	// getters
    
    public Scheme getScheme() {
		return scheme;
	}
    
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
	
	public static LineUpBuilder build() {
		return new LineUpBuilder();
	}
	
	public static abstract class LineUpBuilderSteps {
		
		interface ReadyForTeam {
			ReadyForMatch forTeam(FantaTeam team);
		}
		
		interface ReadyForMatch {
			ReadyForStarterLineUp inMatch(Match match);
		}
		
		public static final class StarterLineUp {

			private Scheme scheme;
			private Goalkeeper starterGoalie;
			private List<Defender> starterDefenders;
			private List<Midfielder> starterMidfielder;
			private List<Forward> starterForwards;

			public StarterLineUp(Scheme scheme, 
					Goalkeeper starterGoalie,
					List<Defender> starterDefenders,
					List<Midfielder> starterMidfielder,
					List<Forward> starterForwards) {
						this.scheme = scheme;
						this.starterGoalie = starterGoalie;
						this.starterDefenders = starterDefenders;
						this.starterMidfielder = starterMidfielder;
						this.starterForwards = starterForwards;
						
						// TODO insert checks on list size == Scheme counts
			}			
		}
		
		interface ReadyForStarterLineUp {
			ReadyForSubstituteGoalkeepers withStarterLineUp(StarterLineUp starters);
		}
		
		interface ReadyForSubstituteGoalkeepers {
			ReadyForSubstituteDefenders withSubstituteGoalkeepers(Goalkeeper goalie1, Goalkeeper goalie2, Goalkeeper goalie3);
		}

		interface ReadyForSubstituteDefenders {
			ReadyForSubstituteMidfielders withSubstituteDefenders(Defender defender1, Defender defender2, Defender defender3);
		}

		interface ReadyForSubstituteMidfielders {
			ReadyForSubstituteForwards withSubstituteMidfielders(Midfielder midfielder1, Midfielder midfielder2,
					Midfielder midfielder3);
		}

		interface ReadyForSubstituteForwards {
			LineUp withSubstituteForwards(Forward forward1, Forward forward2, Forward forward3);
		}		
	}
	
	public static final class LineUpBuilder implements LineUpBuilderSteps.ReadyForTeam,
										LineUpBuilderSteps.ReadyForMatch,
										LineUpBuilderSteps.ReadyForStarterLineUp,
										LineUpBuilderSteps.ReadyForSubstituteGoalkeepers, 
										LineUpBuilderSteps.ReadyForSubstituteDefenders, 
										LineUpBuilderSteps.ReadyForSubstituteMidfielders,
										LineUpBuilderSteps.ReadyForSubstituteForwards {

		// initializes LineUp in order to wire it with Fielding instances
		private LineUp lineUp;

		@Override
		public LineUpBuilderSteps.ReadyForMatch forTeam(FantaTeam team) {
			this.lineUp = new LineUp();
			this.lineUp.team = team;
			return this;
		}

		@Override
		public ReadyForStarterLineUp inMatch(Match match) {
			this.lineUp.match = match;
			return this;
		}

		@Override
		public ReadyForSubstituteGoalkeepers withStarterLineUp(StarterLineUp starterLineUp) {
			this.lineUp.scheme = starterLineUp.scheme;
			this.lineUp.fieldings = new HashSet<Fielding>();
			Stream.of(List.of(starterLineUp.starterGoalie), 
					starterLineUp.starterDefenders, 
					starterLineUp.starterMidfielder, 
					starterLineUp.starterForwards)
				.flatMap(List::stream)
				.map(player -> new Fielding.StarterFielding(player, lineUp))
				.forEach(lineUp.fieldings::add);
			return this;
		}
		
		private void addSubstituteFieldings(Player player1, Player player2, Player player3) {
			this.lineUp.fieldings.add(new Fielding.SubstituteFielding(Objects.requireNonNull(player1), lineUp, 1));
			this.lineUp.fieldings.add(new Fielding.SubstituteFielding(Objects.requireNonNull(player2), lineUp, 2));
			this.lineUp.fieldings.add(new Fielding.SubstituteFielding(Objects.requireNonNull(player3), lineUp, 3));
		}

		@Override
		public ReadyForSubstituteDefenders withSubstituteGoalkeepers(Goalkeeper goalie1, Goalkeeper goalie2,
				Goalkeeper goalie3) {
			// TODO add duplicate checks
			addSubstituteFieldings(goalie1, goalie2, goalie3);
			return this;
		}

		@Override
		public ReadyForSubstituteMidfielders withSubstituteDefenders(Defender defender1, Defender defender2,
				Defender defender3) {
			// TODO add duplicate checks
			addSubstituteFieldings(defender1, defender2, defender3);
			return this;
		}

		@Override
		public ReadyForSubstituteForwards withSubstituteMidfielders(Midfielder midfielder1, Midfielder midfielder2,
				Midfielder midfielder3) {
			// TODO add duplicate checks
			addSubstituteFieldings(midfielder1, midfielder2, midfielder3);
			return this;
		}

		@Override
		public LineUp withSubstituteForwards(Forward forward1, Forward forward2, Forward forward3) {
			// TODO add duplicate checks
			addSubstituteFieldings(forward1, forward2, forward3);
			return lineUp;
		}
	}
}
