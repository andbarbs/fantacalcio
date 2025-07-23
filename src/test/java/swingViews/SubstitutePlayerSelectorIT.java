package swingViews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static swingViews.utilities.TypedJComboBoxFixtureAssert.assertThat;

import java.awt.FlowLayout;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import domainModel.Player.Defender;
import swingViews.FillableSwappableSequenceDriver.FillableSwappableVisual;
import swingViews.utilities.AssertJSwingJUnit5TestCase;
import swingViews.utilities.TypedJComboBoxFixture;


@DisplayName("Substitute Player Selector: MVP Presenter + View integration test")
@ExtendWith(MockitoExtension.class)
public class SubstitutePlayerSelectorIT extends AssertJSwingJUnit5TestCase {

    private static final Defender chiellini = new Defender("Giorgio", "Chiellini");
    private static final Defender pique = new Defender("Gerard", "Piqué");
    private static final Defender ramos = new Defender("Sergio", "Ramos");
    private static final Defender silva = new Defender("Thiago", "Silva");
    private static final Defender vanDijk = new Defender("Virgil", "van Dijk");    

	private SubstitutePlayerSelectorPresenter<Defender> substitutePresenter1, substitutePresenter2;

    @Mock
    private OptionDealerGroupDriver<PlayerSelectorPresenter<Defender>, Defender> mockGroupDriver;
    
    @Mock
    private FillableSwappableSequenceDriver<SubstitutePlayerSelectorPresenter<Defender>> mockSequenceDriver;
    
    @Mock
    private FillableSwappableVisual<SubstitutePlayerSelectorPresenter<Defender>> mockVisual;
    
    // strongly typed fixtures for the combos
    private TypedJComboBoxFixture<Defender> combo1;
	private TypedJComboBoxFixture<Defender> combo2;
    
    @BeforeEach
    public void testSpecificSetUp() {
        JFrame frame = GuiActionRunner.execute(() -> {
        	SwingSubPlayerSelector<Defender> sel1View = new SwingSubPlayerSelector<Defender>();
        	sel1View.setName("sel1");
        	substitutePresenter1 = new SubstitutePlayerSelectorPresenter<Defender>(sel1View);
			sel1View.setPresenter(substitutePresenter1);

			SwingSubPlayerSelector<Defender> sel2View = new SwingSubPlayerSelector<Defender>();
        	sel2View.setName("sel2");
        	substitutePresenter2 = new SubstitutePlayerSelectorPresenter<Defender>(sel2View);
			sel2View.setPresenter(substitutePresenter2);

            // wires a real group driver for tests' setup phase
            OptionDealerGroupDriver.initializeDealing(
                    Set.of(substitutePresenter1, substitutePresenter2),
                    List.of(chiellini, pique, ramos, silva, vanDijk));
            
            // wires a real sequence driver to avoid NPE in tests' setup phase
            new FillableSwappableSequenceDriver<SubstitutePlayerSelectorPresenter<Defender>>(
            		List.of(substitutePresenter1, substitutePresenter2), 
            		mockVisual);

            JFrame f = new JFrame("Test Frame");
            f.setLayout(new FlowLayout());
            f.add(sel1View);
            f.add(sel2View);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            return f;
        });

        window = new FrameFixture(robot, frame);
        window.show();

		combo1 = TypedJComboBoxFixture.of(window.panel("sel1").comboBox(), Defender.class);
		combo2 = TypedJComboBoxFixture.of(window.panel("sel2").comboBox(), Defender.class);
    }

    private void attachMockDrivers() {
        GuiActionRunner.execute(() -> {
            List.of(substitutePresenter1, substitutePresenter2).forEach(sel -> {
            	sel.attachDriver(mockGroupDriver);
            	sel.attachDriver(mockSequenceDriver);
            });
        });
    }
    
    /**
     * this test case aims to specify the intended behavior of 
     * protected silent option operators of {@code StarterPlayerSelector}, namely
     * <ol>
	 * 		<li>{@link PlayerSelectorPresenter#silentlyDrop(Optional)}
	 * 		<li>{@link PlayerSelectorPresenter#silentlyAdd(Optional)}
	 * 		<li>{@link PlayerSelectorPresenter#silentlySelect(Optional)}
	 * </ol>
     */
    
    // TODO this next testcase looks more like an IT than a unit test, because
    //		- an actual driver is used for the setup phase inside tests
    //		- two instances (more than one!) of StarterPlayerSelector are used 
    //
    // Consider making this an IT and adding a testcase where these methods 
    // are truly tested in isolation
    
	@Nested
	@DisplayName("silent option operators for subclasses")
	class SilentOptionOperators {

		@Nested
		@DisplayName("silentlyDrop(option) operator")
		class SilentlyDrop {

			@Test @GUITest
			@DisplayName("silentlyDrop valid option with an existing selection")
			public void testSilentlyDropValidOption_WithExistingSelection() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently drop a not-selected option
				GuiActionRunner.execute(() -> {
					substitutePresenter1.silentlyDrop(Optional.of(chiellini));
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// chiellini was dropped from combo1 and not restored to combo2,
				// while combo1 retained its selection
				assertThat(combo1).hasSelected(ramos).amongOptions(pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyDrop a valid option which is also the selected option")
			public void testSilentlyDropValidOption_WhichIsAlsoSelected() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to drop its selection silently
				GuiActionRunner.execute(() -> {
					substitutePresenter1.silentlyDrop(Optional.of(ramos));
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// ramos was dropped from combo1 and not restored to combo2
				assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyDrop valid option with no existing selection")
			public void testSilentlyDropValidOption_WithNoExistingSelection() {

				// don't select anything on combo1

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently drop a not-selected option
				GuiActionRunner.execute(() -> {
					substitutePresenter1.silentlyDrop(Optional.of(chiellini));
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// combo1 still has no selection and lost chiellini, while combo2 unchanged
				assertThat(combo1).hasSelected(null).amongOptions(pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyDrop empty option with an existing selection")
			public void testSilentlyDropEmptyOption_WithExistingSelection() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently drop an empty option
				GuiActionRunner.execute(() -> {
					substitutePresenter1.silentlyDrop(Optional.empty());
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// nothing has changed on either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyDrop empty option with no existing selection")
			public void testSilentlyDropEmptyOption_WithNoExistingSelection() {

				// don't select anything on combo1

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently drop an empty option
				GuiActionRunner.execute(() -> {
					substitutePresenter1.silentlyDrop(Optional.empty());
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// verify nothing has happened to either combo
				assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyDrop invalid option with an existing selection")
			public void testSilentlyDropInvalidOption_WithExistingSelection() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						substitutePresenter1.silentlyDrop(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// nothing has changed on either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyDrop invalid option with no existing selection")
			public void testSilentlyDropInvalidOption_WithNoExistingSelection() {

				// don't select anything on combo1

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						substitutePresenter1.silentlyDrop(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// verify nothing has happened to either combo
				assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyDrop a valid but already missing option")
			public void testSilentlyDropValidOption_ButAlreadyMissing() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 2 to silently drop an option it already lacks
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						substitutePresenter2.silentlyDrop(Optional.of(ramos));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// verify nothing has happened to either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}
		}

		@Nested
		@DisplayName("silentlyAdd(option) operator")
		class SilentlyAdd {

			@Test @GUITest
			@DisplayName("silentlyAdd valid option with an existing selection")
			public void testSilentlyAddValidOption_WithExistingSelection() {

				// select something on combo1 and verify it no longer appears in combo2
				combo1.selectItem("Sergio Ramos");
				assertThat(combo2.allItems()).doesNotContain(ramos);

				// select something on combo2
				combo2.selectItem("Giorgio Chiellini");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 2 to silently add the missing option
				GuiActionRunner.execute(() -> {
					substitutePresenter2.silentlyAdd(Optional.of(ramos));
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// combo2 restored ramos and retains chiellini selected, while combo1 is
				// unchanged
				assertThat(combo1).hasSelected(ramos).amongOptions(pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(chiellini).amongOptions(chiellini, pique, ramos, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyAdd valid option with no existing selection")
			public void testSilentlyAddValidOption_WithNoExistingSelection() {

				// select something on combo1 and verify it no longer appears in combo2
				combo1.selectItem("Sergio Ramos");
				assertThat(combo2.allItems()).doesNotContain(ramos);

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 2 to silently add the missing option
				GuiActionRunner.execute(() -> {
					substitutePresenter2.silentlyAdd(Optional.of(ramos));
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// combo2 restored ramos and still has no selection, while combo1 is unchanged
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyAdd invalid option with an existing selection")
			public void testSilentlyAddInvalidOption_WithExistingSelection() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						substitutePresenter1.silentlyAdd(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// nothing has changed on either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyAdd invalid option with no existing selection")
			public void testSilentlyAddInvalidOption_WithNoExistingSelection() {

				// don't select anything on combo1

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						substitutePresenter1.silentlyAdd(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// verify nothing has happened to either combo
				assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyAdd a valid but already present option")
			public void testSilentlyAddValidOption_ButAlreadyPresent() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						substitutePresenter1.silentlyAdd(Optional.of(chiellini));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// verify nothing has happened to either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}
		}

		@Nested
		@DisplayName("silentlySelect(option) operator")
		class SilentlySelect {

			@Test @GUITest
			@DisplayName("silentlySelect valid option with no previous selection")
			public void testSilentlySelectValidOption_WithNoPreviousSelection() {

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently select a valid option
				GuiActionRunner.execute(() -> {
					substitutePresenter1.silentlySelect(Optional.of(chiellini));
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// verify intended result is achieved
				assertThat(combo1).hasSelected(chiellini).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlySelect valid option with a previous selection")
			public void testSilentlySelectValidOption_WithPreviousSelection() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently select a valid option
				GuiActionRunner.execute(() -> {
					substitutePresenter1.silentlySelect(Optional.of(chiellini));
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// verify intended result is achieved
				assertThat(combo1).hasSelected(chiellini).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}
			
			@Test @GUITest
			@DisplayName("silentlySelect valid option which is the selected ones")
			public void testSilentlySelectValidOption_WhichIsAlsoSelected() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently select the already selected option
				GuiActionRunner.execute(() -> {
					substitutePresenter1.silentlySelect(Optional.of(ramos));
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// both combos are unaffected
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlySelect of empty option with no previous selection")
			public void testSilentlySelectEmptyOption_WithNoPreviousSelection() {

				// don't select anything on either combo

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently select a valid option
				GuiActionRunner.execute(() -> {
					substitutePresenter1.silentlySelect(Optional.empty());
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// nothing has happened to both combos
				assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlySelect of empty option with a previous selection")
			public void testSilentlySelectEmptyOption_WithPreviousSelection() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently select a valid option
				GuiActionRunner.execute(() -> {
					substitutePresenter1.silentlySelect(Optional.empty());
				});

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// selection was reset on combo1 but ramos not restored in combo2
				assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlySelect invalid option with an existing selection")
			public void testSilentlySelectInvalidOption_WithExistingSelection() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						substitutePresenter1.silentlySelect(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// nothing has changed on either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlySelect invalid option with no existing selection")
			public void testSilentlySelectInvalidOption_WithNoExistingSelection() {

				// don't select anything on combo1

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						substitutePresenter1.silentlySelect(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// verify nothing has happened to either combo
				assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, ramos, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlySelect valid but missing option with an existing selection")
			public void testSilentlySelectMissingOption_WithExistingSelection() {

				// select something on combo1 and combo2
				combo1.selectItem("Sergio Ramos");
				combo2.selectItem("Giorgio Chiellini");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 2 to silently select a valid but missing option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						substitutePresenter2.silentlySelect(Optional.of(ramos));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// nothing has changed on either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(chiellini).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlySelect valid but missing option with no existing selection")
			public void testSilentlySelectMissingOption_WithNoExistingSelection() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDrivers();

				// call for 2 to silently select a valid but missing option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						substitutePresenter2.silentlySelect(Optional.of(ramos));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the drivers were not notified
				verifyNoInteractions(mockGroupDriver);
				verifyNoInteractions(mockSequenceDriver);

				// verify nothing has happened to either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}
		}
	}
	
	// TODO consider whether it should be enough to test aggregation of 
	// local option operators inside the unit testcase for SubstitutePlayerSelectorPresenter
	
	@Nested
	@DisplayName("LEGACY: swap, equalize and drop using the silentlyDrop/Restore API")
	class AggregateSilentOptionOperators {

		@Test @GUITest
		@DisplayName("local swap with both source and other having a selection")
		public void testLocalSwapWithBothSelected() {
			combo1 = TypedJComboBoxFixture.of(window.panel("sel1").comboBox(), Defender.class);
			combo2 = TypedJComboBoxFixture.of(window.panel("sel2").comboBox(), Defender.class);

			// select something on both combos
			combo1.selectItem("Sergio Ramos");
			combo2.selectItem("Giorgio Chiellini");
			attachMockDrivers();

			// call for a swap between 1 (source - S) and 2 (other- S)
			GuiActionRunner.execute(() -> {
				Optional<Defender> sel1selection = substitutePresenter1.getSelection();
				Optional<Defender> sel2selection = substitutePresenter2.getSelection();
				substitutePresenter1.silentlyDrop(substitutePresenter1.getSelection());
				substitutePresenter1.silentlyAdd(sel2selection);
				substitutePresenter1.silentlySelect(sel2selection);
				substitutePresenter2.silentlyDrop(substitutePresenter2.getSelection());
				substitutePresenter2.silentlyAdd(sel1selection);
				substitutePresenter2.silentlySelect(sel1selection);
			});

			// verify that the drivers were not notified
			verifyNoInteractions(mockGroupDriver);
			verifyNoInteractions(mockSequenceDriver);

			// verify intended result is achieved
			assertThat(combo1).hasSelected(chiellini).amongOptions(chiellini, pique, silva, vanDijk);
			assertThat(combo2).hasSelected(ramos).amongOptions(pique, ramos, silva, vanDijk);
		}

		@Test @GUITest
		@DisplayName("local swap with source selected, other not selected")
		public void testLocalSwapWithOtherNotSelected() {
			combo1 = TypedJComboBoxFixture.of(window.panel("sel1").comboBox(), Defender.class);
			combo2 = TypedJComboBoxFixture.of(window.panel("sel2").comboBox(), Defender.class);

			// select something on combo1
			combo1.selectItem("Sergio Ramos");
			attachMockDrivers();

			// call for a swap between 1 (source - S) and 2 (other- N)
			GuiActionRunner.execute(() -> {
				Optional<Defender> sel1selection = substitutePresenter1.getSelection();
				Optional<Defender> sel2selection = substitutePresenter2.getSelection();
				substitutePresenter1.silentlyDrop(substitutePresenter1.getSelection());
				substitutePresenter1.silentlyAdd(sel2selection);
				substitutePresenter1.silentlySelect(sel2selection);
				substitutePresenter2.silentlyDrop(substitutePresenter2.getSelection());
				substitutePresenter2.silentlyAdd(sel1selection);
				substitutePresenter2.silentlySelect(sel1selection);
			});

			// verify that the drivers were not notified
			verifyNoInteractions(mockGroupDriver);
			verifyNoInteractions(mockSequenceDriver);
			
			// verify intended result is achieved
			assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			assertThat(combo2).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
		}

		@Test @GUITest
		@DisplayName("local swap with source not selected, other selected")
		public void testLocalSwapWithSourceNotSelected() {
			combo1 = TypedJComboBoxFixture.of(window.panel("sel1").comboBox(), Defender.class);
			combo2 = TypedJComboBoxFixture.of(window.panel("sel2").comboBox(), Defender.class);

			// select something on combo1
			combo1.selectItem("Sergio Ramos");
			attachMockDrivers();

			// call for a swap between 2 (source - N) and 1 (other - S)
			GuiActionRunner.execute(() -> {
				Optional<Defender> sel1selection = substitutePresenter1.getSelection();
				Optional<Defender> sel2selection = substitutePresenter2.getSelection();
				substitutePresenter1.silentlyDrop(substitutePresenter1.getSelection());
				substitutePresenter1.silentlyAdd(sel2selection);
				substitutePresenter1.silentlySelect(sel2selection);
				substitutePresenter2.silentlyDrop(substitutePresenter2.getSelection());
				substitutePresenter2.silentlyAdd(sel1selection);
				substitutePresenter2.silentlySelect(sel1selection);
			});
			
			// verify that the drivers were not notified
			verifyNoInteractions(mockGroupDriver);
			verifyNoInteractions(mockSequenceDriver);

			// verify intended result is achieved
			assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			assertThat(combo2).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
		}

		@Test @GUITest
		@DisplayName("local equalize with both source and other having a selection")
		public void testLocalEqualize_SourceSelected_OtherSelected() {
			combo1 = TypedJComboBoxFixture.of(window.panel("sel1").comboBox(), Defender.class);
			combo2 = TypedJComboBoxFixture.of(window.panel("sel2").comboBox(), Defender.class);

			// select something on both combos
			combo1.selectItem("Sergio Ramos");
			combo2.selectItem("Giorgio Chiellini");
			attachMockDrivers();

			// call for 1 (source - S) to equalize to 2 (other - S)
			GuiActionRunner.execute(() -> {
				Optional<Defender> sel2selection = substitutePresenter2.getSelection();
				substitutePresenter1.silentlyDrop(substitutePresenter1.getSelection());
				substitutePresenter1.silentlyAdd(sel2selection);
				substitutePresenter1.silentlySelect(sel2selection);
			});
			
			// verify that the drivers were not notified
			verifyNoInteractions(mockGroupDriver);
			verifyNoInteractions(mockSequenceDriver);

			// verify intended result is achieved
			assertThat(combo1).hasSelected(chiellini).amongOptions(chiellini, pique, silva, vanDijk);
			assertThat(combo2).hasSelected(chiellini).amongOptions(chiellini, pique, silva, vanDijk);

		}

		@Test @GUITest
		@DisplayName("local equalize with source selected, other not selected")
		public void testLocalEqualize_SourceSelected_OtherNotSelected() {
			combo1 = TypedJComboBoxFixture.of(window.panel("sel1").comboBox(), Defender.class);
			combo2 = TypedJComboBoxFixture.of(window.panel("sel2").comboBox(), Defender.class);

			// select something on combo1
			combo1.selectItem("Sergio Ramos");
			attachMockDrivers();

			// call for 1 (source - S) to equalize to 2 (other - N)
			GuiActionRunner.execute(() -> {
				// compPlayerSelector1.setLocalState(compPlayerSelector2.getLocalState());

				Optional<Defender> sel2selection = substitutePresenter2.getSelection();
				substitutePresenter1.silentlyDrop(substitutePresenter1.getSelection());
				substitutePresenter1.silentlyAdd(sel2selection);
				substitutePresenter1.silentlySelect(sel2selection);
			});
			
			// verify that the drivers were not notified
			verifyNoInteractions(mockGroupDriver);
			verifyNoInteractions(mockSequenceDriver);

			// verify intended result is achieved
			assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);

		}

		@Test @GUITest
		@DisplayName("local equalize with source not selected, other selected")
		public void testLocalEqualize_SourceNotSelected_OtherSelected() {
			combo1 = TypedJComboBoxFixture.of(window.panel("sel1").comboBox(), Defender.class);
			combo2 = TypedJComboBoxFixture.of(window.panel("sel2").comboBox(), Defender.class);
			
			// select something on combo1
			combo1.selectItem("Sergio Ramos");
			attachMockDrivers();

			// call for 2 (source - N) to equalize to 1 (other - S)
			GuiActionRunner.execute(() -> {
				Optional<Defender> sel1selection = substitutePresenter1.getSelection();
				substitutePresenter2.silentlyDrop(substitutePresenter2.getSelection());
				substitutePresenter2.silentlyAdd(sel1selection);
				substitutePresenter2.silentlySelect(sel1selection);
			});
			
			// verify that the drivers were not notified
			verifyNoInteractions(mockGroupDriver);
			verifyNoInteractions(mockSequenceDriver);

			// verify intended result is achieved
			assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
			assertThat(combo2).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
		}

		@Test @GUITest
		@DisplayName("local drop with an existing selection")
		public void testLocalDropSelection_WithExistingSelection___retire_restore() {
			combo1 = TypedJComboBoxFixture.of(window.panel("sel1").comboBox(), Defender.class);
			combo2 = TypedJComboBoxFixture.of(window.panel("sel2").comboBox(), Defender.class);

			// select something on combo1
			combo1.selectItem("Sergio Ramos");
			attachMockDrivers();

			// call for 1 to drop its selection silently
			GuiActionRunner.execute(() -> {
				substitutePresenter1.silentlyDrop(substitutePresenter1.getSelection());
			});
			
			// verify that the drivers were not notified
			verifyNoInteractions(mockGroupDriver);
			verifyNoInteractions(mockSequenceDriver);

			// verify intended result is achieved
			assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
		}
	}


}