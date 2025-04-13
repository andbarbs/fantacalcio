package domainModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import domainModel.Player.Defender;
import domainModel.Player.Midfielder;
import domainModel.Player.Striker;

public class _433LineUp extends LineUp.ThreePositionLineUp {

	private _433LineUp(Match match, Set<Fielding> fieldings) {
		super(match, fieldings);
	}

	// Static inner Builder class.
	public static class _443LineUpBuilder {
		private final _433LineUp lineUp;
		private final Set<Fielding> fieldings = new HashSet<>();

		// Builder constructor with any required fields.
		public _443LineUpBuilder(Match match) {
			lineUp = new _433LineUp(match, fieldings);
		}

		// Fluent method to add defenders.
		public _443LineUpBuilder withDefenders(Defender defender1, Defender defender2, Defender defender3,
				Defender defender4) {
			this.fieldings.addAll(List.of(defender1, defender2, defender3, defender4).stream()
					.map(def -> new LineUp.ThreePositionLineUp.DefenderFielding(def, lineUp))
					.collect(Collectors.toList()));
			return this;
		}

		// Fluent method to add midfielders.
		public _443LineUpBuilder withMidfielders(Midfielder midfielder1, Midfielder midfielder2,
				Midfielder midfielder3) {
			this.fieldings.addAll(List.of(midfielder1, midfielder2, midfielder3).stream()
					.map(mid -> new LineUp.ThreePositionLineUp.MidfielderFielding(mid, lineUp))
					.collect(Collectors.toList()));
			return this;
		}

		// Fluent method to add forwards.
		public _443LineUpBuilder withForwards(Striker striker1, Striker striker2, Striker striker3) {
			this.fieldings.addAll(List.of(striker1, striker2, striker3).stream()
					.map(fwd -> new LineUp.ThreePositionLineUp.ForwardFielding(fwd, lineUp))
					.collect(Collectors.toList()));
			return this;
		}

		// Adding substitute players
		public _443LineUpBuilder withSubstitutes(List<Player> substitutes) {
			this.fieldings.addAll(substitutes.stream()
					.map(subs -> new Fielding.SubstituteFielding(subs, lineUp))
					.collect(Collectors.toList()));
			return this;
		}

		// The build method
		public _433LineUp build() {
			return lineUp;
		}
	}
}
