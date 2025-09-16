package domainModel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import domainModel.Player.*;
import domainModel.scheme.Scheme433;
import domainModel.Fielding.*;

/**
 * Test class for LineUpViewer.
 */
public class LineUpViewerTest {

    @Test
    public void testLineUpViewer() {
        // Create starter players
        Goalkeeper starterGK = new Goalkeeper("John", "Doe");
        Defender starterDef = new Defender("Alan", "Smith");
        Midfielder starterMid = new Midfielder("Bruce", "Wayne");
        Forward starterFwd = new Forward("Clark", "Kent");

        // Create substitute players
        Goalkeeper position2Glk = new Goalkeeper("SubGk1", "McFly");
        Goalkeeper position1Glk = new Goalkeeper("SubGk2", "McFly");
        Defender position2Def = new Defender("SubDef1", "McFly");
        Defender position1Def = new Defender("SubDef2", "McFly");
        Midfielder subMid1 = new Midfielder("SubMid1", "McFly");
        Forward position3For = new Forward("SubFwd1", "McFly");
        Forward position1For = new Forward("SubFwd2", "McFly");
        Forward position2For = new Forward("SubFwd3", "McFly");

        // Create an empty set for fieldings
        Set<Fielding> fieldings = new HashSet<>();

        // Create the concrete lineup using the empty fieldings set.
        // The _433LineUp constructor will link the set to each Fielding.
        Match match = new Match();
		FantaTeam team = new FantaTeam();
		LineUp lineUp = new LineUp(Scheme433.INSTANCE, match, team, fieldings);

        // Add starter fieldings for each player.
        fieldings.add(new StarterFielding(starterGK, lineUp));
        fieldings.add(new StarterFielding(starterDef, lineUp));
        fieldings.add(new StarterFielding(starterMid, lineUp));
        fieldings.add(new StarterFielding(starterFwd, lineUp));

        // Add substitute fieldings.
        // For goalkeepers: add two substitutes with bench positions 2 and 1.
        // The ordering should be by bench position, so bench position 1 comes first.
        fieldings.add(new SubstituteFielding(position2Glk, lineUp, 2));
        fieldings.add(new SubstituteFielding(position1Glk, lineUp, 1));

        // For defenders: add two substitutes with bench positions 2 and 1.
        fieldings.add(new SubstituteFielding(position2Def, lineUp, 2));
        fieldings.add(new SubstituteFielding(position1Def, lineUp, 1));

        // For midfielders: add one substitute.
        fieldings.add(new SubstituteFielding(subMid1, lineUp, 1));

        // For forwards: add three substitutes with bench positions unsorted (3, 1, 2);
        // the expected sorted order is: position 1, then 2, then 3.
        fieldings.add(new SubstituteFielding(position3For, lineUp, 3));
        fieldings.add(new SubstituteFielding(position1For, lineUp, 1));
        fieldings.add(new SubstituteFielding(position2For, lineUp, 2));

        // Create the SUT: instantiate the LineUpViewer with the lineup.
        LineUpViewer viewer = new LineUpViewer(lineUp);

        // --- Assertions using AssertJ ---

        // Starters should be present exactly once.
        assertThat(viewer.starterGoalkeepers())
            .containsExactly(starterGK);
        assertThat(viewer.starterDefenders())
            .containsExactly(starterDef);
        assertThat(viewer.starterMidfielders())
            .containsExactly(starterMid);
        assertThat(viewer.starterForwards())
            .containsExactly(starterFwd);

        // Substitutes should appear sorted by benchPosition.
        // For goalkeepers: bench position order: 1 then 2.
        assertThat(viewer.substituteGoalkeepers())
            .containsExactly(position1Glk, position2Glk);
        // For defenders: bench position order: 1 then 2.
        assertThat(viewer.substituteDefenders())
            .containsExactly(position1Def, position2Def);
        // For midfielders: only one substitute.
        assertThat(viewer.substituteMidfielders())
            .containsExactly(subMid1);
        // For forwards: expected order is bench positions 1, 2, then 3.
        assertThat(viewer.substituteForwards())
            .containsExactly(position1For, position2For, position3For);
    }
}
