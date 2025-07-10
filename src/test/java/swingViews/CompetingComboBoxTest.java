package swingViews;

import static org.assertj.core.api.Assertions.assertThat;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.ComboBoxModel;
import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GUITestRunner.class)
public class CompetingComboBoxTest extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    // References to our three competing combo boxes
    private CompetingComboBox<String> combo1;
    private CompetingComboBox<String> combo2;
    private CompetingComboBox<String> combo3;

    /**
     * Utility method to retrieve the items from a combo box by its name.
     */
    private List<String> getComboBoxItems(String comboBoxName) {
    	JComboBoxFixture cbFixture = window.comboBox(comboBoxName);
    	return GuiActionRunner.execute(() -> {
    		ComboBoxModel<?> model = cbFixture.target().getModel();
    		List<String> items = new ArrayList<>();
    		for (int i = 0; i < model.getSize(); i++) {
    			items.add(Objects.requireNonNull(model.getElementAt(i).toString()));
    		}
    		return items;
    	});
    }
    @Override
    protected void onSetUp() {
        // Use GuiActionRunner.execute to ensure that the creation and
        // manipulation of Swing components happen on the Event Dispatch Thread.
        JFrame frame = GuiActionRunner.execute(() -> {

            // Instantiate the three combo boxes.
            combo1 = new CompetingComboBox<>();
            combo2 = new CompetingComboBox<>();
            combo3 = new CompetingComboBox<>();
            
            // Give each combo box a name so that AssertJSwing can reference it.
            combo1.setName("combo1");
            combo2.setName("combo2");
            combo3.setName("combo3");

            // Set each box's competitors
            Set<CompetingComboBox<String>> competitors = Set.of(combo1, combo2, combo3);
            combo1.setCompetitors(competitors);
            combo2.setCompetitors(competitors);
            combo3.setCompetitors(competitors);

            // Set up a simple frame that contains these combo boxes.
            JFrame frameContainer = new JFrame("Test Frame");
            frameContainer.setLayout(new FlowLayout());
            frameContainer.add(combo1);
            frameContainer.add(combo2);
            frameContainer.add(combo3);
            frameContainer.pack();
            frameContainer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            return frameContainer;
        });

        // Instantiate the AssertJSwing fixture for the frame.
        window = new FrameFixture(robot(), frame);
        window.show(); // Makes the frame visible for testing.
    }

    @Test @GUITest
    public void testCompetingBehavior() {
    	GuiActionRunner.execute(() -> {
    		// Create sample content
            List<String> sampleContents = Arrays.asList("A", "B", "C");

            // Fill each combo box with content.
            combo1.setContents(sampleContents);
            combo2.setContents(sampleContents);
            combo3.setContents(sampleContents);
    	});
    	
        // Initially, all three combo boxes should have all three items.
        window.comboBox("combo1").requireItemCount(3);
        window.comboBox("combo2").requireItemCount(3);
        window.comboBox("combo3").requireItemCount(3);

        // Use AssertJSwing to simulate selecting item "A" in combo1.
        // At this point, the listener in combo1 should remove "A" from its competitors.
        // We'll verify that combo2 and combo3 no longer contain "A"
        window.comboBox("combo1").selectItem("A");
        assertThat(getComboBoxItems("combo2")).containsExactly("B", "C");
        assertThat(getComboBoxItems("combo3")).containsExactly("B", "C");

        // Now simulate resetting combo1's selection. Since it was the one that caused "A" to be removed,
        // clearing its selection should cause "A" to be reinserted into its competitors.
        GuiActionRunner.execute(() -> combo1.setSelectedIndex(-1));
        assertThat(getComboBoxItems("combo2")).containsExactly("A", "B", "C");
        assertThat(getComboBoxItems("combo3")).containsExactly("A", "B", "C");
    }
    
    @Test @GUITest
    public void testProgrammaticCompetingBehavior() {
    	GuiActionRunner.execute(() -> {
    		// Create sample content
            List<String> sampleContents = Arrays.asList("A", "B", "C");

            // Fill each combo box with content.
            combo1.setContents(sampleContents);
            combo2.setContents(sampleContents);
            combo3.setContents(sampleContents);
    	});
    	
        // Initially, all three combo boxes should have all three items.
        window.comboBox("combo1").requireItemCount(3);
        window.comboBox("combo2").requireItemCount(3);
        window.comboBox("combo3").requireItemCount(3);

        // Select item "A" in combo1 programmatically.
        // We'll verify that combo2 and combo3 no longer contain "A"
		GuiActionRunner.execute(() -> combo1.setSelectedItem("A"));
        assertThat(getComboBoxItems("combo2")).containsExactly("B", "C");
        assertThat(getComboBoxItems("combo3")).containsExactly("B", "C");

        // Now simulate resetting combo1's selection. Since it was the one that caused "A" to be removed,
        // clearing its selection should cause "A" to be reinserted into its competitors.
        GuiActionRunner.execute(() -> combo1.setSelectedIndex(-1));
        assertThat(getComboBoxItems("combo2")).containsExactly("A", "B", "C");
        assertThat(getComboBoxItems("combo3")).containsExactly("A", "B", "C");
    }
    
    @Test @GUITest
    public void testSequentialSelectionsOnSameCombo() {
        GuiActionRunner.execute(() -> {
            List<String> sampleContents = Arrays.asList("A", "B", "C");
            combo1.setContents(sampleContents);
            combo2.setContents(sampleContents);
            combo3.setContents(sampleContents);
        });
        
        // Verify that initially each combo box has exactly "A", "B", "C".
        assertThat(getComboBoxItems("combo1")).containsExactly("A", "B", "C");
        assertThat(getComboBoxItems("combo2")).containsExactly("A", "B", "C");
        assertThat(getComboBoxItems("combo3")).containsExactly("A", "B", "C");

        // Simulate selecting "A" on combo1.
        window.comboBox("combo1").selectItem("A");
        // Competitor boxes lose "A"
        assertThat(getComboBoxItems("combo2")).containsExactly("B", "C");
        assertThat(getComboBoxItems("combo3")).containsExactly("B", "C");
        // combo1: selected "A", model unchanged
        assertThat(getComboBoxItems("combo1")).containsExactly("A", "B", "C");
        assertThat(window.comboBox("combo1").target().getSelectedItem()).isEqualTo("A");

        // Now change the selection on combo1 to "B".
        window.comboBox("combo1").selectItem("B");
        // combo1: selected "B", model unchanged
        assertThat(window.comboBox("combo1").target().getSelectedItem()).isEqualTo("B");
        assertThat(getComboBoxItems("combo1")).containsExactly("A", "B", "C");
        // Competitor boxes regain "A" and lose "B"
        assertThat(getComboBoxItems("combo2")).containsExactly("A", "C");
        assertThat(getComboBoxItems("combo3")).containsExactly("A", "C");
    }
    
    @Test @GUITest
    public void testProgrammaticSequentialSelectionsOnSameCombo() {
        GuiActionRunner.execute(() -> {
            List<String> sampleContents = Arrays.asList("A", "B", "C");
            combo1.setContents(sampleContents);
            combo2.setContents(sampleContents);
            combo3.setContents(sampleContents);
        });
        
        // Verify that initially each combo box has exactly "A", "B", "C".
        assertThat(getComboBoxItems("combo1")).containsExactly("A", "B", "C");
        assertThat(getComboBoxItems("combo2")).containsExactly("A", "B", "C");
        assertThat(getComboBoxItems("combo3")).containsExactly("A", "B", "C");

        // Simulate selecting "A" on combo1.
        GuiActionRunner.execute(() -> combo1.setSelectedItem("A"));
        // Competitor boxes lose "A"
        assertThat(getComboBoxItems("combo2")).containsExactly("B", "C");
        assertThat(getComboBoxItems("combo3")).containsExactly("B", "C");
        // combo1: selected "A", model unchanged
        assertThat(getComboBoxItems("combo1")).containsExactly("A", "B", "C");
        assertThat(window.comboBox("combo1").target().getSelectedItem()).isEqualTo("A");

        // Now change the selection on combo1 to "B".
        GuiActionRunner.execute(() -> combo1.setSelectedItem("B"));
        // combo1: selected "B", model unchanged
        assertThat(window.comboBox("combo1").target().getSelectedItem()).isEqualTo("B");
        assertThat(getComboBoxItems("combo1")).containsExactly("A", "B", "C");
        // Competitor boxes regain "A" and lose "B"
        assertThat(getComboBoxItems("combo2")).containsExactly("A", "C");
        assertThat(getComboBoxItems("combo3")).containsExactly("A", "C");
    }
    
    @Test @GUITest
    public void testCompetingBehaviorAcrossDifferentCombos() {
        GuiActionRunner.execute(() -> {
            List<String> sampleContents = Arrays.asList("A", "B", "C");
            combo1.setContents(sampleContents);
            combo2.setContents(sampleContents);
            combo3.setContents(sampleContents);
        });
        
        // Confirm initial state.
        assertThat(getComboBoxItems("combo1")).containsExactly("A", "B", "C");
        assertThat(getComboBoxItems("combo2")).containsExactly("A", "B", "C");
        assertThat(getComboBoxItems("combo3")).containsExactly("A", "B", "C");

		// Step 1: Select "A" on combo1.
		window.comboBox("combo1").selectItem("A");
		// Competitor boxes lose "A"
		assertThat(getComboBoxItems("combo2")).containsExactly("B", "C");
		assertThat(getComboBoxItems("combo3")).containsExactly("B", "C");
		// combo1: selected "A", model unchanged
		assertThat(window.comboBox("combo1").target().getSelectedItem()).isEqualTo("A");
        assertThat(getComboBoxItems("combo1")).containsExactly("A", "B", "C");

        // Step 2: Select "B" on combo2
        window.comboBox("combo2").selectItem("B");
        // combo2: selected "B", model unchanged
        assertThat(window.comboBox("combo2").target().getSelectedItem()).isEqualTo("B");
        assertThat(getComboBoxItems("combo2")).containsExactly("B", "C");
        // combo1: remains selected "A", model loses "B"
        assertThat(window.comboBox("combo1").target().getSelectedItem()).isEqualTo("A");
        assertThat(getComboBoxItems("combo1")).containsExactly("A", "C");
        // combo3: model loses "B"
        assertThat(getComboBoxItems("combo3")).containsExactly("C");
    }
    
    @Test @GUITest
    public void testCyclicCompetitionWithSpectator_stressTest() {
        // Set a content pool of 5 items.
    	List<String> contents = Arrays.asList("A", "B", "C", "D", "E");
    	
        GuiActionRunner.execute(() -> {
            combo1.setContents(contents);
            combo2.setContents(contents);
            combo3.setName("spectator");
            combo3.setContents(contents);
        });
        
        // make combo1 and combo2 cycle through the content pool, each taking 
        // the next available item, so with (ind1, ind2) having values:
        // (0, 1), (2, 3), (4, 0), (1, 2), (3, 4), winding back to (0, 1)
        int ind1 = 0, ind2 = 1;
		do {
			int currentInd1 = ind1, currentInd2 = ind2;
			window.comboBox("combo1").selectItem(contents.get(ind1));
			window.comboBox("combo2").selectItem(contents.get(ind2));

			assertThat(window.comboBox("combo1").target().getSelectedItem()).isEqualTo(contents.get(ind1));
			assertThat(getComboBoxItems("combo1")).containsExactlyElementsOf(IntStream.range(0, contents.size())
					.filter(i -> i != currentInd2).mapToObj(contents::get).collect(Collectors.toList()));
			assertThat(window.comboBox("combo2").target().getSelectedItem()).isEqualTo(contents.get(ind2));
			assertThat(getComboBoxItems("combo2")).containsExactlyElementsOf(IntStream.range(0, contents.size())
					.filter(i -> i != currentInd1).mapToObj(contents::get).collect(Collectors.toList()));
			assertThat(getComboBoxItems("spectator")).containsExactlyElementsOf(IntStream.range(0, contents.size())
					.filter(i -> i != currentInd1 && i != currentInd2).mapToObj(contents::get).collect(Collectors.toList()));
			
			ind1 = (ind1 + 2) % contents.size();
			ind2 = (ind2 + 2) % contents.size();
		} while (ind1 != 0 && ind2 != 1);
    }

}
