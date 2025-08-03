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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domainModel.Player.Defender;
import swingViews.StarterPlayerSelector.StarterPlayerSelectorListener;
import swingViews.utilities.AssertJSwingJUnit5TestCase;


@DisplayName("Starter Player Selector: MVP Presenter + View integration test")
@ExtendWith(MockitoExtension.class)
public class StarterPlayerSelectorIT extends AssertJSwingJUnit5TestCase {

    private static final Defender chiellini = new Defender("Giorgio", "Chiellini");
    private static final Defender pique = new Defender("Gerard", "Piqué");
    private static final Defender ramos = new Defender("Sergio", "Ramos");
    private static final Defender silva = new Defender("Thiago", "Silva");
    private static final Defender vanDijk = new Defender("Virgil", "van Dijk");    

    /**
     * this test case aims to specify what circumstances should
     * elicit a communication from a StarterPlayerSelector to both
     * 	<ul>
	 * 		<li>its {@code OptionDealerGroupDriver}
	 * 		<li>attached {@code StarterPlayerSelectorListener}s
	 * 	</ul>
     */

    @Nested
	@DisplayName("notifications to driver and listeners")
	class DriverAndListenerInteraction {

    	private StarterPlayerSelector<Defender> compPlayerSelector;    	
    	@Mock private CompetitiveOptionDealingGroup<OrderedDealerPresenter<Defender>, Defender> driver;    	
    	@Mock private StarterPlayerSelectorListener<Defender> listener;

    	@BeforeEach
    	public void testCaseSpecificSetup() {    		
    		JFrame frame = GuiActionRunner.execute(() -> { // Wrap the panel in a frame.
    			SwingSubPlayerSelector<Defender> selView= new SwingSubPlayerSelector<Defender>();
    			compPlayerSelector = new StarterPlayerSelector<Defender>(selView);
    			selView.setPresenter(compPlayerSelector);

    			// manually wires mock driver and options
    			compPlayerSelector.attachDriver(driver);
    			compPlayerSelector.attachOptions(
    					List.of(chiellini, pique, ramos, silva, vanDijk));
    			compPlayerSelector.attachListener(listener); // attaches the listener

    			JFrame f = new JFrame("Test Frame");
    			f.add(selView);
    			f.pack();
    			f.setLocationRelativeTo(null);
    			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    			return f;
    		});
    		
    		window = new FrameFixture(robot, frame);
    		window.show();
    	}

        @Test @GUITest
        @DisplayName("GUI selector interaction notified to both driver and listener")
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
        @DisplayName("programmatic selector interaction notified to both driver and listener")
        public void testProgrammaticInteractions() {      

            // programmatic selection on selector is notified to driver and listener
            GuiActionRunner.execute(() -> {
            	compPlayerSelector.setSelection(Optional.of(chiellini));
            });
            
            verify(driver, times(1)).selectionMadeOn(compPlayerSelector, 0);
            verify(listener, times(1)).selectionMadeOn(compPlayerSelector);

            // subsequent clearing of selection is notified to driver and listener
            GuiActionRunner.execute(() -> {
            	compPlayerSelector.setSelection(Optional.empty());
            }); 
            
            verify(driver, times(1)).selectionClearedOn(compPlayerSelector, 0);
            verify(listener, times(1)).selectionClearedOn(compPlayerSelector);
            
            verifyNoMoreInteractions(driver);
        }
        
        // is this test useful? combo encapsulation => it only serves as an implementation note
        @Test @GUITest
        @DisplayName("programmatic combo interaction not notified to driver nor listener")
        public void testNotNotifiedInteractions() {
            JComboBoxFixture comboBoxFixture = window.comboBox();

            // programmatic selection on combo is NOT notified to the driver
            GuiActionRunner.execute(() -> {
            	comboBoxFixture.target().setSelectedIndex(0);
            });
            
            verify(driver, times(0)).selectionMadeOn(compPlayerSelector, 0);
        }    
    }
}