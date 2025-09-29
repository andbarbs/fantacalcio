package gui.lineup.starter;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domainModel.Scheme;
import gui.utils.AssertJSwingJUnit5TestCase;
import gui.utils.schemes.SpringSchemePanel;

/**
 * <h1>Unit test isolation</h1> two test-specific <b>fake</b> {@link Scheme}
 * instances and corresponding {@link SpringSchemePanel}s are used to maximize
 * isolation. Correctness is assumed, however, for the {@link SpringSchemePanel}
 * superclass
 * 
 * <p>
 * <h1>Compositional cardinality</h1> as specified in
 * {@link SwingStarterLineUpChooserWidget}, the number of {@link JPanel}
 * instances that this type composes substantially determines its runtime
 * behavior. Test instantiation happens at
 * {@link SwingStarterLineUpChooserWidgetTest#instantiator()}
 */
@DisplayName("A SwingStarterLineUpChooserWidget")
@ExtendWith(MockitoExtension.class)
@Tag("non-JPMS-compliant")
public class SwingStarterLineUpChooserWidgetTest extends AssertJSwingJUnit5TestCase {
	
	// private constants simulate Singletons
	private static final Scheme scheme123 = new Scheme(1, 2, 3) {

		@Override
		public void accept(SchemeVisitor visitor) {} 
	};
	
	private static final Scheme scheme321 = new Scheme(3, 2, 1) {
		
		@Override
		public void accept(SchemeVisitor visitor) {} 
	};
	
	private SpringSchemePanel panel123, panel321;
	
	private JPanel goalieWidget;
	private JPanel defWidget1, defWidget2, defWidget3;
	private JPanel midWidget1, midWidget2, midWidget3;
	private JPanel forwWidget1, forwWidget2, forwWidget3;
	
	private List<JPanel> selectorWidgets;

	// the SUT reference
	private SwingStarterLineUpChooserWidget widget;

	@SuppressWarnings("serial")
	@BeforeEach
	void instantiator() {
		JFrame frame = GuiActionRunner.execute(() -> {
			
			JFrame f = new JFrame("Test Frame");		
			
			// instantiates & preps fake selectorWidgets
			goalieWidget = new JPanel(); 
			defWidget1 = new JPanel(); 
			defWidget2 = new JPanel(); 
			defWidget3 = new JPanel();
			midWidget1 = new JPanel(); 
			midWidget2 = new JPanel(); 
			midWidget3 = new JPanel();
			forwWidget1 = new JPanel();
			forwWidget2 = new JPanel(); 
			forwWidget3 = new JPanel();
			
			selectorWidgets = List.of(goalieWidget, 
					defWidget1, defWidget2, defWidget3, 
					midWidget1, midWidget2, midWidget3,
					forwWidget1, forwWidget2, forwWidget3);
			
			Dimension screenSize = f.getToolkit().getScreenSize();
			Dimension availableWindow = new Dimension(
					(int) (screenSize.width * 0.2), 
					(int) (screenSize.height * 0.7));
			Dimension eventualFieldSize = SwingStarterLineUpChooserWidget.eventualFieldDimension(availableWindow);
			Dimension widgetSize = SpringSchemePanel.recommendedSlotDimensions(eventualFieldSize);
			
			selectorWidgets.stream()
				.forEach(widget -> {
					widget.setPreferredSize(widgetSize);
					widget.setBackground(Color.ORANGE);
			});
			
			panel123 = new SpringSchemePanel(scheme123) {
				
				@Override
				public void accept(SpringSchemeVisitor visitor) {}
			};
			
			panel321 = new SpringSchemePanel(scheme321) {
				
				@Override
				public void accept(SpringSchemeVisitor visitor) {}
			};
			
			// instantiates SUT on a (3, 3, 3) Selector widget cardinality
			widget = new SwingStarterLineUpChooserWidget(
					false,					
					availableWindow,					
					List.of(panel123, panel321),					
					goalieWidget,					
					List.of(defWidget1, defWidget2, defWidget3),					
					List.of(midWidget1, midWidget2, midWidget3),					
					List.of(forwWidget1, forwWidget2, forwWidget3));

			// sets up the test Frame			
			f.add(widget);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return f;
		});

		window = new FrameFixture(robot, frame);
		window.show();
	}

	@Nested
	@DisplayName("once instantiated")
	class JustInstantiated {
		
		@Test
		@GUITest
		@DisplayName("contains the scheme panels and as many radio buttons")
		public void graphicalContents() {
			
			// THEN all scheme panels are added to the SUT hierarchy
			window.panel(sameAs(panel123));
			window.panel(sameAs(panel321));
			
			// AND three corresponding radio buttons, none of which is selected
			window.radioButton(withText("1-2-3")).requireNotSelected();
			window.radioButton(withText("3-2-1")).requireNotSelected();
		}
	}
	
	@Nested
	@DisplayName("as a StarterLineUpChooserWidget")
	class AsAStarterLineUpChooserWidget {
		
		@Nested
		@DisplayName("notifies to the Controller a user request to")
		class ForGraphicalUsers {
			
			@Mock
			private StarterLineUpChooserController controller;
			
			@BeforeEach
			void setController() {
				widget.setController(controller);
			}
			
			@Test
			@GUITest
			@DisplayName("switch to a scheme")
			public void forwardsRequest() {
				
				// WHEN the user selects a scheme on the radios
				window.radioButton(withText("1-2-3")).click();
				robot.waitForIdle();
				
				// THEN a corresponding request is sent to the Controller
				verify(controller).switchToScheme(scheme123);
			}
		}
		
		@Nested
		@DisplayName("satisfies Controller requests to switch to")
		class AsMVPWidget {
			
			@BeforeEach
			void ensureNoParent() {
				
				// GIVEN selector widgets are forcibly removed from whatever scheme
				GuiActionRunner.execute(() -> selectorWidgets.forEach(
						widget -> Optional.ofNullable(widget.getParent()).ifPresent(
								parent -> parent.remove(widget))));
			}
			
			@Nested
			@DisplayName("a Scheme that is feasible")
			class AskedToSwitchToFeasibleScheme {
				
				@Test
				@GUITest
				@DisplayName("when no previous scheme existed")
				public void noPreviousSchemeExisted() {
					
					// WHEN the Controller requests changing to the '1-2-3' scheme
					GuiActionRunner.execute(() -> widget.switchTo(scheme123));
					
					// THEN the '1-2-3' radio button becomes selected
					window.radioButton(withText("1-2-3")).requireSelected();
					
					// AND Selector widgets within '1-2-3' are added to the appropriate panel
					List<JPanel> selsIn123 = List.of(goalieWidget, 
							defWidget1, 
							midWidget1, midWidget2, 
							forwWidget1, forwWidget2, forwWidget3);
					selsIn123.stream()
						.forEach(fakeWidget -> assertThat(fakeWidget.getParent().getParent()).isSameAs(panel123));
					
					// AND Selector widgets not within '1-2-3' are without parent
					selectorWidgets.stream().filter(not(selsIn123::contains))
						.forEach(widget -> assertThat(widget.getParent()).isNull());
				}
				
				
				@Test
				@GUITest
				@DisplayName("when a previous scheme existed")
				public void previousSchemeExisted() {
					
					// GIVEN selectors in '1-2-3' are manually wired to the scheme panel
					GuiActionRunner.execute(() -> {
						panel123.getGoalieSlot().add(goalieWidget);
						panel123.getDefenderSlots().get(0).add(defWidget1);
						panel123.getMidfielderSlots().get(0).add(midWidget1);
						panel123.getMidfielderSlots().get(1).add(midWidget2);
						panel123.getForwardSlots().get(0).add(forwWidget1);
						panel123.getForwardSlots().get(1).add(forwWidget2);
						panel123.getForwardSlots().get(2).add(forwWidget3);
					});
					
					// WHEN the Controller requests changing to the '3-2-1' scheme
					GuiActionRunner.execute(() -> widget.switchTo(scheme321));
					
					// THEN the corresponding radio button becomes selected
					window.radioButton(withText("3-2-1")).requireSelected();
					
					// AND the other radio button becomes not selected
					window.radioButton(withText("1-2-3")).requireNotSelected();
					
					// AND Selector widgets within '3-2-1' are added to the corresponding panel
					List<JPanel> widgetsIn321 = List.of(
							goalieWidget, 
							defWidget1, defWidget2, defWidget3, 
							midWidget1, midWidget2, 
							forwWidget1);
					widgetsIn321.stream()
						.forEach(fakeWidget -> assertThat(fakeWidget.getParent().getParent()).isSameAs(panel321));
					
					// AND Selector widgets not within '3-2-1' have no parent
					selectorWidgets.stream().filter(not(widgetsIn321::contains))
						.forEach(widget -> assertThat(widget.getParent()).isNull());
				}
			}
			
			@Nested
			@DisplayName("a scheme that has too many")
			class AskedToSwitchToInfeasibleScheme {
				
				@Nested
				@DisplayName("Defenders")
				class TooManyDefenders {
					
					private static final Scheme tooManyDefenders = new Scheme(5, 1, 1) {

						@Override
						public void accept(SchemeVisitor visitor) {} 
					};
					
					@Test
					@GUITest
					@DisplayName("when no previous scheme existed")
					public void noPreviousSchemeExisted() {
						
						// WHEN the Controller requests changing to a Scheme that is not feasible
						ThrowingCallable shouldThrow = () -> 
							GuiActionRunner.execute(() -> widget.switchTo(tooManyDefenders));
						
						// THEN an error is thrown 
						assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining(
									"requested Scheme has 5 Defenders, this composes only 3 widgets for Defender");
						
						// THEN no radio button becomes selected
						window.radioButton(withText("1-2-3")).requireNotSelected();
						window.radioButton(withText("3-2-1")).requireNotSelected();
						
						// AND Selector widgets are not given a parent
						selectorWidgets.stream().forEach(widget -> assertThat(widget.getParent()).isNull());
					}					
					
					@Test
					@GUITest
					@DisplayName("when a previous scheme existed")
					public void previousSchemeExisted() {
						
						// GIVEN selectors in '1-2-3' are manually inserted in the scheme panel
						GuiActionRunner.execute(() -> {
							panel123.getGoalieSlot().add(goalieWidget);
							panel123.getDefenderSlots().get(0).add(defWidget1);
							panel123.getMidfielderSlots().get(0).add(midWidget1);
							panel123.getMidfielderSlots().get(1).add(midWidget2);
							panel123.getForwardSlots().get(0).add(forwWidget1);
							panel123.getForwardSlots().get(1).add(forwWidget2);
							panel123.getForwardSlots().get(2).add(forwWidget3);
						});
						
						// WHEN the Controller requests changing to a Scheme that is not feasible
						ThrowingCallable shouldThrow = () -> 
							GuiActionRunner.execute(() -> widget.switchTo(tooManyDefenders));
						
						// THEN an error is thrown 
						assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining(
									"requested Scheme has 5 Defenders, this composes only 3 widgets for Defender");
						
						// AND radio buttons reflect the state prior to the call
						window.radioButton(withText("1-2-3")).requireNotSelected();
						window.radioButton(withText("3-2-1")).requireNotSelected();
						
						// AND Selector widget attachment reflects the state prior to the call
						List<JPanel> selsIn123 = List.of(goalieWidget, 
								defWidget1, 
								midWidget1, midWidget2, 
								forwWidget1, forwWidget2, forwWidget3);
						selsIn123.stream()
							.forEach(fakeWidget -> assertThat(fakeWidget.getParent().getParent()).isSameAs(panel123));
						selectorWidgets.stream().filter(not(selsIn123::contains))
							.forEach(widget -> assertThat(widget.getParent()).isNull());
					}
				}
				
				@Nested
				@DisplayName("Midfielders")
				class TooManyMidfielders {
					
					private static final Scheme tooManyMidfielders = new Scheme(1, 5, 1) {

						@Override
						public void accept(SchemeVisitor visitor) {} 
					};
					
					@Test
					@GUITest
					@DisplayName("when no previous scheme existed")
					public void noPreviousSchemeExisted() {
						
						// WHEN the Controller requests changing to a Scheme that is not feasible
						ThrowingCallable shouldThrow = () -> 
							GuiActionRunner.execute(() -> widget.switchTo(tooManyMidfielders));
						
						// THEN an error is thrown 
						assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining(
									"requested Scheme has 5 Midfielders, this composes only 3 widgets for Midfielder");
						
						// THEN no radio button becomes selected
						window.radioButton(withText("1-2-3")).requireNotSelected();
						window.radioButton(withText("3-2-1")).requireNotSelected();
						
						// AND Selector widgets are not given a parent
						selectorWidgets.stream().forEach(widget -> assertThat(widget.getParent()).isNull());
					}					
					
					@Test
					@GUITest
					@DisplayName("when a previous scheme existed")
					public void previousSchemeExisted() {
						
						// GIVEN selectors in '1-2-3' are manually inserted in the scheme panel
						GuiActionRunner.execute(() -> {
							panel123.getGoalieSlot().add(goalieWidget);
							panel123.getDefenderSlots().get(0).add(defWidget1);
							panel123.getMidfielderSlots().get(0).add(midWidget1);
							panel123.getMidfielderSlots().get(1).add(midWidget2);
							panel123.getForwardSlots().get(0).add(forwWidget1);
							panel123.getForwardSlots().get(1).add(forwWidget2);
							panel123.getForwardSlots().get(2).add(forwWidget3);
						});
						
						// WHEN the Controller requests changing to a Scheme that is not feasible
						ThrowingCallable shouldThrow = () -> 
							GuiActionRunner.execute(() -> widget.switchTo(tooManyMidfielders));
						
						// THEN an error is thrown 
						assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining(
									"requested Scheme has 5 Midfielders, this composes only 3 widgets for Midfielder");
						
						// AND radio buttons reflect the state prior to the call
						window.radioButton(withText("1-2-3")).requireNotSelected();
						window.radioButton(withText("3-2-1")).requireNotSelected();
						
						// AND Selector widget attachment reflects the state prior to the call
						List<JPanel> selsIn123 = List.of(goalieWidget, 
								defWidget1, 
								midWidget1, midWidget2, 
								forwWidget1, forwWidget2, forwWidget3);
						selsIn123.stream()
							.forEach(fakeWidget -> assertThat(fakeWidget.getParent().getParent()).isSameAs(panel123));
						selectorWidgets.stream().filter(not(selsIn123::contains))
							.forEach(widget -> assertThat(widget.getParent()).isNull());
					}
				}
				
				@Nested
				@DisplayName("Forwards")
				class TooManyForwards {
					
					private static final Scheme tooManyForwards = new Scheme(1, 1, 5) {

						@Override
						public void accept(SchemeVisitor visitor) {} 
					};
					
					@Test
					@GUITest
					@DisplayName("when no previous scheme existed")
					public void noPreviousSchemeExisted() {
						
						// WHEN the Controller requests changing to a Scheme that is not feasible
						ThrowingCallable shouldThrow = () -> 
							GuiActionRunner.execute(() -> widget.switchTo(tooManyForwards));
						
						// THEN an error is thrown 
						assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining(
									"requested Scheme has 5 Forwards, this composes only 3 widgets for Forward");
						
						// THEN no radio button becomes selected
						window.radioButton(withText("1-2-3")).requireNotSelected();
						window.radioButton(withText("3-2-1")).requireNotSelected();
						
						// AND Selector widgets are not given a parent
						selectorWidgets.stream().forEach(widget -> assertThat(widget.getParent()).isNull());
					}					
					
					@Test
					@GUITest
					@DisplayName("when a previous scheme existed")
					public void previousSchemeExisted() {
						
						// GIVEN selectors in '1-2-3' are manually inserted in the scheme panel
						GuiActionRunner.execute(() -> {
							panel123.getGoalieSlot().add(goalieWidget);
							panel123.getDefenderSlots().get(0).add(defWidget1);
							panel123.getMidfielderSlots().get(0).add(midWidget1);
							panel123.getMidfielderSlots().get(1).add(midWidget2);
							panel123.getForwardSlots().get(0).add(forwWidget1);
							panel123.getForwardSlots().get(1).add(forwWidget2);
							panel123.getForwardSlots().get(2).add(forwWidget3);
						});
						
						// WHEN the Controller requests changing to a Scheme that is not feasible
						ThrowingCallable shouldThrow = () -> 
							GuiActionRunner.execute(() -> widget.switchTo(tooManyForwards));
						
						// THEN an error is thrown 
						assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
							.hasMessageContaining(
									"requested Scheme has 5 Forwards, this composes only 3 widgets for Forward");
						
						// AND radio buttons reflect the state prior to the call
						window.radioButton(withText("1-2-3")).requireNotSelected();
						window.radioButton(withText("3-2-1")).requireNotSelected();
						
						// AND Selector widget attachment reflects the state prior to the call
						List<JPanel> selsIn123 = List.of(goalieWidget, 
								defWidget1, 
								midWidget1, midWidget2, 
								forwWidget1, forwWidget2, forwWidget3);
						selsIn123.stream()
							.forEach(fakeWidget -> assertThat(fakeWidget.getParent().getParent()).isSameAs(panel123));
						selectorWidgets.stream().filter(not(selsIn123::contains))
							.forEach(widget -> assertThat(widget.getParent()).isNull());
					}
				}
			}
		}
	}	
}

