package domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import domain.LineUp.LineUpBuilderSteps.ReadyForStarterLineUp;
import domain.LineUp.LineUpBuilderSteps.ReadyForSubstituteDefenders;
import domain.LineUp.LineUpBuilderSteps.ReadyForSubstituteForwards;
import domain.LineUp.LineUpBuilderSteps.ReadyForSubstituteGoalkeepers;
import domain.LineUp.LineUpBuilderSteps.ReadyForSubstituteMidfielders;
import domain.LineUp.LineUpBuilderSteps.StarterLineUp;
import domain.Player.Defender;
import domain.Player.Forward;
import domain.Player.Goalkeeper;
import domain.Player.Midfielder;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {LineUp_.MATCH, LineUp_.TEAM}))
public class LineUp {
	
	@Convert(converter = Scheme.SchemeConverter.class)
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
	 *   > this feels better given the unique constraint on LineUp.(match, team)
	 *   > an alternative solution would be: requiring lineUp == other.lineUp in Fielding.equals(),
	 *     and excluding lineUp from Fielding.hashCode?
	 */

	@Override
	public int hashCode() {
		return Objects.hash(match, team);
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
		return Objects.equals(match, other.match) && Objects.equals(team, other.team);
	}
	
	public boolean recursiveEquals(LineUp other) {
		return this.equals(other)
				&& Objects.equals(scheme, other.scheme)
				&& Objects.equals(extract().starterGoalkeepers(), other.extract().starterGoalkeepers())
				&& Objects.equals(extract().starterDefenders(), other.extract().starterDefenders())
				&& Objects.equals(extract().starterMidfielders(), other.extract().starterMidfielders())
				&& Objects.equals(extract().substituteGoalkeepers(), other.extract().substituteGoalkeepers())
				&& Objects.equals(extract().substituteDefenders(), other.extract().substituteDefenders())
				&& Objects.equals(extract().substituteMidfielders(), other.extract().substituteMidfielders())
				&& Objects.equals(extract().substituteForwards(), other.extract().substituteForwards());
	}
	
	public static LineUpBuilderSteps.ReadyForTeam build() {
		return new LineUpBuilder();
	}
	
	public static abstract class LineUpBuilderSteps {
		
		public interface ReadyForTeam {
			ReadyForMatch forTeam(FantaTeam team);
		}
		
		public interface ReadyForMatch {
			ReadyForStarterLineUp inMatch(Match match);
		}
		
		public static final class StarterLineUp {

			private Scheme scheme;
			private Goalkeeper starterGoalie;
			private Set<Defender> starterDefenders;
			private Set<Midfielder> starterMidfielders;
			private Set<Forward> starterForwards;

			public StarterLineUp(Scheme scheme, 
					Goalkeeper starterGoalie, 
					Set<Defender> starterDefenders,
					Set<Midfielder> starterMidfielders, 
					Set<Forward> starterForwards) {
				this.scheme = Objects.requireNonNull(scheme,
						"LineUpBuilder: cannot instantiate on a null Scheme");;
				
				this.starterGoalie = Objects.requireNonNull(starterGoalie,
						"LineUpBuilder: cannot instantiate on null Starter Goalkeeper");

				if (scheme.getNumDefenders() != starterDefenders.size())
					throw new IllegalArgumentException("not enough Starter Defenders");
				this.starterDefenders = starterDefenders.stream().map(def -> Objects.requireNonNull(def,
						"LineUpBuilder: cannot instantiate on null Starter Defender")).collect(Collectors.toSet());
				
				if (scheme.getNumMidfielders() != starterMidfielders.size())
					throw new IllegalArgumentException("not enough Starter Midfielders");
				this.starterMidfielders = starterMidfielders.stream().map(def -> Objects.requireNonNull(def,
						"LineUpBuilder: cannot instantiate on null Starter Midfielder")).collect(Collectors.toSet());
				
				if (scheme.getNumForwards() != starterForwards.size())
					throw new IllegalArgumentException("not enough Starter Forwards");
				this.starterForwards = starterForwards.stream().map(def -> Objects.requireNonNull(def,
						"LineUpBuilder: cannot instantiate on null Starter Forward")).collect(Collectors.toSet());
			}

			@Override
			public int hashCode() {
				return Objects.hash(scheme, starterDefenders, starterForwards, starterGoalie, starterMidfielders);
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				StarterLineUp other = (StarterLineUp) obj;
				return Objects.equals(scheme, other.scheme) && Objects.equals(starterDefenders, other.starterDefenders)
						&& Objects.equals(starterForwards, other.starterForwards)
						&& Objects.equals(starterGoalie, other.starterGoalie)
						&& Objects.equals(starterMidfielders, other.starterMidfielders);
			}			
		}
		
		public interface ReadyForStarterLineUp {
			ReadyForSubstituteGoalkeepers withStarterLineUp(StarterLineUp starters);
		}
		
		public interface ReadyForSubstituteGoalkeepers {
			ReadyForSubstituteDefenders withSubstituteGoalkeepers(Goalkeeper goalie1, Goalkeeper goalie2, Goalkeeper goalie3);
		}

		public interface ReadyForSubstituteDefenders {
			ReadyForSubstituteMidfielders withSubstituteDefenders(Defender defender1, Defender defender2, Defender defender3);
		}

		public interface ReadyForSubstituteMidfielders {
			ReadyForSubstituteForwards withSubstituteMidfielders(Midfielder midfielder1, Midfielder midfielder2,
					Midfielder midfielder3);
		}

		public interface ReadyForSubstituteForwards {
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
			Stream.of(Set.of(starterLineUp.starterGoalie), 
					starterLineUp.starterDefenders, 
					starterLineUp.starterMidfielders, 
					starterLineUp.starterForwards)
				.flatMap(Set::stream)
				.map(player -> new Fielding.StarterFielding(player, lineUp))
				.forEach(lineUp.fieldings::add);
			return this;
		}
		
		private void addSubstituteFieldings(List<Player> substitutes) {
			IntStream.range(0, 3).forEach(i -> this.lineUp.fieldings
					.add(new Fielding.SubstituteFielding(Objects.requireNonNull(substitutes.get(i)), lineUp, i + 1)));
		}

		@Override
		public ReadyForSubstituteDefenders withSubstituteGoalkeepers(Goalkeeper goalie1, Goalkeeper goalie2,
				Goalkeeper goalie3) {
			if (3 != Stream.of(goalie1, goalie2, goalie3).distinct().count())
				throw new IllegalArgumentException(
						"LineUpBuilder: unable to instantiate on duplicate Substitute Goalkeeper");
			addSubstituteFieldings(Stream.of(goalie1, goalie2, goalie3)
					.map(def -> Objects.requireNonNull(def,
							"LineUpBuilder: cannot instantiate on null Substitute Goalkeeper"))
					.collect(Collectors.toList()));
			return this;
		}

		@Override
		public ReadyForSubstituteMidfielders withSubstituteDefenders(Defender defender1, Defender defender2,
				Defender defender3) {
			if (3 != Stream.of(defender1, defender2, defender3).distinct().count())
				throw new IllegalArgumentException("LineUpBuilder: unable to instantiate on duplicate Substitute Defender");
			addSubstituteFieldings(Stream.of(defender1, defender2, defender3)
					.map(def -> Objects.requireNonNull(def,
							"LineUpBuilder: cannot instantiate on null Substitute Defender"))
					.collect(Collectors.toList()));
			return this;
		}

		@Override
		public ReadyForSubstituteForwards withSubstituteMidfielders(Midfielder midfielder1, Midfielder midfielder2,
				Midfielder midfielder3) {
			if (3 != Stream.of(midfielder1, midfielder2, midfielder3).distinct().count())
				throw new IllegalArgumentException("LineUpBuilder: unable to instantiate on duplicate Substitute Midfielder");
			addSubstituteFieldings(Stream.of(midfielder1, midfielder2, midfielder3)
					.map(def -> Objects.requireNonNull(def,
							"LineUpBuilder: cannot instantiate on null Substitute Midfielder"))
					.collect(Collectors.toList()));			
			return this;
		}

		@Override
		public LineUp withSubstituteForwards(Forward forward1, Forward forward2, Forward forward3) {
			if (3 != Stream.of(forward1, forward2, forward3).distinct().count())
				throw new IllegalArgumentException("LineUpBuilder: unable to instantiate on duplicate Substitute Forward");
			addSubstituteFieldings(Stream.of(forward1, forward2, forward3)
					.map(def -> Objects.requireNonNull(def,
							"LineUpBuilder: cannot instantiate on null Substitute Forward"))
					.collect(Collectors.toList()));	
			return lineUp;
		}
	}
}
