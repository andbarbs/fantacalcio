package swingViews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.Beans;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import swingViews.FillableSwappableSequence.FillableSwappable;
import swingViews.utilities.AssertJSwingJUnit5TestCase;

@DisplayName("A SwingFillableSwappableTriplet")
@ExtendWith(MockitoExtension.class)
public class SwingFillableSwappableTripletTest extends AssertJSwingJUnit5TestCase {
	
	// a test-private abstract implementor of FillableSwappable intended for mocking
	private abstract class TestFillableSwappable implements FillableSwappable<TestFillableSwappable> {}
	
	private @Mock TestFillableSwappable mockFillable1, mockFillable2, mockFillable3;
	private JPanel fakeWidget1, fakeWidget2, fakeWidget3;

	// the SUT reference
	private SwingFillableSwappableTriplet<TestFillableSwappable> triplet;	

	private JButtonFixture swap1_2, swap2_3;

	@BeforeEach
	public void testCaseSpecificSetup() {
		JFrame frame = GuiActionRunner.execute(() -> {

			// appropriate dims for a widget
			Dimension widgetDims = new Dimension(120, 225);
			
			fakeWidget1 = new JPanel();
			fakeWidget2 = new JPanel();
			fakeWidget3 = new JPanel();
			
			List.of(fakeWidget1, fakeWidget2, fakeWidget3).forEach(fakeWidget -> {
				fakeWidget.setPreferredSize(widgetDims);
				fakeWidget.setBackground(Color.ORANGE);
			});
			
			triplet = new SwingFillableSwappableTriplet<TestFillableSwappable>(Beans.isDesignTime(), 
					mockFillable1, fakeWidget1,
					mockFillable2, fakeWidget2,
					mockFillable3, fakeWidget3);

			// sets up the test Frame
			JFrame f = new JFrame("Test Frame");
			f.add(triplet);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return f;
		});

		window = new FrameFixture(robot, frame);
		window.show();

		swap1_2 = window.button("swap1_2");
		swap2_3 = window.button("swap2_3");
	}

	@Nested
	@DisplayName("graphically contains")
	class Contains {
		
		@Test
		@GUITest
		@DisplayName("the three widgets")
		public void widgetsProvidedToConstructor() {
			List.of(fakeWidget1, fakeWidget2, fakeWidget3).forEach(fakeWidget -> {
				assertThat(fakeWidget.getParent()).isSameAs(triplet);
			});
		}

		@Nested
		@DisplayName("two \"swap\" buttons, which")
		class ContainsSwapButtons {
			
			@Test
			@GUITest
			@DisplayName("are initially disabled")
			public void buttonsDisabled() {
				swap1_2.requireDisabled();
				swap2_3.requireDisabled();
			}

			@Nested
			@DisplayName("are subsequently")
			class Subsequently {

				@Nested
				@DisplayName("selectively enabled")
				class Enabled {

					@BeforeEach
					void disableButtons() {
						GuiActionRunner.execute(() -> {
							List.of(swap1_2, swap2_3).forEach(button -> button.target().setEnabled(false));
						});
					}

					@Nested
					@DisplayName("when notfied that")
					class OnNotifiaction {

						@Test
						@GUITest
						@DisplayName("the second selector was filled")
						public void selector2Filled() {

							// WHEN driver notifies second selector filled
							GuiActionRunner.execute(() -> {
								triplet.getSequenceListener().becameFilled(mockFillable2);
							});

							// THEN the first swap button is enabled
							swap1_2.requireEnabled();
						}

						@Test
						@GUITest
						@DisplayName("the third selector was filled")
						public void selector3Filled() {

							// WHEN driver notifies third selector filled
							GuiActionRunner.execute(() -> {
								triplet.getSequenceListener().becameFilled(mockFillable3);
							});

							// THEN the second swap button is enabled
							swap2_3.requireEnabled();
						}
					}
				}

				@Nested
				@DisplayName("selectively disabled")
				class Disabled {

					@BeforeEach
					void enableButtons() {
						GuiActionRunner.execute(() -> {
							List.of(swap1_2, swap2_3).forEach(button -> button.target().setEnabled(true));
						});
					}

					@Nested
					@DisplayName("when notfied that")
					class OnNotifiaction {

						@Test
						@GUITest
						@DisplayName("the third selector was emptied")
						public void selector3Emptied() {

							// WHEN driver notifies third selector emptied
							GuiActionRunner.execute(() -> {
								triplet.getSequenceListener().becameEmpty(mockFillable3);
							});

							// THEN the second swap button is disabled
							swap2_3.requireDisabled();
						}

						@Test
						@GUITest
						@DisplayName("the second selector was emptied")
						public void selector2Emptied() {

							// WHEN driver notifies second selector emptied
							GuiActionRunner.execute(() -> {
								triplet.getSequenceListener().becameEmpty(mockFillable2);
							});

							// THEN the first swap button is disabled
							swap1_2.requireDisabled();
						}
					}
				}
			}
		}
	}

	@Nested
	@DisplayName("forwards user swap requests to sequence")
	class ForwardsSwapRequests {

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
				GuiActionRunner.execute(() -> {
					swap1_2.target().setEnabled(true);
				});

				// WHEN user clicks first swap button
				swap1_2.click();

				// Wait for the EDT to process the click event
				robot.waitForIdle();

				// THEN the correct swap request is sent to the sequence
				verify(mockSequence).swapRight(mockFillable1);
			}

			@Test
			@GUITest
			@DisplayName("swap the second selector pair")
			public void swapSelectors2And3() {
				GuiActionRunner.execute(() -> {
					swap2_3.target().setEnabled(true);
				});

				// WHEN user clicks second swap button
				swap2_3.click();

				// Wait for the EDT to process the click event
				robot.waitForIdle();

				// THEN the correct swap request is sent to the sequence
				verify(mockSequence).swapRight(mockFillable2);
			}
		}
	}

}
