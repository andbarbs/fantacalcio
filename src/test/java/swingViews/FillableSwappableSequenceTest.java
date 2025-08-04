package swingViews;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import swingViews.FillableSwappableSequence.FillableSwappableGadget;
import swingViews.FillableSwappableSequence.FillableSwappableSequenceListener;

@ExtendWith(MockitoExtension.class)
@DisplayName("A FillableSwappableSequence")
class FillableSwappableSequenceTest {

	/**
	 * a test-specific {@link FillableSwappableGadget} implementor 
	 * used solely for the purpose of interaction verifications
	 */
	private static abstract class TestSpecificFillable 
			implements FillableSwappableGadget<TestSpecificFillable> {
	}

	@Mock private TestSpecificFillable fillable0, fillable1, fillable2, fillable3;
	
	@Mock private FillableSwappableSequenceListener<TestSpecificFillable> listener;

	private FillableSwappableSequence<TestSpecificFillable> driver;

	@Test
	@DisplayName("when being initialized")
	void staticFactoryMethod() {
		
		// WHEN a sequence is created with the mocked gadgets
		driver = FillableSwappableSequence.createSequence(
				List.of(fillable0, fillable1, fillable2, fillable3));

		// THEN appropriate initialization members are called on gadgets
		Stream.of(fillable0, fillable1, fillable2,fillable3).forEach(fillable -> {
			verify(fillable).attachDriver(driver);
			verify(fillable).disableFilling();
		});
		verify(fillable0).enableFilling();
		verify(fillable0).highlight();
		verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3);
	}

	@Nested
	@DisplayName("once initialized")
	class OnceSequenceInitialized {
		@BeforeEach
		void commonSetup() {
			driver = new FillableSwappableSequence<TestSpecificFillable>(
					List.of(fillable0, fillable1, fillable2, fillable3));
			driver.listeners = List.of(listener);
		}

		@Nested
		@DisplayName("when notified that a gadget has been filled")
		class OnGadgetFilled {

			@Nested
			@DisplayName("advances the sequence")
			class AdvancesSequence {

				@Nested
				@DisplayName("when that gadget is the next-fillable")
				class OnNFFilled {

					@Nested
					@DisplayName("with next-fillable being")
					class WithNFBeing {

						@Test
						@DisplayName("the first gadget")
						void onFirstNextFillable() {
							driver.rightmostFillablePosition = 0;

							// WHEN notified of filling on NF
							driver.contentAdded(fillable0);

							// THEN the sequence is advanced
							verify(fillable1).enableFilling();
							verify(fillable0).dehighlight();
							verify(fillable1).highlight();

							// AND listeners are notified
							verify(listener).becameFilled(fillable0);

							verifyNoMoreInteractions(fillable1, fillable2, fillable3, listener);
						}

						@Test
						@DisplayName("the last gadget")
						void onLastNextFillable() {
							driver.rightmostFillablePosition = 3;

							// WHEN notified of filling on NF
							driver.contentAdded(fillable3);

							// THEN the sequence is advanced
							verify(fillable3).dehighlight();

							// AND listeners are notified
							verify(listener).becameFilled(fillable3);

							verifyNoMoreInteractions(fillable1, fillable2, fillable3, listener);
						}

						@Test
						@DisplayName("an intermediate gadget")
						void onIntermediateNextFillable() {
							driver.rightmostFillablePosition = 2;

							// WHEN notified of filling on NF
							driver.contentAdded(fillable2);

							// THEN the sequence is advanced
							verify(fillable3).enableFilling();
							verify(fillable2).dehighlight();
							verify(fillable3).highlight();

							// AND listeners are notified
							verify(listener).becameFilled(fillable2);

							verifyNoMoreInteractions(fillable1, fillable2, fillable3, listener);
						}
					}
				}
			}
			
			@Nested
			@DisplayName("does not advance")
			class DoesNotAdvanceSequence {

				@Test
				@DisplayName("when that gadget is before the next-fillable")
				void onPriorToNFMember() {
					driver.rightmostFillablePosition = 2;
					
					// WHEN notified of filling on NF
					driver.contentAdded(fillable0);
					
					// THEN the sequence is not advanced
					verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
				}
			}
			
			@Nested
			@DisplayName("throws an error and does not advance")
			class DoesNotAdvanceAndThrows {

				@Nested
				@DisplayName("when that gadget")
				class OnInvalidGadgetFilled {
					
					@Test
					@DisplayName("is in the sequence, but is past the next-fillable")
					void onNonNFMember() {
						driver.rightmostFillablePosition = 0;
						
						// WHEN notified of filling a past-NF gadget
						ThrowingCallable call = () -> driver.contentAdded(fillable1);
						
						// THEN an error is thrown
						assertThatThrownBy(call)
								.isInstanceOf(IllegalStateException.class)
								.hasMessageContaining("beyond the next-fillable");
						
						// AND the sequence is not advanced
						verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
					}

					@Test
					@DisplayName("is not even in the sequence")
					void onNonMember(@Mock TestSpecificFillable stranger) {
						
						// WHEN notified of filling on NF
						ThrowingCallable call = () -> driver.contentAdded(stranger);
						
						// THEN the sequence is advanced
						assertThatThrownBy(call)
								.isInstanceOf(IllegalArgumentException.class)
								.hasMessageContaining("not a member of this sequence");
						
						verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
					}
				}
			}
		}		
		
		@Nested
		@DisplayName("when notified that a gadget has been emptied")
		class OnGadgetEmpties {

			@Nested
			@DisplayName("collapses the sequence")
			class CollapsesSequence {

				@Nested
				@DisplayName("when the emptied gadget is")
				class WithEmptiedBeing {

					@Test
					@DisplayName("right before next-fillable")
					void rigthBeforeNF() {
						driver.rightmostFillablePosition = 2;
						
						// WHEN notified of emptying on fillable1
						driver.contentRemoved(fillable1);
						
						// THEN next-fillable status is moved
						verify(fillable2).disableFilling();
						verify(fillable2).dehighlight();
						verify(fillable1).highlight();
						
						// AND listeners are notified of emptying
						verify(listener).becameEmpty(fillable1);
						
						verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
					}
					
					@Test
					@DisplayName("some steps before next-fillable")
					void someStepsBeforeNF() {
						driver.rightmostFillablePosition = 3;
						
						// WHEN notified of emptying on fillable1
						driver.contentRemoved(fillable0);
						
						// THEN content is collapsed
						InOrder inOrder = inOrder(fillable0, fillable1, fillable2);
						inOrder.verify(fillable0).acquireContentFrom(fillable1);
						inOrder.verify(fillable1).acquireContentFrom(fillable2);
						inOrder.verify(fillable2).discardContent();
						
						// AND next-fillable status is moved
						verify(fillable3).disableFilling();
						verify(fillable3).dehighlight();
						verify(fillable2).highlight();
						
						// AND listeners are notified of emptying
						verify(listener).becameEmpty(fillable2);  // the gadget emptied as a result of collapsing
						
						verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
					}
				}
			}
			
			@Nested
			@DisplayName("throws an error and does not collapse")
			class DoesNotCollapseSequence {

				@Nested
				@DisplayName("when the emptied gadget is")
				class OnInvalidGadgetEmptied {
					
					@Test
					@DisplayName("the next-fillable")
					void onNFMember() {
						driver.rightmostFillablePosition = 2;
						
						// WHEN notified of filling a past-NF gadget
						ThrowingCallable call = () -> driver.contentRemoved(fillable2);
						
						// THEN an error is thrown
						assertThatThrownBy(call)
								.isInstanceOf(IllegalStateException.class)
								.hasMessageContaining("for which filling had not been reported");
						
						// AND the sequence is not collapsed
						verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
					}
					
					@Test
					@DisplayName("after next-fillable")
					void onPostNFMember() {
						driver.rightmostFillablePosition = 2;
						
						// WHEN notified of filling a past-NF gadget
						ThrowingCallable call = () -> driver.contentRemoved(fillable3);
						
						// THEN an error is thrown
						assertThatThrownBy(call)
								.isInstanceOf(IllegalStateException.class)
								.hasMessageContaining("for which filling should have been disabled");
						
						// AND the sequence is not collapsed
						verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
					}

					@Test
					@DisplayName("not even in the sequence")
					void onNonMember(@Mock TestSpecificFillable stranger) {
						
						// WHEN notified of filling on an extraneous gadget
						ThrowingCallable call = () -> driver.contentRemoved(stranger);
						
						// THEN an error is thrown
						assertThatThrownBy(call)
								.isInstanceOf(IllegalArgumentException.class)
								.hasMessageContaining("not a member of this sequence");
						
						// AND the sequence is not collapsed
						verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
					}
				}
			}
		}
		
		@Nested
		@DisplayName("when asked to swap the contents of a gadget")
		class WhenAskedToSwap {

			@Nested
			@DisplayName("and the pair are both filled")
			class SwapIsPossible {

				@Nested
				@DisplayName("makes the gadget swap")
				class MakedGadgetSwap {

					@Nested
					@DisplayName("with its neighbor")
					class WithNeighbor {

						@Test
						@DisplayName("to the left")
						void toTheLeft() {
							driver.rightmostFillablePosition = 2;
							
							// WHEN asked to swap fillable2 to the left
							driver.swapLeft(fillable1);
							
							// THEN next-fillable status is moved
							verify(fillable1).swapContentWith(fillable0);
							
							// AND listeners are not notified						
							verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
						}
						
						@Test
						@DisplayName("to the right")
						void toTheRight() {
							driver.rightmostFillablePosition = 2;
							
							// WHEN asked to swap fillable1 to the right
							driver.swapRight(fillable0);
							
							// THEN next-fillable status is moved
							verify(fillable0).swapContentWith(fillable1);
							
							// AND listeners are not notified						
							verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
						}
					}
				}
			}
			
			@Nested
			@DisplayName("throws an error and does not swap")
			class DoesNotSwapAndThrows {

				@Nested
				@DisplayName("when asked to swap")
				class WhenSwapping {
					
					@Test
					@DisplayName("an empty gadget")
					void anEmptyGadget() {
						driver.rightmostFillablePosition = 2;
						
						// WHEN asked to swap the next-fillable
						ThrowingCallable swapLeft = () -> driver.swapLeft(fillable2);
						ThrowingCallable swapRight = () -> driver.swapRight(fillable2);
						
						// THEN an error is thrown
						assertThatThrownBy(swapLeft)
								.isInstanceOf(IllegalArgumentException.class)
								.hasMessageContainingAll("unable to swap left");
						
						assertThatThrownBy(swapRight)
								.isInstanceOf(IllegalArgumentException.class)
								.hasMessageContaining("unable to swap right");
						
						// AND the sequence is not collapsed
						verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
					}
					
					@Test
					@DisplayName("a filled gadget with an empty neighbor")
					void anEmptyNeighbor() {
						driver.rightmostFillablePosition = 2;
						
						// WHEN asked to swap with the neighbor being the next-fillable
						ThrowingCallable call = () -> driver.swapRight(fillable1);
						
						// THEN an error is thrown
						assertThatThrownBy(call)
								.isInstanceOf(IllegalArgumentException.class)
								.hasMessageContaining("unable to swap right");
						
						// AND the sequence is not collapsed
						verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
					}

					@Test
					@DisplayName("a gadget that is not in the sequence")
					void onNonMember(@Mock TestSpecificFillable stranger) {
						
						// WHEN asked to swap a non-member
						ThrowingCallable call = () -> driver.swapRight(stranger);
						
						// THEN an error is thrown
						assertThatThrownBy(call)
								.isInstanceOf(IllegalArgumentException.class)
								.hasMessageContaining("not a member of this sequence");
						
						// AND the sequence is not collapsed
						verifyNoMoreInteractions(fillable0, fillable1, fillable2, fillable3, listener);
					}
				}
			}
		}
	}
}
