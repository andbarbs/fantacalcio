package swingViews;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domainModel.Player.Defender;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel.Player.Midfielder;
import swingViews.Selector.SelectorListener;
import swingViews.LineUpChooser.LineUpChooserWidget;
import swingViews.LineUpChooser.StarterLineUpChooserDelegate;
import swingViews.LineUpChooser.SubstituteTripletChooserDelegate;

@DisplayName("A LineUpChooser")
@ExtendWith(MockitoExtension.class)
public class LineUpChooserTest {
	
	private static final Forward FAKE_FORWARD = new Forward(null, null);
	private static final Midfielder FAKE_MIDFIELDER = new Midfielder(null, null);
	private static final Defender FAKE_DEFENDER = new Defender(null, null);
	private static final Goalkeeper FAKE_GOALIE = new Goalkeeper(null, null);
	
	private @Mock LineUpChooserWidget mockWidget;	

	@Nested
	@DisplayName("commands its Widget to")
	class SelectionSet {
		
		@Nested
		@DisplayName("enable saving the LineUp")
		class EnableSaving {
			
			private @Mock Selector<Goalkeeper> starterGoalie;
			private @Mock Selector<Defender> starterDef1, starterDef2, starterDef3, starterDef4, starterDef5;
			private @Mock Selector<Midfielder> starterMid1, starterMid2, starterMid3, starterMid4;
			private @Mock Selector<Forward> starterForw1, starterForw2, starterForw3;
			
			private @Mock Selector<Goalkeeper> tripletGoalie1, tripletGoalie2, tripletGoalie3;
			private @Mock Selector<Defender> tripletDef1, tripletDef2, tripletDef3;
			private @Mock Selector<Midfielder> tripletMid1, tripletMid2, tripletMid3;
			private @Mock Selector<Forward> tripletForw1, tripletForw2, tripletForw3;
			
			private @Mock StarterLineUpChooserDelegate starterChooser;
			private @Mock SubstituteTripletChooserDelegate<Goalkeeper> goalieTriplet;
			private @Mock SubstituteTripletChooserDelegate<Defender> defTriplet;
			private @Mock SubstituteTripletChooserDelegate<Midfielder> midTriplet;
			private @Mock SubstituteTripletChooserDelegate<Forward> forwTriplet;
			
			private List<Selector<?>> selsInCurrentScheme;
			private List<Selector<Goalkeeper>> tripletGoalies;
			private List<Selector<Defender>> starterDefs, tripletDefs;
			private List<Selector<Midfielder>> starterMids, tripletMids;
			private List<Selector<Forward>> starterForws, tripletForws;
			
			private void populateSelsLists() {
				// starters, by role
				starterDefs = List.of(starterDef1, starterDef2, starterDef3, starterDef4, starterDef5);
				starterMids = List.of(starterMid1, starterMid2, starterMid3, starterMid4);
				starterForws = List.of(starterForw1, starterForw2, starterForw3);
				
				// starters, in a fictitious current scheme
				selsInCurrentScheme = List.of(
						starterGoalie, 
						starterDef1, starterDef2, starterDef3,
						starterMid1, starterMid2, starterMid3, 
						starterForw1, starterForw2);
				
				// triplet, by role
				tripletGoalies = List.of(tripletGoalie1, tripletGoalie2, tripletGoalie3);
				tripletDefs = List.of(tripletDef1, tripletDef2, tripletDef3);
				tripletMids = List.of(tripletMid1, tripletMid2, tripletMid3);
				tripletForws = List.of(tripletForw1, tripletForw2, tripletForw3);
			}

			// the SUT reference
			private LineUpChooser chooser;

			@BeforeEach
			void testCaseSpecificSetup() {
				
				populateSelsLists();
				
				// stubs Starter and Substitute Delegates to allow listener attachment
				when(starterChooser.getGoalieSelector()).thenReturn(starterGoalie);
				when(starterChooser.getAllDefSelectors()).thenReturn(starterDefs);
				when(starterChooser.getAllMidSelectors()).thenReturn(starterMids);
				when(starterChooser.getAllForwSelectors()).thenReturn(starterForws);				

				when(goalieTriplet.getSelectors()).thenReturn(tripletGoalies);
				when(defTriplet.getSelectors()).thenReturn(tripletDefs);
				when(midTriplet.getSelectors()).thenReturn(tripletMids);
				when(forwTriplet.getSelectors()).thenReturn(tripletForws);
				
				// instantiates SUT
				chooser = new LineUpChooser(starterChooser,	goalieTriplet, defTriplet, midTriplet, forwTriplet);
				chooser.setWidget(mockWidget);
			}
			
			@Nested
			@DisplayName("when a line-up choice emerges")
			class WhenChoiceEmerges {
				
				private void affirmAllListenerFlags() {
					chooser.hasStarterGoalieChoice = true;
					chooser.hasStarterDefChoice = true;
					chooser.hasStarterMidChoice = true;
					chooser.hasStarterForwChoice = true;
					chooser.hasSubsGoaliesChoice = true;
					chooser.hasSubsDefsChoice = true;
					chooser.hasSubsMidsChoice = true;
					chooser.hasSubsForwsChoice = true;
				}

				@Nested
				@DisplayName("under the current scheme")
				class UnderCurrentScheme {
					
					@Nested
					@DisplayName("thanks to the choice for")
					class ThanksToChoiceFor {
						
						@Nested
						@DisplayName("starter")
						class Starter {
							@Captor ArgumentCaptor<SelectorListener<Goalkeeper>> starterGoalieListener;
							@Captor ArgumentCaptor<SelectorListener<Defender>> starterDefListener;
							@Captor ArgumentCaptor<SelectorListener<Midfielder>> starterMidListener;			
							@Captor ArgumentCaptor<SelectorListener<Forward>> starterForwListener;
							
							@Test
							@DisplayName("goalkeeper")
							public void StarterGoalkeeper() {
								verify(starterGoalie).attachListener(starterGoalieListener.capture());
								
								// GIVEN all other Listeners have reported a choice
								affirmAllListenerFlags();
								chooser.hasStarterGoalieChoice = false;
								
								// WHEN the Listener is triggered
								starterGoalieListener.getValue().selectionMadeOn(starterGoalie);
								
								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							
							@Test
							@DisplayName("defenders")
							public void StarterDefenders() {
								starterDefs.forEach(sel -> verify(sel).attachListener(starterDefListener.capture()));
								
								// GIVEN all other Listeners have reported a choice
								affirmAllListenerFlags();
								chooser.hasStarterDefChoice = false;
								
								// AND Starter Delegate reports those in the scheme as current starter selectors
								when(starterChooser.getCurrentDefSelectors()).thenReturn(
										starterDefs.stream().filter(selsInCurrentScheme::contains).collect(toList()));
								// AND selectors in the scheme report being non-empty
								starterDefs.stream().filter(selsInCurrentScheme::contains).forEach(
										selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));
								
								// WHEN the Listener is triggered
								starterDefListener.getValue().selectionMadeOn(starterDef1);
								
								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							
							@Test
							@DisplayName("midfielders")
							public void StarterMidfielders() {
								starterMids.forEach(sel -> verify(sel).attachListener(starterMidListener.capture()));
								
								// GIVEN all other Listeners have reported a choice
								affirmAllListenerFlags();
								chooser.hasStarterMidChoice = false;

								// AND Starter Delegate reports those in the scheme as current starter selectors
								when(starterChooser.getCurrentMidSelectors()).thenReturn(
										starterMids.stream().filter(selsInCurrentScheme::contains).collect(toList()));
								// AND selectors in the scheme report being non-empty
								starterMids.stream().filter(selsInCurrentScheme::contains).forEach(
										selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));

								// WHEN the Listener is triggered
								starterMidListener.getValue().selectionMadeOn(starterMid1);

								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							
							@Test
							@DisplayName("forwards")
							public void StarterForwards() {
								starterForws.forEach(sel -> verify(sel).attachListener(starterForwListener.capture()));
								
								// GIVEN all other Listeners have reported a choice
								affirmAllListenerFlags();
								chooser.hasStarterForwChoice = false;

								// AND Starter Delegate reports those in the scheme as current starter selectors
								when(starterChooser.getCurrentForwSelectors()).thenReturn(
										starterForws.stream().filter(selsInCurrentScheme::contains).collect(toList()));
								// AND selectors in the scheme report being non-empty
								starterForws.stream().filter(selsInCurrentScheme::contains).forEach(
										selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));

								// WHEN the Listener is triggered
								starterForwListener.getValue().selectionMadeOn(starterForw1);

								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
						}
						
						@Nested
						@DisplayName("substitute")
						class Sustitute {							
							
							@Captor ArgumentCaptor<SelectorListener<Goalkeeper>> tripletGoalieListener;
							@Captor ArgumentCaptor<SelectorListener<Defender>> tripletDefListener;
							@Captor ArgumentCaptor<SelectorListener<Midfielder>> tripletMidListener;				
							@Captor ArgumentCaptor<SelectorListener<Forward>> tripletForwListener;
							
							@Test
							@DisplayName("goalkeepers")
							public void SubstituteGoalkeepers() {
								tripletGoalies.forEach(sel -> verify(sel).attachListener(tripletGoalieListener.capture()));
								
								// GIVEN all other Listeners have reported a choice
								affirmAllListenerFlags();
								chooser.hasSubsGoaliesChoice = false;
								
								// AND all triplet selectors report being non-empty
								tripletGoalies.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_GOALIE)));
								
								// WHEN the Listener is triggered
								tripletGoalieListener.getValue().selectionMadeOn(tripletGoalie1);
								
								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							
							@Test
							@DisplayName("defenders")
							public void SubstituteDefenders() {
								tripletDefs.forEach(sel -> verify(sel).attachListener(tripletDefListener.capture()));
								
								// GIVEN all other Listeners have reported a choice
								affirmAllListenerFlags();
								chooser.hasSubsDefsChoice = false;

								// AND all triplet selectors report being non-empty
								tripletDefs.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));

								// WHEN the Listener is triggered
								tripletDefListener.getValue().selectionMadeOn(tripletDef1);

								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							
							@Test
							@DisplayName("midfielders")
							public void SubstituteMidfielders() {
								tripletMids.forEach(sel -> verify(sel).attachListener(tripletMidListener.capture()));
								
								// GIVEN all other Listeners have reported a choice
								affirmAllListenerFlags();
								chooser.hasSubsMidsChoice = false;

								// AND all triplet selectors report being non-empty
								tripletMids.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));

								// WHEN the Listener is triggered
								tripletMidListener.getValue().selectionMadeOn(tripletMid1);

								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							
							@Test
							@DisplayName("forwards")
							public void SubstituteForwards() {
								tripletForws.forEach(sel -> verify(sel).attachListener(tripletForwListener.capture()));
								
								// GIVEN all other Listeners have reported a choice
								affirmAllListenerFlags();
								chooser.hasSubsForwsChoice = false;

								// AND all triplet selectors report being non-empty
								tripletForws.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));

								// WHEN the Listener is triggered
								tripletForwListener.getValue().selectionMadeOn(tripletForw1);

								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
						}
					}					
				}
				
				/**
				 * examines the case where a choice emerges due to
				 * some selectors being excluded 
				 */
				@Nested
				@DisplayName("as a result of a scheme change excluding")
				class FollowingExclusion {
					
					@Captor ArgumentCaptor<SelectorListener<Defender>> starterDefListener;
					@Captor ArgumentCaptor<SelectorListener<Midfielder>> starterMidListener;			
					@Captor ArgumentCaptor<SelectorListener<Forward>> starterForwListener;
					
					@Test
					@DisplayName("a defender")
					public void ExcludedStarterDefender() {
						starterDefs.forEach(sel -> verify(sel).attachListener(starterDefListener.capture()));

						// GIVEN all other Listeners have reported a choice
						affirmAllListenerFlags();
						chooser.hasStarterDefChoice = false;

						// AND Starter Delegate reports those in the scheme as current starter selectors
						when(starterChooser.getCurrentDefSelectors()).thenReturn(
								starterDefs.stream().filter(selsInCurrentScheme::contains).collect(toList()));
						// AND selectors in the scheme report being non-empty
						starterDefs.stream().filter(selsInCurrentScheme::contains).forEach(
								selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));

						// WHEN the Listener is notified of 'selection-cleared' 
						// for a selector outside the current scheme
						starterDefListener.getValue().selectionClearedOn(starterDefs.stream()
								.filter(sel -> !selsInCurrentScheme.contains(sel)).findFirst().get());

						// THEN the Widget is commanded to enable line-up saving
						verify(mockWidget).enableSavingLineUp();
					}
					
					@Test
					@DisplayName("a midfielder")
					public void ExcludedStarterMidfielder() {
						starterMids.forEach(sel -> verify(sel).attachListener(starterMidListener.capture()));

						// GIVEN all other Listeners have reported a choice
						affirmAllListenerFlags();
						chooser.hasStarterMidChoice = false;

						// AND Starter Delegate reports those in the scheme as current starter selectors
						when(starterChooser.getCurrentMidSelectors()).thenReturn(
								starterMids.stream().filter(selsInCurrentScheme::contains).collect(toList()));
						// AND selectors in the scheme report being non-empty
						starterMids.stream().filter(selsInCurrentScheme::contains).forEach(
								selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));

						// WHEN the Listener is notified of 'selection-cleared' 
						// for a selector outside the current scheme
						starterMidListener.getValue().selectionClearedOn(starterMids.stream()
								.filter(sel -> !selsInCurrentScheme.contains(sel)).findFirst().get());

						// THEN the Widget is commanded to enable line-up saving
						verify(mockWidget).enableSavingLineUp();
					}
					
					@Test
					@DisplayName("a forward")
					public void ExcludedStarterForward() {
						starterForws.forEach(sel -> verify(sel).attachListener(starterForwListener.capture()));

						// GIVEN all other Listeners have reported a choice
						affirmAllListenerFlags();
						chooser.hasStarterForwChoice = false;

						// AND Starter Delegate reports those in the scheme as current starter selectors
						when(starterChooser.getCurrentForwSelectors()).thenReturn(
								starterForws.stream().filter(selsInCurrentScheme::contains).collect(toList()));
						// AND selectors in the scheme report being non-empty
						starterForws.stream().filter(selsInCurrentScheme::contains).forEach(
								selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));

						// WHEN the Listener is notified of 'selection-cleared' 
						// for a selector outside the current scheme
						starterForwListener.getValue().selectionClearedOn(starterForws.stream()
								.filter(sel -> !selsInCurrentScheme.contains(sel)).findFirst().get());

						// THEN the Widget is commanded to enable line-up saving
						verify(mockWidget).enableSavingLineUp();
					}
				}
			}			
		}
	}
	
	@Nested
	@DisplayName("allows a programmatic client to")
	class ForClients {
		
		// TODO: all of this has to move to LineUpChooser!!
		
		@Nested
		@DisplayName("retrieve the starter choice")
		class Allows {
			
			@Nested
			@DisplayName("when one is present")
			class SelectionSet {
				
				@Test
				@DisplayName("ghshh")
				public void widgetsAddedTo343() {
					
				}
			}
			
			@Nested
			@DisplayName("when none is present")
			class SelectionCleared {
				
				@Test
				@DisplayName("shgsfdh")
				public void widgetsAddedTo343() {
					
				}
			}
		}
	}

	@Nested
	@DisplayName("as instantiated")
	class JustInstantiated {		
				
	}

	@Nested
	@DisplayName("upon notifications from its LineUpChooserWidget")
	class AsStarterLineUpChooserController {
		
	}


}

