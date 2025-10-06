package gui.utils;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.List;

public class TypedJComboBoxFixtureAssert<T> extends AbstractAssert<TypedJComboBoxFixtureAssert<T>, TypedJComboBoxFixture<T>> {

    private TypedJComboBoxFixtureAssert(TypedJComboBoxFixture<T> actual) {
        super(actual, TypedJComboBoxFixtureAssert.class);
    }

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