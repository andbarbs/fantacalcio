package swingViews;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import swingViews.OptionDealerGroupDriver.OrderedOptionDealer;

public abstract class OrderedDealerPresenter<T> 
				implements OrderedOptionDealer<OrderedDealerPresenter<T>, T> {	

	// 1. OrderedOptionDealer: bookkeeping & mandated functions
	
	protected OptionDealerGroupDriver<OrderedDealerPresenter<T>, T> groupDriver;
	
	@Override
	public void attachDriver(OptionDealerGroupDriver<OrderedDealerPresenter<T>, T> driver) {
		this.groupDriver = driver;
	}
	
	List<T> options;            // the original option pool, ordered
	List<Integer> mask;         // contains the linear indices in this.options of options in the View's list
	Integer currentSelection;   // contains the linear index in this.options of the View's current selection	
	static final int NO_SELECTION = -1;

	@Override
	public void attachOptions(List<T> options) {
		this.options = options;
		this.mask = new ArrayList<Integer>(
				IntStream.range(0, options.size()).boxed().collect(Collectors.toList()));
		this.currentSelection = NO_SELECTION;
		this.view.initOptions(options);
	}
	
	/**
	 * @implNote does not feed back into {@code OptionDealerGroupDriver}
	 * as long as the {@code OrderedDealerView} collaborator 
	 * honors requirements on event-feedback avoidance
	 * @see OrderedDealerView#removeOptionAt(int)
	 */
	@Override
	public void retireOption(int absoluteIndex) {
		int pos = mask.indexOf(absoluteIndex);
		mask.remove(pos);
		view.removeOptionAt(pos);
	}

	/**
	 * @implNote does not feed back into {@code OptionDealerGroupDriver}
	 * as long as the {@code OrderedDealerView} collaborator 
	 * honors requirements on event-feedback avoidance
	 * @see OrderedDealerView#insertOptionAt(Object, int)
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
	 * {@link OrderedDealerPresenter} according to the <b>MVP pattern</b>.
	 * 
	 * <p> Functionally, a {@code OrderedDealerView} is supposed to 
	 * <ul> 
	 * 		<li>display an ordered list of options
	 * 		<li>allow at most one option to be selected at any given time 
	 * 		<li>permit insertion/removal of options from its list
	 * </ul>
	 * 
	 * In addition to methods mandated by this interface, an implementor 
	 * should also honor the MVP pattern by
	 * <ul>
	 * 		<li>composing one instance of {@code OrderedDealerPresenter}
	 * 		<li>notifying its {@code OrderedDealerPresenter} whenever
	 * 			<ol>
	 * 				<li>an option in the View's list is selected
	 * 				<li>the previous selection of an option is cleared
	 * 			</ol>
	 * 
	 * 			<h1>Event-feedback avoidance</h1>
	 * 			Notifications to the {@code OrderedDealerPresenter} should
	 * 			<i>not</i> take place for mutations induced by the 
	 * 			{@code OrderedDealerPresenter} itself: 
	 * 			see notes to individual members of this interface
	 * </ul>
	 * @param <T> the type for options in the View's option list
	 */	
	public interface OrderedDealerView<T> {
		
		/**
		 * requests the {@link OrderedDealerView} to initialize its option list.
		 * @param options the initial option list
		 */
		void initOptions(List<T> options);
		
		/**
		 * requests the {@code OrderedDealerView} to remove an option 
		 * from its current option list.
		 * @param removalIndex the position of the option to be removed 
		 * 		relative to the {@code OrderedDealerView}'s current option list 
		 * @implSpec option removal <i>should not be notified back</i> to 
		 * 		the {@code OrderedDealerView}'s {@code OrderedDealerPresenter}
		 */
		void removeOptionAt(int removalIndex);
		
		/**
		 * requests the {@code OrderedDealerView} to add an option
		 * to its current option list.
		 * @param option the option to be inserted
		 * @param insertionIndex the position in the {@code OrderedDealerView}'s 
		 * 		current option list where the specified option should be inserted
		 * @implSpec option insertion <i>should not be notified back</i> to 
		 * 		the {@code OrderedDealerView}'s {@code OrderedDealerPresenter}
		 */
		void insertOptionAt(T option, int insertionIndex);
		
		/**
		 * requests the {@code OrderedDealerView} to select an option
		 * from its current option list.
		 * @param selectionIndex the position of the option to be selected 
		 * 		relative to the {@code OrderedDealerView}'s current option list 
		 * @implSpec option selection <i>should not be notified back</i> to 
		 * 		the {@code OrderedDealerView}'s {@code OrderedDealerPresenter}
		 */
		void selectOptionAt(int selectionIndex);		
	}

	private final OrderedDealerPresenter.OrderedDealerView<T> view;
	
	public OrderedDealerPresenter(OrderedDealerView<T> view) {
		this.view = view;
	}
	
	/**
	 * allows a {@code OrderedDealerView} to notify its 
	 * {@code OrderedDealerPresenter} that an option has been selected
	 * from its current option list.
	 * 
	 * <p><h1>Event-feedback avoidance</h1>
	 * This notifications should <i>not</i> take place for mutations 
	 * induced on the View by the {@code OrderedDealerPresenter} itself: 
	 * see notes to {@link OrderedDealerView}
	 * 
	 * <p><h1>Notification Redundancy</h1>
	 * {@code OrderedDealerView} implementors should be aware that
	 * {@code OrderedDealerPresenter} has no mechanism for detecting a
	 * redundant {@code #selectedOption(int)} notification.<p>		
	 * 
	 * @param position the position of the option having been selected 
	 * 		relative to the {@code OrderedDealerView}'s current option list
	 */
	public void selectedOption(int position) {
		
		// handles a previously existing selection
		if (currentSelection != NO_SELECTION)
			selectionClearedFor(currentSelection);
		
		// updates bookkeeping
		currentSelection = mask.get(position);
		
		// notifies selection set to driver
		selectionSetFor(currentSelection);
	}
	
	/**
	 * allows subclasses to intervene upon a <i>selection-set</i> notification by the View.
	 * @param absoluteIndex the absolute index of the option having been selected
	 * @implNote calls to {@link #getSelection()} will reflect the selection being notified
	 */	
	protected abstract void selectionSetFor(int absoluteIndex);

	/**
	 * allows a {@code OrderedDealerView} to notify its 
	 * {@code OrderedDealerPresenter} that the previous selection 
	 * on the View has been cleared.
	 * 
	 * <p><h1>Event-feedback avoidance</h1>
	 * This notifications should <i>not</i> take place for mutations 
	 * induced on the View by the {@code OrderedDealerPresenter} itself: 
	 * see notes to {@link OrderedDealerView}
	 * 
	 * <p><h1>Notification Redundancy</h1>
	 * {@code OrderedDealerView} implementors should be aware that
	 * {@code OrderedDealerPresenter} has no mechanism for detecting a
	 * redundant {@code #selectionCleared()} notification.
	 */
	public void selectionCleared() {
		if (currentSelection != null && // false before option attachment
				currentSelection != NO_SELECTION) {
			
			//stores currentSelection for subsequent subclass notification
			int clearedSelection = currentSelection;
			
			// updates bookkeeping ensuring getSelection() will reflect clearance
			// TODO document this in the javadoc for subclass hook!!
			currentSelection = NO_SELECTION;

			// notifies selection cleared to driver
			selectionClearedFor(clearedSelection);
		}
	}
	
	/**
	 * allows subclasses to intervene upon a  <i>selection-cleared</i> notification by the View.
	 * @param absoluteIndex the absolute index of the option that was selected prior to clearance
	 * @implNote calls to {@link #getSelection()} will reflect the clearance being notified
	 */
	protected abstract void selectionClearedFor(int absoluteIndex);
	
	
	// 3. Selection querying/setting APIs for clients	

	/**
	 * @return an {@code Optional} containing the option currently selected on
	 * this dealer, or an empty one if the dealer has no selection
	 */
	public Optional<T> getSelection() {
		return Optional.ofNullable(
				currentSelection != NO_SELECTION ? options.get(currentSelection) : null);
	}
	
	/**
	 * @param option an {@code Optional} containing the option to be set on 
	 * this dealer, or an empty one if one wishes to clear the dealer's selection 
	 */
	public void setSelection(Optional<T> option) {
		option.ifPresentOrElse(o -> {
			int absoluteIndex = options.indexOf(o);
			if (absoluteIndex == -1)
				throw new IllegalArgumentException(String.format(
						"OrderedDealerPresenter.setSelection: Illegal Argument\n" +
						"option: %s not found in dealer group option list\n", o));
			int pos = mask.indexOf(absoluteIndex);
			if (pos == -1)
				throw new IllegalArgumentException(String.format(
						"OrderedDealerPresenter.setSelection: Illegal Argument\n" +
						"option: %s not found among this dealer's available options\n", o));
			view.selectOptionAt(pos);
			selectedOption(pos);
		}, () -> {
			view.selectOptionAt(NO_SELECTION);
			selectionCleared();
		});
	}	
}
