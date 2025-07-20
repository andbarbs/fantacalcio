package swingViews;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verifyNoInteractions;

import java.awt.FlowLayout;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.ComboBoxModel;
import javax.swing.JFrame;
import static org.assertj.core.api.Assertions.assertThat;

import domainModel.Player.Defender;
import swingViews.utilities.AssertJSwingJUnit5TestCase;

/**
 * this test case aims to specify the intended behavior of 
 * local selection operators on StarterPlayerSelector
 * 
 */

@DisplayName("Starter Player Selector Local Operations Test Suite")
@ExtendWith(MockitoExtension.class)
public class StarterPlayerSelectorLocalOpsTest_Jupiter extends AssertJSwingJUnit5TestCase {

    private static final Defender chiellini = new Defender("Giorgio", "Chiellini");
    private static final Defender pique = new Defender("Gerard", "Piqué");
    private static final Defender ramos = new Defender("Sergio", "Ramos");
    private static final Defender silva = new Defender("Thiago", "Silva");
    private static final Defender vanDijk = new Defender("Virgil", "van Dijk");

    private StarterPlayerSelector<Defender> compPlayerSelector1, compPlayerSelector2;

    @Mock
    private OptionDealerGroupDriver<StarterPlayerSelector<Defender>, Defender> mockDriver;

    // This method is now used to perform test-specific setup
    @BeforeEach
    public void testSpecificSetUp() {
        JFrame frame = GuiActionRunner.execute(() -> {
            compPlayerSelector1 = new StarterPlayerSelector<>();
            compPlayerSelector2 = new StarterPlayerSelector<>();
            compPlayerSelector1.setName("sel1");
            compPlayerSelector2.setName("sel2");

            OptionDealerGroupDriver.initializeDealing(
                    Set.of(compPlayerSelector1, compPlayerSelector2),
                    List.of(chiellini, pique, ramos, silva, vanDijk));

            JFrame f = new JFrame("Test Frame");
            f.setLayout(new FlowLayout());
            f.add(compPlayerSelector1);
            f.add(compPlayerSelector2);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            return f;
        });

        // The 'robot' field is inherited from AssertJSwingJUnit5TestCase
        window = new FrameFixture(robot, frame);
        window.show();
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

    // You can now group related tests using @Nested
    @Nested
    @DisplayName("Tests with both components having a selection")
    class BothSelected {
        @Test @GUITest
        @DisplayName("Should successfully swap selections between components")
        public void testLocalSwapWithBothSelected() {
            JComboBoxFixture combo1 = window.panel("sel1").comboBox();
            JComboBoxFixture combo2 = window.panel("sel2").comboBox();

            combo1.selectItem("Sergio Ramos");
            combo2.selectItem("Giorgio Chiellini");
            attachMockDriver();

            GuiActionRunner.execute(() -> {
                compPlayerSelector1.locally()
                        .takeOverSelectionFrom(compPlayerSelector2)
                        .pushingYoursToThem();
            });

            assertThat(combo1.selectedItem()).isEqualTo("Giorgio Chiellini");
            assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);

            assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
            assertThat(getComboItems(combo2)).containsExactly(pique, ramos, silva, vanDijk);

            verifyNoInteractions(mockDriver);
        }
    }
}