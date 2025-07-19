package swingViews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

import java.awt.FlowLayout;
import java.util.List;
import java.util.Optional;
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
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import domainModel.Player.Defender;
import swingViews.StarterPlayerSelector.LocalPlayerSelectorState;

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
	
	@Mock
	private OptionDealerGroupDriver<StarterPlayerSelector<Defender>, Defender> mockDriver;
	private AutoCloseable closeable;	
	
	private FrameFixture window;

	@Override
	public void onSetUp() {
		// TODO consider making this a headless test
		
		// initializes the mock driver to allow tests to exercise
		closeable = MockitoAnnotations.openMocks(this);
		
		JFrame frame = GuiActionRunner.execute(() -> { // Wrap the panel in a frame.
			compPlayerSelector1 = new StarterPlayerSelector<Defender>();
			compPlayerSelector2 = new StarterPlayerSelector<Defender>();
			compPlayerSelector1.setName("sel1"); 
			compPlayerSelector2.setName("sel2");
			
			// attaches the real driver to allow tests to setup
			OptionDealerGroupDriver.initializeDealing(
					Set.of(compPlayerSelector1, compPlayerSelector2), 
					List.of(chiellini, pique, ramos, silva, vanDijk));  // in alphabetical order

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
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	private void attachMockDriver() {
		GuiActionRunner.execute(() -> {
	    	List.of(compPlayerSelector1, compPlayerSelector2).forEach(
	    			sel -> sel.attachDriver(mockDriver));
	    });
	}

	private List<Defender> getComboItems(JComboBoxFixture cbFixture) {
    	return GuiActionRunner.execute(() -> {
    		ComboBoxModel<?> model = cbFixture.target().getModel();
    		return IntStream.range(0, model.getSize())
    				.mapToObj(value -> (Defender) model.getElementAt(value))
    				.collect(Collectors.toList());
    	});
    }

	// 1) with fluent API
	
    @Test @GUITest
    public void testLocalSwapWithBothSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on both combos and attach mock driver
        combo1.selectItem("Sergio Ramos");
        combo2.selectItem("Giorgio Chiellini");
        attachMockDriver();
        
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
        verifyNoInteractions(mockDriver);
    }

	@Test @GUITest
    public void testLocalSwapWithOtherNotSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
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
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalSwapWithSourceNotSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
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
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalEqualize_SourceSelected_OtherSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on both combos
        combo1.selectItem("Sergio Ramos");
        combo2.selectItem("Giorgio Chiellini");
        attachMockDriver();
        
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
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalEqualize_SourceSelected_OtherNotSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
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
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalEqualize_SourceNotSelected_OtherSelected() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
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
        verifyNoInteractions(mockDriver);
    }
    
    // 2) with silentlyDrop/restoreOption API
    
    @Test @GUITest
    public void testLocalEqualize_SourceNotSelected_OtherSelected______retire_restore() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
        // call for 2 (source - N) to equalize to 1 (other - S)
        GuiActionRunner.execute(() -> {        	
        	Optional<Defender> sel1selection = compPlayerSelector1.getSelectedOption();
        	compPlayerSelector2.silentlyDrop(compPlayerSelector2.getSelectedOption());
        	compPlayerSelector2.silentlyAdd(sel1selection);
        	compPlayerSelector2.silentlySelect(sel1selection);
        });
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalEqualize_SourceSelected_OtherNotSelected_____retire_restore() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
        // call for 1 (source - S) to equalize to 2 (other - N)
        GuiActionRunner.execute(() -> {
        	// compPlayerSelector1.setLocalState(compPlayerSelector2.getLocalState());
        	
        	Optional<Defender> sel2selection = compPlayerSelector2.getSelectedOption();
        	compPlayerSelector1.silentlyDrop(compPlayerSelector1.getSelectedOption());
        	compPlayerSelector1.silentlyAdd(sel2selection);
        	compPlayerSelector1.silentlySelect(sel2selection);
        });
        // verify that the driver was never notified
        verifyNoInteractions(mockDriver);
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isEqualTo(null);
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        
        assertThat(combo2.selectedItem()).isEqualTo(null);
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, silva, vanDijk);
        
    }
    
    @Test @GUITest
    public void testLocalSwapWithBothSelected_____retire_restore() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on both combos
        combo1.selectItem("Sergio Ramos");
        combo2.selectItem("Giorgio Chiellini");
        attachMockDriver();
        
        assertThat(getComboItems(combo1)).containsExactly(pique, ramos, silva, vanDijk);
        
        // call for a swap between 1 (source - S) and 2 (other- S)
        GuiActionRunner.execute(() -> {
        	Optional<Defender> sel1selection = compPlayerSelector1.getSelectedOption();
        	Optional<Defender> sel2selection = compPlayerSelector2.getSelectedOption();
        	compPlayerSelector1.silentlyDrop(compPlayerSelector1.getSelectedOption());
        	compPlayerSelector1.silentlyAdd(sel2selection);
        	compPlayerSelector1.silentlySelect(sel2selection);
        	compPlayerSelector2.silentlyDrop(compPlayerSelector2.getSelectedOption());
        	compPlayerSelector2.silentlyAdd(sel1selection);
        	compPlayerSelector2.silentlySelect(sel1selection);
        });
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isEqualTo("Giorgio Chiellini");
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalSwapWithOtherNotSelected____retire_restore() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
        // call for a swap between 1 (source - S) and 2 (other- N)
		GuiActionRunner.execute(() -> {
			Optional<Defender> sel1selection = compPlayerSelector1.getSelectedOption();
        	Optional<Defender> sel2selection = compPlayerSelector2.getSelectedOption();
        	compPlayerSelector1.silentlyDrop(compPlayerSelector1.getSelectedOption());
        	compPlayerSelector1.silentlyAdd(sel2selection);
        	compPlayerSelector1.silentlySelect(sel2selection);
        	compPlayerSelector2.silentlyDrop(compPlayerSelector2.getSelectedOption());
        	compPlayerSelector2.silentlyAdd(sel1selection);
        	compPlayerSelector2.silentlySelect(sel1selection);
		});
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isNull();
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalSwapWithSourceNotSelected___retire_restore() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
        // call for a swap between 2 (source - N) and 1 (other - S)
		GuiActionRunner.execute(() -> {
			Optional<Defender> sel1selection = compPlayerSelector1.getSelectedOption();
        	Optional<Defender> sel2selection = compPlayerSelector2.getSelectedOption();
        	compPlayerSelector1.silentlyDrop(compPlayerSelector1.getSelectedOption());
        	compPlayerSelector1.silentlyAdd(sel2selection);
        	compPlayerSelector1.silentlySelect(sel2selection);
        	compPlayerSelector2.silentlyDrop(compPlayerSelector2.getSelectedOption());
        	compPlayerSelector2.silentlyAdd(sel1selection);
        	compPlayerSelector2.silentlySelect(sel1selection);
		});
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isNull();
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalDropSelection_WithExistingSelection___retire_restore() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
        // call for 1 to drop its selection silently
		GuiActionRunner.execute(() -> {
			compPlayerSelector1.silentlyDrop(compPlayerSelector1.getSelectedOption());
		});
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isNull();
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isNull();
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(mockDriver);
    }
    	
    // 3) with get/setState API
    
    @Test @GUITest
    public void testLocalEqualize_SourceNotSelected_OtherSelected______getState() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
        // call for 2 (source - N) to equalize to 1 (other - S)
        GuiActionRunner.execute(() -> {
        	compPlayerSelector2.setLocalState(compPlayerSelector1.getLocalState());
        });
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalEqualize_SourceSelected_OtherNotSelected_____getState() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
        // call for 1 (source - S) to equalize to 2 (other - N)
        GuiActionRunner.execute(() -> {
        	compPlayerSelector1.setLocalState(compPlayerSelector2.getLocalState());
        });
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isEqualTo(null);
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo(null);
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalSwapWithBothSelected_____getState() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on both combos
        combo1.selectItem("Sergio Ramos");
        combo2.selectItem("Giorgio Chiellini");
        attachMockDriver();
        
        assertThat(getComboItems(combo1)).containsExactly(pique, ramos, silva, vanDijk);
        
        // call for a swap between 1 (source - S) and 2 (other- S)
        GuiActionRunner.execute(() -> {
        	LocalPlayerSelectorState<Defender> oldSelector1State = compPlayerSelector1.getLocalState();
        	compPlayerSelector1.setLocalState(compPlayerSelector2.getLocalState());
        	compPlayerSelector2.setLocalState(oldSelector1State);
        });
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isEqualTo("Giorgio Chiellini");
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        System.out.println("got here");
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalSwapWithOtherNotSelected____getState() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
        // call for a swap between 1 (source - S) and 2 (other- N)
		GuiActionRunner.execute(() -> {
			LocalPlayerSelectorState<Defender> oldSelector1State = compPlayerSelector1.getLocalState();
			compPlayerSelector1.setLocalState(compPlayerSelector2.getLocalState());
			compPlayerSelector2.setLocalState(oldSelector1State);
		});
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isNull();
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(mockDriver);
    }
    
    @Test @GUITest
    public void testLocalSwapWithSourceNotSelected___getState() {
        JComboBoxFixture combo1 = window.panel("sel1").comboBox();
        JComboBoxFixture combo2 = window.panel("sel2").comboBox();

        // select something on combo1
        combo1.selectItem("Sergio Ramos");
        attachMockDriver();
        
        // call for a swap between 2 (source - N) and 1 (other - S)
		GuiActionRunner.execute(() -> {
			LocalPlayerSelectorState<Defender> oldSelector1State = compPlayerSelector1.getLocalState();
			compPlayerSelector1.setLocalState(compPlayerSelector2.getLocalState());
			compPlayerSelector2.setLocalState(oldSelector1State);
		});
        
        // verify intended result is achieved
        assertThat(combo1.selectedItem()).isNull();
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
        assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
        
        // verify that the driver was never notified
        verifyNoInteractions(mockDriver);
    }
    
}