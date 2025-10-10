package gui.lineup.selectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domain.Player;
import domain.Player.Midfielder;
import gui.lineup.chooser.LineUpChooser.StarterSelectorDelegate;
import gui.lineup.chooser.Selector.SelectorListener;
import gui.lineup.dealing.CompetitiveOptionDealingGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("A StarterPlayerSelector")
@ExtendWith(MockitoExtension.class)
@Tag("mockito-agent")
class StarterPlayerSelectorTest {

	// collaborator Mocks	
	private @Mock OrderedDealerPresenter.SelectorWidget<Midfielder> mockView;	
	private @Mock CompetitiveOptionDealingGroup<StarterSelectorDelegate<Midfielder>, Midfielder> mockGroupDriver;	
	private @Mock SelectorListener<Midfielder> mockListener;
	
	// SUT instance 	
	private @InjectMocks StarterPlayerSelector<Midfielder> presenter;

	// global option pool
	private static final List<Midfielder> INITIAL_OPTIONS = List.of(
			new Midfielder("Alpha", null, Player.Club.ATALANTA),
			new Midfielder("Beta", null, Player.Club.ATALANTA),
			new Midfielder("Gamma", null, Player.Club.ATALANTA),
			new Midfielder("Delta", null, Player.Club.ATALANTA));

	@BeforeEach
	void commonSetup() {
		presenter.attachDriver(mockGroupDriver);
		presenter.attachListener(mockListener);
	}

	@Nested
	@DisplayName("as an OrderedOptionDealer")
	class AsAnOrderedOptionDealer {

		@Test
		@DisplayName("when attaching options")
		void attachOptions() {
			
			// WHEN attaching options
			presenter.attachOptions(INITIAL_OPTIONS);

			// THEN it should command the view to initialize its own options
			verify(mockView).initOptions(INITIAL_OPTIONS);

			// AND have no interaction back to the driver
			verifyNoInteractions(mockGroupDriver, mockListener);
		}
		
		@Test
		@DisplayName("when retiring an option")
		void onRetireOption() {
			presenter.options = new ArrayList<>(INITIAL_OPTIONS);			
			presenter.mask = new ArrayList<>(List.of(0, 1, 3));  // current options "Alpha", "Beta", "Delta"

			// WHEN the driver retires "Delta" (absolute index 3)
			presenter.retireOption(3);

			// THEN the view is commanded to remove "Delta" (relative position 2)
			verify(mockView).removeOptionAt(2);

			// AND no feedback is sent back to the driver
			verifyNoMoreInteractions(mockGroupDriver, mockListener);
		}
		
		@Test
		@DisplayName("when restoring an option")
		void onRestoreOption() {
			presenter.options = new ArrayList<>(INITIAL_OPTIONS);			
			presenter.mask = new ArrayList<>(List.of(0, 3));   // current options "Alpha", "Delta"

			// WHEN the driver restores "Gamma" (absolute index 2)
			presenter.restoreOption(2);

			// THEN the view is commanded to insert "Gamma" (relative position 1)
			verify(mockView).insertOptionAt(new Midfielder("Gamma", null, Player.Club.ATALANTA), 1);

			// AND no feedback is sent to the driver
			verifyNoMoreInteractions(mockGroupDriver, mockListener);
		}		
	}

	@Nested
	@DisplayName("as an MVP Presenter")
	class AsAnMVPPresenter {
		
		/**
		 * presenter.options initialized only where needed
		 */
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

				// THEN the driver is notified of the new selection for "Delta" (absolute index 3)
				verify(mockGroupDriver).selectionMadeOn(presenter, 3);
				// AND the listener is notified
				verify(mockListener).selectionMadeOn(presenter);
				verifyNoMoreInteractions(mockGroupDriver, mockListener);
			}

			@Test
			@DisplayName("and a previous selection existed")
			void withPriorSelection() {
				presenter.options = new ArrayList<>(INITIAL_OPTIONS);
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
				presenter.currentSelection = 0; 				  // prior selection is "Alpha"

				// WHEN the view notifies a selection for "Delta" (relative position 1)
				presenter.selectedOption(1);

				// THEN the driver is notified of the clearance and the new selection, in order
				InOrder inOrder = inOrder(mockGroupDriver);
				inOrder.verify(mockGroupDriver).selectionClearedOn(presenter, 0); // Old selection "Alpha"
				inOrder.verify(mockGroupDriver).selectionMadeOn(presenter, 3);    // New selection "Delta"
				verifyNoMoreInteractions(mockGroupDriver);
				// AND listeners are NOT notified
				verifyNoMoreInteractions(mockListener);
			}
		}
		
		@Test
		@DisplayName("when notified of clearance")
		void onSelectionCleared() {			
			presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
			presenter.currentSelection = 3;					  // prior selection is "Delta"

			// WHEN the view notifies a selection clearance for "Delta"
			presenter.selectionCleared();

			// THEN driver is notified of a selection clearance for "Delta" (absolute index 3)
			verify(mockGroupDriver).selectionClearedOn(presenter, 3);
			// AND listeners are notified of a selection clearance
			verify(mockListener).selectionClearedOn(presenter);
		}
	}

	@Nested
	@DisplayName("as a public Selector")
	class AsAPublicSelector {
		
		@BeforeEach
		void setupWithOptions() {
			presenter.options = new ArrayList<>(INITIAL_OPTIONS);
		}
		
		@Nested
		@DisplayName("when the selection is queried")
		class OnGetSelection {
			
			@Test
			@DisplayName("and a selection exists")
			void withExistingSelection() {				
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
				presenter.currentSelection = 0; 				  // prior selection is "Alpha"
				
				assertThat(presenter.getSelection()).hasValue(new Midfielder("Alpha", null, Player.Club.ATALANTA));
			}
			
			@Test
			@DisplayName("and no selection exists")
			void withNoExistingSelection() {				
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options are "Alpha", "Delta"
				presenter.currentSelection = -1; 				  // no prior selection
				
				assertThat(presenter.getSelection()).isEmpty();
			}
		}
		
		@Nested
		@DisplayName("when the selection is set")
		class onSetSelection {
			
			@Test
			@DisplayName("and no previous selection existed")
			void WithNoPriorSelection() {				
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options are "Alpha", "Delta"
				presenter.currentSelection = -1; 				  // no prior selection
				
				// WHEN a client sets the selection to "Delta"
				presenter.setSelection(Optional.of(new Midfielder("Delta", null, Player.Club.ATALANTA)));

				// THEN the view is commanded to select "Delta" (relative position 1)
				verify(mockView).selectOptionAt(1);
				// AND the driver is notified of selection for "Delta" (absolute index 3)
				verify(mockGroupDriver).selectionMadeOn(presenter, 3);
				// AND listeners are notified of the selection
				verify(mockListener).selectionMadeOn(presenter);
				verifyNoMoreInteractions(mockGroupDriver, mockListener);
			}
			
			@Test
			@DisplayName("and a previous selection existed")
			void WithPriorSelection() {				
				presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
				presenter.currentSelection = 0; 				  // prior selection is "Alpha"
				
				// WHEN a client sets the selection to "Delta"
				presenter.setSelection(Optional.of(new Midfielder("Delta", null, Player.Club.ATALANTA)));

				// THEN the view is commanded to select "Delta" (relative position 1)
				verify(mockView).selectOptionAt(1);
				// AND the driver is notified of the clearance and the new selection, in order
				InOrder inOrder = inOrder(mockGroupDriver);
				inOrder.verify(mockGroupDriver).selectionClearedOn(presenter, 0); // Old selection "Alpha"
				inOrder.verify(mockGroupDriver).selectionMadeOn(presenter, 3);    // New selection "Delta"
				// AND listeners are NOT notified
				verifyNoMoreInteractions(mockGroupDriver, mockListener);
			}
			
			@Nested
			@DisplayName("but the provided option does not belong")
			class WithInvalidOption {
				
				@Test
				@DisplayName("to this dealer")
				void withUnavailableOption() {
					presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
					presenter.currentSelection = 0; 				  // prior selection is "Alpha"
					
					// WHEN attempting to select "Gamma", which is not in the current mask
					assertThatThrownBy(() -> presenter.setSelection(Optional.of(new Midfielder("Gamma", null, Player.Club.ATALANTA))))
							.isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining("not found among this dealer's available options");

					// THEN no interactions should have occurred with collaborators
					verifyNoInteractions(mockView, mockGroupDriver, mockListener);
				}
				
				@Test
				@DisplayName("to any dealer in this group dealer")
				void withUnknownOption() {
					presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
					presenter.currentSelection = 0; 				  // prior selection is "Alpha"
					
					// WHEN attempting to select "Theta", which is not in the common option list
					assertThatThrownBy(() -> presenter.setSelection(Optional.of(new Midfielder("Theta", null, Player.Club.ATALANTA))))
							.isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining("not found in dealer group option list");

					// THEN no interactions should have occurred with collaborators
					verifyNoInteractions(mockView, mockGroupDriver, mockListener);
				}
			}
		}
		
		@Test
		@DisplayName("when the selection is cleared")
		void ClearingSelection() {				
			presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
			presenter.currentSelection = 0; 				  // prior selection is "Alpha"
			
			// WHEN a client clears the selection
			presenter.setSelection(Optional.empty());

			// THEN the view is commanded to clear its selection
			verify(mockView).selectOptionAt(-1);
			// AND the driver and listener are notified
			verify(mockGroupDriver).selectionClearedOn(presenter, 0);
			verify(mockListener).selectionClearedOn(presenter);
		}
	}
}
