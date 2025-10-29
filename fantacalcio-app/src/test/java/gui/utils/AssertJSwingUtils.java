package gui.utils;

import java.awt.Component;

import javax.swing.JRadioButton;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JRadioButtonFixture;

/**
 * contains useful tools for writing tests using AssertJ Swing
 */
public abstract class AssertJSwingUtils {

	private AssertJSwingUtils() {
		super();
	}
	
	/**
	 * a timeout value to be used when synchronizing with the <code>EDT</code>
	 */
    public static final int TIMEOUT = 5000;
    
    /**
     * helps diagnose CI environment state before calls to ButtonFixture.click()
     * @param buttonFixture
     * @param window
     */
    public static void diagnosePreClickState(JButtonFixture buttonFixture, FrameFixture window) {
		System.out.println("--- PRE-CLICK DIAGNOSTICS ---");
		GuiActionRunner.execute(() -> {
			System.out.println("Button isShowing: " + buttonFixture.target().isShowing());
		    System.out.println("Button isEnabled: " + buttonFixture.target().isEnabled());
			System.out.println("Window isActive: " + window.target().isActive());
		    System.out.println("Window isFocused: " + window.target().isFocused());
		    try {
		        java.awt.Point location = buttonFixture.target().getLocationOnScreen();
		        System.out.println("Button locationOnScreen: " + location);
		    } catch (Exception e) {
		        System.out.println("Button locationOnScreen: FAILED with " + e.getMessage());
		    }
		});
		System.out.println("-----------------------------");
	}
    
    /**
     * helps diagnose CI environment state before calls to ButtonFixture.click()
     * @param radioButtonFixture
     * @param window
     */
    public static void diagnosePreClickState(JRadioButtonFixture radioButtonFixture, FrameFixture window) {
		System.out.println("--- PRE-CLICK DIAGNOSTICS ---");
		GuiActionRunner.execute(() -> {
			System.out.println("Button isShowing: " + radioButtonFixture.target().isShowing());
		    System.out.println("Button isEnabled: " + radioButtonFixture.target().isEnabled());
			System.out.println("Window isActive: " + window.target().isActive());
		    System.out.println("Window isFocused: " + window.target().isFocused());
		    try {
		        java.awt.Point location = radioButtonFixture.target().getLocationOnScreen();
		        System.out.println("Button locationOnScreen: " + location);
		    } catch (Exception e) {
		        System.out.println("Button locationOnScreen: FAILED with " + e.getMessage());
		    }
		});
		System.out.println("-----------------------------");
	}

	// Matchers

	public static GenericTypeMatcher<JRadioButton> withText(String text) {
		return new GenericTypeMatcher<>(JRadioButton.class) {
			@Override
			protected boolean isMatching(JRadioButton component) {
				return component.getText().equals(text);
			}
		};
	}

	/**
	 * Creates a matcher that finds a component by its object identity. This is
	 * useful for testing DI-based components where you need to verify that a
	 * specific instance has been added to the hierarchy.
	 *
	 * @param <S>      The type of the component to match.
	 * @param expected The component instance to find.
	 * @return A matcher that returns true if the actual component is the same as
	 *         the expected one.
	 */
	@SuppressWarnings("unchecked")
	public static <S extends Component> GenericTypeMatcher<S> sameAs(S expected) {
		return new GenericTypeMatcher<S>((Class<S>) expected.getClass()) {
			@Override
			protected boolean isMatching(S actual) {
				return actual == expected;
			}
		};
	}
}
