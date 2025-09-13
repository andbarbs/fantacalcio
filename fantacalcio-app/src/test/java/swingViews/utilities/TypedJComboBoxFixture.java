package swingViews.utilities;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.JComboBoxFixture;

import javax.swing.ComboBoxModel;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A type-aware wrapper around AssertJ Swing's JComboBoxFixture.
 * This fixture carries the generic type information of its items, 
 * enabling type-safe custom assertions.
 *
 * @param <T> The type of items in the JComboBox.
 */
public class TypedJComboBoxFixture<T> {

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
