package swingViews;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import swingViews.OptionDealerGroupDriver.OrderedOptionDealer;

/**
 * an implementation of CompetingComboBox that relies on an
 * {@link OptionDealerGroupDriver} to drive competition.
 * 
 * Clients should use it as follows: 
 * <pre>
 * {@code NewCompetingComboBox<String> combo1 = new NewCompetingComboBox<>();
 * NewCompetingComboBox<String> combo2 = new NewCompetingComboBox<>();
 * 
 * OptionDealerGroupDriver.initializeDealing(
 *     Set.of(combo1, combo2),
 *     Arrays.asList("A", "B", "C"));
 * }</pre>
 *
 * @see OptionDealerGroupDriver
 */
@SuppressWarnings("serial")
public class NewCompetingComboBox<T> extends JComboBox<T> implements OrderedOptionDealer<NewCompetingComboBox<T>, T>{
	
	private static final int COMPETING_CBOX_NO_CHOICE = -1;

	private List<T> options;          // the original option pool, ordered
	private List<Integer> mask;       // contains the linear indices in this.options of elements in the combo's model
	private Integer currentSelection; // contains the linear index in this.options of the combo's current selection

	private OptionDealerGroupDriver<NewCompetingComboBox<T>, T> driver;	
	
	public NewCompetingComboBox() {
		/* an ActionEvent is fired on a JComboBox under these circumstances:
		 * 		1) a new/the existing selection is set on the combo, either by the user or programmatically
		 * 		2) the selection is (programmatically) set to -1
		 * 		3) an entry is REMOVED from the underlying model AND no user selection exists   */
		addActionListener(e -> compete(getSelectedIndex()));
	}

	private void compete(int selectedIndex) {
		
		// a programmatic or user choice has been made on this CBox
		if (selectedIndex != -1) {
			if (currentSelection != -1)
				driver.selectionClearedOn(this, currentSelection); 
			currentSelection = mask.get(selectedIndex);
			driver.selectionMadeOn(this, currentSelection);
		}
		
		// this CBox's choice has just been cleared
		else if (selectedIndex == -1 && 
				currentSelection != null &&   // corner condition
				currentSelection != COMPETING_CBOX_NO_CHOICE) {
			driver.selectionClearedOn(this, currentSelection);
			currentSelection = COMPETING_CBOX_NO_CHOICE;
		}
	}

	@Override
	public void attachDriver(OptionDealerGroupDriver<NewCompetingComboBox<T>, T> driver) {
		this.driver = driver;
	}

	@Override
	public void attachOptions(List<T> options) {
		this.options = options;
		mask = new ArrayList<Integer>(
				IntStream.rangeClosed(0, options.size() - 1).boxed().collect(Collectors.toList()));
		
		// fills the cbox with initial contents
		setModel(new DefaultComboBoxModel<>(new Vector<>(options)));
		
		// sets the cbox's starting selection to none
		currentSelection = -1;
		setSelectedIndex(-1);
	}

	@Override
	public void retireOption(int index) {
		int pos = mask.indexOf(index);
		mask.remove(pos);
		DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) this.getModel();
		model.removeElementAt(pos);
	}

	@Override
	public void restoreOption(int index) {
		int insertionIndex = IntStream
				.range(0, mask.size())
				.filter(k -> mask.get(k) >= index)
				.findFirst().orElse(mask.size());
		mask.add(insertionIndex, index);
		DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) this.getModel();
		model.insertElementAt(options.get(index), insertionIndex);
	}
}
