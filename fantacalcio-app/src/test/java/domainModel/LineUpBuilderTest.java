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
					.withGoalkeeper(new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA))
					.withDefenders(
							new Defender("difensore1", "titolare", Player.Club.ATALANTA),
							new Defender("difensore2", "titolare", Player.Club.ATALANTA),
							new Defender("difensore3", "titolare", Player.Club.ATALANTA),
							new Defender("difensore4", "titolare", Player.Club.ATALANTA))
					.withMidfielders(
							new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
							new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA),
							new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA))
					.withForwards(
							new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
							new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
							new Forward("attaccante3", "titolare", Player.Club.ATALANTA)))
			.withSubstituteGoalkeepers(
					new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
					new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
					new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
			.withSubstituteDefenders(
					new Defender("difensore1", "panchina", Player.Club.ATALANTA),
					new Defender("difensore2", "panchina", Player.Club.ATALANTA),
					new Defender("difensore3", "panchina", Player.Club.ATALANTA))
			.withSubstituteMidfielders(
					new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA),
					new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA),
					new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA))
			.withSubstituteForwards(
					new Forward("attaccante1", "panchina", Player.Club.ATALANTA),
					new Forward("attaccante2", "panchina", Player.Club.ATALANTA),
					new Forward("attaccante3", "panchina", Player.Club.ATALANTA));
		
		List<Player> fieldedPlayers = lineUp.getFieldings().stream()
				.map(fielding -> fielding.getPlayer())
				.collect(Collectors.toList());
		
		assertThat(fieldedPlayers).containsExactlyInAnyOrder(
				new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),

				new Defender("difensore1", "titolare", Player.Club.ATALANTA),
				new Defender("difensore2", "titolare", Player.Club.ATALANTA),
				new Defender("difensore3", "titolare", Player.Club.ATALANTA),
				new Defender("difensore4", "titolare", Player.Club.ATALANTA),

				new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
				new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA),
				new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA),

				new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
				new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
				new Forward("attaccante3", "titolare", Player.Club.ATALANTA),

				new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
				new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
				new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA),

				new Defender("difensore1", "panchina", Player.Club.ATALANTA),
				new Defender("difensore2", "panchina", Player.Club.ATALANTA),
				new Defender("difensore3", "panchina", Player.Club.ATALANTA),

				new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA),
				new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA),
				new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA),

				new Forward("attaccante1", "panchina", Player.Club.ATALANTA),
				new Forward("attaccante2", "panchina", Player.Club.ATALANTA),
				new Forward("attaccante3", "panchina", Player.Club.ATALANTA));
	}

}
