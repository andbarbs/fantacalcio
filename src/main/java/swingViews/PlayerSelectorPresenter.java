package swingViews;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import domainModel.Player;
import swingViews.OptionDealerGroupDriver.OrderedOptionDealer;

public class PlayerSelectorPresenter<P extends Player> 
				implements OrderedOptionDealer<PlayerSelectorPresenter<P>, P> {
	
	// 1. OrderedOptionDealer: bookkeeping & mandated functions

	private OptionDealerGroupDriver<PlayerSelectorPresenter<P>, P> driver;
	
	protected List<P> options;          // the original option pool, ordered
	protected List<Integer> mask;       // contains the linear indices in this.options of elements in the combo's model
	protected Integer currentSelection; // contains the linear index in this.options of the combo's current selection	
	protected static final int NO_SELECTION = -1;

	@Override
	public void attachDriver(OptionDealerGroupDriver<PlayerSelectorPresenter<P>, P> driver) {
		this.driver = driver;		
	}

	@Override
	public void attachOptions(List<P> options) {
		this.options = options;
		this.mask = new ArrayList<Integer>(
				IntStream.range(0, options.size()).boxed().collect(Collectors.toList()));
		this.currentSelection = NO_SELECTION;
		this.view.initOptions(options);
	}
	
	/**
	 * @implNote does not feed back into {@code OptionDealerGroupDriver}
	 * as long as the {@code PlayerSelectorView} collaborator 
	 * honors requirements on event-feedback avoidance
	 * @see PlayerSelectorView#removeOptionAt(int)
	 */
	@Override
	public void retireOption(int absoluteIndex) {
		int pos = mask.indexOf(absoluteIndex);
		mask.remove(pos);
		view.removeOptionAt(pos);
	}

	/**
	 * @implNote does not feed back into {@code OptionDealerGroupDriver}
	 * as long as the {@code PlayerSelectorView} collaborator 
	 * honors requirements on event-feedback avoidance
	 * @see PlayerSelectorView#insertOptionAt(Object, int)
	 */
	@Override
	public void restoreOption(int absoluteIndex) {
		int insertionIndex = IntStream
				.range(0, mask.size())
				.filter(k -> mask.get(k) >= absoluteIndex)
				.findFirst().orElse(mask.size());
		mask.add(insertionIndex, absoluteIndex);
		view.insertOptionAt(options.get(absoluteIndex), insertionIndex);
	}
	
	// 2. MVP Presenter: View interface & notification points
	
	/**
	 * an interface for Views wishing to collaborate with 
	 * {@link PlayerSelectorPresenter} according to the <b>MVP pattern</b>.
	 * 
	 * <p> Functionally, a {@code PlayerSelectorView} is supposed to 
	 * <ul> 
	 * 		<li>display an ordered list of options
	 * 		<li>allow at most one option to be selected at any given time 
	 * 		<li>permit insertion/removal of options from its list
	 * </ul>
	 * 
	 * In addition to methods mandated by this interface, an implementor 
	 * should also honor the MVP pattern by
	 * <ul>
	 * 		<li>composing one instance of {@code PlayerSelectorPresenter}
	 * 		<li>notifying its {@code PlayerSelectorPresenter} whenever
	 * 			<ol>
	 * 				<li>an option in the View's list is selected
	 * 				<li>the previous selection of an option is cleared
	 * 			</ol>
	 * 
	 * 			<h1>Event-feedback avoidance</h1>
	 * 			Notifications to the {@code PlayerSelectorPresenter} should
	 * 			<i>not</i> take place for mutations induced by the 
	 * 			{@code PlayerSelectorPresenter} itself: 
	 * 			see notes to individual members of this interface
	 * </ul>
	 * @param <P> the type for options in the View's option list
	 */	
	public interface PlayerSelectorView<T> {
		
		/**
		 * requests the {@link PlayerSelectorView} to initialize its option list.
		 * @param options the initial option list
		 */
		void initOptions(List<T> options);
		
		/**
		 * requests the {@code PlayerSelectorView} to remove an option 
		 * from its current option list.
		 * @param removalIndex the position of the option to be removed 
		 * 		relative to the {@code PlayerSelectorView}'s current option list 
		 * @implSpec option removal <i>should not be notified back</i> to 
		 * 		the {@code PlayerSelectorView}'s {@code PlayerSelectorPresenter}
		 */
		void removeOptionAt(int removalIndex);
		
		/**
		 * requests the {@code PlayerSelectorView} to add an option
		 * to its current option list.
		 * @param option the option to be inserted
		 * @param insertionIndex the position in the {@code PlayerSelectorView}'s 
		 * 		current option list where the specified option should be inserted
		 * @implSpec option insertion <i>should not be notified back</i> to 
		 * 		the {@code PlayerSelectorView}'s {@code PlayerSelectorPresenter}
		 */
		void insertOptionAt(T option, int insertionIndex);
		
		/**
		 * requests the {@code PlayerSelectorView} to select an option
		 * from its current option list.
		 * @param selectionIndex the position of the option to be selected 
		 * 		relative to the {@code PlayerSelectorView}'s current option list 
		 * @implSpec option selection <i>should not be notified back</i> to 
		 * 		the {@code PlayerSelectorView}'s {@code PlayerSelectorPresenter}
		 */
		void selectOptionAt(int selectionIndex);		
	}

	private final PlayerSelectorPresenter.PlayerSelectorView<P> view;
	
	public PlayerSelectorPresenter(PlayerSelectorView<P> view) {
		this.view = view;
	}
	
	/**
	 * allows a {@code PlayerSelectorView} to notify its 
	 * {@code PlayerSelectorPresenter} that an option has been selected
	 * from its current option list.
	 * 
	 * <p><h1>Event-feedback avoidance</h1>
	 * This notifications should <i>not</i> take place for mutations 
	 * induced on the View by the {@code PlayerSelectorPresenter} itself: 
	 * see notes to {@link PlayerSelectorView}
	 * 
	 * <p><h1>Notification Redundancy</h1>
	 * {@code PlayerSelectorView} implementors should be aware that
	 * {@code PlayerSelectorPresenter} has no mechanism for detecting a
	 * redundant {@code #selectedOption(int)} notification.<p>		
	 * 
	 * @param position the position of the option having been selected 
	 * 		relative to the {@code PlayerSelectorView}'s current option list
	 */
	public void selectedOption(int position) {
		
		// handles a previously existing selection
		if (currentSelection != NO_SELECTION)
			driver.selectionClearedOn(this, currentSelection);
		
		// updates bookkeeping
		currentSelection = mask.get(position);
		
		// notifies selection set to driver
		System.out.println("about to call driver.selectionMadeOn");
		driver.selectionMadeOn(this, currentSelection);

		// notifies user selection to listeners
		listeners.forEach(l -> l.selectionMadeOn(this));
	}
	
	/**
	 * allows a {@code PlayerSelectorView} to notify its 
	 * {@code PlayerSelectorPresenter} that the previous selection 
	 * on the View has been cleared.
	 * 
	 * <p><h1>Event-feedback avoidance</h1>
	 * This notifications should <i>not</i> take place for mutations 
	 * induced on the View by the {@code PlayerSelectorPresenter} itself: 
	 * see notes to {@link PlayerSelectorView}
	 * 
	 * <p><h1>Notification Redundancy</h1>
	 * {@code PlayerSelectorView} implementors should be aware that
	 * {@code PlayerSelectorPresenter} has no mechanism for detecting a
	 * redundant {@code #selectionCleared()} notification.
	 */
	public void selectionCleared() {
		if (currentSelection != null && // false before option attachment
				currentSelection != NO_SELECTION) {

			// notifies selection cleared to driver
			System.out.println("about to call driver.selectionClearedOn");
			driver.selectionClearedOn(this, currentSelection);
			
			// updates bookkeeping
			currentSelection = NO_SELECTION;
			
			// notifies selection clearance to to listeners
			listeners.forEach(l -> l.selectionClearedOn(this));
		}
	}
	
	// 3. Selection querying/setting APIs for clients
	
	/**
	 * an interface for clients wishing to be notified of selection
	 * events related to this {@link PlayerSelectorPresenter}.
	 */
	public interface PlayerSelectorListener<Q extends Player > {
		void selectionMadeOn(PlayerSelectorPresenter<Q> selector);
		void selectionClearedOn(PlayerSelectorPresenter<Q> selector);
	}
	
	private List<PlayerSelectorListener<P>> listeners = new ArrayList<>();
	
	public void attachListener(PlayerSelectorListener<P> listener) {
		listeners.add(listener);
	}
	
	/**
	 * @return an {@code Optional} containing the option currently selected on
	 * this dealer, or an empty one if the dealer has no selection
	 */
	public Optional<P> getSelection() {
		return Optional.ofNullable(
				currentSelection != NO_SELECTION ? options.get(currentSelection) : null);
	}
	
	/**
	 * @param player an {@code Optional} containing the option to be set on 
	 * this dealer, or an empty one if one wishes to clear the dealer's selection 
	 */
	public void setSelection(Optional<P> player) {
		player.ifPresentOrElse(p -> {
			if (options.indexOf(p) == -1)
				throw new IllegalArgumentException(String.format(
						"PlayerSelectorPresenter.setSelection: Illegal Argument\n" +
						"option %s not found in dealer group option list\n", player));
			int playerInd = mask.indexOf(options.indexOf(p));
			if (!mask.contains(playerInd))
				throw new IllegalArgumentException(String.format(
						"PlayerSelectorPresenter.setSelection: Illegal Argument\n" +
						"option %s not found among this dealer's available options\n", player));
			view.selectOptionAt(playerInd);
			selectedOption(playerInd);
		}, () -> {
			view.selectOptionAt(NO_SELECTION);
			selectionCleared();
		});
	}	
}
