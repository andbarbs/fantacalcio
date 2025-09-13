package swingViews;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domainModel.Player;
import domainModel.Player.Defender;
import swingViews.utilities.AssertJSwingJUnit5TestCase;

@DisplayName("SwingSubPlayerSelector: MVP-View unit test")
@ExtendWith(MockitoExtension.class)
public class SwingSubPlayerSelectorTest extends AssertJSwingJUnit5TestCase {

	@Mock
    private OrderedDealerPresenter<Defender> selectorPresenter;

	@BeforeEach
	public void testCaseSpecificSetup() {
		
		JFrame frame = GuiActionRunner.execute(() -> {			
			// Constructs a SwingSubPlayerSelector instance with a mocked Presenter
			SwingSubPlayerSelector<Defender> selectorView = new SwingSubPlayerSelector<Defender>();
			selectorView.setPresenter(selectorPresenter);
			
			selectorView.initOptions(List.of(
					new Player.Defender("Gigi", "Buffon"), 
					new Player.Defender("Mario", "Rossi")));
			
			JFrame f = new JFrame("Test Frame");
			f.add(selectorView);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return f;
		});
        // Initialize the FrameFixture.
        window = new FrameFixture(robot, frame);
        window.show(); // displays the frame to test the UI.
    }

    @Test @GUITest
    @DisplayName("verifies interactions between combo and reset button")
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
