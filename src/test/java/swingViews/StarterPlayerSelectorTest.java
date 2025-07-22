package swingViews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static swingViews.utilities.TypedJComboBoxFixtureAssert.assertThat;

import java.awt.FlowLayout;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import swingViews.PlayerSelectorPresenter.StarterPlayerSelectorListener;
import swingViews.utilities.AssertJSwingJUnit5TestCase;
import swingViews.utilities.TypedJComboBoxFixture;


@DisplayName("StarterPlayerSelector: comprehensive unit test")
@ExtendWith(MockitoExtension.class)
public class StarterPlayerSelectorTest extends AssertJSwingJUnit5TestCase {

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

    	private PlayerSelectorPresenter<Defender> compPlayerSelector;    	
    	@Mock private OptionDealerGroupDriver<PlayerSelectorPresenter<Defender>, Defender> driver;    	
    	@Mock private StarterPlayerSelectorListener<Defender> listener;

    	@BeforeEach
    	public void testCaseSpecificSetup() {    		
    		JFrame frame = GuiActionRunner.execute(() -> { // Wrap the panel in a frame.
    			SwingSubPlayerSelector<Defender> selView= new SwingSubPlayerSelector<Defender>();
    			compPlayerSelector = new PlayerSelectorPresenter<Defender>(selView);
    			selView.setPresenter(compPlayerSelector);

    			// manually wires mock driver and options
    			compPlayerSelector.attachDriver(driver);
    			compPlayerSelector.attachOptions(
    					List.of(chiellini, ramos, silva));
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
            	compPlayerSelector.select(Optional.of(chiellini));
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
		
		private PlayerSelectorPresenter<Defender> compPlayerSelector1, compPlayerSelector2;

	    @Mock
	    private OptionDealerGroupDriver<PlayerSelectorPresenter<Defender>, Defender> mockDriver;
	    
	    // strongly typed fixtures for the combos
	    private TypedJComboBoxFixture<Defender> combo1;
		private TypedJComboBoxFixture<Defender> combo2;
	    
	    @BeforeEach
	    public void testSpecificSetUp() {
	        JFrame frame = GuiActionRunner.execute(() -> {
	        	SwingSubPlayerSelector<Defender> sel1View = new SwingSubPlayerSelector<Defender>();
	        	sel1View.setName("sel1");
	        	compPlayerSelector1 = new PlayerSelectorPresenter<Defender>(sel1View);
    			sel1View.setPresenter(compPlayerSelector1);

    			SwingSubPlayerSelector<Defender> sel2View = new SwingSubPlayerSelector<Defender>();
	        	sel2View.setName("sel2");
	        	compPlayerSelector2 = new PlayerSelectorPresenter<Defender>(sel2View);
    			sel2View.setPresenter(compPlayerSelector2);

	            // wires a real driver for tests' setup phase
	            OptionDealerGroupDriver.initializeDealing(
	                    Set.of(compPlayerSelector1, compPlayerSelector2),
	                    List.of(chiellini, pique, ramos, silva, vanDijk));

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

	    private void attachMockDriver() {
	        GuiActionRunner.execute(() -> {
	            List.of(compPlayerSelector1, compPlayerSelector2).forEach(
	                    sel -> sel.attachDriver(mockDriver));
	        });
	    }

		@Nested
		@DisplayName("silentlyDrop(option) operator")
		class SilentlyDrop {

			@Test @GUITest
			@DisplayName("silentlyDrop valid option with an existing selection")
			public void testSilentlyDropValidOption_WithExistingSelection() {

				// select something on combo1
				combo1.selectItem("Sergio Ramos");

				// attach the mock driver to capture subsequent interactions
				attachMockDriver();

				// call for 1 to silently drop a not-selected option
				GuiActionRunner.execute(() -> {
					compPlayerSelector1.silentlyDrop(Optional.of(chiellini));
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 1 to drop its selection silently
				GuiActionRunner.execute(() -> {
					compPlayerSelector1.silentlyDrop(Optional.of(ramos));
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

				// ramos was dropped from combo1 and not restored to combo2
				assertThat(combo1).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyDrop valid option with no existing selection")
			public void testSilentlyDropValidOption_WithNoExistingSelection() {

				// don't select anything on combo1

				// attach the mock driver to capture subsequent interactions
				attachMockDriver();

				// call for 1 to silently drop a not-selected option
				GuiActionRunner.execute(() -> {
					compPlayerSelector1.silentlyDrop(Optional.of(chiellini));
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 1 to silently drop an empty option
				GuiActionRunner.execute(() -> {
					compPlayerSelector1.silentlyDrop(Optional.empty());
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

				// nothing has changed on either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyDrop empty option with no existing selection")
			public void testSilentlyDropEmptyOption_WithNoExistingSelection() {

				// don't select anything on combo1

				// attach the mock driver to capture subsequent interactions
				attachMockDriver();

				// call for 1 to silently drop an empty option
				GuiActionRunner.execute(() -> {
					compPlayerSelector1.silentlyDrop(Optional.empty());
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						compPlayerSelector1.silentlyDrop(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

				// nothing has changed on either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyDrop invalid option with no existing selection")
			public void testSilentlyDropInvalidOption_WithNoExistingSelection() {

				// don't select anything on combo1

				// attach the mock driver to capture subsequent interactions
				attachMockDriver();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						compPlayerSelector1.silentlyDrop(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 2 to silently drop an option it already lacks
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						compPlayerSelector2.silentlyDrop(Optional.of(ramos));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 2 to silently add the missing option
				GuiActionRunner.execute(() -> {
					compPlayerSelector2.silentlyAdd(Optional.of(ramos));
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 2 to silently add the missing option
				GuiActionRunner.execute(() -> {
					compPlayerSelector2.silentlyAdd(Optional.of(ramos));
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						compPlayerSelector1.silentlyAdd(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

				// nothing has changed on either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlyAdd invalid option with no existing selection")
			public void testSilentlyAddInvalidOption_WithNoExistingSelection() {

				// don't select anything on combo1

				// attach the mock driver to capture subsequent interactions
				attachMockDriver();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						compPlayerSelector1.silentlyAdd(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						compPlayerSelector1.silentlyAdd(Optional.of(chiellini));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 1 to silently select a valid option
				GuiActionRunner.execute(() -> {
					compPlayerSelector1.silentlySelect(Optional.of(chiellini));
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 1 to silently select a valid option
				GuiActionRunner.execute(() -> {
					compPlayerSelector1.silentlySelect(Optional.of(chiellini));
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 1 to silently select the already selected option
				GuiActionRunner.execute(() -> {
					compPlayerSelector1.silentlySelect(Optional.of(ramos));
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

				// both combos are unaffected
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlySelect of empty option with no previous selection")
			public void testSilentlySelectEmptyOption_WithNoPreviousSelection() {

				// don't select anything on either combo

				// attach the mock driver to capture subsequent interactions
				attachMockDriver();

				// call for 1 to silently select a valid option
				GuiActionRunner.execute(() -> {
					compPlayerSelector1.silentlySelect(Optional.empty());
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 1 to silently select a valid option
				GuiActionRunner.execute(() -> {
					compPlayerSelector1.silentlySelect(Optional.empty());
				});

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						compPlayerSelector1.silentlySelect(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

				// nothing has changed on either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}

			@Test @GUITest
			@DisplayName("silentlySelect invalid option with no existing selection")
			public void testSilentlySelectInvalidOption_WithNoExistingSelection() {

				// don't select anything on combo1

				// attach the mock driver to capture subsequent interactions
				attachMockDriver();

				// call for 1 to silently drop an invalid option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						compPlayerSelector1.silentlySelect(Optional.of(new Defender("Luca", "Toni")));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 2 to silently select a valid but missing option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						compPlayerSelector2.silentlySelect(Optional.of(ramos));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

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
				attachMockDriver();

				// call for 2 to silently select a valid but missing option
				assertThatThrownBy(() -> {
					GuiActionRunner.execute(() -> {
						compPlayerSelector2.silentlySelect(Optional.of(ramos));
					});
				}).isInstanceOf(IllegalArgumentException.class);

				// verify that the driver was never notified
				verifyNoInteractions(mockDriver);

				// verify nothing has happened to either combo
				assertThat(combo1).hasSelected(ramos).amongOptions(chiellini, pique, ramos, silva, vanDijk);
				assertThat(combo2).hasSelected(null).amongOptions(chiellini, pique, silva, vanDijk);
			}
		}
	}

}