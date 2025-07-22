package swingViews;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import domainModel.Player;
import swingViews.OptionDealerGroupDriver.OrderedOptionDealer;

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

public class PlayerSelectorPresenter<P extends Player> 
				implements OrderedOptionDealer<PlayerSelectorPresenter<P>, P> {
	
	public interface PlayerSelectorView<T> {
		void initOptions(List<T> options);
		void removeOptionAt(int pos);
		void insertOptionAt(T option, int insertionIndex);
		void selectOptionAt(int pos);		
	}

	private final PlayerSelectorPresenter.PlayerSelectorView<P> view;
	
	public PlayerSelectorPresenter(PlayerSelectorView<P> view) {
		this.view = view;
	}

	
	/***********     selection event notification to listener clients 	 ***********/
	
	public interface StarterPlayerSelectorListener<Q extends Player > {
		void selectionMadeOn(PlayerSelectorPresenter<Q> selector);
		void selectionClearedOn(PlayerSelectorPresenter<Q> selector);
	}
	
	private List<StarterPlayerSelectorListener<P>> listeners = new ArrayList<>();
	
	public void attachListener(StarterPlayerSelectorListener<P> listener) {
		listeners.add(listener);
	}
	
	/**************     OrderedOptionDealer internal bookkeeping 	 **************/

	private OptionDealerGroupDriver<PlayerSelectorPresenter<P>, P> driver;
	private List<P> options;          // the original option pool, ordered
	private List<Integer> mask;       // contains the linear indices in this.options of elements in the combo's model
	private Integer currentSelection; // contains the linear index in this.options of the combo's current selection	
	private static final int NO_SELECTION = -1;

	@Override
	public void attachDriver(OptionDealerGroupDriver<PlayerSelectorPresenter<P>, P> driver) {
		this.driver = driver;		
	}

	@Override
	public void attachOptions(List<P> options) {
		this.options = options;
		this.mask = new ArrayList<Integer>(
				IntStream.rangeClosed(0, options.size() - 1).boxed().collect(Collectors.toList()));
		this.currentSelection = NO_SELECTION;
		this.view.initOptions(options);
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
		view.removeOptionAt(pos);
	}

	@Override
	public void restoreOption(int index) {
		int insertionIndex = IntStream
				.range(0, mask.size())
				.filter(k -> mask.get(k) >= index)
				.findFirst().orElse(mask.size());
		mask.add(insertionIndex, index);
		view.insertOptionAt(options.get(index), insertionIndex);
	}
	
	// methods to be called by the View
	
	/**
	 * to avoid driver feedback, Presenter could
	 * 		- document to the View the circumstances upon which
	 * 		  these method ought to be called
	 * 		- use a reentrance-blocking flag 
	 */
	
	
	// a user selection has been set on the View
	public void selectedOption(int position) {
		
		// handles a previously existing selection
		if (currentSelection != -1)
			driver.selectionClearedOn(this, currentSelection);
		
		// updates bookkeeping
		currentSelection = mask.get(position);
		
		// notifies selection set to driver
		System.out.println("about to call driver.selectionMadeOn");
		driver.selectionMadeOn(this, currentSelection);

		// notifies user selection to listeners
		listeners.forEach(l -> l.selectionMadeOn(this));
	}
	
	public void selectionCleared() {
		// the View's selection has just been cleared
		if (currentSelection != null && // false before option attachment
				currentSelection != NO_SELECTION) {

			// updates bookkeeping
			currentSelection = NO_SELECTION;

			// notifies selection cleared to driver
			System.out.println("about to call driver.selectionClearedOn");
			driver.selectionClearedOn(this, currentSelection);
			
			// notifies selection clearance to to listeners
			listeners.forEach(l -> l.selectionClearedOn(this));
		}
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
	 * {@link PlayerSelectorPresenter}, this class is the <i>sole possible 
	 * originator</i> of programmatic interactions with the combo.
	 * Thus, any call to the driver must happen within this method.
	 */
	
	/***********     Programmatic selection-setting API for Clients     **********/
	
	public void select(Optional<P> player) {
		player.ifPresentOrElse(
				p -> {
			int playerInd = mask.indexOf(options.indexOf(p));
			view.selectOptionAt(playerInd);
			currentSelection = playerInd;
			driver.selectionMadeOn(this, playerInd);
			listeners.forEach(l -> l.selectionMadeOn(this));
		}, 
				() -> {
			view.selectOptionAt(NO_SELECTION); // might engage the driver
			listeners.forEach(l -> l.selectionClearedOn(this));
		});
	}
	
	/*********************     Dedicated Subclasses APIs    *********************/

	
	// 3) local Selection operators	
	
	// TODO consider completely overhauling these operators in favor of
	// opening up bookkeeping to subclasses, given that this class
	// no longer has to worry about being the sole originator of
	// combo events
	
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
			view.selectOptionAt(pos);
		}, () -> {
			currentSelection = NO_SELECTION;
			view.selectOptionAt(NO_SELECTION);
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
				view.selectOptionAt(NO_SELECTION);  // in this order, no driver feedback
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
