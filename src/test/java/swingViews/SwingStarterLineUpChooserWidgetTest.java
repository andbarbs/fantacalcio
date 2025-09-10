package swingViews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JPanel;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import swingViews.LineUpScheme.*;
import swingViews.StarterLineUpChooser.StarterLineUpChooserWidget;
import swingViews.utilities.AssertJSwingJUnit5TestCase;

@DisplayName("A SwingStarterLineUpChooserWidget")
@ExtendWith(MockitoExtension.class)
public class SwingStarterLineUpChooserWidgetTest extends AssertJSwingJUnit5TestCase {
	
	private Spring433Scheme panel433;
	private Spring343Scheme panel343;
	private Spring532Scheme panel532;
	
	private JPanel goalieWidget;
	private JPanel defWidget1, defWidget2, defWidget3, defWidget4, defWidget5;
	private JPanel midWidget1, midWidget2, midWidget3, midWidget4;
	private JPanel forwWidget1, forwWidget2, forwWidget3;
	
	private List<JPanel> widgets, widgetsIn433, widgetsIn343, widgetsIn532;

	// the SUT reference
	private SwingStarterLineUpChooserWidget widget;

	@BeforeEach
	void testCaseSpecificSetup() {
		JFrame frame = GuiActionRunner.execute(() -> {
			
			JFrame f = new JFrame("Test Frame");

			// instantiates scheme panels
			panel433 = new Spring433Scheme(false);
			panel343 = new Spring343Scheme(false);
			panel532 = new Spring532Scheme(false);			
			
			// instantiates & preps fake widgets
			goalieWidget = new JPanel(); 
			defWidget1 = new JPanel(); 
			defWidget2 = new JPanel(); 
			defWidget3 = new JPanel(); 
			defWidget4 = new JPanel(); 
			defWidget5 = new JPanel(); 
			midWidget1 = new JPanel(); 
			midWidget2 = new JPanel(); 
			midWidget3 = new JPanel(); 
			midWidget4 = new JPanel(); 
			forwWidget1 = new JPanel(); 
			forwWidget2 = new JPanel(); 
			forwWidget3 = new JPanel(); 
			
			populateWidgetsLists();
			
			Dimension screenSize = f.getToolkit().getScreenSize();
			Dimension availableWindow = new Dimension(
					(int) (screenSize.width * 0.2), 
					(int) (screenSize.height * 0.7));
			Dimension eventualFieldSize = SwingStarterLineUpChooserWidget.eventualFieldDimension(availableWindow);
			Dimension widgetSize = SpringSchemePanel.recommendedSlotDimensions(eventualFieldSize);
			
			widgets.stream()
				.forEach(widget -> {
					widget.setPreferredSize(widgetSize);
					widget.setBackground(Color.ORANGE);
			});
			
			// instantiates SUT
			widget = new SwingStarterLineUpChooserWidget(
					false,					
					availableWindow,					
					panel433, panel343, panel532,					
					goalieWidget,					
					defWidget1, defWidget2, defWidget3, defWidget4, defWidget5,					
					midWidget1, midWidget2, midWidget3, midWidget4,					
					forwWidget1, forwWidget2, forwWidget3);

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

	private void populateWidgetsLists() {
		widgets = List.of(goalieWidget, 
				defWidget1, defWidget2, defWidget3, defWidget4, defWidget5, 
				midWidget1, midWidget2, midWidget3, midWidget4, 
				forwWidget1, forwWidget2, forwWidget3);
		
		// by scheme
		widgetsIn433 = List.of(
				goalieWidget, 
				defWidget1, defWidget2, defWidget3, defWidget4, 
				midWidget1, midWidget2, midWidget3, 
				forwWidget1, forwWidget2, forwWidget3);
		widgetsIn343 = List.of(
				goalieWidget, 
				defWidget1, defWidget2, defWidget3,
				midWidget1, midWidget2, midWidget3, midWidget4,
				forwWidget1, forwWidget2, forwWidget3);
		widgetsIn532 = List.of(
				goalieWidget, 
				defWidget1, defWidget2, defWidget3, defWidget4, defWidget5,
				midWidget1, midWidget2, midWidget3,
				forwWidget1, forwWidget2);
	}

	@Nested
	@DisplayName("as instantiated")
	class JustInstantiated {
		
		@Nested
		@DisplayName("contains")
		class Contains {			
			
			@Test
			@GUITest
			@DisplayName("the scheme panels and three corresponding radio buttons")
			public void graphicalContents() {
				
				// THEN all tree scheme panels are added to the SUT hierarchy
				window.panel(sameAs(panel433));
				window.panel(sameAs(panel343));
				window.panel(sameAs(panel532));

				// AND three corresponding radio buttons are also present
				window.radioButton(withText("4-3-3"));
				window.radioButton(withText("3-4-3"));
				window.radioButton(withText("5-3-2"));
				
				// none of which is selected
				window.radioButton(withText("4-3-3")).requireNotSelected();
				window.radioButton(withText("3-4-3")).requireNotSelected();
				window.radioButton(withText("5-3-2")).requireNotSelected();
			}
		}		
	}
	
	@Nested
	@DisplayName("allows a graphical user to")
	class ForGraphicalUsers {
		
		@Mock
		private StarterLineUpChooserController controller;
		
		@BeforeEach
		void testCaseSpecificSetup() {
			widget.setController(controller);
		}		

		@Nested
		@DisplayName("request a scheme change to")
		class SwitchesSchemes {
			
			@Test
			@GUITest
			@DisplayName("the '3-4-3' scheme")
			public void forwardsRequestFor343() {
				
				// WHEN the user selects the '3-4-3' scheme
				window.radioButton(withText("3-4-3")).click();
				robot.waitForIdle();
				
				// THEN a corresponding request is sent to the Controller
				verify(controller).switchToScheme(new Scheme343());
			}
			
			@Test
			@GUITest
			@DisplayName("the '5-3-2' scheme")
			public void forwardsRequestFor532() {
				
				// WHEN the user selects the '5-3-2' scheme
				window.radioButton(withText("5-3-2")).click();
				robot.waitForIdle();				

				// THEN a corresponding request is sent to the Controller
				verify(controller).switchToScheme(new Scheme532());
			}
			
			@Test
			@GUITest
			@DisplayName("the '4-3-3' scheme")
			public void forwardsRequestFor433() {
				
				// WHEN the user selects the '5-3-2' scheme
				window.radioButton(withText("4-3-3")).click();
				robot.waitForIdle();				

				// THEN a corresponding request is sent to the Controller
				verify(controller).switchToScheme(new Scheme433());
			}
		}

	}
	
	@Nested
	@DisplayName("satisfies Controller requests")
	class AsMVPWidget {
		
		private StarterLineUpChooserWidget asWidget;

		@BeforeEach
		void testCaseSpecificSetup() {
			asWidget = widget;
			
			// GIVEN selector widgets are forcibly removed from whatever scheme
			GuiActionRunner.execute(() -> {
				widgets.forEach(widget -> Optional.ofNullable(widget.getParent())
						.ifPresent(parent -> parent.remove(widget)));
			});
		}

		@Nested
		@DisplayName("to change to")
		class SwitchesSchemes {
			
			@Test
			@GUITest
			@DisplayName("the '3-4-3' scheme")
			public void changesTo343() {
				
				// WHEN the Controller requests changing to the '3-4-3' scheme
				GuiActionRunner.execute(() -> asWidget.switchTo(new Scheme343()));
				
				// THEN the '3-4-3' radio button becomes selected
				window.radioButton(withText("3-4-3")).requireSelected();
				
				// AND widgets within '3-4-3' are added to the 343 panel
				widgetsIn343.stream().forEach(fakeWidget ->
					assertThat(fakeWidget.getParent().getParent()).isSameAs(panel343));				
				// WHILE widgets not within '3-4-3' are without parent
				widgets.stream().filter(Widget -> !widgetsIn343.contains(Widget))
				.forEach(Widget -> assertThat(Widget.getParent()).isNull());
			}
			
			@Test
			@GUITest
			@DisplayName("the '5-3-2' scheme")
			public void changesTo532() {
				
				// WHEN the Controller requests changing to the '5-3-2' scheme
				GuiActionRunner.execute(() -> asWidget.switchTo(new Scheme532()));
				
				// THEN the '5-3-2' radio button becomes selected
				window.radioButton(withText("5-3-2")).requireSelected();
				
				// AND  widgets within '5-3-2' are added to the 532 panel
				widgetsIn532.stream().forEach(fakeWidget ->
					assertThat(fakeWidget.getParent().getParent()).isSameAs(panel532));				
				// WHILE widgets not within '5-3-2' are without parent
				widgets.stream().filter(Widget -> !widgetsIn532.contains(Widget))
				.forEach(Widget -> assertThat(Widget.getParent()).isNull());
			}
			
			@Test
			@GUITest
			@DisplayName("the '4-3-3' scheme")
			public void changesTo433() {
				
				// WHEN the Controller requests changing to the '4-3-3' scheme
				GuiActionRunner.execute(() -> asWidget.switchTo(new Scheme433()));
				
				// THEN the '4-3-3' radio button becomes selected
				window.radioButton(withText("4-3-3")).requireSelected();
				
				// AND widgets within '4-3-3' are added to the 433 panel
				widgetsIn433.stream().forEach(fakeWidget ->
					assertThat(fakeWidget.getParent().getParent()).isSameAs(panel433));				
				// WHILE widgets not within '4-3-3' are without parent
				widgets.stream().filter(Widget -> !widgetsIn433.contains(Widget))
				.forEach(Widget -> assertThat(Widget.getParent()).isNull());
			}
		}
	}

}

