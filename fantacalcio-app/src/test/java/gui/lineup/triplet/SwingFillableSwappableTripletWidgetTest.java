package gui.lineup.triplet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.mockito.Mockito.verify;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.timing.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gui.utils.AssertJSwingJupiterTestCase;
import gui.utils.AssertJSwingUtils;
import gui.utils.GUITestExtension;

@DisplayName("A SwingFillableSwappableTriplet")
@ExtendWith(MockitoExtension.class)
@ExtendWith(GUITestExtension.class)
@Tag("non-JPMS-compliant")
@Tag("mockito-agent")
class SwingFillableSwappableTripletWidgetTest extends AssertJSwingJupiterTestCase {
	
	private JPanel fakeWidget1, fakeWidget2, fakeWidget3;

	// the SUT reference
	private SwingFillableSwappableTripletWidget triplet;	

	private JButtonFixture swap1_2, swap2_3;
	private FrameFixture window;

	@Override
	protected void onSetUp() throws Exception {
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
			
			triplet = new SwingFillableSwappableTripletWidget(false, 
					fakeWidget1,
					fakeWidget2,
					fakeWidget3);
			
			// sets up the test Frame
			JFrame f = new JFrame("Test Frame");
			f.add(triplet);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return f;
		});
		
		window = new FrameFixture(robot(), frame);
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
			@DisplayName("can subsequently be")
			class Subsequently {
				
				@Test
				@GUITest
				@DisplayName("enabled")
				public void controllerEnables() {
					GuiActionRunner.execute(() -> {
						List.of(swap1_2, swap2_3).forEach(button -> button.target().setEnabled(false));
					});
					
					// WHEN Controller requests enabling first pair swap
					GuiActionRunner.execute(() -> triplet.setSwappingFirstPair(true));
					
					// THEN the first swap button is enabled
					swap1_2.requireEnabled();
					
					// WHEN Controller requests enabling second pair swap
					GuiActionRunner.execute(() -> triplet.setSwappingSecondPair(true));
					
					// THEN the second swap button is enabled
					swap2_3.requireEnabled();
				}

				@Test
				@GUITest
				@DisplayName("disabled")
				public void controllerDisables() {
					GuiActionRunner.execute(() -> {
						List.of(swap1_2, swap2_3).forEach(button -> button.target().setEnabled(true));
					});
					
					// WHEN Controller requests enabling first pair swap
					GuiActionRunner.execute(() -> triplet.setSwappingFirstPair(false));
					
					// THEN the first swap button is enabled
					swap1_2.requireDisabled();
					
					// WHEN Controller requests enabling second pair swap
					GuiActionRunner.execute(() -> triplet.setSwappingSecondPair(false));
					
					// THEN the second swap button is enabled
					swap2_3.requireDisabled();
				}
			}
		}
	}

	@Nested
	@DisplayName("forwards user swap requests to Controller")
	class ForwardsSwapRequests {

		@Mock
		FillableSwappableTripletController mockController;

		@BeforeEach
		void wireUpMockSelectorsAndSequence() {
			triplet.setController(mockController);
		}

		@Nested
		@DisplayName("when asked by user to")
		class OnUserClickTo {

			private final static int TIMEOUT = 2000;

			@Test
			@GUITest
			@DisplayName("swap the first selector pair")
			public void swapSelectors1And2() {
				GuiActionRunner.execute(() -> {
					swap1_2.target().setEnabled(true);
				});				

				// added a diagnostic step
				AssertJSwingUtils.diagnosePreClickState(swap1_2, window);

				// WHEN user clicks first swap button
				swap1_2.click();	
				
				// Store the start time
				long startTime = System.currentTimeMillis();
				
				// THEN the correct swap request is sent to the Controller
				pause(new Condition("FillableSwappableTripletController.swapFirstPair to be called") {
			        @Override
			        public boolean test() {
			            try {
			            	verify(mockController).swapFirstPair();
			                return true; 
			            } catch (Throwable e) {
			                return false;
			            }
			        }
			    }, TIMEOUT);
				
				// Calculate the elapsed time *after* the pause successfully returns
				long endTime = System.currentTimeMillis();
				long elapsedTimeMs = endTime - startTime;
				
				// Print the elapsed time to the console
				System.out.println("####################### FLAKY TEST A #####################");
				System.out.println("FillableSwappableTripletController.swapFirstPair called in: " 
						+ (elapsedTimeMs / 1000.0) + " s");
			}

			@Test
			@GUITest
			@DisplayName("swap the second selector pair")
			public void swapSelectors2And3() {
				GuiActionRunner.execute(() -> {
					swap2_3.target().setEnabled(true);
				});
				
				// added a diagnostic step
				AssertJSwingUtils.diagnosePreClickState(swap2_3, window);

				// WHEN user clicks second swap button
				swap2_3.click();	
				
				// Store the start time
				long startTime = System.currentTimeMillis();				
				
				// THEN the correct swap request is sent to the Controller
				pause(new Condition("FillableSwappableTripletController.swapSecondPair to be called") {
			        @Override
			        public boolean test() {
			            try {
			            	verify(mockController).swapSecondPair();
			                return true; 
			            } catch (Throwable e) {
			                return false;
			            }
			        }
			    }, TIMEOUT);
				
				// Calculate the elapsed time *after* the pause successfully returns
				long endTime = System.currentTimeMillis();
				long elapsedTimeMs = endTime - startTime;
				
				// Print the elapsed time to the console
				System.out.println("####################### FLAKY TEST b #####################");
				System.out.println("FillableSwappableTripletController.swapSecondPair called in: " 
						+ (elapsedTimeMs / 1000.0) + " s");
			}
		}
	}
}
