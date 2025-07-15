package swingViews;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.ComboBoxModel;
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

import domainModel.Player.Defender;

@RunWith(GUITestRunner.class)
public class CompetingSubstituteChooserTest extends AssertJSwingJUnitTestCase {

    private FrameFixture window;
    
	private JComboBoxFixture combo1, combo2, combo3;
	private JButtonFixture reset1, reset2, reset3;
	private JButtonFixture swap1_2, swap2_3;
	
	private static final Defender chiellini = new Defender("Giorgio", "Chiellini");
	private static final Defender pique = new Defender("Gerard", "Piqué");
	private static final Defender ramos = new Defender("Sergio", "Ramos");
	private static final Defender silva = new Defender("Thiago", "Silva");
	private static final Defender vanDijk = new Defender("Virgil", "van Dijk");
	
	private List<Defender> getComboItems(JComboBoxFixture cbFixture) {
    	return GuiActionRunner.execute(() -> {
    		ComboBoxModel<?> model = cbFixture.target().getModel();
    		return IntStream.range(0, model.getSize())
    				.mapToObj(value -> (Defender) model.getElementAt(value))
    				.collect(Collectors.toList());
    	});
    }

    @Override
    public void onSetUp() {    	
		JFrame frame = GuiActionRunner.execute(() -> {
			
			// sets up the SUT
			CompetingSubstituteChooser<Defender> chooser = new CompetingSubstituteChooser<Defender>();
			OptionDealerGroupDriver.initializeDealing(
					Set.copyOf(chooser.getSubstituteSelectors()), 
					List.of(chiellini, pique, ramos, silva, vanDijk));  // in alphabetical order
			
			// sets up the test Frame
			JFrame f = new JFrame("Test Frame");
			f.add(chooser);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return f;
		});
        
        window = new FrameFixture(robot(), frame);
        window.show();
        
        combo1 = window.panel("selector1").comboBox();
        combo2 = window.panel("selector2").comboBox();
        combo3 = window.panel("selector3").comboBox();
        
        reset1 = window.panel("selector1").button(JButtonMatcher.withText("Reset"));
        reset2 = window.panel("selector2").button(JButtonMatcher.withText("Reset"));
        reset3 = window.panel("selector3").button(JButtonMatcher.withText("Reset"));
        
        swap1_2 = window.button("swap1_2");
        swap2_3 = window.button("swap2_3");
    }

    @Test @GUITest
    public void testSwapping1and2() {

        combo1.selectItem("Giorgio Chiellini");
        combo2.selectItem("Gerard Piqué");
        
        swap1_2.click();
        
        assertThat(combo1.selectedItem()).isEqualTo("Gerard Piqué");
        assertThat(getComboItems(combo1)).containsExactly(pique, ramos, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Giorgio Chiellini");
        assertThat(getComboItems(combo2)).containsExactly(chiellini, ramos, silva, vanDijk);
    }
    
    @Test @GUITest
    public void testSwapping2and3() {

        combo1.selectItem("Giorgio Chiellini");
        combo2.selectItem("Gerard Piqué");
        combo3.selectItem("Thiago Silva");
        
        swap2_3.click();
        
        assertThat(combo1.selectedItem()).isEqualTo("Giorgio Chiellini");
        assertThat(getComboItems(combo1)).containsExactly(chiellini, ramos, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Thiago Silva");
        assertThat(getComboItems(combo2)).containsExactly(ramos, silva, vanDijk);
        
        assertThat(combo3.selectedItem()).isEqualTo("Gerard Piqué");
        assertThat(getComboItems(combo3)).containsExactly(pique, ramos, vanDijk);        
    }
    
    @Test @GUITest
    public void testCollapsingByZero() {

        combo1.selectItem("Giorgio Chiellini");
        combo2.selectItem("Gerard Piqué");
        combo3.selectItem("Thiago Silva");
        
        reset3.click();
        
        assertThat(combo1.selectedItem()).isEqualTo("Giorgio Chiellini");
        assertThat(getComboItems(combo1)).containsExactly(chiellini, ramos, silva, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Gerard Piqué");
        assertThat(getComboItems(combo2)).containsExactly(pique, ramos, silva, vanDijk);
        
        assertThat(combo3.selectedItem()).isNull();
        assertThat(getComboItems(combo3)).containsExactly(ramos, silva, vanDijk);        
    }
    
    @Test @GUITest
    public void testCollapsingByOne() {

        combo1.selectItem("Giorgio Chiellini");
        combo2.selectItem("Gerard Piqué");
        combo3.selectItem("Thiago Silva");
        
        reset2.click();
        
        assertThat(combo1.selectedItem()).isEqualTo("Giorgio Chiellini");
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, ramos, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Thiago Silva");
        assertThat(getComboItems(combo2)).containsExactly(pique, ramos, silva, vanDijk);
        
        assertThat(combo3.selectedItem()).isNull();
        assertThat(getComboItems(combo3)).containsExactly(pique, ramos, vanDijk);        
    }
    
    @Test @GUITest
    public void testCollapsingByTwo() {

        combo1.selectItem("Giorgio Chiellini");
        combo2.selectItem("Gerard Piqué");
        combo3.selectItem("Thiago Silva");
        
        reset1.click();
        
        assertThat(combo1.selectedItem()).isEqualTo("Gerard Piqué");
        assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, ramos, vanDijk);
        
        assertThat(combo2.selectedItem()).isEqualTo("Thiago Silva");
        assertThat(getComboItems(combo2)).containsExactly(chiellini,ramos, silva, vanDijk);
        
        assertThat(combo3.selectedItem()).isNull();
        assertThat(getComboItems(combo3)).containsExactly(chiellini, ramos, vanDijk);        
    }
}
