package gui.utils;

import java.awt.Component;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.JComboBoxFixture;

/**
 * contains useful tools for writing tests using AssertJ Swing
 */
public abstract class AssertJSwingUtils {

	private AssertJSwingUtils() {
		super();
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
	
	/**
	 * a custom {@link TypedJComboBoxFixtureAssert} for writing fluent assertions on
	 * {@link JComboBox} using a static overload for
	 * {@link org.assertj.core.api.Assertions.assertThat}
	 * 
	 * @param <T> the type for options in the {@link JComboBox}
	 */
	public static class TypedJComboBoxFixtureAssert<T> extends
			AbstractAssert<TypedJComboBoxFixtureAssert<T>, TypedJComboBoxFixtureAssert.TypedJComboBoxFixture<T>> {
	
		/**
		 * A type-aware wrapper around AssertJ Swing's {@link JComboBoxFixture}.
		 * This fixture carries the generic type information of its items, 
		 * enabling type-safe custom assertions.
		 *
		 * @param <T> the type for options in the {@link JComboBox}
		 */
		public static class TypedJComboBoxFixture<T> {

		    private final JComboBoxFixture delegate;
		    private final Class<T> itemType;

		    private TypedJComboBoxFixture(JComboBoxFixture delegate, Class<T> itemType) {
		        this.delegate = delegate;
		        this.itemType = itemType;
		        
		        this.allItems(); // validates combo items against the provided itemType
		    }
		    
		    /**
		     * Converts a standard JComboBoxFixture into a type-aware TypedJComboBoxFixture.
		     * This method performs an immediate runtime check to ensure the items in the combo box
		     * are compatible with the provided item type.
		     *
		     * @param comboFixture The standard JComboBoxFixture to wrap.
		     * @param itemType The Class object representing the type of items in the combo box.
		     * @param <T> The type of items in the combo box.
		     * @return A new TypedJComboBoxFixture instance.
		     * @throws NullPointerException if comboFixture or itemType is null.
		     * @throws IllegalStateException if the combo box contains items incompatible with itemType.
		     */
		    public static <T> TypedJComboBoxFixture<T> of(JComboBoxFixture comboFixture, Class<T> itemType) {
		        Objects.requireNonNull(comboFixture, "JComboBoxFixture cannot be null.");
		        Objects.requireNonNull(itemType, "Item type cannot be null.");
		        return new TypedJComboBoxFixture<>(comboFixture, itemType);
		    }
		    
		    // used in tests to simulate GUI interaction with the combo
		    public TypedJComboBoxFixture<T> selectItem(String item) {
		        delegate.selectItem(item);
		        return this;
		    }

		    // used by JComboBoxFixtureAssert to implement hasSelectedOption()
		    public T selectedItem() {
		        return GuiActionRunner.execute(() -> {
		            Object selected = delegate.target().getSelectedItem();
		            if (selected == null)
		                return null;
		            if (!itemType.isInstance(selected)) {
		                throw new IllegalStateException(
		                    String.format("Selected item is not of expected type. "
		                    		+ "Expected <%s> but found <%s> of type %s",
		                        itemType.getName(), selected, selected.getClass().getName()));
		            }
		            return itemType.cast(selected);
		        });
		    }

		    // used by JComboBoxFixtureAssert to implement amongAvailableOptions()
		    public List<T> allItems() {
		        return GuiActionRunner.execute(() -> {
		            ComboBoxModel<?> model = delegate.target().getModel();
		            return IntStream.range(0, model.getSize())
		                    .mapToObj(i -> {
		                    	Object element = model.getElementAt(i);
		                    	if (!itemType.isInstance(element)) {
		                            throw new IllegalStateException(
		                                String.format("Combo box model contains object of unexpected type. "
		                                		+ "Expected <%s> but found <%s> of type %s",
		                                    itemType.getName(), element, 
		                                    element != null ? element.getClass().getName() : "null"));
		                        }
		                        return itemType.cast(element);
		                    })
		                    .collect(Collectors.toList());
		        });
		    }
		}

	    private TypedJComboBoxFixtureAssert(TypedJComboBoxFixture<T> actual) {
	        super(actual, TypedJComboBoxFixtureAssert.class);
	    }

		/**
		 * a static overloaded version of
		 * {@link org.assertj.core.api.Assertions.assertThat} for use with this custom
		 * {@link TypedJComboBoxFixtureAssert}
		 * 
		 * @param <T> the type for options in the {@link JComboBox}
		 */
	    public static <T> TypedJComboBoxFixtureAssert<T> assertThat(TypedJComboBoxFixture<T> actual) {
	        return new TypedJComboBoxFixtureAssert<>(actual);
	    }

	    public TypedJComboBoxFixtureAssert<T> hasSelected(T expectedSelected) {
	        isNotNull();
	        T actualSelected = actual.selectedItem();

	        if (actualSelected == null && expectedSelected == null) {} 
	        
	        // effectively asserts equality
	        else if (actualSelected == null || 
	        		!actualSelected.equals(expectedSelected)) {
	            failWithActualExpectedAndMessage(actualSelected, expectedSelected,
	                "Expected selected item to be <%s> but was <%s>", expectedSelected, actualSelected);
	        }
	        return this;
	    }

	    @SafeVarargs
		public final TypedJComboBoxFixtureAssert<T> amongOptions(T... expectedItems) {
	        isNotNull();
	        List<T> actualItems = actual.allItems();
	        Assertions.assertThat(actualItems).containsExactly(expectedItems);
	        return this;
	    }
	}
}
