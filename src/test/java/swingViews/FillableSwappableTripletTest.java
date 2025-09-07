package swingViews;

import static org.mockito.Mockito.verify;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import swingViews.FillableSwappableSequence.FillableSwappable;
import swingViews.FillableSwappableTriplet.FillableSwappableTripletWidget;

@DisplayName("A FillableSwappableTriplet")
@ExtendWith(MockitoExtension.class)
public class FillableSwappableTripletTest {
	
	// a test-private abstract implementor of FillableSwappable intended for mocking
	private abstract class TestFillableSwappable implements FillableSwappable<TestFillableSwappable> {}
	
	private @Mock TestFillableSwappable mockFillable1, mockFillable2, mockFillable3;

	// the SUT reference
	private FillableSwappableTriplet<TestFillableSwappable> triplet;

	@BeforeEach
	public void testCaseSpecificSetup() {		
		triplet = new FillableSwappableTriplet<TestFillableSwappable>( 
				mockFillable1, mockFillable2, mockFillable3);
	}
	
	@Nested
	@DisplayName("reacts to notifications from the Sequence")
	class FromSequenceToWidget {

		private @Mock FillableSwappableTripletWidget mockWidget;

		@BeforeEach
		void testCaseSpecificSetup() {
			triplet.setWidget(mockWidget);
		}
		
		@Nested
		@DisplayName("enabling swpping")
		class Enabled {
			
			@Nested
			@DisplayName("when notfied that")
			class OnNotifiaction {
				
				@Test
				@GUITest
				@DisplayName("the second fillable was filled")
				public void selector2Filled() {
					
					// WHEN driver notifies second selector filled
					GuiActionRunner.execute(() -> {
						triplet.getSequenceListener().becameFilled(mockFillable2);
					});
					
					// THEN the Widget is requested to enable swapping first pair
					verify(mockWidget).setSwappingFirstPair(true);
				}
				
				@Test
				@GUITest
				@DisplayName("the third fillable was filled")
				public void selector3Filled() {
					
					// WHEN driver notifies third selector filled
					GuiActionRunner.execute(() -> {
						triplet.getSequenceListener().becameFilled(mockFillable3);
					});
					
					// THEN the Widget is requested to enable swapping first pair
					verify(mockWidget).setSwappingSecondPair(true);
				}
			}
		}
		
		@Nested
		@DisplayName("disabling swapping")
		class Disabled {
			
			@Nested
			@DisplayName("when notfied that")
			class OnNotifiaction {
				
				@Test
				@GUITest
				@DisplayName("the third fillable was emptied")
				public void selector3Emptied() {
					
					// WHEN driver notifies third selector emptied
					GuiActionRunner.execute(() -> {
						triplet.getSequenceListener().becameEmpty(mockFillable3);
					});
					
					// THEN the Widget is requested to disable swapping first pair
					verify(mockWidget).setSwappingSecondPair(false);
				}
				
				@Test
				@GUITest
				@DisplayName("the second fillable was emptied")
				public void selector2Emptied() {
					
					// WHEN driver notifies second selector emptied
					GuiActionRunner.execute(() -> {
						triplet.getSequenceListener().becameEmpty(mockFillable2);
					});
					
					// THEN the Widget is requested to disable swapping first pair
					verify(mockWidget).setSwappingFirstPair(false);
				}
			}
		}
	}

	@Nested
	@DisplayName("forwards Widget swap requests to Sequence")
	class FromWidgetToSequence {

		@Mock
		FillableSwappableSequence<TestFillableSwappable> mockSequence;

		@BeforeEach
		void wireUpMockSelectorsAndSequence() {
			triplet.setSequenceDriver(mockSequence);
		}

		@Nested
		@DisplayName("when asked by user to")
		class OnUserClickTo {

			@Test
			@GUITest
			@DisplayName("swap the first selector pair")
			public void swapSelectors1And2() {

				// WHEN Widget requests swapping first pair
				triplet.swapFirstPair();

				// THEN the correct swap request is sent to the sequence
				verify(mockSequence).swapRight(mockFillable1);
			}

			@Test
			@GUITest
			@DisplayName("swap the second selector pair")
			public void swapSelectors2And3() {

				// WHEN user clicks second swap button
				triplet.swapSecondPair();

				// THEN the correct swap request is sent to the sequence
				verify(mockSequence).swapRight(mockFillable2);
			}
		}
	}

}
