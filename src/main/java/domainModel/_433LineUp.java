package domainModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import domainModel.Player.Defender;
import domainModel.Player.Midfielder;
import jakarta.persistence.Entity;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;

@Entity
public class _433LineUp extends LineUp {
	
	protected _433LineUp() {}

	private _433LineUp(Match match, FantaTeam team, Set<Fielding> fieldings) {
		super(match, team, fieldings);
	}

	// Static inner Builder class
	public static class _443LineUpBuilder {
		private final _433LineUp lineUp;
		private final Set<Fielding> fieldings = new HashSet<>();

		// Builder constructor with any required fields.
		public _443LineUpBuilder(Match match, FantaTeam team) {
			lineUp = new _433LineUp(match, team, fieldings);
		}
		
		// Fluent method to add starter goalkeeper
		public _443LineUpBuilder withGoalkeeper(Goalkeeper goalkeeper) {
			this.fieldings.add(new LineUp.GoalkeeperStarterFielding(goalkeeper, lineUp));
			return this;
		}

		// Fluent method to add defenders
		public _443LineUpBuilder withDefenders(Defender defender1, Defender defender2, Defender defender3,
				Defender defender4) {
			this.fieldings.addAll(List.of(defender1, defender2, defender3, defender4).stream()
					.map(def -> new LineUp.DefenderStarterFielding(def, lineUp))
					.collect(Collectors.toList()));
			return this;
		}

		// Fluent method to add midfielders
		public _443LineUpBuilder withMidfielders(Midfielder midfielder1, Midfielder midfielder2,
				Midfielder midfielder3) {
			this.fieldings.addAll(List.of(midfielder1, midfielder2, midfielder3).stream()
					.map(mid -> new LineUp.MidfielderStarterFielding(mid, lineUp))
					.collect(Collectors.toList()));
			return this;
		}

		// Fluent method to add forwards
		public _443LineUpBuilder withForwards(Forward striker1, Forward striker2, Forward striker3) {
			this.fieldings.addAll(List.of(striker1, striker2, striker3).stream()
					.map(fwd -> new LineUp.ForwardStarterFielding(fwd, lineUp)).collect(Collectors.toList()));
			return this;
		}

		// Adding substitute goalkeepers
		public _443LineUpBuilder withSubstituteGoalkeepers(List<Goalkeeper> substituteGoalkeepers) {
			for (int i = 0; i < substituteGoalkeepers.size(); i++) {
				this.fieldings
						.add(new LineUp.GoalkeeperSubstituteFielding(substituteGoalkeepers.get(i), lineUp, i + 1));
			}
			return this;
		}

		// Adding substitute defenders
		public _443LineUpBuilder withSubstituteDefenders(List<Defender> substituteDefenders) {
			for (int i = 0; i < substituteDefenders.size(); i++) {
				this.fieldings.add(new LineUp.DefenderSubstituteFielding(substituteDefenders.get(i), lineUp, i + 1));
			}
			return this;
		}
		
		// Adding substitute midfielders
		public _443LineUpBuilder withSubstituteMidfielders(List<Midfielder> substituteMidfielders) {
			for (int i = 0; i < substituteMidfielders.size(); i++) {
				this.fieldings.add(new LineUp.MidfielderSubstituteFielding(substituteMidfielders.get(i), lineUp, i + 1));
			}
			return this;
		}
		
		// Adding substitute forwards
		public _443LineUpBuilder withSubstituteForwards(List<Forward> substituteForwards) {
			for (int i = 0; i < substituteForwards.size(); i++) {
		        this.fieldings.add(new LineUp.ForwardSubstituteFielding(substituteForwards.get(i), lineUp, i + 1));
		    }
		    return this;
		}

		// The build method
		public _433LineUp build() {
			return lineUp;
		}
	}
}
