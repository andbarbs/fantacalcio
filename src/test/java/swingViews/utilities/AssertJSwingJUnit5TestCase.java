package swingViews.utilities;

import org.assertj.swing.core.Robot;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

// This class centralizes only GUI-related extensions
@ExtendWith({ GUITestExtension.class })
public abstract class AssertJSwingJUnit5TestCase {
    
    protected FrameFixture window;
    protected Robot robot;

	/*
	 * JUnit 5 guarantees a very specific and reliable execution order for life-cycle
	 * methods in an inheritance hierarchy:
	 * 
	 * @BeforeAll: Executed from the top-most superclass down to the subclass.
	 * 
	 * @BeforeEach: Executed from the top-most superclass down to the subclass.
	 * 
	 * @Test: The test method itself is executed.
	 * 
	 * @AfterEach: Executed from the subclass up to the top-most superclass.
	 * 
	 * @AfterAll: Executed from the subclass up to the top-most superclass.
	 */
    
    
    @BeforeAll
    static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }

    @BeforeEach
    public void baseSetUp() {
        robot = BasicRobot.robotWithCurrentAwtHierarchy();
    }

    @AfterEach
    public void baseTearDown() {
        if (window != null) {
            window.cleanUp();
        }
        if (robot != null) {
            robot.cleanUp();
        }
    }
}