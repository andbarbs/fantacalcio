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

class _433LineUpTest {

	@Test
	void testBuilder() {
		MatchDaySerieA matchDay = new MatchDaySerieA();
		FantaTeam team = new FantaTeam();
		FantaTeam opponent = new FantaTeam();
		_443LineUpBuilder builder = new _433LineUp._443LineUpBuilder(
				new Match(matchDay, team, opponent), team);
		builder.withGoalkeeper(new Goalkeeper("gigi", "buffon", "Juventus"));
		builder.withDefenders(
				new Defender("ciccio1", "bomba", "Juventus"), 
				new Defender("ciccio2", "bomba", "Juventus"), 
				new Defender("ciccio3", "bomba", "Juventus"), 
				new Defender("ciccio4", "bomba", "Juventus"));
		builder.withMidfielders(
				new Midfielder("tizio1", "bomba", "Juventus"), 
				new Midfielder("tizio2", "bomba", "Juventus"), 
				new Midfielder("tizio3", "bomba", "Juventus"));
		builder.withForwards(
				new Forward("caio1", "bomba", "Juventus"), 
				new Forward("caio2", "bomba", "Juventus"), 
				new Forward("caio3", "bomba", "Juventus"));
		builder.withSubstituteGoalkeepers(List.of(
				new Goalkeeper("gigi", "panchina1", "Juventus"),
				new Goalkeeper("gigi", "panchina2", "Juventus")));
		builder.withSubstituteDefenders(List.of(
				new Defender("def", "panchina1", "Juventus"), 
				new Defender("def", "panchina2", "Juventus")));
		builder.withSubstituteMidfielders(List.of(
				new Midfielder("mid", "panchina1", "Juventus"), 
				new Midfielder("mid", "panchina2", "Juventus"),
				new Midfielder("mid", "panchina3", "Juventus")));
		builder.withSubstituteForwards(List.of(
				new Forward("for", "panchina1", "Juventus"), 
				new Forward("for", "panchina2", "Juventus"),
				new Forward("for", "panchina3", "Juventus"),
				new Forward("for", "panchina4", "Juventus")));
		
		_433LineUp lineUp = builder.build();
		
		List<Player> fieldedPlayers = lineUp.getFieldings().stream()
				.map(fielding -> fielding.getPlayer())
				.collect(Collectors.toList());
		
		assertThat(fieldedPlayers).containsExactlyInAnyOrder(
				new Goalkeeper("gigi", "buffon", "Juventus"),
				new Goalkeeper("gigi", "panchina1", "Juventus"),
				new Goalkeeper("gigi", "panchina2", "Juventus"),
				new Midfielder("tizio1", "bomba", "Juventus"),
				new Midfielder("tizio2", "bomba", "Juventus"),
				new Midfielder("tizio3", "bomba", "Juventus"),
				new Defender("ciccio1", "bomba", "Juventus"),
				new Defender("ciccio2", "bomba", "Juventus"),
				new Defender("ciccio3", "bomba", "Juventus"),
				new Defender("ciccio4", "bomba", "Juventus"),
				new Forward("caio1", "bomba", "Juventus"),
				new Forward("caio2", "bomba", "Juventus"),
				new Forward("caio3", "bomba", "Juventus"),
				new Defender("def", "panchina1", "Juventus"), 
				new Defender("def", "panchina2", "Juventus"),
				new Midfielder("mid", "panchina1", "Juventus"), 
				new Midfielder("mid", "panchina2", "Juventus"),
				new Midfielder("mid", "panchina3", "Juventus"),
				new Forward("for", "panchina1", "Juventus"), 
				new Forward("for", "panchina2", "Juventus"),
				new Forward("for", "panchina3", "Juventus"),
				new Forward("for", "panchina4", "Juventus"));		
	}

}
