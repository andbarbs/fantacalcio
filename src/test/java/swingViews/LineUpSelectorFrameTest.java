package swingViews;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GUITestRunner.class)
public class LineUpSelectorFrameTest extends AssertJSwingJUnitTestCase  {
	
	private FrameFixture window;
	
	private LineUpSelectorFrame view;
	
	

	@Override
	protected void onSetUp() {
		GuiActionRunner.execute(() -> {
			view = new LineUpSelectorFrame();
			return view;
		});
		window = new FrameFixture(robot(), view);
		window.show();		
	}
	
	@Test @GUITest
	public void test() {
		window.label(JLabelMatcher.withText("seleziona formazione"));
	}


}
