package swingViews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
	
	private static final Forward FAKE_FORWARD = new Forward(null, null);
	private static final Midfielder FAKE_MIDFIELDER = new Midfielder(null, null);
	private static final Defender FAKE_DEFENDER = new Defender(null, null);
	private static final Goalkeeper FAKE_GOALIE = new Goalkeeper(null, null);
	
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
	
	private List<SelectorWidgetPair<?>> pairs, pairsIn433, pairsIn343, pairsIn532;
	private List<SelectorWidgetPair<Defender>> defPairs;
	private List<SelectorWidgetPair<Midfielder>> midPairs;
	private List<SelectorWidgetPair<Forward>> forwPairs;
	
	private @Mock Consumer<Selector<? extends Player>> mockConsumer;

	// the SUT reference
	private SwingStarterLineUpChooser chooser;

	@BeforeEach
	void testCaseSpecificSetup(
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
			
			populatePairsLists();
			
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
					false,
					
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

	private void populatePairsLists() {
		// by role
		defPairs = List.of(defPair1, defPair2, defPair3, defPair4, defPair5);
		midPairs = List.of(midPair1, midPair2, midPair3, midPair4);
		forwPairs = List.of(forwPair1, forwPair2, forwPair3);
		
		pairs = List.of(goaliePair, 
				defPair1, defPair2, defPair3, defPair4, defPair5, 
				midPair1, midPair2, midPair3, midPair4, 
				forwPair1, forwPair2, forwPair3);
		
		// by scheme
		pairsIn433 = List.of(
				goaliePair, 
				defPair1, defPair2, defPair3, defPair4, 
				midPair1, midPair2, midPair3, 
				forwPair1, forwPair2, forwPair3);
		pairsIn343 = List.of(
				goaliePair, 
				defPair1, defPair2, defPair3,
				midPair1, midPair2, midPair3, midPair4,
				forwPair1, forwPair2, forwPair3);
		pairsIn532 = List.of(
				goaliePair, 
				defPair1, defPair2, defPair3, defPair4, defPair5,
				midPair1, midPair2, midPair3,
				forwPair1, forwPair2);
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
			pairsIn433.stream().map(pair -> pair.widget).forEach(fakeWidget -> {
				assertThat(fakeWidget.getParent().getParent()).isSameAs(panel433);
			});

			// while widgets outside 433 have no parent
			pairs.stream()
				.filter(pair -> !pairsIn433.contains(pair))
				.map(pair -> pair.widget).forEach(fakeWidget -> {
					assertThat(fakeWidget.getParent()).isNull();
			});
			
			// with no selectors being excluded
			verifyNoInteractions(mockConsumer);
		}		
	}
	
	@Nested
	@DisplayName("allows a graphical user to")
	class ForGraphicalUsers {

		@Nested
		@DisplayName("access widgets for")
		class SwitchesSchemes {
			
			@Test
			@GUITest
			@DisplayName("the '3-4-3' scheme")
			public void widgetsAddedTo343() {
				
				// WHEN the user selects the '3-4-3' scheme
				window.radioButton(withText("3-4-3")).click();
				robot.waitForIdle();
				
				// THEN only the '3-4-3' radio button is selected
				window.radioButton(withText("4-3-3")).requireNotSelected();
				window.radioButton(withText("3-4-3")).requireSelected();
				window.radioButton(withText("5-3-2")).requireNotSelected();
				
				// AND widgets within '3-4-3' are added to the 343 panel
				pairsIn343.stream().map(pair -> pair.widget).forEach(fakeWidget -> {
					assertThat(fakeWidget.getParent().getParent()).isSameAs(panel343);
				});
				
				// WHILE widgets not within '3-4-3' are without parent
				pairs.stream().filter(pair -> !pairsIn343.contains(pair))
				.forEach(pair -> assertThat(pair.widget.getParent()).isNull());
				
				// AND widgets within '4-3-3' but not within '3-4-3' are excluded
				pairsIn433.stream().filter(pair -> !pairsIn343.contains(pair))
				.forEach(pair -> verify(mockConsumer).accept(pair.selector));
				
				// with no other selectors being excluded
				verifyNoMoreInteractions(mockConsumer);
			}
			
			@Test
			@GUITest
			@DisplayName("the '5-3-2' scheme")
			public void widgetsAddedTo532() {
				
				// WHEN the user selects the '5-3-2' scheme
				window.radioButton(withText("5-3-2")).click();
				robot.waitForIdle();
				
				// THEN only the '5-3-2' radio button is selected
				window.radioButton(withText("4-3-3")).requireNotSelected();
				window.radioButton(withText("3-4-3")).requireNotSelected();
				window.radioButton(withText("5-3-2")).requireSelected();
				
				// AND widgets within '5-3-2' are added to the 532 panel
				pairsIn532.stream().map(pair -> pair.widget).forEach(fakeWidget -> {
					assertThat(fakeWidget.getParent().getParent()).isSameAs(panel532);
				});
				
				// while widgets not within '5-3-2' are without parent
				pairs.stream().filter(pair -> !pairsIn532.contains(pair))
				.forEach(pair -> assertThat(pair.widget.getParent()).isNull());
				
				// AND widgets within '4-3-3' but not within '5-3-2' are excluded
				pairsIn433.stream().filter(pair -> !pairsIn532.contains(pair))
				.forEach(pair -> verify(mockConsumer).accept(pair.selector));
				
				// with no other selectors being excluded
				verifyNoMoreInteractions(mockConsumer);
			}
		}
	}
	
	@Nested
	@DisplayName("allows a programmatic client to")
	class ForClients {
		
		@Test
		@GUITest
		@DisplayName("retrieve selectors")
		public void retrieveSelectors() {

			assertThat(chooser.getGoalieSelector()).isSameAs(goaliePair.selector);
			assertThat(chooser.getDefenderSelectors()).containsExactlyElementsOf(
					defPairs.stream().map(pair -> pair.selector).collect(Collectors.toList()));
			assertThat(chooser.getMidfielderSelectors()).containsExactlyElementsOf(
					midPairs.stream().map(pair -> pair.selector).collect(Collectors.toList()));
			assertThat(chooser.getForwardSelectors()).containsExactlyElementsOf(
					forwPairs.stream().map(pair -> pair.selector).collect(Collectors.toList()));
		}

		@Nested
		@DisplayName("query the existence of a starter choice")
		class SelectionSet {

			

			@Nested
			@DisplayName("under")
			class Under {

				@Test
				@GUITest
				@DisplayName("the '3-4-3' scheme")
				public void selectionSet343() {

					// GIVEN the current scheme is '3-4-3'
					chooser.currentSchemePanel = panel343;

					// AND only some selectors in '3-4-3' report being non-empty
					when(goaliePair.selector.getSelection()).thenReturn(Optional.of(FAKE_GOALIE));
					defPairs.stream().filter(pair -> pairsIn343.contains(pair)).map(pair -> pair.selector)
							.forEach(selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));
					midPairs.stream().filter(pair -> pairsIn343.contains(pair)).map(pair -> pair.selector).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));

					// THEN no starter choice is reported as present
					GuiActionRunner.execute(() -> assertThat(chooser.hasChoice()).isFalse());

					// BUT GIVEN all selectors in '3-4-3' report being non-empty
					forwPairs.stream().filter(pair -> pairsIn343.contains(pair)).map(pair -> pair.selector)
							.forEach(selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));

					// THEN a starter choice is reported as present
					GuiActionRunner.execute(() -> assertThat(chooser.hasChoice()).isTrue());
				}

				@Test
				@GUITest
				@DisplayName("the '4-3-3' scheme")
				public void selectionSet433() {

					// GIVEN the current scheme is '4-3-3'
					chooser.currentSchemePanel = panel433;

					// AND only some selectors in '4-3-3' report being non-empty
					when(goaliePair.selector.getSelection()).thenReturn(Optional.of(FAKE_GOALIE));
					defPairs.stream().filter(pair -> pairsIn433.contains(pair)).map(pair -> pair.selector)
							.forEach(selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));
					midPairs.stream().filter(pair -> pairsIn433.contains(pair)).map(pair -> pair.selector).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));

					// THEN no starter choice is reported as present
					GuiActionRunner.execute(() -> assertThat(chooser.hasChoice()).isFalse());

					// BUT GIVEN all selectors in '4-3-3' report being non-empty
					forwPairs.stream().filter(pair -> pairsIn433.contains(pair)).map(pair -> pair.selector)
							.forEach(selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));

					// THEN a starter choice is reported as present
					GuiActionRunner.execute(() -> assertThat(chooser.hasChoice()).isTrue());
				}

				@Test
				@GUITest
				@DisplayName("the '5-3-2' scheme")
				public void selectionSet532() {

					// GIVEN the current scheme is '5-3-2'
					chooser.currentSchemePanel = panel532;

					// AND only some selectors in '5-3-2' report being non-empty
					when(goaliePair.selector.getSelection()).thenReturn(Optional.of(FAKE_GOALIE));
					defPairs.stream().filter(pair -> pairsIn532.contains(pair)).map(pair -> pair.selector)
							.forEach(selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_DEFENDER)));
					midPairs.stream().filter(pair -> pairsIn532.contains(pair)).map(pair -> pair.selector).forEach(
							selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_MIDFIELDER)));

					// THEN no starter choice is reported as present
					GuiActionRunner.execute(() -> assertThat(chooser.hasChoice()).isFalse());

					// BUT GIVEN all selectors in '5-3-2' report being non-empty
					forwPairs.stream().filter(pair -> pairsIn532.contains(pair)).map(pair -> pair.selector)
							.forEach(selector -> when(selector.getSelection()).thenReturn(Optional.of(FAKE_FORWARD)));

					// THEN a starter choice is reported as present
					GuiActionRunner.execute(() -> assertThat(chooser.hasChoice()).isTrue());
				}
			}
		}
		
		@Nested
		@DisplayName("retrieve the starter choice")
		class Allows {
			
			@Nested
			@DisplayName("when one is present")
			class SelectionSet {
				
				@Test
				@GUITest
				@DisplayName("ghshh")
				public void widgetsAddedTo343() {
					
				}
			}
			
			@Nested
			@DisplayName("when none is present")
			class SelectionCleared {
				
				@Test
				@GUITest
				@DisplayName("shgsfdh")
				public void widgetsAddedTo343() {
					
				}
			}
		}
	}


}

