package swingViews;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domainModel.Player.Midfielder;
import swingViews.FillableSwappableSequence.FillableSwappableSequenceListener;
import swingViews.LineUpChooser.StarterSelectorDelegate;
import swingViews.LineUpChooser.SubstituteSelectorDelegate;
import swingViews.Selector.SelectorListener;
import swingViews.SubstitutePlayerSelector.SubstitutePlayerSelectorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static swingViews.OrderedDealerPresenter.NO_SELECTION;

@ExtendWith(MockitoExtension.class)
@DisplayName("A SubstitutePlayerSelector")
class SubstitutePlayerSelectorTest {
	
	private @Mock SubstitutePlayerSelectorView<Midfielder> view;	
	private @Mock CompetitiveOptionDealingGroup<StarterSelectorDelegate<Midfielder>, Midfielder> groupDriver;	
	private @Mock FillableSwappableSequence<SubstituteSelectorDelegate<Midfielder>> sequenceDriver;	
	
	// the SUT instance
	private @InjectMocks SubstitutePlayerSelector<Midfielder> presenter;

	// global option pool
	private static final List<Midfielder> INITIAL_OPTIONS = List.of(
			new Midfielder("Alpha", null), 
			new Midfielder("Beta", null), 
			new Midfielder("Gamma", null),
			new Midfielder("Delta", null));

	@Nested
	@DisplayName("as an member of an ordered dealer group")
	class AsAnOrderedOptionDealer {
		
		@BeforeEach
		void setup() {
			presenter.attachDriver(groupDriver);
			presenter.attachDriver(sequenceDriver);
			
			verify(sequenceDriver).attachListener(any());  // discounts this interaction
		}

		@Test
		@DisplayName("when attaching options")
		void attachOptions() {
			// WHEN attaching options
			presenter.attachOptions(INITIAL_OPTIONS);

			// THEN it should command the view to initialize its own options
			verify(view).initOptions(INITIAL_OPTIONS);

			// no interaction back to the drivers
			verifyNoMoreInteractions(groupDriver, sequenceDriver);
		}
		
		@Test
		@DisplayName("when retiring an option")
		void onRetireOption() {
			presenter.options = new ArrayList<>(INITIAL_OPTIONS);			
			presenter.mask = new ArrayList<>(List.of(0, 1, 3));  // current options "Alpha", "Beta", "Delta"

			// WHEN the driver retires "Delta" (absolute index 3)
			presenter.retireOption(3);

			// THEN the view is commanded to remove "Delta" (relative position 2)
			verify(view).removeOptionAt(2);

			// AND no feedback is sent back to the driver
			verifyNoMoreInteractions(groupDriver, sequenceDriver);
		}
		
		@Test
		@DisplayName("when restoring an option")
		void onRestoreOption() {
			presenter.options = new ArrayList<>(INITIAL_OPTIONS);			
			presenter.mask = new ArrayList<>(List.of(0, 3));   // current options "Alpha", "Delta"

			// WHEN the driver restores "Gamma" (absolute index 2)
			presenter.restoreOption(2);

			// THEN the view is commanded to insert "Gamma" (relative position 1)
			verify(view).insertOptionAt(new Midfielder("Gamma", null), 1);

			// AND no feedback is sent to the driver
			verifyNoMoreInteractions(groupDriver, sequenceDriver);
		}		
	}
	
	@Nested
	@DisplayName("as a member of a fillable-swappable sequence")
	class AsAFillableSwappable {
		
		@BeforeEach
		void setup() {
			presenter.options = new ArrayList<>(INITIAL_OPTIONS);
			presenter.attachDriver(groupDriver);
			presenter.attachDriver(sequenceDriver);
			
			verify(sequenceDriver).attachListener(any());  // discounts this interaction
		}

		@Nested
		@DisplayName("during a collapse operation")
		class OnCollapsingOperations {

			@Test
			@DisplayName("when being the next rightmost fillable")
			void onDiscardContent() {
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
				presenter.currentSelection = 3;					  // current selection is "Delta"
				
				// WHEN the presenter is asked to discard its content
				presenter.discardContent();
				
				// THEN the presenter clears selection and drops option "Delta"
				assertThat(presenter.currentSelection).isEqualTo(NO_SELECTION);
				assertThat(presenter.mask).containsExactly(0);
				// AND its View is asked to do the same
				InOrder inOrder = inOrder(view);
				inOrder.verify(view).selectOptionAt(-1);
				inOrder.verify(view).removeOptionAt(1);
				
				// AND neither driver has been contacted
				verifyNoMoreInteractions(groupDriver, sequenceDriver);				
			}

			@Nested
			@DisplayName("when having to take over content")
			class OnAcquireContent {

				@Mock
				private SubstitutePlayerSelector<Midfielder> other;

				@Test
				@DisplayName("as the collapse initiator")
				void whenNoSelectionExists() {
					presenter.mask = new ArrayList<>(List.of(0, 3));     // current options "Alpha", "Delta"
					presenter.currentSelection = NO_SELECTION;			 // collapse initiator is empty
					
					Midfielder gamma = new Midfielder("Gamma", null);
					when(other.getSelection()).thenReturn(
							Optional.of(gamma)); 						 // other reports "Gamma" selected
					
					// WHEN the presenter is asked to acquire content from other
					presenter.acquireContentFrom(other);
					
					// THEN as regards
					
					// 1. presenter: it restores and selects "Gamma"
					assertThat(presenter.mask).containsExactly(0, 2, 3);
					assertThat(presenter.currentSelection).isEqualTo(2);
					// AND its View is asked to do the same
					InOrder inOrder = inOrder(view);
					inOrder.verify(view).insertOptionAt(gamma, 1);
					inOrder.verify(view).selectOptionAt(1);
					// in particular, presenter's View is NOT told to clear selection
					verify(view, never()).selectOptionAt(-1);					
					
					// 2. other: it is not instructed to do anything
					verifyNoMoreInteractions(ignoreStubs(other));
					
					// 3. drivers: neither is contacted
					verifyNoMoreInteractions(groupDriver, sequenceDriver);
				}
				
				@Test
				@DisplayName("as a non-empty selector")
				void whenASelectionExists() {
					presenter.mask = new ArrayList<>(List.of(0, 3));   // current options "Alpha", "Delta"
					presenter.currentSelection = 0; 				   // current selection is "Alpha"
					
					Midfielder gamma = new Midfielder("Gamma", null);
					when(other.getSelection()).thenReturn(
							Optional.of(gamma)); 					   // other reports "Gamma" selected
					
					// WHEN the presenter is asked to acquire content from other
					presenter.acquireContentFrom(other);
					
					// THEN as regards
					
					// 1. presenter: it restores and selects "Gamma" and drops "Alpha"
					assertThat(presenter.mask).containsExactly(2, 3);
					assertThat(presenter.currentSelection).isEqualTo(2);
					// AND its View is asked to do the same
					InOrder inOrder = inOrder(view);
					inOrder.verify(view).insertOptionAt(gamma, 1);
					inOrder.verify(view).selectOptionAt(1);
					inOrder.verify(view).removeOptionAt(0);
					// in particular, presenter's View is NOT told to clear selection
					verify(view, never()).selectOptionAt(-1);					
					
					// 2. other: it is not instructed to do anything
					verifyNoMoreInteractions(ignoreStubs(other));
					
					// 3. drivers: neither is contacted
					verifyNoMoreInteractions(groupDriver, sequenceDriver);
				}
			}
		}
		
		@Test
		@DisplayName("when having to swap contents")
		void onSwap(@Mock SubstitutePlayerSelectorView<Midfielder> otherView) {
			SubstitutePlayerSelector<Midfielder> other = spy(
					new SubstitutePlayerSelector<Midfielder>(otherView));
			
			presenter.mask = new ArrayList<>(List.of(0, 3));   // current options "Alpha", "Delta"
			presenter.currentSelection = 0; 				   // current selection is "Alpha"
			
			other.options = new ArrayList<>(INITIAL_OPTIONS);
			other.mask = new ArrayList<>(List.of(2, 3));       // current options "Gamma", "Delta"
			other.currentSelection = 2; 				       // current selection is "Gamma"
			
			Midfielder gamma = new Midfielder("Gamma", null);
			Midfielder alpha = new Midfielder("Alpha", null);
			
			// WHEN the presenter is asked to swap content with other
			presenter.swapContentWith(other);
			
			// THEN as regards
			
			// 1. presenter: it restores and selects "Gamma" and drops "Alpha"
			assertThat(presenter.mask).containsExactly(2, 3);
			assertThat(presenter.currentSelection).isEqualTo(2);
			// AND its View is asked to do the same
			InOrder inOrder = inOrder(view);
			inOrder.verify(view).insertOptionAt(gamma, 1);
			inOrder.verify(view).selectOptionAt(1);
			inOrder.verify(view).removeOptionAt(0);
			// in particular, presenter's View is NOT told to clear selection
			verify(view, never()).selectOptionAt(-1);
			
			// 2. other: it restores and selects "Alpha" and retires "Gamma"
			assertThat(other.mask).containsExactly(0, 3);
			assertThat(other.currentSelection).isEqualTo(0);
			// AND its View is asked to do the same
			inOrder = inOrder(otherView);
			inOrder.verify(otherView).insertOptionAt(alpha, 0);
			inOrder.verify(otherView).selectOptionAt(0);
			inOrder.verify(otherView).removeOptionAt(1);
			// in particular, other's View is NOT told to clear selection
			verify(otherView, never()).selectOptionAt(-1);
			
			// 3. drivers: neither is contacted
			verifyNoMoreInteractions(groupDriver, sequenceDriver);
		}
		
		@Nested
		@DisplayName("when toggling filling")
		class WhenTogglingFillingEnabling {

			@Test
			@DisplayName("when being asked to enable filling")
			void enableFilling() {
				presenter.setFillingEnabled(true);
				
				// THEN the View is asked to enable controls
				verify(view).setControlsEnabled(true);
				verifyNoMoreInteractions(view);
				
				// AND neither driver has been contacted
				verifyNoMoreInteractions(groupDriver, sequenceDriver);
			}

			@Test
			@DisplayName("when being asked to disable filling")
			void disableFilling() {
				presenter.setFillingEnabled(false);
				
				// THEN the View is asked to disable controls
				verify(view).setControlsEnabled(false);
				verifyNoMoreInteractions(view);
				
				// AND neither driver is contacted
				verifyNoMoreInteractions(groupDriver, sequenceDriver);
			}			
		}
		
		@Nested
		@DisplayName("when toggling \"next-fillable\" status")
		class WhenTogglingNextFillableStatus {
			
			@Test
			@DisplayName("when being asked to take on \"next-fillable\" status")
			void highlight() {
				presenter.setNextFillable(true);
				
				// THEN the View is asked to take on "next-fillable" status
				verify(view).highlight();
				verifyNoMoreInteractions(view);
				
				// AND neither driver is contacted
				verifyNoMoreInteractions(groupDriver, sequenceDriver);
			}
			
			@Test
			@DisplayName("when being asked to relinquish \"next-fillable\" status")
			void dehighlight() {
				presenter.setNextFillable(false);
				
				// THEN the View is asked to relinquish "next-fillable" status
				verify(view).dehighlight();
				verifyNoMoreInteractions(view);
				
				// AND neither driver has been contacted
				verifyNoMoreInteractions(groupDriver, sequenceDriver);
			}	
		}		
	}	

	@Nested
	@DisplayName("as an MVP Presenter")
	class AsAnMVPPresenter {
		
		/**
		 * presenter.options initialized only where needed
		 */
		@BeforeEach
		void setup() {
			presenter.attachDriver(groupDriver);
			presenter.attachDriver(sequenceDriver);
			
			verify(sequenceDriver).attachListener(any());  // discounts this interaction
		}

		@Nested
		@DisplayName("when notified of a selection")
		class OnSelectedOption {

			@Test
			@DisplayName("and no previous selection existed")
			void withNoPriorSelection() {				
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
				presenter.currentSelection = -1; 				  // no prior selection

				// WHEN the view notifies a selection for "Delta" (relative position 1)
				presenter.selectedOption(1);

				// THEN the group driver is notified of the new selection for "Delta" (absolute index 3)
				verify(groupDriver).selectionMadeOn(presenter, 3);
				// AND the sequence driver is notified of filling
				verify(sequenceDriver).contentAdded(presenter);
				verifyNoMoreInteractions(groupDriver, sequenceDriver);
			}

			@Test
			@DisplayName("and a previous selection existed")
			void withPriorSelection() {
				presenter.options = new ArrayList<>(INITIAL_OPTIONS);
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
				presenter.currentSelection = 0; 				  // prior selection is "Alpha"

				// WHEN the view notifies a selection for "Delta" (relative position 1)
				presenter.selectedOption(1);

				// THEN the group driver is notified of the clearance and the new selection, in order
				InOrder inOrder = inOrder(groupDriver);
				inOrder.verify(groupDriver).selectionClearedOn(presenter, 0); // Old selection "Alpha"
				inOrder.verify(groupDriver).selectionMadeOn(presenter, 3); 	  // New selection "Delta"
				// AND the sequence driver is NOT notified
				verifyNoMoreInteractions(groupDriver, sequenceDriver);
			}
		}
		
		@Test
		@DisplayName("when notified of clearance")
		void onSelectionCleared() {			
			presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
			presenter.currentSelection = 3;					  // prior selection is "Delta"

			// WHEN the view notifies a selection clearance for "Delta"
			presenter.selectionCleared();

			// THEN the group driver is notified of a selection clearance for "Delta" (absolute index 3)
			verify(groupDriver).selectionClearedOn(presenter, 3);
			// AND the sequence driver is notified of emptying
			verify(sequenceDriver).contentRemoved(presenter);
			verifyNoMoreInteractions(groupDriver, sequenceDriver);
		}
	}

	@Nested
	@DisplayName("as a public Selector, allows clients to")
	class AsAPublicSelector {
		
		@BeforeEach
		void setupWithOptions() {
			presenter.options = new ArrayList<>(INITIAL_OPTIONS);presenter.attachDriver(groupDriver);
			presenter.attachDriver(sequenceDriver);
		}
		
		@Nested
		@DisplayName("query the selection")
		class OnGetSelection {
			
			@Test
			@DisplayName("when a selection exists")
			void withExistingSelection() {				
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
				presenter.currentSelection = 0; 				  // prior selection is "Alpha"
				
				assertThat(presenter.getSelection()).hasValue(new Midfielder("Alpha", null));
			}
			
			@Test
			@DisplayName("when no selection exists")
			void withNoExistingSelection() {				
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options are "Alpha", "Delta"
				presenter.currentSelection = -1; 				  // no prior selection
				
				assertThat(presenter.getSelection()).isEmpty();
			}
		}
		
		@Nested
		@DisplayName("set the selection")
		class onSetSelection {
			
			@BeforeEach
			void discountsAttachmentInteraction() {
				verify(sequenceDriver).attachListener(any());  // discounts this interaction
			}
			
			@Test
			@DisplayName("when no previous selection existed")
			void WithNoPriorSelection() {				
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options are "Alpha", "Delta"
				presenter.currentSelection = -1; 				  // no prior selection
				
				// WHEN a client sets the selection to "Delta"
				presenter.setSelection(Optional.of(new Midfielder("Delta", null)));

				// THEN the view is commanded to select "Delta" (relative position 1)
				verify(view).selectOptionAt(1);
				// AND the driver is notified of selection for "Delta" (absolute index 3)
				verify(groupDriver).selectionMadeOn(presenter, 3);
				// AND the sequence driver is notified of filling
				verify(sequenceDriver).contentAdded(presenter);
				verifyNoMoreInteractions(groupDriver, sequenceDriver);
			}
			
			@Test
			@DisplayName("when a previous selection existed")
			void WithPriorSelection() {				
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
				presenter.currentSelection = 0; 				  // prior selection is "Alpha"
				
				// WHEN a client sets the selection to "Delta"
				presenter.setSelection(Optional.of(new Midfielder("Delta", null)));

				// THEN the view is commanded to select "Delta" (relative position 1)
				verify(view).selectOptionAt(1);
				// AND the group driver is notified of the clearance and the new selection, in order
				InOrder inOrder = inOrder(groupDriver);
				inOrder.verify(groupDriver).selectionClearedOn(presenter, 0); // Old selection "Alpha"
				inOrder.verify(groupDriver).selectionMadeOn(presenter, 3);    // New selection "Delta"
				// AND the sequence driver is not notified
				verifyNoMoreInteractions(groupDriver, sequenceDriver);
			}
			
			@Nested
			@DisplayName("except with the option provided not belonging")
			class WithInvalidOption {
				
				@Test
				@DisplayName("to this dealer")
				void withUnavailableOption() {
					presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
					presenter.currentSelection = 0; 				  // prior selection is "Alpha"
					
					// WHEN attempting to select "Gamma", which is not in the current mask
					assertThatThrownBy(() -> presenter.setSelection(Optional.of(new Midfielder("Gamma", null))))
							.isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining("not found among this dealer's available options");

					// THEN no interactions should have occurred with collaborators
					verifyNoMoreInteractions(view, groupDriver, sequenceDriver);
				}
				
				@Test
				@DisplayName("to any dealer in this group")
				void withUnknownOption() {
					presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
					presenter.currentSelection = 0; 				  // prior selection is "Alpha"
					
					// WHEN attempting to select "Theta", which is not in the common option list
					assertThatThrownBy(() -> presenter.setSelection(Optional.of(new Midfielder("Theta", null))))
							.isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining("not found in dealer group option list");

					// THEN no interactions should have occurred with collaborators
					verifyNoMoreInteractions(view, groupDriver, sequenceDriver);
				}
			}
		}
		
		@Test
		@DisplayName("clear the selection")
		void ClearingSelection() {
			verify(sequenceDriver).attachListener(any());  // discounts this interaction
			
			presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
			presenter.currentSelection = 0; 				  // prior selection is "Alpha"
			
			// WHEN a client clears the selection
			presenter.setSelection(Optional.empty());

			// THEN the view is commanded to clear its selection
			verify(view).selectOptionAt(-1);
			// AND the group driver is notified of a selection clearance for "Alpha" (absolute index 0)
			verify(groupDriver).selectionClearedOn(presenter, 0);
			// AND the sequence driver is notified of emptying
			verify(sequenceDriver).contentRemoved(presenter);
			verifyNoMoreInteractions(groupDriver, sequenceDriver);
		}
		
		@Nested
		@DisplayName("be informed of")
		class NotifiesSelectorListeners {
			
			private @Captor ArgumentCaptor<FillableSwappableSequenceListener
						<SubstituteSelectorDelegate<Midfielder>>> sequenceListenerCaptor;
			
			private @Mock SelectorListener<Midfielder> mockListener;
			
			@BeforeEach
			void setUp() {
				// captures the Sequence listener installed by presenter.attachDriver
				verify(sequenceDriver).attachListener(sequenceListenerCaptor.capture());
				
				presenter.attachListener(mockListener);
			}
			
			@Test
			@DisplayName("a \"selection-set\" event on the Selector")
			void SelectionSetEvent(@Mock SubstitutePlayerSelector<Midfielder> other) {
				
				// WHEN the Sequence broadcasts a "filled" event for this Selector
				sequenceListenerCaptor.getValue().becameFilled(presenter);
				
				// THEN listeners for this Selector are notified of a "selection-made" event
				verify(mockListener).selectionMadeOn(presenter);
				
				// BUT WHEN the Sequence broadcasts a "filled" event for another Selector
				sequenceListenerCaptor.getValue().becameFilled(other);
				
				// THEN listeners for this Selector are not engaged
				verifyNoMoreInteractions(mockListener, sequenceDriver);
			}
			
			@Test
			@DisplayName("a \"selection-cleared\" event on the Selector")
			void SelectionClearedEvent(@Mock SubstitutePlayerSelector<Midfielder> other) {	

				// WHEN the Sequence broadcasts an "emptied" event for this Selector
				sequenceListenerCaptor.getValue().becameEmpty(presenter);
				
				// THEN listeners for this Selectors are notified of a "selection-cleared" event
				verify(mockListener).selectionClearedOn(presenter);
				
				// BUT WHEN the Sequence broadcasts an "emptied" event for another Selector
				sequenceListenerCaptor.getValue().becameEmpty(other);
				
				// THEN listeners for this Selector are not engaged
				verifyNoMoreInteractions(mockListener, sequenceDriver);
			}			
		}
	}
}

