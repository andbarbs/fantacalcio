package domainModel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import domainModel.Player.Defender;
import domainModel.Player.Midfielder;
import domainModel.Player.Striker;
import domainModel.Player.Goalkeeper;
import domainModel._433LineUp._443LineUpBuilder;

class _433LineUpTest {

	@Test
	void testBuilder() {
		MatchDaySerieA matchDay = new MatchDaySerieA();
		_443LineUpBuilder builder = new _433LineUp._443LineUpBuilder(new Match(matchDay, null, null));
		builder.withDefenders(new Defender("ciccio1", "bomba"), 
				new Defender("ciccio2", "bomba"), 
				new Defender("ciccio3", "bomba"), 
				new Defender("ciccio4", "bomba"));
		builder.withMidfielders(new Midfielder("tizio1", "bomba"), 
				new Midfielder("tizio2", "bomba"), 
				new Midfielder("tizio3", "bomba"));
		builder.withForwards(new Striker("caio1", "bomba"), 
				new Striker("caio2", "bomba"), 
				new Striker("caio3", "bomba"));
		builder.withSubstitutes(List.of(new Goalkeeper("gigi", "buffon")));
		
		_433LineUp lineUp = builder.build();
		
		List<Player> fieldedPlayers = lineUp.getFieldings().stream()
				.map(fielding -> fielding.getPlayer())
				.collect(Collectors.toList());
		
		assertThat(fieldedPlayers).containsExactlyInAnyOrder(
				new Midfielder("tizio1", "bomba"),
				new Midfielder("tizio2", "bomba"),
				new Midfielder("tizio3", "bomba"),
				new Defender("ciccio1", "bomba"),
				new Defender("ciccio2", "bomba"),
				new Defender("ciccio3", "bomba"),
				new Defender("ciccio4", "bomba"),
				new Striker("caio1", "bomba"),
				new Striker("caio2", "bomba"),
				new Striker("caio3", "bomba"),
				new Goalkeeper("gigi", "buffon"));		
	}

}
