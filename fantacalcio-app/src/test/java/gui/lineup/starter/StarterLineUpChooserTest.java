package gui.lineup.starter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.assertj.swing.annotation.GUITest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domainModel.Player;
import domainModel.Player.Defender;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel.Player.Midfielder;
import gui.LineUpScheme.*;
import gui.lineup.chooser.LineUpChooser.StarterSelectorDelegate;
import gui.lineup.starter.StarterLineUpChooser.StarterLineUpChooserWidget;

@DisplayName("A SwingStarterLineUpChooser")
@ExtendWith(MockitoExtension.class)
public class StarterLineUpChooserTest {
	
	private static final Forward FAKE_FORWARD = new Forward(null, null);
	private static final Midfielder FAKE_MIDFIELDER = new Midfielder(null, null);
	private static final Defender FAKE_DEFENDER = new Defender(null, null);
	private static final Goalkeeper FAKE_GOALIE = new Goalkeeper(null, null);
	
	private @Mock StarterSelectorDelegate<Goalkeeper> goalieSelector;
	private @Mock StarterSelectorDelegate<Defender> defSel1, defSel2, defSel3, defSel4, defSel5;
	private @Mock StarterSelectorDelegate<Midfielder> midSel1, midSel2, midSel3, midSel4;
	private @Mock StarterSelectorDelegate<Forward> forwSel1, forwSel2, forwSel3;
	
	private List<StarterSelectorDelegate<?>> selsIn433, selsIn343, selsIn532;
	private List<StarterSelectorDelegate<Defender>> defSels;
	private List<StarterSelectorDelegate<Midfielder>> midSels;
	private List<StarterSelectorDelegate<Forward>> forwSels;
	
	private @Mock Consumer<StarterSelectorDelegate<? extends Player>> mockConsumer;

	// the SUT reference
	private StarterLineUpChooser chooser;

	@BeforeEach
	void testCaseSpecificSetup() {
		
		populateSelsLists();
		
		// instantiates SUT
		chooser = new StarterLineUpChooser(
				goalieSelector,				
				defSel1, defSel2, defSel3, defSel4, defSel5,				
				midSel1, midSel2, midSel3, midSel4,				
				forwSel1, forwSel2, forwSel3,				
				mockConsumer);
	}

	private void populateSelsLists() {
		// by role
		defSels = List.of(defSel1, defSel2, defSel3, defSel4, defSel5);
		midSels = List.of(midSel1, midSel2, midSel3, midSel4);
		forwSels = List.of(forwSel1, forwSel2, forwSel3);
		
//		sels = List.of(goalieSelector, 
//				defSel1, defSel2, defSel3, defSel4, defSel5, 
//				midSel1, midSel2, midSel3, midSel4, 
//				forwSel1, forwSel2, forwSel3);
		
		// by scheme
		selsIn433 = List.of(
				goalieSelector, 
				defSel1, defSel2, defSel3, defSel4, 
				midSel1, midSel2, midSel3, 
				forwSel1, forwSel2, forwSel3);
		selsIn343 = List.of(
				goalieSelector, 
				defSel1, defSel2, defSel3,
				midSel1, midSel2, midSel3, midSel4,
				forwSel1, forwSel2, forwSel3);
		selsIn532 = List.of(
				goalieSelector, 
				defSel1, defSel2, defSel3, defSel4, defSel5,
				midSel1, midSel2, midSel3,
				forwSel1, forwSel2);
	}

	@Nested
	@DisplayName("as instantiated")
	class JustInstantiated {
		
				
	}
	
	@Nested
	@DisplayName("upon notifications from its StarterLineUpChooserWidget")
	class AsStarterLineUpChooserController {
		
		private @Mock StarterLineUpChooserWidget mockWidget;
		
		@BeforeEach
		void testCaseSpecificSetup() {
			chooser.setWidget(mockWidget);
		}

		@Nested
		@DisplayName("effects a scheme change to")
		class SwitchesSchemes {
			
			@Test
			@GUITest
			@DisplayName("the '4-3-3' scheme")
			public void widgetsAddedTo433() {
				
				// WHEN the Widget reports a user request to change to '4-3-3'
				chooser.switchToScheme(new Scheme433());
				
				// THEN the '4-3-3' scheme is registered as the current one
				assertThat(chooser.currentScheme).isEqualTo(new Scheme433());
				
				// AND the Widget is instructed to switch to '4-3-3'
				verify(mockWidget).switchTo(new Scheme433());
			}
			
			@Test
			@GUITest
			@DisplayName("the '3-4-3' scheme")
			public void widgetsAddedTo343() {
				
				// WHEN the Widget reports a user request to change to '3-4-3'
				chooser.switchToScheme(new Scheme343());
				
				// THEN the '3-4-3' scheme is registered as the current one
				assertThat(chooser.currentScheme).isEqualTo(new Scheme343());
				
				// AND the Widget is instructed to switch to '3-4-3'
				verify(mockWidget).switchTo(new Scheme343());
			}
			
			@Test
			@GUITest
			@DisplayName("the '5-3-2' scheme")
			public void widgetsAddedTo532() {

				// WHEN the Widget reports a user request to change to '5-3-2'
				chooser.switchToScheme(new Scheme532());
				
				// THEN the '5-3-2' scheme is registered as the current one
				assertThat(chooser.currentScheme).isEqualTo(new Scheme532());
				
				// AND the Widget is instructed to switch to '5-3-2'
				verify(mockWidget).switchTo(new Scheme532());
			}
		}
	}
	
	@Nested
	@DisplayName("allows a programmatic client to")
	class ForClients {
		
		// TODO: all of this has to move to LineUpChooser!!
		
		@Test
		@GUITest
		@DisplayName("retrieve selectors")
		public void retrieveSelectors() {

			assertThat(chooser.getGoalieSelector()).isEqualTo(goalieSelector);
			assertThat(chooser.getAllDefSelectors()).containsExactlyElementsOf(defSels);
			assertThat(chooser.getAllMidSelectors()).containsExactlyElementsOf(midSels);
			assertThat(chooser.getAllForwSelectors()).containsExactlyElementsOf(forwSels);
		}

		@Nested
		@DisplayName("query the existence of a starter choice")
		class SelectionSet {			

			@Nested
			@DisplayName("under")
			class Under {

				@Test
				@GUITest
				@DisplayName("the '3-4-3' scheme")
				public void selectionSet343() {

					// GIVEN the current scheme is '3-4-3'
					chooser.currentScheme = new Scheme343();

					// AND only some selectors in '3-4-3' report being non-empty
					when(goalieSelector.getSelection()).thenReturn(Optional.of(FAKE_GOALIE));
					defSels.stream().filter(sel -> selsIn343.contains(sel)).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));
					midSels.stream().filter(sel -> selsIn343.contains(sel)).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));

					// THEN no starter choice is reported as present
					assertThat(chooser.hasChoice()).isFalse();

					// BUT GIVEN all selectors in '3-4-3' report being non-empty
					forwSels.stream().filter(sel -> selsIn343.contains(sel)).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));

					// THEN a starter choice is reported as present
					assertThat(chooser.hasChoice()).isTrue();
				}

				@Test
				@GUITest
				@DisplayName("the '4-3-3' scheme")
				public void selectionSet433() {

					// GIVEN the current scheme is '4-3-3'
					chooser.currentScheme = new Scheme433();

					// AND only some selectors in '3-4-3' report being non-empty
					when(goalieSelector.getSelection()).thenReturn(Optional.of(FAKE_GOALIE));
					defSels.stream().filter(sel -> selsIn433.contains(sel)).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));
					midSels.stream().filter(sel -> selsIn433.contains(sel)).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));

					// THEN no starter choice is reported as present
					assertThat(chooser.hasChoice()).isFalse();

					// BUT GIVEN all selectors in '3-4-3' report being non-empty
					forwSels.stream().filter(sel -> selsIn433.contains(sel)).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));

					// THEN a starter choice is reported as present
					assertThat(chooser.hasChoice()).isTrue();
				}

				@Test
				@GUITest
				@DisplayName("the '5-3-2' scheme")
				public void selectionSet532() {					

					// GIVEN the current scheme is '5-3-2'
					chooser.currentScheme = new Scheme532();

					// AND only some selectors in '3-4-3' report being non-empty
					when(goalieSelector.getSelection()).thenReturn(Optional.of(FAKE_GOALIE));
					defSels.stream().filter(sel -> selsIn532.contains(sel)).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));
					midSels.stream().filter(sel -> selsIn532.contains(sel)).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));

					// THEN no starter choice is reported as present
					assertThat(chooser.hasChoice()).isFalse();

					// BUT GIVEN all selectors in '3-4-3' report being non-empty
					forwSels.stream().filter(sel -> selsIn532.contains(sel)).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));

					// THEN a starter choice is reported as present
					assertThat(chooser.hasChoice()).isTrue();
				}
			}
		}
		
		@Nested
		@DisplayName("retrieve the starter choice")
		class Allows {
			
			@Nested
			@DisplayName("when one is present")
			class SelectionSet {
				
				@Test
				@GUITest
				@DisplayName("ghshh")
				public void widgetsAddedTo343() {
					
				}
			}
			
			@Nested
			@DisplayName("when none is present")
			class SelectionCleared {
				
				@Test
				@GUITest
				@DisplayName("shgsfdh")
				public void widgetsAddedTo343() {
					
				}
			}
		}
	}


}

