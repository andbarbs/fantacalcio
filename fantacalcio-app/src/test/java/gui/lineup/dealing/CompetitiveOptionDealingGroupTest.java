package gui.lineup.dealing;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gui.lineup.dealing.CompetitiveOptionDealingGroup.CompetitiveOrderedDealer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@DisplayName("A CompetitiveOptionDealingGroup")
class CompetitiveOptionDealingGroupTest {
	
	/**
	 * a test-specific {@link CompetitiveOrderedDealer} implementor
	 * used solely for the purpose of interaction verifications
	 */
	private static abstract class TestSpecificDealer 
		implements CompetitiveOrderedDealer<TestSpecificDealer, String> {}

	@Mock
	private TestSpecificDealer dealer1, dealer2, dealer3;
	
	private static final List<String> INITIAL_OPTIONS = List.of();
	
	private CompetitiveOptionDealingGroup<TestSpecificDealer, String> driver;
	
	@Test
	@DisplayName("when being initialized")
	void staticFactoryMethod() {
		// WHEN a group is initialized through the static factory API
		driver = CompetitiveOptionDealingGroup.initializeDealing(
				Set.of(dealer1, dealer2, dealer3), 
				INITIAL_OPTIONS);
		
		// THEN appropriate initialization members are called on dealers
		Stream.of(dealer1, dealer2, dealer3).forEach(dealer -> {
			verify(dealer).attachDriver(driver);
			verify(dealer).attachOptions(INITIAL_OPTIONS);
		});
		verifyNoMoreInteractions(dealer1, dealer2, dealer3);
	}

	@Nested
	@DisplayName("once initialized")
	class OnceGroupInitialized {
		@BeforeEach
		void commonSetup() {
			driver = new CompetitiveOptionDealingGroup<TestSpecificDealer, String>(
					Set.of(dealer1, dealer2, dealer3));
		}

		@Nested
		@DisplayName("when notified of a selection-made event on a dealer")
		class OnSelectionMade {
			
			@Test
			@DisplayName("and the dealer is in the group")
			void onValidDealer() {
				// WHEN a selection-made event on a dealer is notified
				int optionIndex = 10;
				driver.selectionMadeOn(dealer2, optionIndex);
				
				// THEN competing dealers are commanded to retire the option in the event
				Stream.of(dealer1, dealer3).forEach(dealer -> {
					verify(dealer).retireOption(optionIndex);
					verifyNoMoreInteractions(dealer);
				});
				
				// AND no interactions happens with the dealer originating the event
				verifyNoInteractions(dealer2);
			}
			
			@Test
			@DisplayName("and the dealer is not in the group")
			void onInvalidDealer(@Mock TestSpecificDealer stranger) {
				// WHEN a selection-made event on a dealer is notified
				int optionIndex = 10;
				ThrowingCallable call = () -> driver.selectionMadeOn(stranger, optionIndex);
				
				// THEN an error is thrown
				assertThatThrownBy(call)
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("not a member of this group");
				
				// AND no interactions happen with any dealers in the group
				verifyNoInteractions(dealer1, dealer2, dealer3);
			}
		}
		
		@Nested
		@DisplayName("when notified of a selection-cleared event on a dealer")
		class OnSelectionCleared {
			
			@Test
			@DisplayName("and the dealer is in the group")
			void onValidDealer() {
				// WHEN a selection-cleared event on a dealer is notified
				int optionIndex = 10;
				driver.selectionClearedOn(dealer2, optionIndex);

				// THEN competing dealers are commanded to restore the option in the event
				Stream.of(dealer1, dealer3).forEach(dealer -> {
					verify(dealer).restoreOption(optionIndex);
					verifyNoMoreInteractions(dealer);
				});

				// AND no interactions happens with the dealer originating the event
				verifyNoInteractions(dealer2);
			}
			
			@Test
			@DisplayName("and the dealer is not in the group")
			void onInvalidDealer(@Mock TestSpecificDealer stranger) {
				// WHEN a selection-cleared event on a dealer is notified
				int optionIndex = 10;
				ThrowingCallable call = () -> driver.selectionClearedOn(stranger, optionIndex);
				
				// THEN an error is thrown
				assertThatThrownBy(call)
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("not a member of this group");
				
				// AND no interactions happen with any dealers in the group
				verifyNoInteractions(dealer1, dealer2, dealer3);
			}
		}

		
	}

	
}
