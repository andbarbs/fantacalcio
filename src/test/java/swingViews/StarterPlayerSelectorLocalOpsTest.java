package swingViews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;

import java.awt.FlowLayout;
import java.util.List;
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
import domainModel.Player.Defender;

/**
 * this test case aims to specify the intended behavior of 
 * local selection operators on StarterPlayerSelector
 * 
 */

@RunWith(GUITestRunner.class)
public class StarterPlayerSelectorLocalOpsTest extends AssertJSwingJUnitTestCase {

	private static final Defender chiellini = new Defender("Giorgio", "Chiellini");
	private static final Defender pique = new Defender("Gerard", "Piqué");
	private static final Defender ramos = new Defender("Sergio", "Ramos");
	private static final Defender silva = new Defender("Thiago", "Silva");
	private static final Defender vanDijk = new Defender("Virgil", "van Dijk");
	
	private StarterPlayerSelector<Defender> compPlayerSelector1, compPlayerSelector2;	
	private OptionDealerGroupDriver<StarterPlayerSelector<Defender>, Defender> driver;
	
	private FrameFixture window;	

	@Override
	public void onSetUp() {
		// TODO consider making this a headless test
		
		JFrame frame = GuiActionRunner.execute(() -> { // Wrap the panel in a frame.
			compPlayerSelector1 = new StarterPlayerSelector<Defender>();
			compPlayerSelector2 = new StarterPlayerSelector<Defender>();
			compPlayerSelector1.setName("sel1"); 
			compPlayerSelector2.setName("sel2");
			
			// attaches the driver to verify interactions
			driver = spy(OptionDealerGroupDriver.initializeDealing(
					Set.of(compPlayerSelector1, compPlayerSelector2), 
					List.of(chiellini, pique, ramos, silva, vanDijk)));  // in alphabetical order

			JFrame f = new JFrame("Test Frame");
			f.setLayout(new FlowLayout());
			f.add(compPlayerSelector1); 
			f.add(compPlayerSelector2);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return f;
		});
		
		window = new FrameFixture(robot(), frame);      // Initialize the FrameFixture.
		window.show(); 								    // displays the frame to test the UI.
	}
	
	private List<Defender> getComboItems(JComboBoxFixture cbFixture) {
    	return GuiActionRunner.execute(() -> {
    		ComboBoxModel<?> model = cbFixture.target().getModel();
    		return IntStream.range(0, model.getSize())
    				.mapToObj(value -> (Defender) model.getElementAt(value))
    				.collect(Collectors.toList());
    	});
    }

    @Test @GUITest
    public void testLocalSwapWithBothSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on both combos
        combo1.selectItem("Sergio Ramos");
        combo2.selectItem("Giorgio Chiellini");
        
        // call for a swap between 1 (source - S) and 2 (other- S)
        GuiActionRunner.execute(() -> {
        	compPlayerSelector1.locally()
        		.takeOverSelectionFrom(compPlayerSelector2)
        		.pushingYoursToThem();
        });
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isEqualTo("Giorgio Chiellini");
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(driver);
    }
    
    @Test @GUITest
    public void testLocalSwapWithOtherNotSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        
        // call for a swap between 1 (source - S) and 2 (other- N)
        GuiActionRunner.execute(() -> {
        	compPlayerSelector1.locally()
        		.takeOverSelectionFrom(compPlayerSelector2)
        		.pushingYoursToThem();
        });
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isNull();
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(driver);
    }
    
    @Test @GUITest
    public void testLocalSwapWithSourceNotSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        
        // call for a swap between 2 (source - N) and 1 (other - S)
        GuiActionRunner.execute(() -> {
        	compPlayerSelector2.locally()
        		.takeOverSelectionFrom(compPlayerSelector1)
        		.pushingYoursToThem();
        });
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isNull();
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(driver);
    }
    
    @Test @GUITest
    public void testLocalEqualize_SourceSelected_OtherSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on both combos
        combo1.selectItem("Sergio Ramos");
        combo2.selectItem("Giorgio Chiellini");
        
        // call for 1 (source - S) to equalize to 2 (other - S)
        GuiActionRunner.execute(() -> {
        	compPlayerSelector1.locally()
        		.takeOverSelectionFrom(compPlayerSelector2)
        		.droppingYours();
        });
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isEqualTo("Giorgio Chiellini");
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Giorgio Chiellini");
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(driver);
    }
    
    @Test @GUITest
    public void testLocalEqualize_SourceSelected_OtherNotSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something combo1
        combo1.selectItem("Sergio Ramos");
        
        // call for 1 (source - S) to equalize to 2 (other - N)
        GuiActionRunner.execute(() -> {
        	compPlayerSelector1.locally()
        		.takeOverSelectionFrom(compPlayerSelector2)
        		.droppingYours();
        });
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isEqualTo(null);
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo(null);
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(driver);
    }
    
    @Test @GUITest
    public void testLocalEqualize_SourceNotSelected_OtherSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        
        // call for 2 (source - N) to equalize to 1 (other - S)
        GuiActionRunner.execute(() -> {
        	compPlayerSelector2.locally()
        		.takeOverSelectionFrom(compPlayerSelector1)
        		.droppingYours();
        });
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(driver);
    }
    
    
}