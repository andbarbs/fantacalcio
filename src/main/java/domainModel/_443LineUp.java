package domainModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import domainModel.Player.Defender;
import domainModel.Player.Midfielder;
import domainModel.Player.Striker;

public class _443LineUp extends LineUp.ThreePositionLineUp {

    private _443LineUp(Match match, Set<Fielding> fieldings) {
        super(match, fieldings);
    }

    // Static inner Builder class.
    public static class _443LineUpBuilder {
        private final Match match;
        private final _443LineUp lineUp;
        private final Set<Fielding> fieldings = new HashSet<>();
        
        // Collections to accumulate fielding instances for each role.
        private final Set<Fielding> defenders = new HashSet<>();
        private final Set<Fielding> midfielders = new HashSet<>();
        private final Set<Fielding> forwards = new HashSet<>();
        
        // Builder constructor with any required fields.
        public _443LineUpBuilder(Match match) {
            this.match = match;
            lineUp = new _443LineUp(match, fieldings);
        }
        
        // Fluent method to add defenders.
        public _443LineUpBuilder withDefenders(Defender defender1, Defender defender2, Defender defender3, Defender defender4) {
            this.defenders.addAll(List.of(defender1, defender2, defender3, defender4)
                .stream()
                .map(def -> new LineUp.ThreePositionLineUp.DefenderFielding(def, lineUp))
                .collect(Collectors.toList()));
            return this;
        }
        
        // Fluent method to add midfielders.
        public _443LineUpBuilder withMidfielders(Midfielder midfielder1, Midfielder midfielder2, Midfielder midfielder3) {
        	this.midfielders.addAll(List.of(midfielder1, midfielder2, midfielder3)
                    .stream()
                    .map(mid -> new LineUp.ThreePositionLineUp.MidfielderFielding(mid, lineUp))
                    .collect(Collectors.toList()));
                return this;
        }
        
        // Fluent method to add forwards.
        public _443LineUpBuilder withForwards(Striker striker1, Striker striker2, Striker striker3) {
        	this.forwards.addAll(List.of(striker1, striker2, striker3)
                    .stream()
                    .map(fwd -> new LineUp.ThreePositionLineUp.ForwardFielding(fwd, lineUp))
                    .collect(Collectors.toList()));
                return this;
        }
        
        // Optional: If you want to add individual players rather than a collection.
        public _443LineUpBuilder addDefender(Fielding defender) {
            this.defenders.add(defender);
            return this;
        }
        
        public _443LineUpBuilder addMidfielder(Fielding midfielder) {
            this.midfielders.add(midfielder);
            return this;
        }
        
        public _443LineUpBuilder addForward(Fielding forward) {
            this.forwards.add(forward);
            return this;
        }
        
        // The build method aggregates all the elements and creates a _443LineUp instance.
        public _443LineUp build() {
            // Combine the fieldings as needed. This could also include validations.
            fieldings.addAll(defenders);
            fieldings.addAll(midfielders);
            fieldings.addAll(forwards);
            
            lineUp.
            
            return new _443LineUp(match, fieldings);
        }
    }
}

