package domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import domain.LineUp.LineUpBuilder;
import domain.LineUp.LineUpBuilderSteps.StarterLineUp;
import domain.Player.Defender;
import domain.Player.Forward;
import domain.Player.Goalkeeper;
import domain.Player.Midfielder;
import domain.scheme.Scheme433;

@DisplayName("a LineUpBuilder")
class LineUpBuilderTest {
	
	private LineUp lineUp;

	@Test
	@DisplayName("instantiates a LineUp if valid arguments are provided")
	void testBuilder() {
		MatchDay matchDay = new MatchDay();
		FantaTeam team = new FantaTeam();
		FantaTeam opponent = new FantaTeam();
		lineUp = LineUp.build()
			.forTeam(team)
			.inMatch(new Match(matchDay, team, opponent))
			.withStarterLineUp(new StarterLineUp(
					Scheme433.INSTANCE,
					new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
					Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
							new Defender("difensore2", "titolare", Player.Club.ATALANTA),
							new Defender("difensore3", "titolare", Player.Club.ATALANTA),
							new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
					Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
							new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA),
							new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
					Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
							new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
							new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
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
	
	@Nested
	@DisplayName("throws and does not instantiate a LineUp")
	class ListenersOrderingWidgetTo {
		
		/**
		 * Substitutes are free from the "insufficient" problem, as
		 * {@link LineUpBuilder} steps mandate exact-numbered arguments as opposed to
		 * {@link StarterLineUp}'s {@link Collection}s for Starters
		 */
		@Nested
		@DisplayName("when Starters are less than required by the Scheme, specifically")
		class InsufficientStarters {
			
			@Test
			@DisplayName("defenders")
			void notEnoughDefenders() {
				
				// GIVEN
				MatchDay matchDay = new MatchDay();
				FantaTeam team = new FantaTeam();
				FantaTeam opponent = new FantaTeam();

				// WHEN the SUT is used to instantiate a LineUp on a null arg
				ThrowingCallable shouldThrow = () -> {
					lineUp = LineUp.build()
							.forTeam(team)
							.inMatch(new Match(matchDay, team, opponent))
							.withStarterLineUp(new StarterLineUp(
									Scheme433.INSTANCE,
									new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
									Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
											new Defender("difensore2", "titolare", Player.Club.ATALANTA),
											new Defender("difensore3", "titolare", Player.Club.ATALANTA)),
									Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
											new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
											new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
									Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
											new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
											new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
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
				};			
				
				// THEN an error is thrown
				assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("not enough Starter Defenders");
				
				// AND it does not instantiate
				assertThat(lineUp).isNull();
			}
			
			@Test
			@DisplayName("midfielders")
			void notEnoughMidfielders() {
				
				// GIVEN
				MatchDay matchDay = new MatchDay();
				FantaTeam team = new FantaTeam();
				FantaTeam opponent = new FantaTeam();

				// WHEN the SUT is used to instantiate a LineUp on a null arg
				ThrowingCallable shouldThrow = () -> {
					lineUp = LineUp.build()
							.forTeam(team)
							.inMatch(new Match(matchDay, team, opponent))
							.withStarterLineUp(new StarterLineUp(
									Scheme433.INSTANCE,
									new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
									Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
											new Defender("difensore2", "titolare", Player.Club.ATALANTA),
											new Defender("difensore3", "titolare", Player.Club.ATALANTA),
											new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
									Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
											new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA)),
									Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
											new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
											new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
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
				};			
				
				// THEN an error is thrown
				assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("not enough Starter Midfielders");
				
				// AND it does not instantiate
				assertThat(lineUp).isNull();
			}
			
			@Test
			@DisplayName("forwards")
			void notEnoughForwards() {
				
				// GIVEN
				MatchDay matchDay = new MatchDay();
				FantaTeam team = new FantaTeam();
				FantaTeam opponent = new FantaTeam();

				// WHEN the SUT is used to instantiate a LineUp on a null arg
				ThrowingCallable shouldThrow = () -> {
					lineUp = LineUp.build()
							.forTeam(team)
							.inMatch(new Match(matchDay, team, opponent))
							.withStarterLineUp(new StarterLineUp(
									Scheme433.INSTANCE,
									new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
									Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
											new Defender("difensore2", "titolare", Player.Club.ATALANTA),
											new Defender("difensore3", "titolare", Player.Club.ATALANTA),
											new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
									Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
											new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
											new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
									Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
											new Forward("attaccante2", "titolare", Player.Club.ATALANTA))))
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
				};			
				
				// THEN an error is thrown
				assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("not enough Starter Forwards");
				
				// AND it does not instantiate
				assertThat(lineUp).isNull();
			}
		}
		
		@Nested
		@DisplayName("on null arguments, specifically")
		class NullArgs {
			
			@Test
			@DisplayName("the scheme")
			void nullScheme() {
				
				// GIVEN
				MatchDay matchDay = new MatchDay();
				FantaTeam team = new FantaTeam();
				FantaTeam opponent = new FantaTeam();

				// WHEN the SUT is used to instantiate a LineUp on a null arg
				ThrowingCallable shouldThrow = () -> {
					lineUp = LineUp.build()
							.forTeam(team)
							.inMatch(new Match(matchDay, team, opponent))
							.withStarterLineUp(new StarterLineUp(
									null,
									new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
									Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
											new Defender("difensore2", "titolare", Player.Club.ATALANTA),
											new Defender("difensore3", "titolare", Player.Club.ATALANTA),
											new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
									Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
											new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
											new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
									Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
											new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
											new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
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
				};			
				
				// THEN an error is thrown
				assertThatThrownBy(shouldThrow).isInstanceOf(NullPointerException.class)
						.hasMessageContaining("null Scheme");
				
				// AND it does not instantiate
				assertThat(lineUp).isNull();
			}
			
			/**
			 * <ol>
			 * <li>a {@link HashSet} is used as {@link Set#of()} would throw with a
			 * <code>null</code> element
			 * <li>{@link Arrays#asList()} is used as {@link List#of()} would throw with a
			 * <code>null</code> element
			 * </ol>
			 */
			@Nested
			@DisplayName("Starter")
			class NullStarters {
				
				@Test
				@DisplayName("goalkeeper")
				void nullStarterGoalie() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					// WHEN the SUT is used to instantiate a LineUp on a null arg
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										null,
										Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												new Defender("difensore2", "titolare", Player.Club.ATALANTA),
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
										Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
										Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
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
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(NullPointerException.class)
							.hasMessageContaining("null Starter Goalkeeper");
					
					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
				
				@Test
				@DisplayName("defenders")
				void nullStarterDefender() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					// WHEN the SUT is used to instantiate a LineUp on a null arg
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
										new HashSet<>(Arrays.asList(
												new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												null,
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA))),
										Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
										Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
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
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(NullPointerException.class)
							.hasMessageContaining("null Starter Defender");
					
					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
				
				@Test
				@DisplayName("midfielders")
				void nullStarterMidfielder() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					// WHEN the SUT is used to instantiate a LineUp on a null arg
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
										Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												new Defender("difensore2", "titolare", Player.Club.ATALANTA),
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
										new HashSet<>(Arrays.asList(
												new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												null, 
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA))),
										Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
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
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(NullPointerException.class)
							.hasMessageContaining("null Starter Midfielder");
					
					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
				
				@Test
				@DisplayName("forwards")
				void nullStarterForward() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					// WHEN the SUT is used to instantiate a LineUp on a null arg
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
										Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												new Defender("difensore2", "titolare", Player.Club.ATALANTA),
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
										Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
										new HashSet<>(Arrays.asList(
												new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												null,
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA)))))
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
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(NullPointerException.class)
							.hasMessageContaining("null Starter Forward");
					
					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
			}
			
			@Nested
			@DisplayName("Substitute")
			class NullSubstitutes {
				
				@Test
				@DisplayName("goalkeepers")
				void nullSubstituteGoalkeeper() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					// WHEN the SUT is used to instantiate a LineUp on a null arg
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
										Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												new Defender("difensore2", "titolare", Player.Club.ATALANTA),
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
										Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
										Set.of(
												new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
								.withSubstituteGoalkeepers(
										new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
										null,
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
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(NullPointerException.class)
							.hasMessageContaining("null Substitute Goalkeeper");
					
					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
				
				@Test
				@DisplayName("defenders")
				void nullSubstituteDefender() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					// WHEN the SUT is used to instantiate a LineUp on a null arg
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
										Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												new Defender("difensore2", "titolare", Player.Club.ATALANTA),
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
										Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
										Set.of(
												new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
								.withSubstituteGoalkeepers(
										new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
										new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
										new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
								.withSubstituteDefenders(
										new Defender("difensore1", "panchina", Player.Club.ATALANTA),
										null,
										new Defender("difensore3", "panchina", Player.Club.ATALANTA))
								.withSubstituteMidfielders(
										new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA),
										new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA),
										new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA))
								.withSubstituteForwards(
										new Forward("attaccante1", "panchina", Player.Club.ATALANTA),
										new Forward("attaccante2", "panchina", Player.Club.ATALANTA),
										new Forward("attaccante3", "panchina", Player.Club.ATALANTA));
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(NullPointerException.class)
							.hasMessageContaining("null Substitute Defender");
					
					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
				
				@Test
				@DisplayName("midfielders")
				void nullSubstituteMidfielder() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					// WHEN the SUT is used to instantiate a LineUp on a null arg
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
										Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												new Defender("difensore2", "titolare", Player.Club.ATALANTA),
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
										Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
										Set.of(
												new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
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
										null,
										new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA))
								.withSubstituteForwards(
										new Forward("attaccante1", "panchina", Player.Club.ATALANTA),
										new Forward("attaccante2", "panchina", Player.Club.ATALANTA),
										new Forward("attaccante3", "panchina", Player.Club.ATALANTA));
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(NullPointerException.class)
							.hasMessageContaining("null Substitute Midfielder");
					
					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
				
				@Test
				@DisplayName("forwards")
				void nullSubstituteForward() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					// WHEN the SUT is used to instantiate a LineUp on a null arg
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
										Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												new Defender("difensore2", "titolare", Player.Club.ATALANTA),
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
										Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
										Set.of(
												new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
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
										null,
										new Forward("attaccante3", "panchina", Player.Club.ATALANTA));
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(NullPointerException.class)
							.hasMessageContaining("null Substitute Forward");
					
					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
			}
		}
		
		/**
		 * Starters are free from the "duplicate" problem, as {@link StarterLineUp}'s
		 * {@link Set} arguments avert the issue
		 */
		@Nested
		@DisplayName("when there are duplicate args among")
		class DuplicateArgs {
			
			@Nested
			@DisplayName("Substitute")
			class DuplicateSubstitutes {
				
				@Test
				@DisplayName("goalkeepers")
				void duplicateSubstituteGoalies() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					Goalkeeper duplicate = new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA);

					// WHEN the SUT is used to instantiate a LineUp on duplicate args
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
										Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												new Defender("difensore2", "titolare", Player.Club.ATALANTA),
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
										Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
										Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
								.withSubstituteGoalkeepers(
										duplicate,
										new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
										duplicate)
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
										new Forward("attaccante1", "panchina", Player.Club.ATALANTA));
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining("duplicate Substitute Goalkeeper");

					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
				
				@Test
				@DisplayName("defenders")
				void duplicateSubstituteDefs() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					Defender duplicate = new Defender("difensore2", "panchina", Player.Club.ATALANTA);
					
					// WHEN the SUT is used to instantiate a LineUp on duplicate args
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
										Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												new Defender("difensore2", "titolare", Player.Club.ATALANTA),
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
										Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
										Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
								.withSubstituteGoalkeepers(
										new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
										new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
										new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
								.withSubstituteDefenders(
										new Defender("difensore1", "panchina", Player.Club.ATALANTA),
										duplicate,
										duplicate)
								.withSubstituteMidfielders(
										new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA),
										new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA),
										new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA))
								.withSubstituteForwards(
										new Forward("attaccante1", "panchina", Player.Club.ATALANTA),
										new Forward("attaccante2", "panchina", Player.Club.ATALANTA),
										new Forward("attaccante3", "panchina", Player.Club.ATALANTA));
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining("duplicate Substitute Defender");

					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
				
				@Test
				@DisplayName("midfielders")
				void duplicateSubstituteMids() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					Midfielder duplicate = new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA);
					
					// WHEN the SUT is used to instantiate a LineUp on duplicate args
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
										Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												new Defender("difensore2", "titolare", Player.Club.ATALANTA),
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
										Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
										Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
								.withSubstituteGoalkeepers(
										new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
										new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
										new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
								.withSubstituteDefenders(
										new Defender("difensore1", "panchina", Player.Club.ATALANTA),
										new Defender("difensore2", "panchina", Player.Club.ATALANTA),
										new Defender("difensore3", "panchina", Player.Club.ATALANTA))
								.withSubstituteMidfielders(
										duplicate,
										new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA),
										duplicate)
								.withSubstituteForwards(
										new Forward("attaccante1", "panchina", Player.Club.ATALANTA),
										new Forward("attaccante2", "panchina", Player.Club.ATALANTA),
										new Forward("attaccante3", "panchina", Player.Club.ATALANTA));
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining("duplicate Substitute Midfielder");

					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
				
				@Test
				@DisplayName("forwards")
				void duplicateSubstituteForws() {
					
					// GIVEN
					MatchDay matchDay = new MatchDay();
					FantaTeam team = new FantaTeam();
					FantaTeam opponent = new FantaTeam();

					Forward duplicate = new Forward("attaccante1", "panchina", Player.Club.ATALANTA);
					
					// WHEN the SUT is used to instantiate a LineUp on duplicate args
					ThrowingCallable shouldThrow = () -> {
						lineUp = LineUp.build()
								.forTeam(team)
								.inMatch(new Match(matchDay, team, opponent))
								.withStarterLineUp(new StarterLineUp(
										Scheme433.INSTANCE,
										new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA),
										Set.of(new Defender("difensore1", "titolare", Player.Club.ATALANTA),
												new Defender("difensore2", "titolare", Player.Club.ATALANTA),
												new Defender("difensore3", "titolare", Player.Club.ATALANTA),
												new Defender("difensore4", "titolare", Player.Club.ATALANTA)),
										Set.of(new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
												new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA), 
												new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA)),
										Set.of(new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
												new Forward("attaccante3", "titolare", Player.Club.ATALANTA))))
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
										duplicate,
										new Forward("attaccante2", "panchina", Player.Club.ATALANTA),
										duplicate);
					};			
					
					// THEN an error is thrown
					assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining("duplicate Substitute Forward");

					// AND it does not instantiate
					assertThat(lineUp).isNull();
				}
			}
		}
	}
}
