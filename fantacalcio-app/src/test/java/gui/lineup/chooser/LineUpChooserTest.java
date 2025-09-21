package gui.lineup.chooser;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import businessLogic.UserService;
import domainModel.FantaTeam;
import domainModel.FantaTeamViewer;
import domainModel.LineUp;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import domainModel.Player;
import domainModel.Player.Defender;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel.Player.Midfielder;
import domainModel.scheme.Scheme433;
import gui.lineup.chooser.LineUpChooser.LineUpChooserWidget;
import gui.lineup.chooser.LineUpChooser.StarterLineUpChooserDelegate;
import gui.lineup.chooser.LineUpChooser.StarterSelectorDelegate;
import gui.lineup.chooser.LineUpChooser.SubstituteSelectorDelegate;
import gui.lineup.chooser.LineUpChooser.SubstituteTripletChooserDelegate;
import gui.lineup.chooser.Selector.SelectorListener;
import gui.lineup.dealing.CompetitiveOptionDealingGroup;

@DisplayName("A LineUpChooser")
@ExtendWith(MockitoExtension.class)
public class LineUpChooserTest {
	
	private static final Forward FAKE_FORWARD = new Forward(null, null);
	private static final Midfielder FAKE_MIDFIELDER = new Midfielder(null, null);
	private static final Defender FAKE_DEFENDER = new Defender(null, null);
	private static final Goalkeeper FAKE_GOALIE = new Goalkeeper(null, null);
	
	// first-level dependencies
	private @Mock UserService mockService;
	private @Mock LineUpChooserWidget mockWidget;
	private @Mock StarterLineUpChooserDelegate starterChooser;	
	private @Mock SubstituteTripletChooserDelegate<Goalkeeper> goalieTriplet;
	private @Mock SubstituteTripletChooserDelegate<Defender> defTriplet;
	private @Mock SubstituteTripletChooserDelegate<Midfielder> midTriplet;
	private @Mock SubstituteTripletChooserDelegate<Forward> forwTriplet;

	// second-level dependencies
	private @Mock StarterSelectorDelegate<Goalkeeper> starterGoalie;
	private @Mock StarterSelectorDelegate<Defender> starterDef1, starterDef2, starterDef3, starterDef4, starterDef5;
	private @Mock StarterSelectorDelegate<Midfielder> starterMid1, starterMid2, starterMid3, starterMid4;
	private @Mock StarterSelectorDelegate<Forward> starterForw1, starterForw2, starterForw3;
	
	private @Mock SubstituteSelectorDelegate<Goalkeeper> tripletGoalie1, tripletGoalie2, tripletGoalie3;
	private @Mock SubstituteSelectorDelegate<Defender> tripletDef1, tripletDef2, tripletDef3;
	private @Mock SubstituteSelectorDelegate<Midfielder> tripletMid1, tripletMid2, tripletMid3;
	private @Mock SubstituteSelectorDelegate<Forward> tripletForw1, tripletForw2, tripletForw3;

	// selector lists
	private List<Selector<?>> selsInCurrentScheme;
	private List<StarterSelectorDelegate<Defender>> starterDefs;
	private List<StarterSelectorDelegate<Midfielder>> starterMids;
	private List<StarterSelectorDelegate<Forward>> starterForws;
	private List<SubstituteSelectorDelegate<Goalkeeper>> tripletGoalies;
	private List<SubstituteSelectorDelegate<Defender>> tripletDefs;
	private List<SubstituteSelectorDelegate<Midfielder>> tripletMids;
	private List<SubstituteSelectorDelegate<Forward>> tripletForws;
	
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

	private void affirmAllChoiceFlagsIn(LineUpChooser chooser) {
		chooser.hasStarterGoalieChoice.flag = true;
		chooser.hasStarterDefChoice.flag = true;
		chooser.hasStarterMidChoice.flag = true;
		chooser.hasStarterForwChoice.flag = true;
		chooser.hasSubsGoaliesChoice.flag = true;
		chooser.hasSubsDefsChoice.flag = true;
		chooser.hasSubsMidsChoice.flag = true;
		chooser.hasSubsForwsChoice.flag = true;
	}

	@BeforeEach
	void instantiateSUT() {

		populateSelsLists();

		// stubs Delegates to allow listener attachments in SUT constructor
		when(starterChooser.getGoalieSelector()).thenReturn(starterGoalie);
		when(goalieTriplet.getSelectors()).thenReturn(tripletGoalies);
		when(defTriplet.getSelectors()).thenReturn(tripletDefs);
		when(midTriplet.getSelectors()).thenReturn(tripletMids);
		when(forwTriplet.getSelectors()).thenReturn(tripletForws);

		// instantiates SUT
		chooser = new LineUpChooser(
				mockService, 
				starterChooser, 
				goalieTriplet, defTriplet, midTriplet, forwTriplet);
		chooser.setWidget(mockWidget);
		
		// verifies constructor interactions (de facto, resetting mocks)
		verify(starterChooser).getGoalieSelector();
		verify(starterChooser).setEntryDefConsumer(any());
		verify(starterChooser).setExitDefConsumer(any());
		verify(starterChooser).setEntryMidConsumer(any());
		verify(starterChooser).setExitMidConsumer(any());
		verify(starterChooser).setEntryForwConsumer(any());
		verify(starterChooser).setExitForwConsumer(any());
		verify(goalieTriplet).getSelectors();
		verify(defTriplet).getSelectors();
		verify(midTriplet).getSelectors();
		verify(forwTriplet).getSelectors();
	}
	
	@Test
	@DisplayName("can be initialized to a Team and Match")
	void whenConfigured(@Mock FantaTeam team, @Mock FantaTeamViewer mockViewer, @Mock Match match) {
		
		// GIVEN the Team reports these extractions (alphabetically unsorted)
		when(team.extract()).thenReturn(mockViewer);
		when(mockViewer.goalkeepers()).thenReturn(Set.of(
				new Goalkeeper("goalkeeper", "C"),
				new Goalkeeper("goalkeeper", "B"),
				new Goalkeeper("goalkeeper", "A")));
		when(mockViewer.defenders()).thenReturn(Set.of(
				new Defender("defender", "C"),
				new Defender("defender", "B"),
				new Defender("defender", "A")));
		when(mockViewer.midfielders()).thenReturn(Set.of(
				new Midfielder("midfielder", "C"),
				new Midfielder("midfielder", "B"),
				new Midfielder("midfielder", "A")));
		when(mockViewer.forwards()).thenReturn(Set.of(
				new Forward("forward", "C"),
				new Forward("forward", "B"),
				new Forward("forward", "A")));
		
		// AND the Starter Delegate returns mock Selectors
		when(starterChooser.getAllDefSelectors()).thenReturn(Set.copyOf(starterDefs));
		when(starterChooser.getAllMidSelectors()).thenReturn(Set.copyOf(starterMids));
		when(starterChooser.getAllForwSelectors()).thenReturn(Set.copyOf(starterForws));
		
		try (@SuppressWarnings("rawtypes") MockedStatic<CompetitiveOptionDealingGroup> mockedGroup = 
				mockStatic(CompetitiveOptionDealingGroup.class)) {
			
			// WHEN the SUT is initialized to a Team and Match
			chooser.initTo(team, match);
			
			// THEN its internal state records that
			assertThat(chooser.team).isSameAs(team);
			assertThat(chooser.match).isSameAs(match);
			
			// AND all Selectors are made to compete on players sorted by surname
			class DealingInitializationVerifier<P extends Player> implements 
			BiConsumer<List<List<? extends StarterSelectorDelegate<P>>>, Set<P>> {
				
				@Override
				public void accept(List<List<? extends StarterSelectorDelegate<P>>> selectors, Set<P> options) {
					mockedGroup.verify(() -> CompetitiveOptionDealingGroup.initializeDealing(
							selectors.stream().flatMap(List::stream).collect(Collectors.toSet()),
							options.stream()
							.sorted(Comparator.comparing(Player::getSurname))
							.collect(Collectors.toList())));
				}        		
			}
			
			new DealingInitializationVerifier<Goalkeeper>().accept(
					List.of(List.of(starterGoalie), tripletGoalies), 
					team.extract().goalkeepers());
			new DealingInitializationVerifier<Defender>().accept(
					List.of(starterDefs, tripletDefs), 
					team.extract().defenders());
			new DealingInitializationVerifier<Midfielder>().accept(
					List.of(starterMids, tripletMids), 
					team.extract().midfielders());
			new DealingInitializationVerifier<Forward>().accept(
					List.of(starterForws, tripletForws), 
					team.extract().forwards());
			
			// AND triplets are told to initialize a sequence
			verify(goalieTriplet).initSequence();
			verify(defTriplet).initSequence();
			verify(midTriplet).initSequence();
			verify(forwTriplet).initSequence();
			
			// AND Starter Delegate is asked to swutch to the default scheme
			verify(starterChooser).switchToDefaultScheme();
		}
	}		
	
	@Nested
	@DisplayName("commands its Widget to")
	class ListenersOrderingWidgetTo {				

		@Nested
		@DisplayName("enable saving the LineUp")
		class EnableSaving {			
			
			@Nested
			@DisplayName("when a line-up choice emerges")
			class WhenChoiceEmerges {

				/**
				 * TEST ISOLATION
				 * these tests make the following assumptions about the SUT:
				 * 
				 * 	1. upon instantiation
				 * 		i.  it attaches Listeners to starter goalie & all substitute Selectors
				 * 		ii. it sets Consumers into the Starter Delegate for the latter 
				 * 			to process Selectors entering the current scheme
				 * 	
				 * 	2. said Consumers attach Listeners to Selectors
				 */
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
							
							@Test
							@DisplayName("goalkeeper")
							public void StarterGoalkeeper(@Mock Selector<Goalkeeper> dummySelector) {
								verify(starterGoalie).attachListener(starterGoalieListener.capture());
								
								// GIVEN all other Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								chooser.hasStarterGoalieChoice.flag = false;
								
								// AND starter Goalie selector reports being non-empty
								when(starterGoalie.getSelection()).thenReturn(Optional.of(FAKE_GOALIE));
								
								// WHEN the Listener is triggered
								starterGoalieListener.getValue().selectionMadeOn(dummySelector);
								
								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							
							@Captor ArgumentCaptor<Consumer<Selector<Defender>>> defConsumer;
							@Captor ArgumentCaptor<SelectorListener<Defender>> starterDefListener;
							
							@Test
							@DisplayName("defenders")
							public void StarterDefenders(@Mock Selector<Defender> dummySelector) {
								verify(starterChooser).setEntryDefConsumer(defConsumer.capture());
								defConsumer.getValue().accept(dummySelector);
								verify(dummySelector).attachListener(starterDefListener.capture());
								
								// GIVEN all other Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								chooser.hasStarterDefChoice.flag = false;
								
								// AND Starter Delegate reports those in the scheme as current starter selectors
								when(starterChooser.getCurrentDefSelectors()).thenReturn(
										starterDefs.stream().filter(selsInCurrentScheme::contains).collect(toSet()));
								// AND selectors in the scheme report being non-empty
								starterDefs.stream().filter(selsInCurrentScheme::contains).forEach(
										selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));
								
								// WHEN the Listener is triggered
								starterDefListener.getValue().selectionMadeOn(dummySelector);
								
								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							

							@Captor ArgumentCaptor<Consumer<Selector<Midfielder>>> midConsumer;
							@Captor ArgumentCaptor<SelectorListener<Midfielder>> starterMidListener;

							@Test
							@DisplayName("midfielders")
							public void StarterMidfielders(@Mock Selector<Midfielder> dummySelector) {
								verify(starterChooser).setEntryMidConsumer(midConsumer.capture());
								midConsumer.getValue().accept(dummySelector);
								verify(dummySelector).attachListener(starterMidListener.capture());

								// GIVEN all other Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								chooser.hasStarterMidChoice.flag = false;

								// AND Starter Delegate reports those in the scheme as current starter selectors
								Set<Selector<Midfielder>> currentMids = starterMids.stream()
										.filter(selsInCurrentScheme::contains).collect(toSet());
								when(starterChooser.getCurrentMidSelectors()).thenReturn(currentMids);
								// AND selectors in the scheme report being non-empty
								starterMids.stream().filter(selsInCurrentScheme::contains)
										.forEach(selector -> when(selector.getSelection())
												.thenReturn(Optional.of(FAKE_MIDFIELDER)));

								// WHEN the Listener is triggered
								starterMidListener.getValue().selectionMadeOn(dummySelector);

								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							
							@Captor ArgumentCaptor<Consumer<Selector<Forward>>> forwConsumer;
							@Captor ArgumentCaptor<SelectorListener<Forward>> starterForwListener;

							@Test
							@DisplayName("forwards")
							public void StarterForwards(@Mock Selector<Forward> dummySelector) {
								verify(starterChooser).setEntryForwConsumer(forwConsumer.capture());
								forwConsumer.getValue().accept(dummySelector);
								verify(dummySelector).attachListener(starterForwListener.capture());

								// GIVEN all other Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								chooser.hasStarterForwChoice.flag = false;

								// AND Starter Delegate reports those in the scheme as current starter selectors
								Set<Selector<Forward>> currentForws = starterForws.stream()
										.filter(selsInCurrentScheme::contains).collect(toSet());
								when(starterChooser.getCurrentForwSelectors()).thenReturn(currentForws);
								// AND selectors in the scheme report being non-empty
								currentForws.stream().forEach(selector -> when(selector.getSelection())
										.thenReturn(Optional.of(FAKE_FORWARD)));

								// WHEN the Listener is triggered
								starterForwListener.getValue().selectionMadeOn(dummySelector);

								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
						}
						
						@Nested
						@DisplayName("substitute")
						class Sustitute {							
							
							@Captor ArgumentCaptor<SelectorListener<Goalkeeper>> tripletGoalieListener;
							
							@Test
							@DisplayName("goalkeepers")
							public void SubstituteGoalkeepers(@Mock Selector<Goalkeeper> dummySelector) {
								tripletGoalies.forEach(sel -> verify(sel).attachListener(tripletGoalieListener.capture()));
								
								// GIVEN all other Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								chooser.hasSubsGoaliesChoice.flag = false;
								
								// AND all triplet selectors report being non-empty
								tripletGoalies.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_GOALIE)));
								
								// WHEN the Listener is triggered
								tripletGoalieListener.getValue().selectionMadeOn(dummySelector);
								
								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							
							@Captor ArgumentCaptor<SelectorListener<Defender>> tripletDefListener;
							
							@Test
							@DisplayName("defenders")
							public void SubstituteDefenders(@Mock Selector<Defender> dummySelector) {
								tripletDefs.forEach(sel -> verify(sel).attachListener(tripletDefListener.capture()));
								
								// GIVEN all other Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								chooser.hasSubsDefsChoice.flag = false;

								// AND all triplet selectors report being non-empty
								tripletDefs.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));

								// WHEN the Listener is triggered
								tripletDefListener.getValue().selectionMadeOn(dummySelector);

								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							
							@Captor ArgumentCaptor<SelectorListener<Midfielder>> tripletMidListener;
							
							@Test
							@DisplayName("midfielders")
							public void SubstituteMidfielders(@Mock Selector<Midfielder> dummySelector) {
								tripletMids.forEach(sel -> verify(sel).attachListener(tripletMidListener.capture()));
								
								// GIVEN all other Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								chooser.hasSubsMidsChoice.flag = false;

								// AND all triplet selectors report being non-empty
								tripletMids.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));

								// WHEN the Listener is triggered
								tripletMidListener.getValue().selectionMadeOn(dummySelector);

								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
							
							@Captor ArgumentCaptor<SelectorListener<Forward>> tripletForwListener;
							
							@Test
							@DisplayName("forwards")
							public void SubstituteForwards(@Mock Selector<Forward> dummySelector) {
								tripletForws.forEach(sel -> verify(sel).attachListener(tripletForwListener.capture()));
								
								// GIVEN all other Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								chooser.hasSubsForwsChoice.flag = false;

								// AND all triplet selectors report being non-empty
								tripletForws.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));

								// WHEN the Listener is triggered
								tripletForwListener.getValue().selectionMadeOn(dummySelector);

								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).enableSavingLineUp();
							}
						}
					}					
				}
			}			
		}
		
		@Nested
		@DisplayName("disable saving the LineUp")
		class DisableSaving {
			
			@Nested
			@DisplayName("when a line-up choice is lost")
			class WhenChoiceIsLost {

				/**
				 * TEST ISOLATION
				 * these tests make the following assumptions about the SUT:
				 * 
				 * 	1. upon instantiation
				 * 		i.  it attaches Listeners to starter goalie & all substitute Selectors
				 * 		ii. it sets Consumers into the Starter Delegate for the latter 
				 * 			to process Selectors entering the current scheme
				 * 	
				 * 	2. said Consumers attach Listeners to Selectors
				 */
				@Nested
				@DisplayName("under the current scheme")
				class UnderCurrentScheme {
					
					@Nested
					@DisplayName("owing to the choice for")
					class ThanksToChoiceFor {
						
						@Nested
						@DisplayName("starter")
						class Starter {
							
							@Captor ArgumentCaptor<SelectorListener<Goalkeeper>> starterGoalieListener;
							
							@Test
							@DisplayName("goalkeeper")
							public void StarterGoalkeeper(@Mock Selector<Goalkeeper> dummySelector) {
								verify(starterGoalie).attachListener(starterGoalieListener.capture());
								
								// GIVEN all Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								
								// AND starter Goalie selector reports being empty
								when(starterGoalie.getSelection()).thenReturn(Optional.empty());
								
								// WHEN the Listener is triggered
								starterGoalieListener.getValue().selectionClearedOn(dummySelector);
								
								// THEN the Widget is commanded to disable line-up saving
								verify(mockWidget).disableSavingLineUp();
							}
							
							@Captor ArgumentCaptor<Consumer<Selector<Defender>>> defConsumer;
							@Captor ArgumentCaptor<SelectorListener<Defender>> starterDefListener;
							
							@Test
							@DisplayName("defenders")
							public void StarterDefenders(@Mock Selector<Defender> dummySelector) {
								verify(starterChooser).setEntryDefConsumer(defConsumer.capture());
								defConsumer.getValue().accept(dummySelector);
								verify(dummySelector).attachListener(starterDefListener.capture());

								// GIVEN all Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);

								// AND Starter Delegate reports those in the scheme as current starter selectors
								Set<Selector<Defender>> currentDefs = starterDefs.stream()
										.filter(selsInCurrentScheme::contains).collect(toSet());
								when(starterChooser.getCurrentDefSelectors()).thenReturn(currentDefs);
								// AND one selector in the scheme reports being empty
								when(currentDefs.stream().findFirst().get().getSelection()).thenReturn(Optional.empty());

								// WHEN the Listener is triggered
								starterDefListener.getValue().selectionClearedOn(dummySelector);

								// THEN the Widget is commanded to disable line-up saving
								verify(mockWidget).disableSavingLineUp();
							}
							
							@Captor ArgumentCaptor<Consumer<Selector<Midfielder>>> midConsumer;
							@Captor ArgumentCaptor<SelectorListener<Midfielder>> starterMidListener;

							@Test
							@DisplayName("midfielders")
							public void StarterMidfielders(@Mock Selector<Midfielder> dummySelector) {
								verify(starterChooser).setEntryMidConsumer(midConsumer.capture());
								midConsumer.getValue().accept(dummySelector);
								verify(dummySelector).attachListener(starterMidListener.capture());

								// GIVEN all Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);

								// AND Starter Delegate reports those in the scheme as current starter selectors
								Set<Selector<Midfielder>> currentMids = starterMids.stream()
										.filter(selsInCurrentScheme::contains).collect(toSet());
								when(starterChooser.getCurrentMidSelectors()).thenReturn(currentMids);
								// AND one selector in the scheme reports being empty
								when(currentMids.stream().findFirst().get().getSelection())
										.thenReturn(Optional.empty());

								// WHEN the Listener is triggered
								starterMidListener.getValue().selectionClearedOn(dummySelector);

								// THEN the Widget is commanded to disable line-up saving
								verify(mockWidget).disableSavingLineUp();
							}
							
							@Captor ArgumentCaptor<Consumer<Selector<Forward>>> forwConsumer;
							@Captor ArgumentCaptor<SelectorListener<Forward>> starterForwListener;

							@Test
							@DisplayName("forwards")
							public void StarterForwards(@Mock Selector<Forward> dummySelector) {
								verify(starterChooser).setEntryForwConsumer(forwConsumer.capture());
								forwConsumer.getValue().accept(dummySelector);
								verify(dummySelector).attachListener(starterForwListener.capture());

								// GIVEN all Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);

								// AND Starter Delegate reports those in the scheme as current starter selectors
								Set<Selector<Forward>> currentForws = starterForws.stream()
										.filter(selsInCurrentScheme::contains).collect(toSet());
								when(starterChooser.getCurrentForwSelectors()).thenReturn(currentForws);
								// AND one selector in the scheme reports being empty
								when(currentForws.stream().findFirst().get().getSelection())
										.thenReturn(Optional.empty());

								// WHEN the Listener is triggered
								starterForwListener.getValue().selectionClearedOn(dummySelector);

								// THEN the Widget is commanded to disable line-up saving
								verify(mockWidget).disableSavingLineUp();
							}
						}
						
						@Nested
						@DisplayName("substitute")
						class Sustitute {							
							
							@Captor ArgumentCaptor<SelectorListener<Goalkeeper>> tripletGoalieListener;
							
							@Test
							@DisplayName("goalkeepers")
							public void SubstituteGoalkeepers(@Mock Selector<Goalkeeper> dummySelector) {
								tripletGoalies.forEach(sel -> verify(sel).attachListener(tripletGoalieListener.capture()));
								
								// GIVEN all Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								
								// AND one triplet selector reports being empty
								when(tripletGoalies.get(0).getSelection()).thenReturn(Optional.empty());
								
								// WHEN the Listener is triggered
								tripletGoalieListener.getValue().selectionClearedOn(dummySelector);
								
								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).disableSavingLineUp();
							}
							
							@Captor ArgumentCaptor<SelectorListener<Defender>> tripletDefListener;
							
							@Test
							@DisplayName("defenders")
							public void SubstituteDefenders(@Mock Selector<Defender> dummySelector) {
								tripletDefs.forEach(sel -> verify(sel).attachListener(tripletDefListener.capture()));
								
								// GIVEN all Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								
								// AND one triplet selector reports being empty
								when(tripletDefs.get(0).getSelection()).thenReturn(Optional.empty());
								
								// WHEN the Listener is triggered
								tripletDefListener.getValue().selectionClearedOn(dummySelector);
								
								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).disableSavingLineUp();
							}
							
							@Captor ArgumentCaptor<SelectorListener<Midfielder>> tripletMidListener;
							
							@Test
							@DisplayName("goalkeepers")
							public void SubstituteMidfielders(@Mock Selector<Midfielder> dummySelector) {
								tripletMids.forEach(sel -> verify(sel).attachListener(tripletMidListener.capture()));
								
								// GIVEN all Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								
								// AND one triplet selector reports being empty
								when(tripletMids.get(0).getSelection()).thenReturn(Optional.empty());
								
								// WHEN the Listener is triggered
								tripletMidListener.getValue().selectionClearedOn(dummySelector);
								
								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).disableSavingLineUp();
							}
							
							@Captor ArgumentCaptor<SelectorListener<Forward>> tripletForwListener;
							
							@Test
							@DisplayName("goalkeepers")
							public void SubstituteForwards(@Mock Selector<Forward> dummySelector) {
								tripletForws.forEach(sel -> verify(sel).attachListener(tripletForwListener.capture()));
								
								// GIVEN all Listeners have registered a choice
								affirmAllChoiceFlagsIn(chooser);
								
								// AND one triplet selector reports being empty
								when(tripletForws.get(0).getSelection()).thenReturn(Optional.empty());
								
								// WHEN the Listener is triggered
								tripletForwListener.getValue().selectionClearedOn(dummySelector);
								
								// THEN the Widget is commanded to enable line-up saving
								verify(mockWidget).disableSavingLineUp();
							}
						}
					}					
				}
			}			
		}
	}
	
	@Nested
	@DisplayName("handles a save request from the Widget")
	class AsStarterLineUpChooserController {
		
		@Captor ArgumentCaptor<LineUp> lineUp;
		
		/**
		 * note: the constructed LineUp is NOT fully valid
		 * 		- it doesn't have duplicates in the same role and fielding
		 * 		- (!) it does have duplicates across fieldings for the same role
		 * 		- (!) players have no association to the Team
		 */
		@Test
		@DisplayName("when a choice exists")
		public void WhenChoiceExists() {
			
			// GIVEN the SUT's internal bookkeeping registers a choice
			affirmAllChoiceFlagsIn(chooser);
			
			// GIVEN the SUT has been initialized with a Team and a Match
			FantaTeam team = new FantaTeam("Dream Team", null, 30, null, new HashSet<>());			
			Match match = new Match(
					new MatchDaySerieA("Matchday 1", LocalDate.of(2025, 6, 19)), 
					team, 
					new FantaTeam("Challengers", null, 25, null, new HashSet<>()));
			
			chooser.team = team;
			chooser.match = match;
			
			// AND the Starter Delegate reports a current StarterLineUp
			when(starterChooser.getCurrentStarterLineUp()).thenReturn(
					Scheme433.starterLineUp()
					.withGoalkeeper(new Goalkeeper("goalkeeper", "1"))
					.withDefenders(
							new Defender("defender", "1"), 
							new Defender("defender", "2"),
							new Defender("defender", "3"),
							new Defender("defender", "4"))
					.withMidfielders(
							new Midfielder("midfielder", "1"), 
							new Midfielder("midfielder", "2"),
							new Midfielder("midfielder", "3"))
					.withForwards(
							new Forward("forward", "1"), 
							new Forward("forward", "2"),
							new Forward("forward", "3")));
			
			// AND triplet selectors report substitute lineups
			IntStream.range(0, tripletDefs.size()).forEach(i -> {
				when(tripletGoalies.get(i).getSelection()).thenReturn(
						Optional.of(new Goalkeeper("goalkeeper", "" + (i + 1))));
				when(tripletDefs.get(i).getSelection()).thenReturn(
						Optional.of(new Defender("defender", "" + (i + 1))));
				when(tripletMids.get(i).getSelection()).thenReturn(
						Optional.of(new Midfielder("midfielder", "" + (i + 1))));
				when(tripletForws.get(i).getSelection()).thenReturn(
						Optional.of(new Forward("forward", "" + (i + 1))));
			});
			
			// WHEN the Widget sends a "save LineUp" request
			chooser.saveLineUp();
			
			// THEN the Starter Delegate is requested the current StarterLineUp choice
			verify(starterChooser).getCurrentStarterLineUp();
			
			// AND triplet Selectors are queried
			Stream.of(tripletGoalies, tripletDefs, tripletMids, tripletForws).flatMap(List::stream)
					.forEach(selector -> verify(selector).getSelection());
			
			// AND the Service is requested to save a LineUp instance
			verify(mockService).saveLineUp(lineUp.capture());
			
			// AND the SUT constructed the LineUp correctly
			LineUp constructedLineUp = lineUp.getValue();
			assertThat(constructedLineUp.getTeam()).isEqualTo(team);
			assertThat(constructedLineUp.getMatch()).isEqualTo(match);
			assertThat(constructedLineUp.getScheme()).isEqualTo(Scheme433.INSTANCE);
			assertThat(constructedLineUp.extract().starterGoalkeepers())
					.containsExactly(new Goalkeeper("goalkeeper", "1"));
			assertThat(constructedLineUp.extract().starterDefenders()).containsExactlyInAnyOrder(
					new Defender("defender", "1"), 
					new Defender("defender", "2"),
					new Defender("defender", "3"),
					new Defender("defender", "4"));
			assertThat(constructedLineUp.extract().starterMidfielders()).containsExactlyInAnyOrder(
					new Midfielder("midfielder", "1"), 
					new Midfielder("midfielder", "2"),
					new Midfielder("midfielder", "3"));
			assertThat(constructedLineUp.extract().starterForwards()).containsExactlyInAnyOrder(
					new Forward("forward", "1"), 
					new Forward("forward", "2"),
					new Forward("forward", "3"));
			assertThat(constructedLineUp.extract().substituteGoalkeepers())
					.containsExactly(
							new Goalkeeper("goalkeeper", "1"), 
							new Goalkeeper("goalkeeper", "2"),
							new Goalkeeper("goalkeeper", "3"));
			assertThat(constructedLineUp.extract().substituteDefenders()).containsExactly(
					new Defender("defender", "1"), 
					new Defender("defender", "2"),
					new Defender("defender", "3"));
			assertThat(constructedLineUp.extract().substituteMidfielders()).containsExactly(
					new Midfielder("midfielder", "1"), 
					new Midfielder("midfielder", "2"),
					new Midfielder("midfielder", "3"));
			assertThat(constructedLineUp.extract().substituteForwards()).containsExactly(
					new Forward("forward", "1"), 
					new Forward("forward", "2"),
					new Forward("forward", "3"));		
		}
		
		@Test
		@DisplayName("when a choice does not exist")
		public void WhenChoiceDoesNotExist() {
			
			// GIVEN the SUT's internal bookkeeping registers no choice
			affirmAllChoiceFlagsIn(chooser);
			chooser.hasStarterGoalieChoice.flag = false;
			
			// WHEN the Widget sends a "save LineUp" request
			ThrowingCallable shouldThrow = () -> chooser.saveLineUp();			
			
			// THEN an exception is thrown
			assertThatThrownBy(shouldThrow)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("no choice of LineUp is present on this Controller");

			// AND Delegates are not even contacted
			verifyNoMoreInteractions(starterChooser, goalieTriplet, defTriplet, midTriplet, forwTriplet);			
			
			// AND no request is sent to the Service
			verifyNoInteractions(mockService);
		}		
	}

	@Nested
	@DisplayName("supervises scheme changes")
	class ConsumersTaclkingSchemeChanges {

		/**
		 * TEST ISOLATION 
		 * these tests make the following assumptions about the SUT:
		 * 
		 * 	1. upon instantiation, it sets Consumers into the Starter Delegate 
		 * 	   for the latter to process Selectors 
		 * 		i.  exiting the current scheme 
		 * 		ii. entering the current scheme
		 */
		@Nested
		@DisplayName("ensuring Listeners only monitor the current scheme")
		class AttachingRemovingListeners {
			
			@Nested
			@DisplayName("in the case of")
			class ForGroup {

				@Captor	ArgumentCaptor<Consumer<Selector<Defender>>> entryDefConsumer;
				@Captor	ArgumentCaptor<Consumer<Selector<Defender>>> exitDefConsumer;
				@Captor	ArgumentCaptor<SelectorListener<Defender>> defListener;

				@Test
				@DisplayName("defenders")
				public void AffirmsDefenderChoice(@Mock Selector<Defender> dummySelector) {
					verify(starterChooser).setEntryDefConsumer(entryDefConsumer.capture());
					verify(starterChooser).setExitDefConsumer(exitDefConsumer.capture());
					
					// WHEN the entry Consumer is made to process a Selector
					entryDefConsumer.getValue().accept(dummySelector);
					
					// THEN a Listener is attached to that Selector 
					verify(dummySelector).attachListener(defListener.capture());
					
					// AND WHEN the exit Consumer is made to process the same Selector
					exitDefConsumer.getValue().accept(dummySelector);
					
					// THEN the previously attached Listener is removed from the Selector
					verify(dummySelector).removeListener(defListener.getValue());
				}				

				@Captor ArgumentCaptor<Consumer<Selector<Midfielder>>> entryMidConsumer;
				@Captor ArgumentCaptor<Consumer<Selector<Midfielder>>> exitMidConsumer;
				@Captor ArgumentCaptor<SelectorListener<Midfielder>> midListener;

				@Test
				@DisplayName("midfielders")
				public void AffirmsMidfielderChoice(@Mock Selector<Midfielder> dummySelector) {
					verify(starterChooser).setEntryMidConsumer(entryMidConsumer.capture());
					verify(starterChooser).setExitMidConsumer(exitMidConsumer.capture());

					// WHEN the entry Consumer is made to process a Selector
					entryMidConsumer.getValue().accept(dummySelector);

					// THEN a Listener is attached to that Selector
					verify(dummySelector).attachListener(midListener.capture());

					// AND WHEN the exit Consumer is made to process the same Selector
					exitMidConsumer.getValue().accept(dummySelector);

					// THEN the previously attached Listener is removed from the Selector
					verify(dummySelector).removeListener(midListener.getValue());
				}

				@Captor	ArgumentCaptor<Consumer<Selector<Forward>>> entryForwConsumer;
				@Captor	ArgumentCaptor<Consumer<Selector<Forward>>> exitForwConsumer;
				@Captor	ArgumentCaptor<SelectorListener<Forward>> forwListener;

				@Test
				@DisplayName("forwards")
				public void AffirmsForwardChoice(@Mock Selector<Forward> dummySelector) {
					verify(starterChooser).setEntryForwConsumer(entryForwConsumer.capture());
					verify(starterChooser).setExitForwConsumer(exitForwConsumer.capture());
					
					// WHEN the entry Consumer is made to process a Selector
					entryForwConsumer.getValue().accept(dummySelector);
					
					// THEN a Listener is attached to that Selector 
					verify(dummySelector).attachListener(forwListener.capture());
					
					// AND WHEN the exit Consumer is made to process the same Selector
					exitForwConsumer.getValue().accept(dummySelector);
					
					// THEN the previously attached Listener is removed from the Selector
					verify(dummySelector).removeListener(forwListener.getValue());
				}
			}			
		}

		@Nested
		@DisplayName("keeping group choice bookkeeping correct")
		class KeepingGroupChoicesCorrect {

			/**
			 * TEST ISOLATION 
			 * these tests make the following assumptions about the SUT:
			 * 
			 * 	1. upon instantiation, it sets Consumers into the Starter Delegate 
			 * 	   for the latter to process Selectors 
			 * 		i.  exiting the current scheme 
			 * 		ii. entering the current scheme
			 */
			@Nested
			@DisplayName("when group shrinks")
			class WhenGroupShrinks {

				@Nested
				@DisplayName("and remaining Selectors")
				class AndRemainingSelectors {
					
					@Nested
					@DisplayName("produce a choice")
					class ProduceAChoice {

						@Nested
						@DisplayName("in the case of")
						class ForGroup {

							@Captor	ArgumentCaptor<Consumer<Selector<Defender>>> exitDefConsumer;

							@Test
							@DisplayName("defenders")
							public void AffirmsDefenderChoice(@Mock Selector<Defender> dummySelector) {
								verify(starterChooser).setExitDefConsumer(exitDefConsumer.capture());

								// GIVEN Starter Delegate reports the new scheme as current at Consumer
								// execution
								Set<Selector<Defender>> currentDefs = starterDefs.stream()
										.filter(selsInCurrentScheme::contains).collect(toSet());
								when(starterChooser.getCurrentDefSelectors()).thenReturn(currentDefs);
								// AND those Selectors report being non-empty
								currentDefs.forEach(selector -> when(selector.getSelection())
										.thenReturn(Optional.of(FAKE_DEFENDER)));

								// WHEN the exit Consumer is made to process a Selector
								// (regardless of the Selector's state)
								exitDefConsumer.getValue().accept(dummySelector);

								// THEN the corresponding choice flag is affirmed
								assertThat(chooser.hasStarterDefChoice.flag).isTrue();
							}
							
							@Captor	ArgumentCaptor<Consumer<Selector<Midfielder>>> exitMidConsumer;

							@Test
							@DisplayName("midfielders")
							public void AffirmsMidfielderChoice(@Mock Selector<Midfielder> dummySelector) {
								verify(starterChooser).setExitMidConsumer(exitMidConsumer.capture());

								// GIVEN Starter Delegate reports the new scheme as current at Consumer
								// execution
								Set<Selector<Midfielder>> currentMids = starterMids.stream()
										.filter(selsInCurrentScheme::contains).collect(toSet());
								when(starterChooser.getCurrentMidSelectors()).thenReturn(currentMids);
								// AND those Selectors report being non-empty
								currentMids.stream().forEach(selector -> when(selector.getSelection())
										.thenReturn(Optional.of(FAKE_MIDFIELDER)));

								// WHEN the exit Consumer is made to process a Selector
								// (regardless of the Selector's state)
								exitMidConsumer.getValue().accept(dummySelector);

								// THEN the corresponding choice flag is affirmed
								assertThat(chooser.hasStarterMidChoice.flag).isTrue();
							}

							@Captor	ArgumentCaptor<Consumer<Selector<Forward>>> exitForwConsumer;

							@Test
							@DisplayName("forwards")
							public void AffirmsForwardChoice(@Mock Selector<Forward> dummySelector) {
								verify(starterChooser).setExitForwConsumer(exitForwConsumer.capture());

								// GIVEN Starter Delegate reports the new scheme as current at Consumer
								// execution
								Set<Selector<Forward>> currentForws = starterForws.stream()
										.filter(selsInCurrentScheme::contains).collect(toSet());
								when(starterChooser.getCurrentForwSelectors()).thenReturn(currentForws);
								// AND those Selectors report being non-empty
								currentForws.stream().forEach(selector -> when(selector.getSelection())
										.thenReturn(Optional.of(FAKE_FORWARD)));

								// WHEN the exit Consumer is made to process a Selector
								// (regardless of the Selector's state)
								exitForwConsumer.getValue().accept(dummySelector);

								// THEN the corresponding choice flag is affirmed
								assertThat(chooser.hasStarterForwChoice.flag).isTrue();
							}
						}
					}

					@Nested
					@DisplayName("do not produce a choice")
					class DoNotProduceAChoice {

						@Nested
						@DisplayName("in the case of")
						class ForGroup {

							@Captor	ArgumentCaptor<Consumer<Selector<Defender>>> exitDefConsumer;

							@Test
							@DisplayName("defenders")
							public void LeavesDefenderChoiceAbsent(@Mock Selector<Defender> dummySelector) {
								verify(starterChooser).setExitDefConsumer(exitDefConsumer.capture());
								chooser.hasStarterDefChoice.flag = true;

								// GIVEN Starter Delegate reports the new scheme as current at Consumer
								// execution
								Set<Selector<Defender>> currentDefs = starterDefs.stream()
										.filter(selsInCurrentScheme::contains).collect(toSet());
								when(starterChooser.getCurrentDefSelectors()).thenReturn(currentDefs);
								// AND one of those Selectors reports being empty
								when(currentDefs.stream().findFirst().get().getSelection()).thenReturn(Optional.empty());

								// WHEN the exit Consumer is made to process a Selector
								// (regardless of the Selector's state)
								exitDefConsumer.getValue().accept(dummySelector);

								// THEN the corresponding choice flag is negated
								assertThat(chooser.hasStarterDefChoice.flag).isFalse();
							}
							
							@Captor	ArgumentCaptor<Consumer<Selector<Midfielder>>> exitMidConsumer;

							@Test
							@DisplayName("midfielders")
							public void LeavesMidfielderChoiceAbsent(@Mock Selector<Midfielder> dummySelector) {
								verify(starterChooser).setExitMidConsumer(exitMidConsumer.capture());
								chooser.hasStarterMidChoice.flag = true;

								// GIVEN Starter Delegate reports the new scheme as current at Consumer
								// execution
								Set<Selector<Midfielder>> currentMids = starterMids.stream()
										.filter(selsInCurrentScheme::contains).collect(toSet());
								when(starterChooser.getCurrentMidSelectors()).thenReturn(currentMids);
								// AND one of those Selectors reports being empty
								when(currentMids.stream().findFirst().get().getSelection())
										.thenReturn(Optional.empty());

								// WHEN the exit Consumer is made to process a Selector
								// (regardless of the Selector's state)
								exitMidConsumer.getValue().accept(dummySelector);

								// THEN the corresponding choice flag is negated
								assertThat(chooser.hasStarterMidChoice.flag).isFalse();
							}

							@Captor	ArgumentCaptor<Consumer<Selector<Forward>>> exitForwConsumer;

							@Test
							@DisplayName("forwards")
							public void LeavesForwardChoiceAbsent(@Mock Selector<Forward> dummySelector) {
								verify(starterChooser).setExitForwConsumer(exitForwConsumer.capture());
								chooser.hasStarterForwChoice.flag = true;

								// GIVEN Starter Delegate reports the new scheme as current at Consumer
								// execution
								Set<Selector<Forward>> currentForws = starterForws.stream()
										.filter(selsInCurrentScheme::contains).collect(toSet());
								when(starterChooser.getCurrentForwSelectors()).thenReturn(currentForws);
								// AND one of those Selectors reports being empty
								when(currentForws.stream().findFirst().get().getSelection())
										.thenReturn(Optional.empty());

								// WHEN the exit Consumer is made to process a Selector
								// (regardless of the Selector's state)
								exitForwConsumer.getValue().accept(dummySelector);

								// THEN the corresponding choice flag is negated
								assertThat(chooser.hasStarterForwChoice.flag).isFalse();
							}
						}
					}
				}
			}

			/**
			 * TEST ISOLATION 
			 * these tests make the following assumptions about the SUT:
			 * 
			 * 	1. upon instantiation, it sets Consumers into the Starter Delegate 
			 * 	   for the latter to process Selectors 
			 * 		i.  exiting the current scheme 
			 * 		ii. entering the current scheme
			 * 
			 * 	2. said Consumers enforce the invariant that a Selector entering
			 * 	   the current scheme is always empty
			 */
			@Nested
			@DisplayName("when group expands")
			class WhenGroupExpands {

				@Nested
				@DisplayName("in the case of")
				class ForGroup {

					@Captor	ArgumentCaptor<Consumer<Selector<Defender>>> entryDefConsumer;

					@Test
					@DisplayName("defenders")
					public void LeavesDefenderChoiceAbsent(@Mock Selector<Defender> dummySelector) {
						verify(starterChooser).setEntryDefConsumer(entryDefConsumer.capture());
						chooser.hasStarterDefChoice.flag = true;
						
						// WHEN the entry Consumer is made to process a Selector
						// (regardless of the Selector's state) 
						entryDefConsumer.getValue().accept(dummySelector);
						
						// THEN the corresponding choice flag is negated
						assertThat(chooser.hasStarterDefChoice.flag).isFalse();
					}
					
					@Captor	ArgumentCaptor<Consumer<Selector<Midfielder>>> entryMidConsumer;

					@Test
					@DisplayName("midfielders")
					public void LeavesMidfielderChoiceAbsent(@Mock Selector<Midfielder> dummySelector) {
						verify(starterChooser).setEntryMidConsumer(entryMidConsumer.capture());
						chooser.hasStarterMidChoice.flag = true;
						
						// WHEN the entry Consumer is made to process a Selector
						// (regardless of the Selector's state) 
						entryMidConsumer.getValue().accept(dummySelector);
						
						// THEN the corresponding choice flag is negated
						assertThat(chooser.hasStarterMidChoice.flag).isFalse();
					}

					@Captor	ArgumentCaptor<Consumer<Selector<Forward>>> entryForwConsumer;

					@Test
					@DisplayName("forwards")
					public void LeavesForwardChoiceAbsent(@Mock Selector<Forward> dummySelector) {
						verify(starterChooser).setEntryForwConsumer(entryForwConsumer.capture());
						chooser.hasStarterForwChoice.flag = true;
						
						// WHEN the entry Consumer is made to process a Selector
						// (regardless of the Selector's state) 
						entryForwConsumer.getValue().accept(dummySelector);
						
						// THEN the corresponding choice flag is negated
						assertThat(chooser.hasStarterForwChoice.flag).isFalse();
					}
				}
			}
		}
	
		/**
		 * TEST ISOLATION 
		 * these tests make the following assumptions about the SUT:
		 * 
		 * 	1. upon instantiation, it sets Consumers into the Starter Delegate 
		 * 	   for the latter to process Selectors exiting the current scheme
		 */
		@Nested
		@DisplayName("salvaging selections that would be lost")
		class SalvagingSelections {
			
			@Nested
			@DisplayName("in the case of")
			class ForGroup {

				@Captor	ArgumentCaptor<Consumer<Selector<Defender>>> exitDefConsumer;

				@Test
				@DisplayName("defenders")
				public void AffirmsDefenderChoice(@Mock Selector<Defender> exitingSelector,
						@Mock Selector<Defender> substituteSelector) {
					verify(starterChooser).setExitDefConsumer(exitDefConsumer.capture());

					// GIVEN the triplet reports a Selector as the next fillable
					when(defTriplet.getNextFillableSelector()).thenReturn(Optional.of(substituteSelector));

					// AND the exiting Selector reports being non-empty
					when(exitingSelector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER));

					// WHEN the exit Consumer is made to process the exiting Selector
					exitDefConsumer.getValue().accept(exitingSelector);

					// THEN the substitute Selector is given the exiting Selector's selection
					verify(substituteSelector).setSelection(Optional.of(FAKE_DEFENDER));
				}
				
				@Captor	ArgumentCaptor<Consumer<Selector<Midfielder>>> exitMidConsumer;

				@Test
				@DisplayName("midfielders")
				public void AffirmsMidfielderChoice(@Mock Selector<Midfielder> exitingSelector,
						@Mock Selector<Midfielder> substituteSelector) {
					verify(starterChooser).setExitMidConsumer(exitMidConsumer.capture());

					// GIVEN the triplet reports a Selector as the next fillable
					when(midTriplet.getNextFillableSelector()).thenReturn(Optional.of(substituteSelector));

					// AND the exiting Selector reports being non-empty
					when(exitingSelector.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER));

					// WHEN the exit Consumer is made to process the exiting Selector
					exitMidConsumer.getValue().accept(exitingSelector);

					// THEN the substitute Selector is given the exiting Selector's selection
					verify(substituteSelector).setSelection(Optional.of(FAKE_MIDFIELDER));
				}

				@Captor	ArgumentCaptor<Consumer<Selector<Forward>>> exitForwConsumer;

				@Test
				@DisplayName("forwards")
				public void AffirmsForwardChoice(@Mock Selector<Forward> exitingSelector,
						@Mock Selector<Forward> substituteSelector) {
					verify(starterChooser).setExitForwConsumer(exitForwConsumer.capture());

					// GIVEN the triplet reports a Selector as the next fillable
					when(forwTriplet.getNextFillableSelector()).thenReturn(Optional.of(substituteSelector));

					// AND the exiting Selector reports being non-empty
					when(exitingSelector.getSelection()).thenReturn(Optional.of(FAKE_FORWARD));

					// WHEN the exit Consumer is made to process the exiting Selector
					exitForwConsumer.getValue().accept(exitingSelector);

					// THEN the substitute Selector is given the exiting Selector's selection
					verify(substituteSelector).setSelection(Optional.of(FAKE_FORWARD));
				}
			}
		}
		
		/**
		 * TEST ISOLATION 
		 * these tests make the following assumptions about the SUT:
		 * 
		 * 	1. upon instantiation, it sets Consumers into the Starter Delegate 
		 * 	   for the latter to process Selectors exiting the current scheme
		 */
		@Nested
		@DisplayName("emptying Selectors leaving the current scheme")
		class EmptiesExitingSelectors {
			
			@Nested
			@DisplayName("in the case of")
			class ForGroup {

				@Captor	ArgumentCaptor<Consumer<Selector<Defender>>> exitDefConsumer;

				@Test
				@DisplayName("defenders")
				public void AffirmsDefenderChoice(@Mock Selector<Defender> exitingSelector) {
					verify(starterChooser).setExitDefConsumer(exitDefConsumer.capture());

					// GIVEN the exiting Selector reports being non-empty
					when(exitingSelector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER));

					// WHEN the exit Consumer is made to process the exiting Selector
					exitDefConsumer.getValue().accept(exitingSelector);

					// THEN the exiting Selector is emptied
					verify(exitingSelector).setSelection(Optional.empty());
				}
				
				@Captor	ArgumentCaptor<Consumer<Selector<Midfielder>>> exitMidConsumer;

				@Test
				@DisplayName("midfielders")
				public void AffirmsMidfielderChoice(@Mock Selector<Midfielder> exitingSelector) {
					verify(starterChooser).setExitMidConsumer(exitMidConsumer.capture());

					// GIVEN the exiting Selector reports being non-empty
					when(exitingSelector.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER));

					// WHEN the exit Consumer is made to process the exiting Selector
					exitMidConsumer.getValue().accept(exitingSelector);
					
					// THEN the exiting Selector is emptied
					verify(exitingSelector).setSelection(Optional.empty());
				}

				@Captor	ArgumentCaptor<Consumer<Selector<Forward>>> exitForwConsumer;

				@Test
				@DisplayName("forwards")
				public void AffirmsForwardChoice(@Mock Selector<Forward> exitingSelector) {
					verify(starterChooser).setExitForwConsumer(exitForwConsumer.capture());

					// GIVEN the exiting Selector reports being non-empty
					when(exitingSelector.getSelection()).thenReturn(Optional.of(FAKE_FORWARD));

					// WHEN the exit Consumer is made to process the exiting Selector
					exitForwConsumer.getValue().accept(exitingSelector);

					// THEN the exiting Selector is emptied
					verify(exitingSelector).setSelection(Optional.empty());
				}
			}
		}
	}
}

