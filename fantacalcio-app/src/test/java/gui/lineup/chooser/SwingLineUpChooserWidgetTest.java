package gui.lineup.chooser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gui.utils.AssertJSwingJupiterTestCase;
import gui.utils.GUITestExtension;

@DisplayName("A SwingLineUpChooserWidget")
@ExtendWith(MockitoExtension.class)
@ExtendWith(GUITestExtension.class)
@Tag("non-JPMS-compliant")
@Tag("mockito-agent")
public class SwingLineUpChooserWidgetTest extends AssertJSwingJupiterTestCase {
	
	// injected Widget refs
	private JPanel fakeStarterWidget, 
			fakeGoalieTripletWidget, fakeDefTripletWidget, fakeMidTripletWidget, fakeForwTripletWidget;

	// the SUT reference
	private SwingLineUpChooserWidget widget;	

	private JButtonFixture saveButton;

	@Override
	protected void onSetUp() throws Exception {
		JFrame frame = GuiActionRunner.execute(() -> {
			
			// instantiates fake widget dependencies
			fakeStarterWidget = new JPanel();
			fakeStarterWidget.setPreferredSize(new Dimension(300, 500));
			
			Dimension tripletDims = new Dimension(300, 120);
			fakeGoalieTripletWidget = new JPanel();
			fakeDefTripletWidget = new JPanel();
			fakeMidTripletWidget = new JPanel();
			fakeForwTripletWidget = new JPanel();
			List.of(fakeGoalieTripletWidget, fakeDefTripletWidget, fakeMidTripletWidget, fakeForwTripletWidget)
			.forEach(fakeTriplet -> fakeTriplet.setPreferredSize(tripletDims));
			
			List.of(fakeStarterWidget, fakeGoalieTripletWidget, fakeDefTripletWidget, fakeMidTripletWidget,
					fakeForwTripletWidget).forEach(fake -> fake.setBackground(Color.ORANGE));
			
			// instantiates SUT
			widget = new SwingLineUpChooserWidget(false, fakeStarterWidget, fakeGoalieTripletWidget,
					fakeDefTripletWidget, fakeMidTripletWidget, fakeForwTripletWidget);
			
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
		
		// retrieves 'save' button fixture
		saveButton = window.button(JButtonMatcher.withText("save"));
	}

	@Nested
	@DisplayName("graphically contains")
	class Contains {
		
		@Test
		@GUITest
		@DisplayName("the four injected widgets")
		public void widgetsProvidedToConstructor() {
			List.of(fakeStarterWidget, fakeGoalieTripletWidget, fakeDefTripletWidget, fakeMidTripletWidget,
					fakeForwTripletWidget).forEach(fakeWidget -> assertThat(fakeWidget.getParent()).isSameAs(widget));
		}
		
		@Test
		@GUITest
		@DisplayName("a \"save\" button, which is initially disabled")
		public void buttonsDisabled() {
			saveButton.requireDisabled();
		}
	}
	
	@Nested
	@DisplayName("satisfies Controller requests to")
	class Subsequently {
		
		@Test
		@GUITest
		@DisplayName("enable saving")
		public void controllerEnables() {
			
			// GIVEN the 'save' button is disabled
			GuiActionRunner.execute(() -> saveButton.target().setEnabled(false));
			
			// WHEN Controller requests to enable saving
			GuiActionRunner.execute(() -> widget.enableSavingLineUp());
			
			// THEN the 'save' button is enabled
			saveButton.requireEnabled();
		}

		@Test
		@GUITest
		@DisplayName("disable saving")
		public void controllerDisables() {
			
			// GIVEN the 'save' button is enabled
			GuiActionRunner.execute(() -> saveButton.target().setEnabled(true));

			// WHEN Controller requests to disable saving
			GuiActionRunner.execute(() -> widget.disableSavingLineUp());

			// THEN the 'save' button is disabled
			saveButton.requireDisabled();
		}
	}
	
	@Test
	@GUITest
	@DisplayName("forwards user save request to Controller")
	public void forwardsSaveRequest(@Mock LineUpChooserController mockController) {
		
		// GIVEN a Controller has been set into the SUT
		widget.setController(mockController);
		
		// GIVEN the 'save' button is enabled
		GuiActionRunner.execute(() -> saveButton.target().setEnabled(true));
		
		// WHEN user clicks the 'save' button and EDT goes idle
		saveButton.click();
		robot().waitForIdle();
		
		// THEN a request to save is sent to the Controller
		verify(mockController).saveLineUp();
	}
}
