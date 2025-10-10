package gui.lineup.selectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gui.lineup.selectors.OrderedDealerPresenter.NO_SELECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("An OrderedDealerPresenter")
@ExtendWith(MockitoExtension.class)
@Tag("mockito-agent")
class OrderedDealerPresenterTest {

	/**
	 * a test-specific concrete inheritor of {@link OrderedDealerPresenter}
	 * that serves to exercise {@code protected} members of that type
	 * @param <T> the type for options in the dealer
	 */
	private static class TestOrderedDealerPresenter<T> extends OrderedDealerPresenter<T> {
		
		TestOrderedDealerPresenter(SelectorWidget<T> view) {
			super(view);
		}		
		
		/**
		 * both hooks implemented as no-ops, as this test case
		 * is solely concerned with subclass-facing responsibilities of
		 * {@link OrderedDealerPresenter} 
		 */
		@Override
		protected void selectionSetFor(int absoluteIndex) {}
		
		@Override
		protected void selectionClearedFor(int absoluteIndex) {}
	}
	
	@Mock
	private OrderedDealerPresenter.SelectorWidget<String> mockView;
	
	@InjectMocks
	private TestOrderedDealerPresenter<String> presenter;
	
	@BeforeEach
	void setUp() {
		presenter.options = Arrays.asList("Alpha", "Beta", "Gamma", "Delta");
	}
	
    @Nested
    @DisplayName("when calling selectOption()")
    class OnSelectOption {

        @Test
        @DisplayName("and the argument corresponds to an available option")
        void selectOption_validAbsoluteIndex_updatesSelectionAndNotifiesView() {
            presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
    		presenter.currentSelection = 3;					  // current selection is "Delta"
        	
            // WHEN selectOption() is called on option "Alpha"
            presenter.selectOption(0);

            // THEN option "Alpha" becomes selected
            assertThat(presenter.currentSelection).isEqualTo(0);
            
            // AND the View is instructed to select "Alpha"
            verify(mockView).selectOptionAt(0); 
        }

        @Test
        @DisplayName("with the intent to clear selection")
        void selectOption_NO_SELECTION_clearsSelectionAndNotifiesView() {
        	presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
    		presenter.currentSelection = 3;					  // current selection is "Delta"
        	
            // WHEN selectOption() is called to clear selection
            presenter.selectOption(NO_SELECTION);

            // THEN option "Alpha" becomes selected
            assertThat(presenter.currentSelection).isEqualTo(NO_SELECTION);
            
            // AND the View is instructed to clear selection
            verify(mockView).selectOptionAt(-1);
        }
        
        @Test
        @DisplayName("and the argument does not correspond to an available option")
        void selectOption_invalidAbsoluteIndex_throwsIllegalArgumentException() {
        	presenter.mask = new ArrayList<>(List.of(0, 3));  // current options "Alpha", "Delta"
    		presenter.currentSelection = 3;					  // current selection is "Delta"
        	
            // WHEN selectOption() is called on a non-available option, i.e. "Gamma"
            
    		// THEN an exception is thrown
    		assertThatThrownBy(() -> presenter.selectOption(2))
    			.isInstanceOf(IllegalArgumentException.class)
    			.hasMessageContaining("not found among this dealer's available options");

            // AND presenter's state remains unchanged
    		assertThat(presenter.mask).containsExactly(0, 3);
            assertThat(presenter.currentSelection).isEqualTo(3);
            
            // AND the View is not contacted.
            verifyNoInteractions(mockView);
        }
    }
}