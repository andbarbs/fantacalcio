package gui.lineup.starter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domainModel.LineUp.LineUpBuilderSteps.StarterLineUp;
import domainModel.Player;
import domainModel.Player.Defender;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel.Player.Midfielder;
import domainModel.Scheme;
import gui.lineup.chooser.LineUpChooser.StarterSelectorDelegate;
import gui.lineup.chooser.Selector;
import gui.lineup.starter.StarterLineUpChooser.StarterLineUpChooserWidget;

@DisplayName("A SwingStarterLineUpChooser")
@ExtendWith(MockitoExtension.class)
public class StarterLineUpChooserTest {
	
	private @Mock StarterSelectorDelegate<Goalkeeper> goalieSelector;
	private @Mock StarterSelectorDelegate<Defender> defSel1, defSel2, defSel3, defSel4, defSel5;
	private @Mock StarterSelectorDelegate<Midfielder> midSel1, midSel2, midSel3, midSel4;
	private @Mock StarterSelectorDelegate<Forward> forwSel1, forwSel2, forwSel3;

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
	}

	@Nested
	@DisplayName("as instantiated")
	class JustInstantiated {
		
				
	}
	
	@Nested
	@DisplayName("as a StarterLineUpChooserController")
	class AsStarterLineUpChooserController {
		
		@Test
		@DisplayName("handles requests to effect a scheme change")
		public void whenRequestedToChangeScheme(
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
			
			// WHEN the Widget requests switching to a Scheme
			Scheme fakeScheme = new Scheme(1, 2, 3) {
				
				@Override
				public void accept(SchemeVisitor visitor) {
				}
			};			
			chooser.switchToScheme(fakeScheme);
			
			// THEN the '4-3-3' scheme is registered as the current one
			assertThat(chooser.currentScheme).isSameAs(fakeScheme);
			
			// AND the Widget is instructed to switch to '4-3-3'
			verify(mockWidget).switchTo(fakeScheme);
		}
	}
	
	@Nested
	@DisplayName("as a StarterLineUpChooserDelegate")
	class AsStarterLineUpChooserDelegate {
		
		@Nested
		@DisplayName("allows Clients to")
		class AllowsClientsTo {
			
			@Test
			@DisplayName("retrieve all composed StarterSelectorDelegates")
			public void retrieveSelectors() {
				
				assertThat(chooser.getGoalieSelector()).isEqualTo(goalieSelector);
				assertThat(chooser.getAllDefSelectors()).containsExactlyInAnyOrderElementsOf(defSels);
				assertThat(chooser.getAllMidSelectors()).containsExactlyInAnyOrderElementsOf(midSels);
				assertThat(chooser.getAllForwSelectors()).containsExactlyInAnyOrderElementsOf(forwSels);
			}
			
			@Test
			@DisplayName("retrieve the current choice for StarterLineUp")
			public void widgetsAddedTo532() {
				
				// GIVEN the SUT is set up on a fake Scheme
				Scheme fakeScheme = new Scheme(1, 2, 3) {
					
					@Override
					public void accept(SchemeVisitor visitor) {
					}
				};
				chooser.currentScheme = fakeScheme;
				
				// AND Selectors in the current Scheme report fake selections
				Goalkeeper fakeGoalie = new Goalkeeper("test", "goalie");			
				Defender fakeDefender = new Defender("test", "defender");			
				Midfielder fakeMidfielder1 = new Midfielder("test", "midfielder1"),
						fakeMidfielder2 = new Midfielder("test", "midfielder2");			
				Forward fakeForward1 = new Forward("test", "forward1"),
						fakeForward2 = new Forward("test", "forward2"), 
						fakeForward3 = new Forward("test", "forward3");
				
				when(goalieSelector.getSelection()).thenReturn(Optional.of(fakeGoalie));
				when(defSel1.getSelection()).thenReturn(Optional.of(fakeDefender));
				when(midSel1.getSelection()).thenReturn(Optional.of(fakeMidfielder1));
				when(midSel2.getSelection()).thenReturn(Optional.of(fakeMidfielder2));
				when(forwSel1.getSelection()).thenReturn(Optional.of(fakeForward1));
				when(forwSel2.getSelection()).thenReturn(Optional.of(fakeForward2));
				when(forwSel3.getSelection()).thenReturn(Optional.of(fakeForward3));
				
				// WHEN the SUT is requested to produce a StarterLineUp
				StarterLineUp output = chooser.getCurrentStarterLineUp();
				
				// THEN the output is correctly assembled
				assertThat(output).isEqualTo(new StarterLineUp(
						fakeScheme, 
						fakeGoalie, 
						Set.of(fakeDefender), 
						Set.of(fakeMidfielder2, fakeMidfielder1), 
						Set.of(fakeForward2, fakeForward1, fakeForward3)));
			}
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
	}
}

