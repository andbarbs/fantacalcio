package gui.lineup.starter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
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
	
	private @Mock StarterLineUpChooserWidget mockWidget;

	// the SUT reference
	private StarterLineUpChooser chooser;

	@BeforeEach
	void testCaseSpecificSetup() {
		
		defSels = List.of(defSel1, defSel2, defSel3, defSel4, defSel5);
		midSels = List.of(midSel1, midSel2, midSel3, midSel4);
		forwSels = List.of(forwSel1, forwSel2, forwSel3);
		
		// instantiates SUT
		chooser = new StarterLineUpChooser(
				goalieSelector,	defSels, midSels, forwSels);
		chooser.setWidget(mockWidget);
	}
	
	@Nested
	@DisplayName("as a StarterLineUpChooserController")
	class AsStarterLineUpChooserController {
		
		@Nested
		@DisplayName("handles requests to effect a scheme change")
		class HandlesRequetstToChangeScheme {
			
			private @Mock Consumer<Selector<Defender>> mockDefConsumer;
			private @Mock Consumer<Selector<Midfielder>> mockMidConsumer;
			private @Mock Consumer<Selector<Forward>> mockForwConsumer;
			
			@BeforeEach
			void setMockConsumers() {
				chooser.setExitDefConsumer(mockDefConsumer);
				chooser.setEntryDefConsumer(mockDefConsumer);
				chooser.setExitMidConsumer(mockMidConsumer);
				chooser.setEntryMidConsumer(mockMidConsumer);
				chooser.setExitForwConsumer(mockForwConsumer);
				chooser.setEntryForwConsumer(mockForwConsumer);
			}
		
			@Test
			@DisplayName("when the Scheme is compatible with Selector numbers")
			void whenSchemeIsDoable() {
				
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
			
			@Nested
			@DisplayName("when the Scheme has too many")
			class SchemeNotDoable {
				
				@Test
				@DisplayName("defenders")
				void tooManyDefenders() {
					
					// WHEN the Widget requests switching to a Scheme
					// that CANNOT be realized with existing Selector numbers
					// (too many Defenders needed)
					Scheme fakeScheme = new Scheme(6, 4, 3) {
						
						@Override
						public void accept(SchemeVisitor visitor) {
						}
					};			
					ThrowingCallable shouldThrow = () -> chooser.switchToScheme(fakeScheme);
					
					// THEN a suitable exception is thrown
					assertThatThrownBy(shouldThrow)
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("requested Scheme has 6 Defenders, this composes only 5");
					
					// AND the '4-3-3' scheme is NOT registered as the current one
					assertThat(chooser.currentScheme).isNotSameAs(fakeScheme);
					
					// AND the Widget is never instructed to switch
					verify(mockWidget, never()).switchTo(fakeScheme);
					
					// AND Consumers are never executed
					verifyNoInteractions(mockDefConsumer, mockMidConsumer, mockForwConsumer);
				}
				
				@Test
				@DisplayName("midfielders")
				void tooManyMidfielders() {
					
					// WHEN the Widget requests switching to a Scheme
					// that CANNOT be realized with existing Selector numbers
					// (too many Midfielders needed)
					Scheme fakeScheme = new Scheme(5, 6, 2) {
						
						@Override
						public void accept(SchemeVisitor visitor) {
						}
					};			
					ThrowingCallable shouldThrow = () -> chooser.switchToScheme(fakeScheme);
					
					// THEN a suitable exception is thrown
					assertThatThrownBy(shouldThrow)
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("requested Scheme has 6 Midfielders, this composes only 4");
					
					// AND the '4-3-3' scheme is NOT registered as the current one
					assertThat(chooser.currentScheme).isNotSameAs(fakeScheme);
					
					// AND the Widget is never instructed to switch
					verify(mockWidget, never()).switchTo(fakeScheme);
					
					// AND Consumers are never executed
					verifyNoInteractions(mockDefConsumer, mockMidConsumer, mockForwConsumer);
				}
				
				@Test
				@DisplayName("forwards")
				void tooManyForwards() {
					
					// WHEN the Widget requests switching to a Scheme
					// that CANNOT be realized with existing Selector numbers
					// (too many Midfielders needed)
					Scheme fakeScheme = new Scheme(5, 4, 4) {
						
						@Override
						public void accept(SchemeVisitor visitor) {
						}
					};			
					ThrowingCallable shouldThrow = () -> chooser.switchToScheme(fakeScheme);
					
					// THEN a suitable exception is thrown
					assertThatThrownBy(shouldThrow)
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("requested Scheme has 4 Forwards, this composes only 3");
					
					// AND the '4-3-3' scheme is NOT registered as the current one
					assertThat(chooser.currentScheme).isNotSameAs(fakeScheme);
					
					// AND the Widget is never instructed to switch
					verify(mockWidget, never()).switchTo(fakeScheme);
					
					// AND Consumers are never executed
					verifyNoInteractions(mockDefConsumer, mockMidConsumer, mockForwConsumer);
				}
			}			
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
			void retrieveSelectors() {
				
				assertThat(chooser.getGoalieSelector()).isEqualTo(goalieSelector);
				assertThat(chooser.getAllDefSelectors()).containsExactlyInAnyOrderElementsOf(defSels);
				assertThat(chooser.getAllMidSelectors()).containsExactlyInAnyOrderElementsOf(midSels);
				assertThat(chooser.getAllForwSelectors()).containsExactlyInAnyOrderElementsOf(forwSels);
			}
			
			@Test
			@DisplayName("retrieve the current choice for StarterLineUp")
			void retrieveStarterLineUp() {
				
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
			
			@Nested
			@DisplayName("set a scheme as current")
			class ConfigurableOnCurrentScheme {

				private @Mock Consumer<Selector<Defender>> mockEntryDefConsumer;
				private @Mock Consumer<Selector<Midfielder>> mockEntryMidConsumer;
				private @Mock Consumer<Selector<Forward>> mockEntryForwConsumer;
				private @Mock Consumer<Selector<Defender>> mockExitDefConsumer;
				private @Mock Consumer<Selector<Midfielder>> mockExitMidConsumer;
				private @Mock Consumer<Selector<Forward>> mockExitForwConsumer;
				
				@BeforeEach
				void setMockConsumers() {
					chooser.setEntryDefConsumer(mockEntryDefConsumer);
					chooser.setExitDefConsumer(mockExitDefConsumer);
					chooser.setEntryMidConsumer(mockEntryMidConsumer);
					chooser.setExitMidConsumer(mockExitMidConsumer);
					chooser.setEntryForwConsumer(mockEntryForwConsumer);
					chooser.setExitForwConsumer(mockExitForwConsumer);
				}
				
				@Test
				@DisplayName("when no previous current scheme existed")
				void noPreviousCurrentScheme() {
					
					// GIVEN the SUT is not set up on any Scheme
					chooser.currentScheme = null;
					
					// WHEN the SUT is requested to shift to a new fake Scheme
					Scheme newScheme = new Scheme(1, 2, 3) {
						
						@Override
						public void accept(SchemeVisitor visitor) {}
					};
					chooser.setCurrentScheme(newScheme);
					
					// THEN the SUT records the new Scheme as current
					assertThat(chooser.currentScheme).isSameAs(newScheme);
					
					// AND entering Selectors are processed appropriately
					verify(mockEntryDefConsumer).accept(defSel1);
					
					verify(mockEntryMidConsumer).accept(midSel1);
					verify(mockEntryMidConsumer).accept(midSel2);
					
					verify(mockEntryForwConsumer).accept(forwSel1);
					verify(mockEntryForwConsumer).accept(forwSel2);
					verify(mockEntryForwConsumer).accept(forwSel3);
					
					// AND no other interactions with Consumers are recorded
					verifyNoMoreInteractions(mockEntryDefConsumer, mockEntryMidConsumer, mockEntryForwConsumer,
							mockExitDefConsumer, mockExitMidConsumer, mockExitForwConsumer);
				}
				
				@Test
				@DisplayName("when a previous current scheme existed")
				void previousCurrentScheme() {
					
					// GIVEN the SUT is set up on a fake Scheme
					chooser.currentScheme = new Scheme(1, 2, 3) {
						
						@Override
						public void accept(SchemeVisitor visitor) {}
					};
					
					// WHEN the SUT is requested to shift to a new fake Scheme
					Scheme newScheme = new Scheme(3, 2, 1) {
						
						@Override
						public void accept(SchemeVisitor visitor) {}
					};
					chooser.setCurrentScheme(newScheme);
					
					// THEN the SUT records the new Scheme as current
					assertThat(chooser.currentScheme).isSameAs(newScheme);
					
					// AND entering & exiting Selectors are processed appropriately
					verify(mockEntryDefConsumer).accept(defSel2);
					verify(mockEntryDefConsumer).accept(defSel3);
					
					verify(mockExitForwConsumer).accept(forwSel2);
					verify(mockExitForwConsumer).accept(forwSel3);
					
					// AND no other interactions with Consumers are recorded
					verifyNoMoreInteractions(mockEntryDefConsumer, mockEntryMidConsumer, mockEntryForwConsumer,
							mockExitDefConsumer, mockExitMidConsumer, mockExitForwConsumer);
				}
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
				void executesEntryConsumers(
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
				void executesExitConsumers(
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

