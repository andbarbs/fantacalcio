package gui.utils;

import org.assertj.swing.core.Robot;

import java.awt.Component;

import javax.swing.JRadioButton;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
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
    
    // Matchers
    
	protected static GenericTypeMatcher<JRadioButton> withText(String text) {
		return new GenericTypeMatcher<>(JRadioButton.class) {
			@Override
			protected boolean isMatching(JRadioButton component) {
				return component.getText().equals(text);
			}
		};
	}
	
	/**
     * Creates a matcher that finds a component by its object identity.
     * This is useful for testing DI-based components where you need to
     * verify that a specific instance has been added to the hierarchy.
     *
     * @param <S> The type of the component to match.
     * @param expected The component instance to find.
     * @return A matcher that returns true if the actual component is the same as the expected one.
     */
    @SuppressWarnings("unchecked")
    protected static <S extends Component> GenericTypeMatcher<S> sameAs(S expected) {
        return new GenericTypeMatcher<S>((Class<S>) expected.getClass()) {
            @Override
            protected boolean isMatching(S actual) {
                return actual == expected;
            }
        };
    }
    
    
    
}