package swingViews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;
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

import domainModel.Player;
import domainModel.Player.Defender;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel.Player.Midfielder;
import swingViews.SwingStarterLineUpChooser.Selector;
import swingViews.utilities.AssertJSwingJUnit5TestCase;

@DisplayName("A SwingStarterLineUpChooser")
@ExtendWith(MockitoExtension.class)
public class SwingStarterLineUpChooserTest extends AssertJSwingJUnit5TestCase {
	
	private Spring433Scheme panel433;
	private Spring343Scheme panel343;
	private Spring532Scheme panel532;
	
	private static class SelectorWidgetPair<P extends Player> {
		
		private Selector<P> selector;
		private JPanel widget;
		
		SelectorWidgetPair(Selector<P> selector, JPanel widget) {
			this.selector = selector;
			this.widget = widget;
		}		
	}	
	
	private SelectorWidgetPair<Goalkeeper> goaliePair;
	private SelectorWidgetPair<Defender> defPair1, defPair2, defPair3, defPair4, defPair5;
	private SelectorWidgetPair<Midfielder> midPair1, midPair2, midPair3, midPair4;
	private SelectorWidgetPair<Forward> forwPair1, forwPair2, forwPair3;
	
	private List<SelectorWidgetPair<?>> pairs;
	
	private @Mock Consumer<Selector<? extends Player>> mockConsumer;

	// the SUT reference
	private SwingStarterLineUpChooser chooser;

	@BeforeEach
	public void testCaseSpecificSetup(
			@Mock Selector<Goalkeeper> mockGoalieSel,
			@Mock Selector<Defender> mockDefSel1, 
			@Mock Selector<Defender> mockDefSel2, 
			@Mock Selector<Defender> mockDefSel3, 
			@Mock Selector<Defender> mockDefSel4, 
			@Mock Selector<Defender> mockDefSel5,
			@Mock Selector<Midfielder> mockMidSel1, 
			@Mock Selector<Midfielder> mockMidSel2, 
			@Mock Selector<Midfielder> mockMidSel3, 
			@Mock Selector<Midfielder> mockMidSel4,
			@Mock Selector<Forward> mockForwSel1, 
			@Mock Selector<Forward> mockForwSel2, 
			@Mock Selector<Forward> mockForwSel3
			) {
		JFrame frame = GuiActionRunner.execute(() -> {
			
			JFrame f = new JFrame("Test Frame");

			// instantiates scheme panels
			panel433 = new Spring433Scheme(false);
			panel343 = new Spring343Scheme(false);
			panel532 = new Spring532Scheme(false);			
			
			// instantiates & preps fake widgets
			goaliePair = new SelectorWidgetPair<>(mockGoalieSel, new JPanel()); 
			defPair1 = new SelectorWidgetPair<>(mockDefSel1, new JPanel()); 
			defPair2 = new SelectorWidgetPair<>(mockDefSel2, new JPanel()); 
			defPair3 = new SelectorWidgetPair<>(mockDefSel3, new JPanel()); 
			defPair4 = new SelectorWidgetPair<>(mockDefSel4, new JPanel()); 
			defPair5 = new SelectorWidgetPair<>(mockDefSel5, new JPanel()); 
			midPair1 = new SelectorWidgetPair<>(mockMidSel1, new JPanel()); 
			midPair2 = new SelectorWidgetPair<>(mockMidSel2, new JPanel()); 
			midPair3 = new SelectorWidgetPair<>(mockMidSel3, new JPanel()); 
			midPair4 = new SelectorWidgetPair<>(mockMidSel4, new JPanel()); 
			forwPair1 = new SelectorWidgetPair<>(mockForwSel1, new JPanel()); 
			forwPair2 = new SelectorWidgetPair<>(mockForwSel2, new JPanel()); 
			forwPair3 = new SelectorWidgetPair<>(mockForwSel3, new JPanel()); 
			
			pairs = List.of(goaliePair, 
					defPair1, defPair2, defPair3, defPair4, defPair5, 
					midPair1, midPair2, midPair3, midPair4, 
					forwPair1, forwPair2, forwPair3);
			
			Dimension screenSize = f.getToolkit().getScreenSize();
			Dimension availableWindow = new Dimension(
					(int) (screenSize.width * 0.2), 
					(int) (screenSize.height * 0.7));
			Dimension eventualFieldSize = SwingStarterLineUpChooser.eventualFieldDimension(availableWindow);
			Dimension widgetSize = SpringSchemePanel.recommendedSlotDimensions(eventualFieldSize);
			
			pairs.stream()
				.map(pair -> pair.widget)
				.forEach(widget -> {
					widget.setPreferredSize(widgetSize);
					widget.setBackground(Color.ORANGE);
			});
			
			// instantiates SUT
			chooser = new SwingStarterLineUpChooser(
					availableWindow,
					
					panel433, panel343, panel532,
					
					goaliePair.selector, goaliePair.widget,					
					defPair1.selector, defPair1.widget,
					defPair2.selector, defPair2.widget,
					defPair3.selector, defPair3.widget,
					defPair4.selector, defPair4.widget,
					defPair5.selector, defPair5.widget,
					
					midPair1.selector, midPair1.widget,
					midPair2.selector, midPair2.widget,
					midPair3.selector, midPair3.widget,
					midPair4.selector, midPair4.widget,
					
					forwPair1.selector, forwPair1.widget,
					forwPair2.selector, forwPair2.widget,
					forwPair3.selector, forwPair3.widget,
					
					mockConsumer);

			// sets up the test Frame			
			f.add(chooser);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return f;
		});

		window = new FrameFixture(robot, frame);
		window.show();

//		swap1_2 = window.button("swap1_2");
//		swap2_3 = window.button("swap2_3");
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
			}
		}
		
		@Test
		@GUITest
		@DisplayName("picks the '4-3-3' scheme by default")
		public void widgetsAddedTo433() {			

			// THEN only the '4-3-3' radio button is selected
			window.radioButton(withText("4-3-3")).requireSelected();
			window.radioButton(withText("3-4-3")).requireNotSelected();
			window.radioButton(withText("5-3-2")).requireNotSelected();
			
			// AND widgets within 433 are added to the 433 panel
			List<SelectorWidgetPair<?>> pairsIn433 = List.of(
					goaliePair, 
					defPair1, defPair2, defPair3, defPair4, 
					midPair1, midPair2, midPair3, 
					forwPair1, forwPair2, forwPair3);
			pairsIn433.stream().map(pair -> pair.widget).forEach(fakeWidget -> {
				assertThat(fakeWidget.getParent().getParent()).isSameAs(panel433);
			});

			// AND widgets outside 433 have no parent
			pairs.stream()
				.filter(pair -> !pairsIn433.contains(pair))
				.map(pair -> pair.widget).forEach(fakeWidget -> {
					assertThat(fakeWidget.getParent()).isNull();
			});
		}		
	}
	
	@Nested
	@DisplayName("switches to a different scheme")
	class SwitchesSchemes {			
		
		@Nested
		@DisplayName("when the user picks")
		class OnUserPicking {			
			
			@Test
			@GUITest
			@DisplayName("the '3-4-3' scheme")
			public void widgetsAddedTo343() {
				
				// WHEN the user selects the '3-4-3' scheme
				window.radioButton(withText("3-4-3")).click();				

				// THEN only the '3-4-3' radio button is selected
				window.radioButton(withText("4-3-3")).requireNotSelected();
				window.radioButton(withText("3-4-3")).requireSelected();
				window.radioButton(withText("5-3-2")).requireNotSelected();
				
				// AND widgets within '3-4-3' are added to the 343 panel
				List<SelectorWidgetPair<?>> pairsIn343 = List.of(
						goaliePair, 
						defPair1, defPair2, defPair3,
						midPair1, midPair2, midPair3, midPair4,
						forwPair1, forwPair2, forwPair3);
				pairsIn343.stream().map(pair -> pair.widget).forEach(fakeWidget -> {
					assertThat(fakeWidget.getParent().getParent()).isSameAs(panel343);
				});
				
				// AND widgets within '4-3-3' but not within '3-4-3' are excluded
				List<SelectorWidgetPair<?>> excludedPairs = List.of(
						defPair4);
				excludedPairs.stream().forEach(pair -> {
					assertThat(pair.widget.getParent()).isNull();
					verify(mockConsumer).accept(pair.selector);
				});

				// AND widgets outside both '4-3-3' and '3-4-3' are untouched
				pairs.stream()
					.filter(pair -> !pairsIn343.contains(pair))
					.filter(pair -> !excludedPairs.contains(pair))
					.forEach(pair -> {
						assertThat(pair.widget.getParent()).isNull();
						verify(mockConsumer, never()).accept(pair.selector);
				});
			}
			
			@Test
			@GUITest
			@DisplayName("the '5-3-2' scheme")
			public void widgetsAddedTo532() {
				
				// WHEN the user selects the '5-3-2' scheme
				window.radioButton(withText("5-3-2")).click();				

				// THEN only the '5-3-2' radio button is selected
				window.radioButton(withText("4-3-3")).requireNotSelected();
				window.radioButton(withText("3-4-3")).requireNotSelected();
				window.radioButton(withText("5-3-2")).requireSelected();
				
				// AND widgets within '5-3-2' are added to the 532 panel
				List<SelectorWidgetPair<?>> pairsIn532 = List.of(
						goaliePair, 
						defPair1, defPair2, defPair3, defPair4, defPair5,
						midPair1, midPair2, midPair3,
						forwPair1, forwPair2);
				pairsIn532.stream().map(pair -> pair.widget).forEach(fakeWidget -> {
					assertThat(fakeWidget.getParent().getParent()).isSameAs(panel532);
				});

				// AND widgets within '4-3-3' but not within '5-3-2' are excluded
				List<SelectorWidgetPair<?>> excludedPairs = List.of(
						forwPair3);
				excludedPairs.stream().forEach(pair -> {
					assertThat(pair.widget.getParent()).isNull();
					verify(mockConsumer).accept(pair.selector);
				});

				// AND widgets outside both '4-3-3' and '5-3-2' are untouched
				pairs.stream()
					.filter(pair -> !pairsIn532.contains(pair))
					.filter(pair -> !excludedPairs.contains(pair))
					.forEach(pair -> {
						assertThat(pair.widget.getParent()).isNull();
						verify(mockConsumer, never()).accept(pair.selector);
				});
			}
		}
	}
}
