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
	 *      - this means a reentrance-blocking flag is acceptable
	 * 
	 * TODO consider pulling up observer logic into a superclass,
	 * together with any logic that doesn't directly depend on internal bookkeeping
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
	
	/**
	 * at the moment, these methods only mutate the mask and combo model to
	 * insert or remove an option as mandated by the driver.
	 * They never leak back into the driver, either because
	 * 		- they cause no events on the combo to be fired
	 * 		- they fire an event which is blocked by the listener
	 */

	@Override
	public void retireOption(int index) {
		int pos = mask.indexOf(index);
		mask.remove(pos);
		DefaultComboBoxModel<P> model = (DefaultComboBoxModel<P>) comboBox.getModel();
		model.removeElementAt(pos);    // fires an event under conditions 3) and 4)
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
			System.out.println("about to call driver.selectionMadeOn");
			driver.selectionMadeOn(this, currentSelection);
		}

		// this CBox's selection has just been cleared
		else if (selectedIndex == -1 && 
				currentSelection != null && // false before option attachment
				currentSelection != NO_SELECTION) {
			System.out.println("about to call driver.selectionClearedOn");
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
	
	protected Optional<P> getSelectedOption() {
		return Optional.ofNullable(
				currentSelection != NO_SELECTION ? options.get(currentSelection) : null);
	}
	
	protected void silentlySelect(Optional<P> option) {
		option.ifPresentOrElse(o -> {
			int pos = options.indexOf(o);
			if (pos == -1)
				throw new IllegalArgumentException("option must belong to group option pool");
			if (!mask.contains(pos))
				throw new IllegalArgumentException("option for selecting is not present");
			currentSelection = pos;
			comboBox.setSelectedIndex(pos);
		}, () -> {
			currentSelection = NO_SELECTION;
			comboBox.setSelectedIndex(-1);
		});
	}
	
	protected void silentlyDrop(Optional<P> option) {
		option.ifPresent(o -> {
			int pos = options.indexOf(o);
			if (pos == -1)
				throw new IllegalArgumentException("option must belong to group option pool");
			if (!mask.contains(pos))
				throw new IllegalArgumentException("option for dropping is already missing");
			if (currentSelection == pos) {
				currentSelection = NO_SELECTION;		
				comboBox.setSelectedIndex(-1);  // in this order, no driver feedback
			}
			retireOption(pos);	// no ghost selection nor driver feedback
		});
	}
	
	protected void silentlyAdd(Optional<P> option) {
		option.ifPresent(o -> {
			int pos = options.indexOf(o);
			if (pos == -1)
				throw new IllegalArgumentException("option must belong to group option pool");
			if (mask.contains(pos))
				throw new IllegalArgumentException("option for adding is already present");
			restoreOption(pos);
		});
	}
	
}
