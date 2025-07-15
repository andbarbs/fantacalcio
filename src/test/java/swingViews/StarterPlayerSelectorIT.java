package swingViews;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import domainModel.Player;
import domainModel.Player.Defender;

@RunWith(GUITestRunner.class)
public class StarterPlayerSelectorIT extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    @Override
    public void onSetUp() {
        // Wrap the panel in a frame.
		JFrame frame = GuiActionRunner.execute(() -> {			
			// Construct the SwingPlayerSelector with the injected combo			
			StarterPlayerSelector<Defender> compPlayerSelector = new StarterPlayerSelector<Defender>();
			OptionDealerGroupDriver.initializeDealing(
					Set.of(compPlayerSelector), 
					List.of(new Player.Defender("Gigi", "Buffon"), 
							new Player.Defender("Mario", "Rossi")));
			JFrame f = new JFrame("Test Frame");
			f.add(compPlayerSelector);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return f;
		});
        // Initialize the FrameFixture.
        window = new FrameFixture(robot(), frame);
        window.show(); // displays the frame to test the UI.
    }

    @Test @GUITest
    public void testComboBoxAndResetButtonInteraction() {
        // Get the fixtures for the combo box and reset button.
        JComboBoxFixture comboBoxFixture = window.comboBox();
        JButtonFixture resetButtonFixture = window.button(JButtonMatcher.withText("Reset"));
        
        // Verify the appearance of combo options
        assertThat(comboBoxFixture.contents()).containsExactly(
        		"Gigi Buffon", "Mario Rossi");

        // Initially, no selection has been made so the reset button should be disabled.
        assertThat(comboBoxFixture.target().getSelectedIndex()).isEqualTo(-1);
        resetButtonFixture.requireDisabled();

        // Select the first item in the combo box and 
        comboBoxFixture.selectItem(0);
        
        // Verify that the button becomes enabled.
        resetButtonFixture.requireEnabled();

        // Click the reset button.
        resetButtonFixture.click();

        // Verify that the combo box cleared its selection.
        assertThat(comboBoxFixture.target().getSelectedIndex()).isEqualTo(-1);
        // The reset button should again be disabled.
        resetButtonFixture.requireDisabled();
    }
}
