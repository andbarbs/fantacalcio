package swingViews;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.swing.annotation.GUITest;
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
	@DisplayName("as instantiated")
	class JustInstantiated {		
				
	}
	
	@Nested
	@DisplayName("upon notifications from its LineUpChooserWidget")
	class AsStarterLineUpChooserController {
		
	}
	
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
			
			@Nested
			@DisplayName("under")
			class Under {
				
				@Captor ArgumentCaptor<SelectorListener<Goalkeeper>> starterGoalieListener, tripletGoalieListener;
				@Captor ArgumentCaptor<SelectorListener<Defender>> starterDefListener, tripletDefListener;
				@Captor ArgumentCaptor<SelectorListener<Midfielder>> starterMidListener, tripletMidListener;				
				@Captor ArgumentCaptor<SelectorListener<Forward>> starterForwListener, tripletForwListener;				
				
				@BeforeEach
				void captureSelectorListeners() {
					verify(starterGoalie).attachListener(starterGoalieListener.capture());
					starterDefs.forEach(sel -> verify(sel).attachListener(starterDefListener.capture()));
					starterMids.forEach(sel -> verify(sel).attachListener(starterMidListener.capture()));
					starterForws.forEach(sel -> verify(sel).attachListener(starterForwListener.capture()));
					tripletGoalies.forEach(sel -> verify(sel).attachListener(tripletGoalieListener.capture()));
					tripletDefs.forEach(sel -> verify(sel).attachListener(tripletDefListener.capture()));
					tripletMids.forEach(sel -> verify(sel).attachListener(tripletMidListener.capture()));
					tripletForws.forEach(sel -> verify(sel).attachListener(tripletForwListener.capture()));
				}
				
				@Test
				@DisplayName("the current scheme")
				public void enableSavingUnder343() {
					
					// GIVEN all triplet selectors report being non-empty while listeners are notified
					tripletGoalies.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_GOALIE)));
					tripletGoalieListener.getValue().selectionMadeOn(tripletGoalie1);

					tripletDefs.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));
					tripletDefListener.getValue().selectionMadeOn(tripletDef1);

					tripletMids.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));
					tripletMidListener.getValue().selectionMadeOn(tripletMid1);

					tripletForws.forEach(sel -> when(sel.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));
					tripletForwListener.getValue().selectionMadeOn(tripletForw1);
					
					// AND Starter Delegate reports those in the scheme as current starter selectors
					when(starterChooser.getCurrentDefSelectors()).thenReturn(
							starterDefs.stream().filter(selsInCurrentScheme::contains).collect(Collectors.toList()));
					when(starterChooser.getCurrentMidSelectors()).thenReturn(
							starterMids.stream().filter(selsInCurrentScheme::contains).collect(Collectors.toList()));
					when(starterChooser.getCurrentForwSelectors()).thenReturn(
							starterForws.stream().filter(selsInCurrentScheme::contains).collect(Collectors.toList()));
					
					// AND all selectors in the scheme report being non-empty while listeners are notified
					starterGoalieListener.getValue().selectionMadeOn(starterGoalie);
					
					starterDefs.stream().filter(selsInCurrentScheme::contains).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));
					starterDefListener.getValue().selectionMadeOn(starterDef1);
					
					starterMids.stream().filter(selsInCurrentScheme::contains).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));
					starterMidListener.getValue().selectionMadeOn(starterMid1);
					
					starterForws.stream().filter(selsInCurrentScheme::contains).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));
					starterForwListener.getValue().selectionMadeOn(starterForw1);
					
					// THEN (and not before!) is the Widget commanded to enable line-up saving
					verify(mockWidget).enableSavingLineUp();
					verifyNoMoreInteractions(mockWidget);
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

