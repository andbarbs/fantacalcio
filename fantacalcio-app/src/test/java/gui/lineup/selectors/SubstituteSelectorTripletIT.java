package gui.lineup.selectors;

import static gui.utils.AssertJSwingUtils.TypedJComboBoxFixtureAssert.assertThat;

import java.awt.Dimension;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import domain.Player.Club;
import domain.Player.Defender;
import gui.lineup.dealing.CompetitiveOptionDealingGroup;
import gui.lineup.triplet.FillableSwappableTriplet;
import gui.lineup.triplet.SwingFillableSwappableTripletWidget;
import gui.utils.AssertJSwingJupiterTestCase;
import gui.utils.AssertJSwingUtils.TypedJComboBoxFixtureAssert.TypedJComboBoxFixture;

// TODO: use DisplayName on tests

/**
 * this test case integrates at the SubstituteSelectorTriplet level:
 * it could be replaced by integration at a higher level
 */
@DisplayName("SubstituteSelectorTriplet: MVP View + Presenter IT")
@Tag("non-JPMS-compliant")
class SubstituteSelectorTripletIT extends AssertJSwingJupiterTestCase {
    
	private TypedJComboBoxFixture<Defender> combo1, combo2, combo3;
	private JButtonFixture reset1, reset2, reset3;
	private JButtonFixture swap1_2, swap2_3;
	
	private static final Defender chiellini = new Defender("Giorgio", "Chiellini", Club.ATALANTA);
	private static final Defender pique = new Defender("Gerard", "Piqué", Club.ATALANTA);
	private static final Defender ramos = new Defender("Sergio", "Ramos", Club.ATALANTA);
	private static final Defender silva = new Defender("Thiago", "Silva", Club.ATALANTA);
	private static final Defender vanDijk = new Defender("Virgil", "van Dijk", Club.ATALANTA);
	
	@Override
		protected void onSetUp() throws Exception {
		Dimension selectorDims = new Dimension(120, 225);
		
		JFrame frame = GuiActionRunner.execute(() -> {
			SwingSubPlayerSelector<Defender> view1 = new SwingSubPlayerSelector<Defender>(selectorDims);
			SwingSubPlayerSelector<Defender> view2 = new SwingSubPlayerSelector<Defender>(selectorDims);
			SwingSubPlayerSelector<Defender> view3 = new SwingSubPlayerSelector<Defender>(selectorDims);
			
			SubstitutePlayerSelector<Defender> selPres1 = new SubstitutePlayerSelector<Defender>(view1);
			SubstitutePlayerSelector<Defender> selPres2 = new SubstitutePlayerSelector<Defender>(view2);
			SubstitutePlayerSelector<Defender> selPres3 = new SubstitutePlayerSelector<Defender>(view3);
			
			view1.setController(selPres1);
			view2.setController(selPres2);
			view3.setController(selPres3);
			
			// sets up the SUT
			FillableSwappableTriplet<Defender> triplet = 
					new FillableSwappableTriplet<Defender>(
							selPres1, selPres2, selPres3);
			SwingFillableSwappableTripletWidget widget = new SwingFillableSwappableTripletWidget(
					false, view1, view2, view3);
			triplet.setWidget(widget);
			widget.setController(triplet);
			triplet.initSequence();
			
			CompetitiveOptionDealingGroup.initializeDealing(
					Set.of(selPres1, selPres2, selPres3), 
					List.of(chiellini, pique, ramos, silva, vanDijk));  // in alphabetical order
			
			// sets up the test Frame
			JFrame f = new JFrame("Test Frame");
			f.add(widget);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return f;
		});
		
		FrameFixture window = new FrameFixture(robot(), frame);
		window.show();
		
		combo1 = TypedJComboBoxFixture.of(window.panel("widget1").comboBox(), Defender.class);
		combo2 = TypedJComboBoxFixture.of(window.panel("widget2").comboBox(), Defender.class);
		combo3 = TypedJComboBoxFixture.of(window.panel("widget3").comboBox(), Defender.class);
		
		reset1 = window.panel("widget1").button(JButtonMatcher.withText("Reset"));
		reset2 = window.panel("widget2").button(JButtonMatcher.withText("Reset"));
		reset3 = window.panel("widget3").button(JButtonMatcher.withText("Reset"));
		
		swap1_2 = window.button("swap1_2");
		swap2_3 = window.button("swap2_3");
		}

	@BeforeEach
	public void testCaseSpecificSetup() {
    }

    @Test @GUITest
    public void testSwapping1and2() {

        combo1.selectItem("Giorgio Chiellini");
        combo2.selectItem("Gerard Piqué");
        
        swap1_2.click();
        
        assertThat(combo1).hasSelected(pique).amongOptions(pique, ramos, silva, vanDijk);
        assertThat(combo2).hasSelected(chiellini).amongOptions(chiellini, ramos, silva, vanDijk);
    }
    
    @Test @GUITest
    public void testSwapping2and3() {

        combo1.selectItem("Giorgio Chiellini");
        combo2.selectItem("Gerard Piqué");
        combo3.selectItem("Thiago Silva");
        
        swap2_3.click();
        
        assertThat(combo1).hasSelected(chiellini).amongOptions(chiellini, ramos, vanDijk);
        assertThat(combo2).hasSelected(silva).amongOptions(ramos, silva, vanDijk);
        assertThat(combo3).hasSelected(pique).amongOptions(pique, ramos, vanDijk);        
    }
    
    @Test @GUITest
    public void testCollapsingByZero() {

        combo1.selectItem("Giorgio Chiellini");
        combo2.selectItem("Gerard Piqué");
        combo3.selectItem("Thiago Silva");
        
        reset3.click();
        
        assertThat(combo1).hasSelected(chiellini).amongOptions(chiellini, ramos, silva, vanDijk);       
        assertThat(combo2).hasSelected(pique).amongOptions(pique, ramos, silva, vanDijk);        
        assertThat(combo3).hasSelected(null).amongOptions(ramos, silva, vanDijk);        
    }
    
    @Test @GUITest
    public void testCollapsingByOne() {

        combo1.selectItem("Giorgio Chiellini");
        combo2.selectItem("Gerard Piqué");
        combo3.selectItem("Thiago Silva");
        
        reset2.click();
        
        assertThat(combo1).hasSelected(chiellini).amongOptions(chiellini, pique, ramos, vanDijk);        
        assertThat(combo2).hasSelected(silva).amongOptions(pique, ramos, silva, vanDijk);        
        assertThat(combo3).hasSelected(null).amongOptions(pique, ramos, vanDijk);        
    }
    
    @Test @GUITest
    public void testCollapsingByTwo() {

        combo1.selectItem("Giorgio Chiellini");
        combo2.selectItem("Gerard Piqué");
        combo3.selectItem("Thiago Silva");
        
        reset1.click();
        
        assertThat(combo1).hasSelected(pique).amongOptions(chiellini, pique, ramos, vanDijk);        
        assertThat(combo2).hasSelected(silva).amongOptions(chiellini,ramos, silva, vanDijk);        
        assertThat(combo3).hasSelected(null).amongOptions(chiellini, ramos, vanDijk);        
    }
    
    @Test @GUITest
    public void testUpdateDoesNotTriggerCollapse() {

        combo1.selectItem("Giorgio Chiellini");
        combo2.selectItem("Gerard Piqué");
        combo3.selectItem("Thiago Silva");
        
        combo1.selectItem("Sergio Ramos");
        
        assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, ramos, vanDijk);        
        assertThat(combo2).hasSelected(pique).amongOptions(chiellini, pique, vanDijk);        
        assertThat(combo3).hasSelected(silva).amongOptions(chiellini, silva, vanDijk);        
    }
}
