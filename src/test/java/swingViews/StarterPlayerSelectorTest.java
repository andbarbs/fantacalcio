package swingViews;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import domainModel.Player;
import domainModel.Player.Defender;
import swingViews.StarterPlayerSelector.StarterPlayerSelectorListener;

/**
 * this test case aims to specify what communications should take place
 * from a StarterPlayerSelector to both
 * 		- its OptionDealerGroupDriver
 * 		- an attached StarterPlayerSelectorListener
 */

@RunWith(GUITestRunner.class)
public class StarterPlayerSelectorTest extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private StarterPlayerSelector<Defender> compPlayerSelector;
	
	@Mock
	private OptionDealerGroupDriver<StarterPlayerSelector<Defender>, Defender> driver;
	
	@Mock
	private StarterPlayerSelectorListener<Defender> listener;
	
	private AutoCloseable closeable;

	@Override
	public void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		
		JFrame frame = GuiActionRunner.execute(() -> { // Wrap the panel in a frame.
			compPlayerSelector = new StarterPlayerSelector<Defender>();

			compPlayerSelector.attachDriver(driver);     // attaches the group driver
			compPlayerSelector.attachOptions(		     // attaches the group options
					List.of(new Player.Defender("Gigi", "Buffon"), 
							new Player.Defender("Mario", "Rossi")));
			compPlayerSelector.attachListener(listener); // attaches the listener

			JFrame f = new JFrame("Test Frame");
			f.add(compPlayerSelector);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return f;
		});
		
		window = new FrameFixture(robot(), frame);      // Initialize the FrameFixture.
		window.show(); 								    // displays the frame to test the UI.
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

    @Test @GUITest
    public void testGUIInteractions() {
        JComboBoxFixture comboBoxFixture = window.comboBox();
        JButtonFixture resetButtonFixture = window.button(JButtonMatcher.withText("Reset"));

        // GUI selection on combo is notified to driver and listener
        comboBoxFixture.selectItem(0);
        verify(driver, times(1)).selectionMadeOn(compPlayerSelector, 0);
        verify(listener, times(1)).selectionMadeOn(compPlayerSelector);

        // subsequent clearing of selection is notified to driver and listener
        resetButtonFixture.click();
        verify(driver, times(1)).selectionClearedOn(compPlayerSelector, 0);
        verify(listener, times(1)).selectionClearedOn(compPlayerSelector);
        
        verifyNoMoreInteractions(driver);
        verifyNoMoreInteractions(listener);
    }
    
    @Test @GUITest
    public void testProgrammaticInteractions() {      

        // programmatic selection on selector is notified to driver and listener
        GuiActionRunner.execute(() -> {
        	compPlayerSelector.select(Optional.of(new Player.Defender("Gigi", "Buffon")));
        });        
        verify(driver, times(1)).selectionMadeOn(compPlayerSelector, 0);
        verify(listener, times(1)).selectionMadeOn(compPlayerSelector);

        // subsequent clearing of selection is notified to driver and listener
        GuiActionRunner.execute(() -> {
        	compPlayerSelector.select(Optional.empty());
        }); 
        verify(driver, times(1)).selectionClearedOn(compPlayerSelector, 0);
        verify(listener, times(1)).selectionClearedOn(compPlayerSelector);
        
        verifyNoMoreInteractions(driver);
    }
    
    @Test @GUITest
    public void testNotNotifiedInteractions() {
        JComboBoxFixture comboBoxFixture = window.comboBox();

        // programmatic selection on combo is NOT notified to the driver
        GuiActionRunner.execute(() -> {
        	comboBoxFixture.target().setSelectedIndex(0);
        });
        verify(driver, times(0)).selectionMadeOn(compPlayerSelector, 0);
    }    
    
    
    
}