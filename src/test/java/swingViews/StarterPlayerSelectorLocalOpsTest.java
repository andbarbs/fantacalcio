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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domainModel.Player.Defender;
import swingViews.StarterPlayerSelector.LocalPlayerSelectorState;
import swingViews.utilities.AssertJSwingJUnit5TestCase;

/**
 * this test case aims to specify the intended behavior of local selection
 * operators on StarterPlayerSelector which are of interest to subclass
 * SubstitutePlayerSelector.
 * 
 * The (incomplete) test case documents three supposedly equivalent APIs that
 * offer a varying degree of abstraction: 
 * 		- fluent API: highest level (StarterPS heavier, SubstitutePS leanest) 
 * 		- get/setState API: intermediate level 
 * 		- silentlyDrop/Restore API: lowest level (StarterPS leanest, SubstitutePS heavier)
 * 
 * because of our wish to streamline StarterPlayerSelector, the
 * silentlyDrop/Restore API has been chosen as the preferred facility: the
 * remaining two APIs stopped being developed and thus lack tests for some
 * scenarios
 */

@DisplayName("StarterPlayerSelector: local selection operators for subclasses")
@ExtendWith(MockitoExtension.class)
public class StarterPlayerSelectorLocalOpsTest extends AssertJSwingJUnit5TestCase {

	private static final Defender chiellini = new Defender("Giorgio", "Chiellini");
	private static final Defender pique = new Defender("Gerard", "Piqué");
	private static final Defender ramos = new Defender("Sergio", "Ramos");
	private static final Defender silva = new Defender("Thiago", "Silva");
	private static final Defender vanDijk = new Defender("Virgil", "van Dijk");

	private StarterPlayerSelector<Defender> compPlayerSelector1, compPlayerSelector2;

	@Mock
	private OptionDealerGroupDriver<StarterPlayerSelector<Defender>, Defender> mockDriver;

	// strongly typed fixtures for the combos
	private JComboBoxFixture combo1;
	private JComboBoxFixture combo2;

	@BeforeEach
	// This method is now used to perform testcase-specific setup
	public void testSpecificSetUp() {
		JFrame frame = GuiActionRunner.execute(() -> {
			compPlayerSelector1 = new StarterPlayerSelector<>();
			compPlayerSelector2 = new StarterPlayerSelector<>();
			compPlayerSelector1.setName("sel1");
			compPlayerSelector2.setName("sel2");

			OptionDealerGroupDriver.initializeDealing(Set.of(compPlayerSelector1, compPlayerSelector2),
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
			List.of(compPlayerSelector1, compPlayerSelector2).forEach(sel -> sel.attachDriver(mockDriver));
		});
	}

	private List<Defender> getComboItems(JComboBoxFixture cbFixture) {
		return GuiActionRunner.execute(() -> {
			ComboBoxModel<?> model = cbFixture.target().getModel();
			return IntStream.range(0, model.getSize()).mapToObj(value -> (Defender) model.getElementAt(value))
					.collect(Collectors.toList());
		});
	}

	@Nested // You can now group related tests using @Nested
	@DisplayName("swap and equalize using the fluent API")
	class FluentAPI {

		@Test @GUITest
		@DisplayName("local swap with both source and other having a selection")
		public void testLocalSwapWithBothSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

			// select something on both combos
			combo1.selectItem("Sergio Ramos");
			combo2.selectItem("Giorgio Chiellini");
			attachMockDriver();

			// call for a swap between 1 (source - S) and 2 (other- S)
			GuiActionRunner.execute(() -> {
				compPlayerSelector1.locally().takeOverSelectionFrom(compPlayerSelector2).pushingYoursToThem();
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
		@DisplayName("local swap with source selected, other not selected")
		public void testLocalSwapWithOtherNotSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

			// select something on combo1
			combo1.selectItem("Sergio Ramos");
			attachMockDriver();

			// call for a swap between 1 (source - S) and 2 (other- N)
			GuiActionRunner.execute(() -> {
				compPlayerSelector1.locally().takeOverSelectionFrom(compPlayerSelector2).pushingYoursToThem();
			});

			// verify intended result is achieved
			assertThat(combo1.selectedItem()).isEqualTo(null);
			assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
			assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
			assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);

			// verify that the driver was never notified
			verifyNoInteractions(mockDriver);
		}

		@Test @GUITest
		@DisplayName("local swap with source not selected, other selected")
		public void testLocalSwapWithSourceNotSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

			// select something on combo1
			combo1.selectItem("Sergio Ramos");
			attachMockDriver();

			// call for a swap between 2 (source - N) and 1 (other - S)
			GuiActionRunner.execute(() -> {
				compPlayerSelector2.locally().takeOverSelectionFrom(compPlayerSelector1).pushingYoursToThem();
			});

			// verify intended result is achieved
			assertThat(combo1.selectedItem()).isEqualTo(null);
			assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
			assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
			assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);

			// verify that the driver was never notified
			verifyNoInteractions(mockDriver);
		}

		@Test @GUITest
		@DisplayName("local equalize with both source and other having a selection")
		public void testLocalEqualize_SourceSelected_OtherSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

			// select something on both combos
			combo1.selectItem("Sergio Ramos");
			combo2.selectItem("Giorgio Chiellini");
			attachMockDriver();

			// call for 1 (source - S) to equalize to 2 (other - S)
			GuiActionRunner.execute(() -> {
				compPlayerSelector1.locally().takeOverSelectionFrom(compPlayerSelector2).droppingYours();
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
		@DisplayName("local equalize with source selected, other not selected")
		public void testLocalEqualize_SourceSelected_OtherNotSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

			// select something combo1
			combo1.selectItem("Sergio Ramos");
			attachMockDriver();

			// call for 1 (source - S) to equalize to 2 (other - N)
			GuiActionRunner.execute(() -> {
				compPlayerSelector1.locally().takeOverSelectionFrom(compPlayerSelector2).droppingYours();
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
		@DisplayName("local equalize with source not selected, other selected")
		public void testLocalEqualize_SourceNotSelected_OtherSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

			// select something on combo1
			combo1.selectItem("Sergio Ramos");
			attachMockDriver();

			// call for 2 (source - N) to equalize to 1 (other - S)
			GuiActionRunner.execute(() -> {
				compPlayerSelector2.locally().takeOverSelectionFrom(compPlayerSelector1).droppingYours();
			});

			// verify intended result is achieved
			assertThat(combo1.selectedItem()).isEqualTo("Sergio Ramos");
			assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, ramos, silva, vanDijk);
			assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
			assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);

			// verify that the driver was never notified
			verifyNoInteractions(mockDriver);
		}
	}

	@Nested
	@DisplayName("swap and equalize using the get/setState API")
	class StateAPI {

		@Test @GUITest
		@DisplayName("local swap with both source and other having a selection")
		public void testLocalSwapWithBothSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

			// select something on both combos
			combo1.selectItem("Sergio Ramos");
			combo2.selectItem("Giorgio Chiellini");
			attachMockDriver();

			// call for a swap between 1 (source - S) and 2 (other- S)
			GuiActionRunner.execute(() -> {
				LocalPlayerSelectorState<Defender> oldSelector1State = compPlayerSelector1.getLocalState();
				compPlayerSelector1.setLocalState(compPlayerSelector2.getLocalState());
				compPlayerSelector2.setLocalState(oldSelector1State);
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
		@DisplayName("local swap with source selected, other not selected")
		public void testLocalSwapWithOtherNotSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

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
			assertThat(combo1.selectedItem()).isEqualTo(null);
			assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
			assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
			assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);

			// verify that the driver was never notified
			verifyNoInteractions(mockDriver);
		}

		@Test @GUITest
		@DisplayName("local swap with source not selected, other selected")
		public void testLocalSwapWithSourceNotSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

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
			assertThat(combo1.selectedItem()).isEqualTo(null);
			assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
			assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
			assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);

			// verify that the driver was never notified
			verifyNoInteractions(mockDriver);
		}

		@Test @GUITest
		@DisplayName("local equalize with source selected, other not selected")
		public void testLocalEqualize_SourceSelected_OtherNotSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

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
		@DisplayName("local equalize with source not selected, other selected")
		public void testLocalEqualize_SourceNotSelected_OtherSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

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
	}

	@Nested
	@DisplayName("swap, equalize and drop using the silentlyDrop/Restore API")
	class SilentDropRestoreAPI {

		@Test @GUITest
		@DisplayName("local swap with both source and other having a selection")
		public void testLocalSwapWithBothSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

			// select something on both combos
			combo1.selectItem("Sergio Ramos");
			combo2.selectItem("Giorgio Chiellini");
			attachMockDriver();

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
		@DisplayName("local swap with source selected, other not selected")
		public void testLocalSwapWithOtherNotSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

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
			assertThat(combo1.selectedItem()).isEqualTo(null);
			assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
			assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
			assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);

			// verify that the driver was never notified
			verifyNoInteractions(mockDriver);
		}

		@Test @GUITest
		@DisplayName("local swap with source not selected, other selected")
		public void testLocalSwapWithSourceNotSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

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
			assertThat(combo1.selectedItem()).isEqualTo(null);
			assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
			assertThat(combo2.selectedItem()).isEqualTo("Sergio Ramos");
			assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, ramos, silva, vanDijk);

			// verify that the driver was never notified
			verifyNoInteractions(mockDriver);
		}

		@Test @GUITest
		@DisplayName("local equalize with both source and other having a selection")
		public void testLocalEqualize_SourceSelected_OtherSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

			// select something on both combos
			combo1.selectItem("Sergio Ramos");
			combo2.selectItem("Giorgio Chiellini");
			attachMockDriver();

			// call for 1 (source - S) to equalize to 2 (other - S)
			GuiActionRunner.execute(() -> {
				Optional<Defender> sel2selection = compPlayerSelector2.getSelectedOption();
				compPlayerSelector1.silentlyDrop(compPlayerSelector1.getSelectedOption());
				compPlayerSelector1.silentlyAdd(sel2selection);
				compPlayerSelector1.silentlySelect(sel2selection);
			});
			// verify that the driver was never notified
			verifyNoInteractions(mockDriver);

			// verify intended result is achieved
			assertThat(combo1.selectedItem()).isEqualTo("Giorgio Chiellini");
			assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
			assertThat(combo2.selectedItem()).isEqualTo("Giorgio Chiellini");
			assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, silva, vanDijk);

		}

		@Test @GUITest
		@DisplayName("local equalize with source selected, other not selected")
		public void testLocalEqualize_SourceSelected_OtherNotSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

			// select something on combo1
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
		@DisplayName("local equalize with source not selected, other selected")
		public void testLocalEqualize_SourceNotSelected_OtherSelected() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

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
		@DisplayName("local drop with an existing selection")
		public void testLocalDropSelection_WithExistingSelection___retire_restore() {
			combo1 = window.panel("sel1").comboBox();
			combo2 = window.panel("sel2").comboBox();

			// select something on combo1
			combo1.selectItem("Sergio Ramos");
			attachMockDriver();

			// call for 1 to drop its selection silently
			GuiActionRunner.execute(() -> {
				compPlayerSelector1.silentlyDrop(compPlayerSelector1.getSelectedOption());
			});

			// verify intended result is achieved
			assertThat(combo1.selectedItem()).isEqualTo(null);
			assertThat(getComboItems(combo1)).containsExactly(chiellini, pique, silva, vanDijk);
			assertThat(combo2.selectedItem()).isEqualTo(null);
			assertThat(getComboItems(combo2)).containsExactly(chiellini, pique, silva, vanDijk);

			// verify that the driver was never notified
			verifyNoInteractions(mockDriver);
		}
	}

}