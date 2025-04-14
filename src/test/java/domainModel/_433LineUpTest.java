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
		builder.withGoalkeeper(new Goalkeeper("gigi", "buffon"));
		builder.withDefenders(
				new Defender("ciccio1", "bomba"), 
				new Defender("ciccio2", "bomba"), 
				new Defender("ciccio3", "bomba"), 
				new Defender("ciccio4", "bomba"));
		builder.withMidfielders(
				new Midfielder("tizio1", "bomba"), 
				new Midfielder("tizio2", "bomba"), 
				new Midfielder("tizio3", "bomba"));
		builder.withForwards(
				new Forward("caio1", "bomba"), 
				new Forward("caio2", "bomba"), 
				new Forward("caio3", "bomba"));
		builder.withSubstituteGoalkeepers(List.of(
				new Goalkeeper("gigi", "panchina1"),
				new Goalkeeper("gigi", "panchina2")));
		builder.withSubstituteDefenders(List.of(
				new Defender("def", "panchina1"), 
				new Defender("def", "panchina2")));
		builder.withSubstituteMidfielders(List.of(
				new Midfielder("mid", "panchina1"), 
				new Midfielder("mid", "panchina2"),
				new Midfielder("mid", "panchina3")));
		builder.withSubstituteForwards(List.of(
				new Forward("for", "panchina1"), 
				new Forward("for", "panchina2"),
				new Forward("for", "panchina3"),
				new Forward("for", "panchina4")));
		
		_433LineUp lineUp = builder.build();
		
		List<Player> fieldedPlayers = lineUp.getFieldings().stream()
				.map(fielding -> fielding.getPlayer())
				.collect(Collectors.toList());
		
		assertThat(fieldedPlayers).containsExactlyInAnyOrder(
				new Goalkeeper("gigi", "buffon"),
				new Goalkeeper("gigi", "panchina1"),
				new Goalkeeper("gigi", "panchina2"),
				new Midfielder("tizio1", "bomba"),
				new Midfielder("tizio2", "bomba"),
				new Midfielder("tizio3", "bomba"),
				new Defender("ciccio1", "bomba"),
				new Defender("ciccio2", "bomba"),
				new Defender("ciccio3", "bomba"),
				new Defender("ciccio4", "bomba"),
				new Forward("caio1", "bomba"),
				new Forward("caio2", "bomba"),
				new Forward("caio3", "bomba"),
				new Defender("def", "panchina1"), 
				new Defender("def", "panchina2"),
				new Midfielder("mid", "panchina1"), 
				new Midfielder("mid", "panchina2"),
				new Midfielder("mid", "panchina3"),
				new Forward("for", "panchina1"), 
				new Forward("for", "panchina2"),
				new Forward("for", "panchina3"),
				new Forward("for", "panchina4"));		
	}

}
