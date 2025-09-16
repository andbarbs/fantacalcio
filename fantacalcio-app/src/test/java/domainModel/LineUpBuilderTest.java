package domainModel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import domainModel.Player.Defender;
import domainModel.Player.Midfielder;
import domainModel.scheme.Scheme433;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;

class LineUpBuilderTest {

	@Test
	void testBuilder() {
		MatchDaySerieA matchDay = new MatchDaySerieA();
		FantaTeam team = new FantaTeam();
		FantaTeam opponent = new FantaTeam();
		LineUp lineUp = LineUp.build()
			.forTeam(team)
			.inMatch(new Match(matchDay, team, opponent))
			.withStarterLineUp(Scheme433.starterLineUp()
					.withGoalkeeper(new Goalkeeper("portiere", "titolare"))
					.withDefenders(
							new Defender("difensore1", "titolare"), 
							new Defender("difensore2", "titolare"), 
							new Defender("difensore3", "titolare"), 
							new Defender("difensore4", "titolare"))
					.withMidfielders(
							new Midfielder("centrocampista1", "titolare"), 
							new Midfielder("centrocampista2", "titolare"), 
							new Midfielder("centrocampista3", "titolare"))
					.withForwards(
							new Forward("attaccante1", "titolare"), 
							new Forward("attaccante2", "titolare"), 
							new Forward("attaccante3", "titolare")))
			.withSubstituteGoalkeepers(
					new Goalkeeper("portiere1", "panchina"),
					new Goalkeeper("portiere2", "panchina"), 
					new Goalkeeper("portiere3", "panchina"))
			.withSubstituteDefenders(
					new Defender("difensore1", "panchina"), 
					new Defender("difensore2", "panchina"),
					new Defender("difensore3", "panchina"))
			.withSubstituteMidfielders(
					new Midfielder("centrocampista1", "panchina"), 
					new Midfielder("centrocampista2", "panchina"),
					new Midfielder("centrocampista3", "panchina"))
			.withSubstituteForwards(
					new Forward("attaccante1", "panchina"), 
					new Forward("attaccante2", "panchina"),
					new Forward("attaccante3", "panchina"));
		
		List<Player> fieldedPlayers = lineUp.getFieldings().stream()
				.map(fielding -> fielding.getPlayer())
				.collect(Collectors.toList());
		
		assertThat(fieldedPlayers).containsExactlyInAnyOrder(
				new Goalkeeper("portiere", "titolare"),

				new Defender("difensore1", "titolare"), 
				new Defender("difensore2", "titolare"),
				new Defender("difensore3", "titolare"), 
				new Defender("difensore4", "titolare"),

				new Midfielder("centrocampista1", "titolare"), 
				new Midfielder("centrocampista2", "titolare"),
				new Midfielder("centrocampista3", "titolare"),

				new Forward("attaccante1", "titolare"), 
				new Forward("attaccante2", "titolare"),
				new Forward("attaccante3", "titolare"),

				new Goalkeeper("portiere1", "panchina"), 
				new Goalkeeper("portiere2", "panchina"),
				new Goalkeeper("portiere3", "panchina"),

				new Defender("difensore1", "panchina"), 
				new Defender("difensore2", "panchina"),
				new Defender("difensore3", "panchina"),

				new Midfielder("centrocampista1", "panchina"), 
				new Midfielder("centrocampista2", "panchina"),
				new Midfielder("centrocampista3", "panchina"),

				new Forward("attaccante1", "panchina"), 
				new Forward("attaccante2", "panchina"),
				new Forward("attaccante3", "panchina"));	
	}

}
