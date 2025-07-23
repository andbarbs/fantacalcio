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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("An OrderedDealerPresenter")
class OrderedDealerPresenterTest {

    //--- Collaborator Mocks ---//
    @Mock
    private OrderedDealerPresenter.OrderedDealerView<String> view;
    @Mock
    private OptionDealerGroupDriver<OrderedDealerPresenter<String>, String> driver;
    @Mock
    private OrderedDealerPresenter.OrderedDealerListener<String> listener;

    //--- Argument Captors ---//
    @Captor
    private ArgumentCaptor<Integer> intCaptor;
    @Captor
    private ArgumentCaptor<String> stringCaptor;
    @Captor
    private ArgumentCaptor<List<String>> listCaptor;

    //--- System Under Test (SUT) ---//
    @InjectMocks
    private OrderedDealerPresenter<String> presenter;

    private List<String> initialOptions;

    @BeforeEach
    void commonSetup() {
        initialOptions = List.of("Alpha", "Beta", "Gamma", "Delta");
        presenter.attachListener(listener);
    }
    
    //--------------------------------------------------------------------------------
    
    @Nested
    @DisplayName("when being initialized")
    class WhenInitializing {

        @Test
        @DisplayName("attachDriver should store the driver reference")
        void attachDriver() {
            // WHEN attaching the driver
            presenter.attachDriver(driver);
            presenter.currentSelection = 1; // Arbitrary selection
            
            // THEN calling a method that uses the driver should not fail
            presenter.selectionCleared();
            verify(driver).selectionClearedOn(presenter, 1);
        }

        @Test
        @DisplayName("attachOptions should initialize view and internal state")
        void attachOptions() {
            // WHEN attaching options
            presenter.attachOptions(initialOptions);

            // THEN it should command the view to initialize its own options
            verify(view).initOptions(listCaptor.capture());
            assertThat(listCaptor.getValue()).isEqualTo(initialOptions);

            // AND internal state should be correctly set
            assertThat(presenter.getSelection()).isEmpty();
        }
    }
    
    //--------------------------------------------------------------------------------

    @Nested
    @DisplayName("when options are attached")
    class WhenOptionsAreAttached {

        @BeforeEach
        void setupWithOptions() {
            presenter.attachDriver(driver);
            // Simulate the presenter being fully initialized
            presenter.options = new ArrayList<>(initialOptions);
        }

        @Nested
        @DisplayName("and no selection exists")
        class AndNoSelectionExists {
        	
        	@BeforeEach
            void setupWithNoSelection() {
        		presenter.mask = presenter.mask = new ArrayList<>(List.of(0, 1, 2, 3));
        		presenter.currentSelection = -1; // NO_SELECTION
            }

            @Test
            @DisplayName("getSelection should return an empty Optional")
            void getSelection() {
                assertThat(presenter.getSelection()).isEmpty();
            }

            @Test
            @DisplayName("a view selection should notify driver and listeners")
            void selectedOption() {
                // WHEN the view notifies a selection at relative position 2 ("Gamma")
                presenter.selectedOption(2);

                // THEN the driver is notified of the new selection (absolute index 2)
                verify(driver).selectionMadeOn(presenter, 2);
                // AND the listener is notified
                verify(listener).selectionMadeOn(presenter);
                verifyNoMoreInteractions(driver, listener);
            }

            @Test
            @DisplayName("a client selection should command view and notify others")
            void setSelection() {
                // WHEN a client sets the selection to "Delta"
                presenter.setSelection(Optional.of("Delta"));

                // THEN the view is commanded to select relative position 3
                verify(view).selectOptionAt(3);
                // AND the driver and listener are notified of absolute index 3
                verify(driver).selectionMadeOn(presenter, 3);
                verify(listener).selectionMadeOn(presenter);
                verifyNoMoreInteractions(driver, listener);
            }
        }

        @Nested
        @DisplayName("and a selection already exists")
        class AndASelectionExists {

            @BeforeEach
            void setupWithSelection() {
            	presenter.mask = new ArrayList<>(List.of(0, 1, 2, 3));
                presenter.currentSelection = 1; // "Beta" is at absolute index 1
            }

            @Test
            @DisplayName("getSelection should return the correct Optional")
            void getSelection() {
                assertThat(presenter.getSelection()).hasValue("Beta");
            }

            @Test
            @DisplayName("a new view selection should clear old and set new")
            void selectedOption() {
                // WHEN the view notifies a new selection at relative position 3 ("Delta")
                presenter.selectedOption(3);

                // THEN the driver is notified of the clearance and the new selection, in order
                InOrder inOrder = inOrder(driver);
                inOrder.verify(driver).selectionClearedOn(presenter, 1); // Old selection "Beta"
                inOrder.verify(driver).selectionMadeOn(presenter, 3);   // New selection "Delta"
                verifyNoMoreInteractions(driver);
                
                // AND the listener is notified of the new selection
                verify(listener).selectionMadeOn(presenter);
                verifyNoMoreInteractions(listener);
            }

            @Test
            @DisplayName("a view clearance should notify driver and listeners")
            void selectionCleared() {
                // WHEN the view notifies the selection was cleared
                presenter.selectionCleared();

                // THEN the driver is notified
                verify(driver).selectionClearedOn(presenter, 1);
                // AND the listener is notified
                verify(listener).selectionClearedOn(presenter);
            }

            @Test
            @DisplayName("a client clearance should command view and notify others")
            void setSelectionToEmpty() {
                // WHEN a client clears the selection
                presenter.setSelection(Optional.empty());

                // THEN the view is commanded to clear its selection
                verify(view).selectOptionAt(-1);
                // AND the driver and listener are notified
                verify(driver).selectionClearedOn(presenter, 1);
                verify(listener).selectionClearedOn(presenter);
            }
        }

        @Nested
        @DisplayName("and acting as a dealer when options are contested")
        class AsContestedDealer {

            @BeforeEach
            void setupWithContestedOptions() {
                // Simulate that another dealer took "Gamma" (absolute index 2)
                presenter.mask = new ArrayList<>(List.of(0, 1, 3));
                presenter.currentSelection = -1; // NO_SELECTION
            }

            @Test
            @DisplayName("retireOption should command view with the correct relative index")
            void retireOption() {
                // The current options are "Alpha", "Beta", "Delta".
                // "Delta" (absolute index 3) is at relative position 2.
                
                // WHEN the driver retires "Delta"
                presenter.retireOption(3);

                // THEN the view is commanded to remove the option at relative position 2
                verify(view).removeOptionAt(intCaptor.capture());
                assertThat(intCaptor.getValue()).isEqualTo(2);

                // AND no feedback is sent to the driver
                verifyNoMoreInteractions(driver);
            }

            @Test
            @DisplayName("restoreOption should command view with correct relative index and option")
            void restoreOption() {
                // The current options are "Alpha", "Beta", "Delta".
                // Restoring "Gamma" (absolute index 2) should place it at relative position 2.

                // WHEN the driver restores "Gamma"
                presenter.restoreOption(2);

                // THEN the view is commanded to insert "Gamma" at relative position 2
                verify(view).insertOptionAt(stringCaptor.capture(), intCaptor.capture());
                assertThat(stringCaptor.getValue()).isEqualTo("Gamma");
                assertThat(intCaptor.getValue()).isEqualTo(2);
                
                // AND no feedback is sent to the driver
                verifyNoMoreInteractions(driver);
            }

            @Test
            @DisplayName("setSelection with an unavailable option should throw an exception")
            void setSelectionUnavailable() {
                // WHEN attempting to select "Gamma", which is not in the current mask
                assertThatThrownBy(() -> presenter.setSelection(Optional.of("Gamma")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found among this dealer's available options");

                // THEN no interactions should have occurred with collaborators
                verifyNoInteractions(view, driver, listener);
            }
        }
    }
}
