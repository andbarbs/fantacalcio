package domainModel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import domainModel.Player.Defender;
import domainModel.Player.Midfielder;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel._433LineUp._443LineUpBuilder;
import domainModel.Player.Club;

class _433LineUpTest {

	@Test
	void testBuilder() {
		MatchDaySerieA matchDay = new MatchDaySerieA();
		FantaTeam team = new FantaTeam();
		FantaTeam opponent = new FantaTeam();
		_443LineUpBuilder builder = new _433LineUp._443LineUpBuilder(
				new Match(matchDay, team, opponent), team);
		builder.withGoalkeeper(new Goalkeeper("gigi", "buffon", Club.JUVENTUS));
		builder.withDefenders(
				new Defender("ciccio1", "bomba", Club.JUVENTUS), 
				new Defender("ciccio2", "bomba", Club.JUVENTUS), 
				new Defender("ciccio3", "bomba", Club.JUVENTUS), 
				new Defender("ciccio4", "bomba", Club.JUVENTUS));
		builder.withMidfielders(
				new Midfielder("tizio1", "bomba", Club.JUVENTUS), 
				new Midfielder("tizio2", "bomba", Club.JUVENTUS), 
				new Midfielder("tizio3", "bomba", Club.JUVENTUS));
		builder.withForwards(
				new Forward("caio1", "bomba", Club.JUVENTUS), 
				new Forward("caio2", "bomba", Club.JUVENTUS), 
				new Forward("caio3", "bomba", Club.JUVENTUS));
		builder.withSubstituteGoalkeepers(List.of(
				new Goalkeeper("gigi", "panchina1", Club.JUVENTUS),
				new Goalkeeper("gigi", "panchina2", Club.JUVENTUS)));
		builder.withSubstituteDefenders(List.of(
				new Defender("def", "panchina1", Club.JUVENTUS), 
				new Defender("def", "panchina2", Club.JUVENTUS)));
		builder.withSubstituteMidfielders(List.of(
				new Midfielder("mid", "panchina1", Club.JUVENTUS), 
				new Midfielder("mid", "panchina2", Club.JUVENTUS),
				new Midfielder("mid", "panchina3", Club.JUVENTUS)));
		builder.withSubstituteForwards(List.of(
				new Forward("for", "panchina1", Club.JUVENTUS), 
				new Forward("for", "panchina2", Club.JUVENTUS),
				new Forward("for", "panchina3", Club.JUVENTUS),
				new Forward("for", "panchina4", Club.JUVENTUS)));
		
		_433LineUp lineUp = builder.build();
		
		List<Player> fieldedPlayers = lineUp.getFieldings().stream()
				.map(fielding -> fielding.getPlayer())
				.collect(Collectors.toList());
		
		assertThat(fieldedPlayers).containsExactlyInAnyOrder(
				new Goalkeeper("gigi", "buffon", Club.JUVENTUS),
				new Goalkeeper("gigi", "panchina1", Club.JUVENTUS),
				new Goalkeeper("gigi", "panchina2", Club.JUVENTUS),
				new Midfielder("tizio1", "bomba", Club.JUVENTUS),
				new Midfielder("tizio2", "bomba", Club.JUVENTUS),
				new Midfielder("tizio3", "bomba", Club.JUVENTUS),
				new Defender("ciccio1", "bomba", Club.JUVENTUS),
				new Defender("ciccio2", "bomba", Club.JUVENTUS),
				new Defender("ciccio3", "bomba", Club.JUVENTUS),
				new Defender("ciccio4", "bomba", Club.JUVENTUS),
				new Forward("caio1", "bomba", Club.JUVENTUS),
				new Forward("caio2", "bomba", Club.JUVENTUS),
				new Forward("caio3", "bomba", Club.JUVENTUS),
				new Defender("def", "panchina1", Club.JUVENTUS), 
				new Defender("def", "panchina2", Club.JUVENTUS),
				new Midfielder("mid", "panchina1", Club.JUVENTUS), 
				new Midfielder("mid", "panchina2", Club.JUVENTUS),
				new Midfielder("mid", "panchina3", Club.JUVENTUS),
				new Forward("for", "panchina1", Club.JUVENTUS), 
				new Forward("for", "panchina2", Club.JUVENTUS),
				new Forward("for", "panchina3", Club.JUVENTUS),
				new Forward("for", "panchina4", Club.JUVENTUS));		
	}

}
