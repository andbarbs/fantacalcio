package swingViews;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import domainModel.Player;
import swingViews.OptionDealerGroupDriver.OrderedOptionDealer;

@SuppressWarnings("serial")
public class StarterPlayerSelector<Y extends Player> extends JPanel 
				implements OrderedOptionDealer<StarterPlayerSelector<Y>, Y> {

	

	private PlayerSelectorForm<Y> form;
	
	private JComboBox<Y> comboBox;
	private JLabel figureLabel;
	private JButton resetButton;
	private JLabel headLabel;
	
	private void initFormMembersShortcuts() {
		comboBox = form.getComboBox();
		resetButton = form.getResetButton();
		headLabel = form.getHeadLabel();
		figureLabel = form.getFigureLabel();
	}

	// WB-compatible constructor
	public StarterPlayerSelector() {
		form = new PlayerSelectorForm<Y>();
		wireUpForm();
	}

	// rescaling-augmented constructor available to clients
	public StarterPlayerSelector(Dimension availableWindow) throws IOException {
		form = new PlayerSelectorForm<Y>(availableWindow);
		wireUpForm();
	}
	
	private void wireUpForm() {		
		initFormMembersShortcuts();
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
		add(form);  // adds the PlayerSelectorForm as the only child

		comboBox.addActionListener(e -> {
			// combo -> button interaction
			resetButton.setEnabled(comboBox.getSelectedIndex() > -1);
			
			// decides whether to contact the driver
			notifyDriver();
			
			// propagates user selections to subclasses
			if (comboBox.isPopupVisible() && 
					comboBox.getSelectedIndex() > -1)
				onUserSelectionSet();
		});

		resetButton.addActionListener(e -> {
			// button -> combo interaction
			comboBox.setSelectedIndex(-1);
			resetButton.setEnabled(false);
			
			// propagates selection clearance to subclasses
			onSelectionCleared();
		});
	}	
	
	/**
	 * is responsible for deciding whether to contact the
	 * {@link OptionDealerGroupDriver} in response to a selection event on the
	 * composed combo. In particular, the driver is contacted only under these
	 * circumstances:
	 * <ol>
	 * 		<li>a selection has been set <i> by the user </i> on the combo, thus
	 * 		<ul>
	 * 			<li>a previous selection should be restored on other dealers
	 * 			<li>the current selection should be retired from other dealers
	 * 		</ul>
	 * 		<li>the combo's selection has <i>just</i> been cleared, thus
	 * 		<ul> 
	 * 			<li>the previous selection should be restored on other dealers
	 * 		</ul>
	 * </ol>
	 * as no driver intervention is able to elicit these kinds of event,
	 * this method effectively shields the driver from event feedback.
	 * 
	 * <p> Additionally, due to the combo's complete encapsulation within 
	 * {@link StarterPlayerSelector}, this class is the <i>sole possible 
	 * originator</i> of programmatic interactions with the combo.
	 * Thus, any call to the driver must happen within this method.
	 */
	private void notifyDriver() {
		int selectedIndex = comboBox.getSelectedIndex();
		
		// a user selection has been set on this CBox
		if (comboBox.isPopupVisible() && selectedIndex != -1) {
			if (currentSelection != -1)
				driver.selectionClearedOn(this, currentSelection);
			currentSelection = mask.get(selectedIndex);
			driver.selectionMadeOn(this, currentSelection);
		}

		// this CBox's selection has just been cleared
		else if (selectedIndex == -1 && 
				currentSelection != null && // false before option attachment
				currentSelection != NO_SELECTION) {
			driver.selectionClearedOn(this, currentSelection);
			currentSelection = NO_SELECTION;
		}
	}

	// subclass hooks for augmenting selection event handling
	protected void onUserSelectionSet() {}
	protected void onSelectionCleared() {}
	
	
	/********     OrderedOptionDealer BOOKKEEPING 	 ********/

	private OptionDealerGroupDriver<StarterPlayerSelector<Y>, Y> driver;
	private List<Y> options;          // the original option pool, ordered
	private List<Integer> mask;       // contains the linear indices in this.options of elements in the combo's model
	private Integer currentSelection; // contains the linear index in this.options of the combo's current selection	
	private static final int NO_SELECTION = -1;

	@Override
	public void attachDriver(OptionDealerGroupDriver<StarterPlayerSelector<Y>, Y> driver) {
		this.driver = driver;		
	}

	@Override
	public void attachOptions(List<Y> options) {
		this.options = options;
		mask = new ArrayList<Integer>(
				IntStream.rangeClosed(0, options.size() - 1).boxed().collect(Collectors.toList()));
		comboBox.setModel(  				// fills combo with initial contents
				new DefaultComboBoxModel<>(new Vector<>(options)));
		comboBox.setSelectedIndex(-1);      // must be re-done after setModel
		currentSelection = NO_SELECTION;
	}

	@Override
	public void retireOption(int index) {
		int pos = mask.indexOf(index);
		mask.remove(pos);
		DefaultComboBoxModel<Y> model = (DefaultComboBoxModel<Y>) comboBox.getModel();
		model.removeElementAt(pos);
	}

	@Override
	public void restoreOption(int index) {
		int insertionIndex = IntStream
				.range(0, mask.size())
				.filter(k -> mask.get(k) >= index)
				.findFirst().orElse(mask.size());
		mask.add(insertionIndex, index);
		DefaultComboBoxModel<Y> model = (DefaultComboBoxModel<Y>) comboBox.getModel();
		model.insertElementAt(options.get(index), insertionIndex);
	}
	
	public void select(Optional<Y>  player) {
		if (player.isEmpty()) {
			comboBox.setSelectedIndex(-1);
		}
		else {
			// TODO check that the combo's model contains player!!
			comboBox.setSelectedItem(player);
		}
	}
	
	/* CONVENIENCE METHODS FOR SUBCLASSES 
	 * these methods offer additional functionality subclasses may find useful,
	 * as compromise for keeping the combo and bookkeeping encapsulated.
	 * 
	 * Encapsulation of the combo and bookkeeping ensures these implementations
	 * 		- are mindful of the OptionDealerGroupDriver
	 * 		- do not risk leaking back to drivers in subclasses
	 * 
	 * However, this is GODDAMN AWFUL CODE!!!
	 * 		- StarterPlayerSelector is right to encapsulate its bookkeeping
	 * 		- but in so doing it has become tightly coupled with the business
	 * 		  of one particular subclass!
	 *      	- notice how acquireSelectionFrom is grossly dependent upon
	 *       	  knowledge of the FillableSwappable collapsing operation
	 */
	
	protected void swapSelectionWith(StarterPlayerSelector<Y> other) {
		// does NOT rely on emptying-out combos!
		// does NOT feedback to the driver
		int indMine = currentSelection;
		int indOther = other.currentSelection;
		
		other.restoreOption(indMine);
		other.comboBox.setSelectedIndex(other.mask.indexOf(indMine));
		other.currentSelection = indMine;
		other.retireOption(indOther);
		
		this.restoreOption(indOther);
		this.comboBox.setSelectedIndex(this.mask.indexOf(indOther));
		this.currentSelection = indOther;
		this.retireOption(indMine);
	}
	
	protected void acquireSelectionFrom(StarterPlayerSelector<Y> other) {		
		// does NOT rely on emptying-out combos!
		// does NOT feedback to the driver
		int indOther = other.currentSelection;
		int indMine = this.currentSelection;
		
		this.restoreOption(indOther);
		this.comboBox.setSelectedIndex(this.mask.indexOf(indOther));
		this.currentSelection = indOther;
		if (indMine != NO_SELECTION)
			this.retireOption(indMine);
	}

	protected void discardContent() {
		retireOption(currentSelection);
		currentSelection = NO_SELECTION;		
		comboBox.setSelectedIndex(-1);   // must happen here so no broadcast!
	}

	protected void enableUserInteraction() {
		comboBox.setEnabled(true);
		figureLabel.setEnabled(true);
		headLabel.setEnabled(true);
	}

	protected void disableUserInteraction() {
		comboBox.setEnabled(false);
		figureLabel.setEnabled(false);
		headLabel.setEnabled(false);
	}
	
	
	protected UnaryLocalSelectionOperator<Y> locally() {
		return new UnaryLocalSelectionOperator<Y>(this);
	}
	
	protected static class UnaryLocalSelectionOperator<Y extends Player> {

		private final StarterPlayerSelector<Y> source;

		private UnaryLocalSelectionOperator(StarterPlayerSelector<Y> source) {
			this.source = source;
		}

		protected void dropSelection() {
			if (source.currentSelection != NO_SELECTION) {
				source.retireOption(source.currentSelection);
				source.currentSelection = NO_SELECTION;		
				source.comboBox.setSelectedIndex(-1);
			}
		}

		protected BinaryLocalSelectionOperator<Y> takeOverSelectionFrom(StarterPlayerSelector<Y> other) {
			return new BinaryLocalSelectionOperator<Y>(source, other);
		}
	}
	
	protected static class BinaryLocalSelectionOperator<Y extends Player> {

		private final StarterPlayerSelector<Y> other;
		private final int indSource, indOther;

		private BinaryLocalSelectionOperator(StarterPlayerSelector<Y> source, StarterPlayerSelector<Y> other) {
			this.other = other;
			indOther = other.currentSelection;
			indSource = source.currentSelection;

			// assumes only other has a selection
			source.restoreOption(indOther);
			source.comboBox.setSelectedIndex(source.mask.indexOf(indOther));
			source.currentSelection = indOther;
			if (indSource != NO_SELECTION)
				source.retireOption(indSource);
		}
		
		protected void pushingYoursToThem() {
			// assumes both source and other have a selection			
			other.restoreOption(indSource);
			other.comboBox.setSelectedIndex(other.mask.indexOf(indSource));
			other.currentSelection = indSource;
			other.retireOption(indOther);
		}

		protected void droppingYours() {
			
		}

	}
	


	
	
	
	
	
	
}
