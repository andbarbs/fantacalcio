package swingViews;

import static org.mockito.Mockito.verify;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;

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

import domainModel.Player.Defender;
import swingViews.utilities.AssertJSwingJUnit5TestCase;

@DisplayName("A SubstituteSelectorTriplet")
@ExtendWith(MockitoExtension.class)
public class SubstituteSelectorTripletTest extends AssertJSwingJUnit5TestCase {

	private SubstituteSelectorTriplet<Defender> triplet; // SUT reference

	@Mock
	SubstitutePlayerSelector<Defender> mockSelector1, mockSelector2, mockSelector3;

	private JButtonFixture swap1_2, swap2_3;

	@BeforeEach
	public void testCaseSpecificSetup() {
		JFrame frame = GuiActionRunner.execute(() -> {

			Dimension selectorDims = new Dimension(120, 225); // appropriate dims for rendering a selector
			triplet = new SubstituteSelectorTriplet<Defender>(true, selectorDims);

			// sets up the test Frame
			JFrame f = new JFrame("Test Frame");
			f.setPreferredSize(new Dimension(550, 300));
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

	@Test
	@GUITest
	@DisplayName("contains three Substitute selectors")
	public void buttonsDisabled() {
		window.panel("selector1");
		window.panel("selector2");
		window.panel("selector3");
	}

	@Nested
	@DisplayName("contains two swap buttons, which")
	class ContainsSwapButtons {

		@Nested
		@DisplayName("are initially disabled")
		class Initially {

			@Test
			@GUITest
			@DisplayName("has both swap buttons disabled")
			public void buttonsDisabled() {
				swap1_2.requireDisabled();
				swap2_3.requireDisabled();
			}
		}

		@Nested
		@DisplayName("are subsequently")
		class Subsequently {

			@BeforeEach
			void wireUpMockSelectors() {
				triplet.setSelectors(mockSelector1, mockSelector2, mockSelector3);
			}

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
							triplet.becameFilled(mockSelector2);
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
							triplet.becameFilled(mockSelector3);
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
							triplet.becameEmpty(mockSelector3);
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
							triplet.becameEmpty(mockSelector2);
						});

						// THEN the first swap button is disabled
						swap1_2.requireDisabled();
					}
				}
			}
		}
	}

	@Nested
	@DisplayName("forwards user swap requests to sequence")
	class ForwardsSwapRequests {

		@Mock
		FillableSwappableSequence<SubstitutePlayerSelector<Defender>> mockSequence;

		@BeforeEach
		void wireUpMockSelectorsAndSequence() {
			triplet.setSelectors(mockSelector1, mockSelector2, mockSelector3);
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

				// THEN the correct swap request is sent to the sequence
				verify(mockSequence).swapRight(mockSelector1);
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

				// THEN the correct swap request is sent to the sequence
				verify(mockSequence).swapRight(mockSelector2);
			}
		}
	}

}
