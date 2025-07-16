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
public class StarterPlayerSelector<P extends Player> extends JPanel 
				implements OrderedOptionDealer<StarterPlayerSelector<P>, P> {
	
	
	/*
	 * TODO centralize the handling of combo events into one coherent 
	 * block that defines the rules for 
	 * 		- what events should trigger internal state changes
	 * 		- what events should trigger driver notifications
	 * 		- what events should trigger listener notifications
	 * 		- what events should trigger subclass engagement
	 * bearing in mind that, thanks to combo encapsulation, this class
	 * is the sole originator of programmatic selection events on the combo
	 * 
	 * TODO consider pulling up observer logic into a superclass,
	 * and any logic that doesn't directly depend on internal bookkeeping
	 * 
	 * TODO consider moving any GUI trivial interaction logic,
	 * such as combo <-> button and future combo <-> label, 
	 * into PlayerSelectorForm and have it tested there
	 * 
	 * TODO in PlayerSelectorForm, consider introducing distinct
	 * private and public design logic
	 * 
	 */
	

	/*******************     Graphical control state 	 ******************/
	
	private PlayerSelectorForm<P> form;
	
	private JComboBox<P> comboBox;
	private JLabel figureLabel, headLabel;
	private JButton resetButton;
	
	private void initFormMembersShortcuts() {
		comboBox = form.getComboBox();
		resetButton = form.getResetButton();
		headLabel = form.getHeadLabel();
		figureLabel = form.getFigureLabel();
	}

	// WB-compatible constructor
	public StarterPlayerSelector() {
		form = new PlayerSelectorForm<P>();
		wireUpForm();
	}

	// rescaling-augmented constructor available to clients
	public StarterPlayerSelector(Dimension availableWindow) throws IOException {
		form = new PlayerSelectorForm<P>(availableWindow);
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
			
			if (comboBox.isPopupVisible() && 
					comboBox.getSelectedIndex() > -1) {
				
				// propagates user selections to subclasses
				onUserSelectionSet();
				
				// notifies user selection to listeners
				listeners.forEach(l -> l.selectionMadeOn(this));
			}
		});

		resetButton.addActionListener(e -> {
			// button -> combo interaction
			comboBox.setSelectedIndex(-1);
			resetButton.setEnabled(false);
			
			// propagates selection clearance to subclasses
			onSelectionCleared();
			
			// notifies selection clearance to to listeners
			listeners.forEach(l -> l.selectionClearedOn(this));
		});
	}
	
	/***********     selection event notification to listener clients 	 ***********/
	
	public interface StarterPlayerSelectorListener<Q extends Player > {
		void selectionMadeOn(StarterPlayerSelector<Q> selector);
		void selectionClearedOn(StarterPlayerSelector<Q> selector);
	}
	
	private List<StarterPlayerSelectorListener<P>> listeners = new ArrayList<>();
	
	public void attachListener(StarterPlayerSelectorListener<P> listener) {
		listeners.add(listener);
	}
	
	/**************     OrderedOptionDealer internal bookkeeping 	 **************/

	private OptionDealerGroupDriver<StarterPlayerSelector<P>, P> driver;
	private List<P> options;          // the original option pool, ordered
	private List<Integer> mask;       // contains the linear indices in this.options of elements in the combo's model
	private Integer currentSelection; // contains the linear index in this.options of the combo's current selection	
	private static final int NO_SELECTION = -1;

	@Override
	public void attachDriver(OptionDealerGroupDriver<StarterPlayerSelector<P>, P> driver) {
		this.driver = driver;		
	}

	@Override
	public void attachOptions(List<P> options) {
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
		DefaultComboBoxModel<P> model = (DefaultComboBoxModel<P>) comboBox.getModel();
		model.removeElementAt(pos);
	}

	@Override
	public void restoreOption(int index) {
		int insertionIndex = IntStream
				.range(0, mask.size())
				.filter(k -> mask.get(k) >= index)
				.findFirst().orElse(mask.size());
		mask.add(insertionIndex, index);
		DefaultComboBoxModel<P> model = (DefaultComboBoxModel<P>) comboBox.getModel();
		model.insertElementAt(options.get(index), insertionIndex);
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
	
	// TODO consider opening up to combo.set.. interactions (can only come from this class)
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
	
	/***********     Programmatic selection-setting API for Clients     **********/
	
	public void select(Optional<P> player) {
		player.ifPresentOrElse(
				p -> {
			int playerInd = mask.indexOf(options.indexOf(p));
			comboBox.setSelectedIndex(playerInd);
			currentSelection = playerInd;
			driver.selectionMadeOn(this, playerInd);
			listeners.forEach(l -> l.selectionMadeOn(this));
		}, 
				() -> {
			comboBox.setSelectedIndex(-1); // might engage the driver
			listeners.forEach(l -> l.selectionClearedOn(this));
		});
	}
	
	/*********************     Dedicated Subclasses APIs    *********************/

	// 1) hooks for engaging on selection events
	protected void onUserSelectionSet() {}
	protected void onSelectionCleared() {}
	
	// 2) fluent API for enabling/disabling graphical controls
	protected final StarterPlayerSelectorControls controls() {
		return new StarterPlayerSelectorControls();
	}
	
	protected final class StarterPlayerSelectorControls {
		void setEnabled(boolean bool) {
			comboBox.setEnabled(bool);
			figureLabel.setEnabled(bool);
			headLabel.setEnabled(bool);
		}
	}	
	
	// 3) local Selection operators	
	
	/*
	 * these fluent operators allow subclasses to access their local 
	 * selection behavior without being exposed to any internal details
	 * of StarterPlayerSelector. Benefits of this approach include:
	 * 
	 * for StarterPlayerSelector: 
	 * 	- it remains the sole originator of programmatic combo interactions
	 * 	- these operators are implemented with awareness of OptionDealerGroupDriver
	 * 
	 * for subclasses:
	 *  - these operators do not risk leaking to any drivers inside subclasses
	 *  - subclasses can only intervene via the event-handling engagement hooks
	 */	
	
	protected final UnaryLocalSelectionOperator<P> locally() {
		return new UnaryLocalSelectionOperator<P>(this);
	}
	
	protected final static class UnaryLocalSelectionOperator<Y extends Player> {

		private final StarterPlayerSelector<Y> source;

		private UnaryLocalSelectionOperator(StarterPlayerSelector<Y> source) {
			this.source = source;
		}

		/**
		 * makes the receiver {@link StarterPlayerSelector} clear its selection
		 * and drop that option, without having other dealers restore it
		 */
		protected void dropSelection() {
			if (source.currentSelection != NO_SELECTION) {
				source.retireOption(source.currentSelection);
				source.currentSelection = NO_SELECTION;		
				source.comboBox.setSelectedIndex(-1);  // here, it does not broadcast
			}
		}

		protected BinaryLocalSelectionOperator<Y> takeOverSelectionFrom(StarterPlayerSelector<Y> other) {
			return new BinaryLocalSelectionOperator<Y>(source, other);
		}
	}
	
	protected final static class BinaryLocalSelectionOperator<Y extends Player> {

		private final StarterPlayerSelector<Y> other;
		private final int indSource, indOther;

		private BinaryLocalSelectionOperator(StarterPlayerSelector<Y> source, StarterPlayerSelector<Y> other) {
			this.other = other;
			indOther = other.currentSelection;
			indSource = source.currentSelection;

			if (indOther != NO_SELECTION) {
				source.restoreOption(indOther);
				source.comboBox.setSelectedIndex(source.mask.indexOf(indOther));
				source.currentSelection = indOther;
			}
			
			if (indSource != NO_SELECTION)
				source.retireOption(indSource);
		}

		/**
		 * makes the receiver {@link StarterPlayerSelector} take over the (possibly 
		 * non-existant) selection of the {@code other} selector. If the receiver had 
		 * a previous selection, it drops that option without having other dealers restore it
		 */
		protected void droppingYours() {}
		
		/**
		 * makes the receiver and {@code other} {@link StarterPlayerSelector}s effectively
		 * exchange their (possibly non-existant) selections: after this operation,
		 * each dealer looks like the other did before the operation
		 */
		protected void pushingYoursToThem() {
			if (indSource != NO_SELECTION) {
				other.restoreOption(indSource);
				other.comboBox.setSelectedIndex(other.mask.indexOf(indSource));
				other.currentSelection = indSource;
				other.retireOption(indOther);
			}
		}
	}	
}
