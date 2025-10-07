package domainModel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

import domainModel.Player.*;
import domainModel.scheme.Scheme433;

/**
 * Test class for LineUpViewer.
 */
public class LineUpViewerTest {

    @Test
    public void testLineUpViewer() {
    	
    	// GIVEN a LineUp's ancillary entities are instantiated
		Goalkeeper gk1 = new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA);
		
		Defender d1 = new Defender("difensore1", "titolare", Player.Club.ATALANTA);
		Defender d2 = new Defender("difensore2", "titolare", Player.Club.ATALANTA);
		Defender d3 = new Defender("difensore3", "titolare", Player.Club.ATALANTA);
		Defender d4 = new Defender("difensore4", "titolare", Player.Club.ATALANTA);
		
		Midfielder m1 = new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA);
		Midfielder m2 = new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA);
		Midfielder m3 = new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA);
		
		Forward f1 = new Forward("attaccante1", "titolare", Player.Club.ATALANTA);
		Forward f2 = new Forward("attaccante2", "titolare", Player.Club.ATALANTA);
		Forward f3 = new Forward("attaccante3", "titolare", Player.Club.ATALANTA);
		
		Goalkeeper sgk1 = new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA);
		Goalkeeper sgk2 = new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA);
		Goalkeeper sgk3 = new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA);
		
		Defender sd1 = new Defender("difensore1", "panchina", Player.Club.ATALANTA);
		Defender sd2 = new Defender("difensore2", "panchina", Player.Club.ATALANTA);
		Defender sd3 = new Defender("difensore3", "panchina", Player.Club.ATALANTA);
		
		Midfielder sm1 = new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA);
		Midfielder sm2 = new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA);
		Midfielder sm3 = new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA);
		
		Forward sf1 = new Forward("attaccante1", "panchina", Player.Club.ATALANTA);
		Forward sf2 = new Forward("attaccante2", "panchina", Player.Club.ATALANTA);
		Forward sf3 = new Forward("attaccante3", "panchina", Player.Club.ATALANTA);
		
		FantaTeam team = new FantaTeam("Dream Team", null, 30, null, new HashSet<Contract>());		
		Match match = new Match(null, team, team);
		
		// AND a LineUp instance is assembled
		LineUp lineUp = LineUp.build()
				.forTeam(team)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(gk1)
						.withDefenders(d1, d2, d3, d4)
						.withMidfielders(m1, m2, m3)
						.withForwards(f1, f2, f3))
				.withSubstituteGoalkeepers(sgk1, sgk2, sgk3)
				.withSubstituteDefenders(sd1, sd2, sd3)
				.withSubstituteMidfielders(sm1, sm2, sm3)
				.withSubstituteForwards(sf1, sf2, sf3);

        // WHEN a Viewes is obtained for that LineUp
        LineUpViewer viewer = new LineUpViewer(lineUp);

        // THEN it correctly extracts fielded players by roles and fielding
		assertThat(viewer.starterGoalkeepers()).containsExactlyInAnyOrder(gk1);
		assertThat(viewer.starterDefenders()).containsExactlyInAnyOrder(d1, d2, d3, d4);
		assertThat(viewer.starterMidfielders()).containsExactlyInAnyOrder(m1, m2, m3);
		assertThat(viewer.starterForwards()).containsExactlyInAnyOrder(f1, f2, f3);
		assertThat(viewer.substituteGoalkeepers()).containsExactly(sgk1, sgk2, sgk3);
		assertThat(viewer.substituteDefenders()).containsExactly(sd1, sd2, sd3);
		assertThat(viewer.substituteMidfielders()).containsExactly(sm1, sm2, sm3);
		assertThat(viewer.substituteForwards()).containsExactly(sf1, sf2, sf3);
    }
}
