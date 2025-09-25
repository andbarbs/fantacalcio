package gui.lineup.starter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
import domainModel.Scheme;
import domainModel.scheme.Scheme343;
import domainModel.scheme.Scheme433;
import domainModel.scheme.Scheme532;
import gui.lineup.chooser.LineUpChooser.StarterSelectorDelegate;
import gui.lineup.chooser.Selector;
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

	// the SUT reference
	private StarterLineUpChooser chooser;
	
	private @Mock StarterLineUpChooserWidget mockWidget;

	@BeforeEach
	void testCaseSpecificSetup() {
		
		populateSelsLists();
		
		// instantiates SUT
		chooser = new StarterLineUpChooser(
				goalieSelector,				
				defSel1, defSel2, defSel3, defSel4, defSel5,				
				midSel1, midSel2, midSel3, midSel4,				
				forwSel1, forwSel2, forwSel3);
		chooser.setWidget(mockWidget);
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

		@Nested
		@DisplayName("effects a scheme change to")
		class SwitchesSchemes {
			
			@BeforeEach
			public void setConsumers(
					@Mock Consumer<Selector<Defender>> mockDefConsumer,
					@Mock Consumer<Selector<Midfielder>> mockMidConsumer,
					@Mock Consumer<Selector<Forward>> mockForwConsumer) {				
				
				// GIVEN Consumers are set to avoid NPEs
				chooser.setExitDefConsumer(mockDefConsumer);
				chooser.setEntryDefConsumer(mockDefConsumer);
				chooser.setExitMidConsumer(mockMidConsumer);
				chooser.setEntryMidConsumer(mockMidConsumer);
				chooser.setExitForwConsumer(mockForwConsumer);
				chooser.setEntryForwConsumer(mockForwConsumer);
			}
			
			@Test
			@DisplayName("the '4-3-3' scheme")
			public void widgetsAddedTo433() {
				
				// WHEN the Widget reports a user request to change to '4-3-3'
				chooser.switchToScheme(Scheme433.INSTANCE);
				
				// THEN the '4-3-3' scheme is registered as the current one
				assertThat(chooser.currentScheme).isEqualTo(Scheme433.INSTANCE);
				
				// AND the Widget is instructed to switch to '4-3-3'
				verify(mockWidget).switchTo(Scheme433.INSTANCE);
			}
			
			@Test
			@DisplayName("the '3-4-3' scheme")
			public void widgetsAddedTo343() {
				
				// WHEN the Widget reports a user request to change to '3-4-3'
				chooser.switchToScheme(Scheme343.INSTANCE);
				
				// THEN the '3-4-3' scheme is registered as the current one
				assertThat(chooser.currentScheme).isEqualTo(Scheme343.INSTANCE);
				
				// AND the Widget is instructed to switch to '3-4-3'
				verify(mockWidget).switchTo(Scheme343.INSTANCE);
			}
			
			@Test
			@DisplayName("the '5-3-2' scheme")
			public void widgetsAddedTo532() {

				// WHEN the Widget reports a user request to change to '5-3-2'
				chooser.switchToScheme(Scheme532.INSTANCE);
				
				// THEN the '5-3-2' scheme is registered as the current one
				assertThat(chooser.currentScheme).isEqualTo(Scheme532.INSTANCE);
				
				// AND the Widget is instructed to switch to '5-3-2'
				verify(mockWidget).switchTo(Scheme532.INSTANCE);
			}
		}
	}
	
	@Nested
	@DisplayName("as a StarterLineUpChooserDelegate")
	class AsStarterLineUpChooserDelegate {
		
		@Test
		@DisplayName("allows Clients to retrieve selectors")
		public void retrieveSelectors() {

			assertThat(chooser.getGoalieSelector()).isEqualTo(goalieSelector);
			assertThat(chooser.getAllDefSelectors()).containsExactlyInAnyOrderElementsOf(defSels);
			assertThat(chooser.getAllMidSelectors()).containsExactlyInAnyOrderElementsOf(midSels);
			assertThat(chooser.getAllForwSelectors()).containsExactlyInAnyOrderElementsOf(forwSels);
		}

		@Nested
		@DisplayName("processes scheme changes")
		class ProcessesSchemeChanges {
			
			@Nested
			@DisplayName("executing Consumers on Selectors for each group")
			class ExecutingConsumers {
			
				private <T extends Player> Consumer<Selector<T>> consistencyConsumer(
						Supplier<Collection<Selector<T>>> supplier, Collection<StarterSelectorDelegate<T>> expected) {
					return new Consumer<Selector<T>>() {
						@Override
						public void accept(Selector<T> t) {
							assertThat(supplier.get()).containsExactlyInAnyOrderElementsOf(expected);
						}
					};
				}

				@Test
				@DisplayName("when groups expands")
				public void executesEntryConsumers(
						@Mock Consumer<Selector<Defender>> mockDefConsumer,
						@Mock Consumer<Selector<Midfielder>> mockMidConsumer,
						@Mock Consumer<Selector<Forward>> mockForwConsumer) {
					
					// GIVEN the SUT is set up on a fake Scheme
					chooser.currentScheme = new Scheme(1, 1, 1) {
						
						@Override
						public void accept(SchemeVisitor visitor) {}
					};
					
					// AND fake Consumers are set, which in turn assert
					// current-scheme getters are consistent with the new scheme
					Consumer<Selector<Defender>> spyEntryDefConsumer = spy(
							consistencyConsumer(chooser::getCurrentDefSelectors, List.of(defSel1, defSel2, defSel3)));
					chooser.setEntryDefConsumer(spyEntryDefConsumer);
					
					Consumer<Selector<Midfielder>> spyEntryMidConsumer = spy(
							consistencyConsumer(chooser::getCurrentMidSelectors, List.of(midSel1, midSel2, midSel3)));
					chooser.setEntryMidConsumer(spyEntryMidConsumer);
					
					Consumer<Selector<Forward>> spyEntryForwConsumer = spy(
							consistencyConsumer(chooser::getCurrentForwSelectors, List.of(forwSel1, forwSel2, forwSel3)));
					chooser.setEntryForwConsumer(spyEntryForwConsumer);
					
					// AND non-engaged Consumers are set to avoid NPEs
					chooser.setExitDefConsumer(mockDefConsumer);
					chooser.setExitMidConsumer(mockMidConsumer);
					chooser.setExitForwConsumer(mockForwConsumer);
					
					// WHEN the SUT is requested to shift to a new fake Scheme
					chooser.switchToScheme(new Scheme(3, 3, 3) {
						
						@Override
						public void accept(SchemeVisitor visitor) {}
					});
					
					// THEN exiting & entering Selectors are processed appropriately
					verify(spyEntryDefConsumer).accept(defSel2);
					verify(spyEntryDefConsumer).accept(defSel3);
					
					verify(spyEntryMidConsumer).accept(midSel2);
					verify(spyEntryMidConsumer).accept(midSel3);
					
					verify(spyEntryForwConsumer).accept(forwSel2);
					verify(spyEntryForwConsumer).accept(forwSel3);
					
					// AND fake Consumer execution asserts Consumer-getter consistency
				}

				@Test
				@DisplayName("when groups shrinks")
				public void executesExitConsumers(
						@Mock Consumer<Selector<Defender>> mockDefConsumer,
						@Mock Consumer<Selector<Midfielder>> mockMidConsumer,
						@Mock Consumer<Selector<Forward>> mockForwConsumer) {
					
					// GIVEN the SUT is set up on a fake Scheme
					chooser.currentScheme = new Scheme(3, 3, 3) {
						
						@Override
						public void accept(SchemeVisitor visitor) {}
					};
					
					// AND fake Consumers are set, which in turn assert
					// current-scheme getters are consistent with the new scheme
					Consumer<Selector<Defender>> spyExitDefConsumer = spy(
							consistencyConsumer(chooser::getCurrentDefSelectors, List.of(defSel1)));
					chooser.setExitDefConsumer(spyExitDefConsumer);
					
					Consumer<Selector<Midfielder>> spyExitMidConsumer = spy(
							consistencyConsumer(chooser::getCurrentMidSelectors, List.of(midSel1)));
					chooser.setExitMidConsumer(spyExitMidConsumer);
					
					Consumer<Selector<Forward>> spyExitForwConsumer = spy(
							consistencyConsumer(chooser::getCurrentForwSelectors, List.of(forwSel1)));
					chooser.setExitForwConsumer(spyExitForwConsumer);
					
					// AND non-engaged Consumers are set to avoid NPEs
					chooser.setEntryDefConsumer(mockDefConsumer);
					chooser.setEntryMidConsumer(mockMidConsumer);
					chooser.setEntryForwConsumer(mockForwConsumer);
					
					// WHEN the SUT is requested to shift to a new fake Scheme
					chooser.switchToScheme(new Scheme(1, 1, 1) {
						
						@Override
						public void accept(SchemeVisitor visitor) {}
					});
					
					// THEN exiting & entering Selectors are processed appropriately
					verify(spyExitDefConsumer).accept(defSel2);
					verify(spyExitDefConsumer).accept(defSel3);
					
					verify(spyExitMidConsumer).accept(midSel2);
					verify(spyExitMidConsumer).accept(midSel3);
					
					verify(spyExitForwConsumer).accept(forwSel2);
					verify(spyExitForwConsumer).accept(forwSel3);
					
					// AND fake Consumer execution asserts Consumer-getter consistency
				}
			}			
		}
		
		@Nested
		@DisplayName("retrieve the starter choice")
		class Allows {
			
			
		}
	}
}

