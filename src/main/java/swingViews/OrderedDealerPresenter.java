package swingViews;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import swingViews.CompetitiveOptionDealingGroup.CompetitiveOrderedDealer;

/**
 * implements the mandated members of a {@linkplain CompetitiveOrderedDealer}
 * as a Presenter in the sense of the <b>MVP pattern</b>.
 * 
 * <p>Subclasses are free to implement the behavioral responsibilities of 
 * {@linkplain CompetitiveOrderedDealer}, i.e. notifying the driver, through the 
 * {@linkplain #groupDriver} field and hooks {@linkplain #selectionSetFor(int)} 
 * and {@linkplain #selectionClearedFor(int)}, which complete Presenter 
 * response to View notifications.
 * 
 * @implNote {@linkplain CompetitiveOrderedDealer} members do not leak back into
 * 		the driver, as long as the {@linkplain OrderedDealerView}
 * 		collaborator does <i>not</i> notify back the {@code OrderedDealerPresenter} 
 * 		for mutations induced by the {@code OrderedDealerPresenter} itself
 * @param <T> the type for options in this dealer
 */
public abstract class OrderedDealerPresenter<T> 
				implements CompetitiveOrderedDealer<OrderedDealerPresenter<T>, T> {	

	// 1. OrderedOptionDealer: bookkeeping & mandated functions
	
	protected CompetitiveOptionDealingGroup<OrderedDealerPresenter<T>, T> groupDriver;
	
	@Override
	public void attachDriver(CompetitiveOptionDealingGroup<OrderedDealerPresenter<T>, T> driver) {
		this.groupDriver = driver;
	}
	
	/*
	 * bookkeeping variables are package-private to aid in the set-up phase of unit tests
	 */
	
	List<T> options;            	// the original option pool, ordered
	List<Integer> mask;         	// contains the indices in this.options of options in the View's list
	Integer currentSelection;   	// contains the index in this.options of the View's current selection	
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
	 * @param absoluteIndex 
	 * the index in {@code this.options} of the option to be retired
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
	 * @param absoluteIndex 
	 * the index in {@code this.options} of the option to be restored
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
	 * implements {@code OrderedDealerPresenter}'s response to a 
	 * <i>selection-set</i> event.
	 * 
	 * <p>As a notification point for the View, allows the {@code OrderedDealerView} 
	 * collaborator to notify its {@code OrderedDealerPresenter} that an option 
	 * has been selected from the View's current option list.
	 * 
	 * <p><h1>Event-feedback avoidance</h1>
	 * This notifications should <i>not</i> take place for mutations 
	 * induced on the View by the {@code OrderedDealerPresenter} itself: 
	 * see notes to {@link OrderedDealerView}
	 * 
	 * <p><h1>Notification Redundancy</h1>
	 * {@code OrderedDealerView} implementors should be aware that
	 * {@code OrderedDealerPresenter} has no mechanism for detecting a
	 * redundant {@code selectedOption(int)} notification.<p>		
	 * 
	 * @param position the position of the option having been selected 
	 * 		relative to the {@code OrderedDealerView}'s current option list
	 */
	public void selectedOption(int position) {

		// handles a previously existing selection
		if (currentSelection != NO_SELECTION)
			// engages subclasses on selection-cleared event
			selectionClearedFor(currentSelection);

		// updates bookkeeping
		currentSelection = mask.get(position);

		// engages subclasses on selection-set event
		selectionSetFor(currentSelection);
	}
	
	/**
	 * allows subclasses to intervene upon a <i>selection-set</i> event on this Presenter.
	 * @param absoluteIndex the index in {@code this.options} of the option having been selected
	 * @implNote {@link #getSelection()} within this hook will acknowledge the
	 * selection-set event
	 */	
	protected abstract void selectionSetFor(int absoluteIndex);

	/**
	 * implements {@code OrderedDealerPresenter}'s response to a 
	 * <i>selection-cleared</i> event.
	 * 
	 * <p>As a notification point for the View, allows the {@code OrderedDealerView}
	 * to notify its {@code OrderedDealerPresenter} that the View's previously existing
	 * selection has been cleared.
	 * 
	 * <p><h1>Event-feedback avoidance</h1>
	 * This notifications should <i>not</i> take place for mutations 
	 * induced on the View by the {@code OrderedDealerPresenter} itself: 
	 * see notes to {@link OrderedDealerView}
	 * 
	 * <p><h1>Notification Redundancy</h1>
	 * {@code OrderedDealerView} implementors should be aware that
	 * {@code OrderedDealerPresenter} has no mechanism for detecting a
	 * redundant {@code selectionCleared()} notification.
	 */
	public void selectionCleared() {
		if (currentSelection != null && // false before option attachment
				currentSelection != NO_SELECTION) {
			
			//stores currentSelection for subsequent hook call
			int clearedSelection = currentSelection;
			
			// updates bookkeeping ensuring getSelection() will reflect clearance
			currentSelection = NO_SELECTION;

			// engages subclasses on selection-cleared event
			selectionClearedFor(clearedSelection);
		}
	}
	
	/**
	 * allows subclasses to intervene upon a  <i>selection-cleared</i> event 
	 * on this Presenter.
	 * @param absoluteIndex the index in {@code this.options} of the option 
	 * that was selected prior to clearance
	 * @implNote {@link #getSelection()} within this hook will acknowledge the
	 * selection-cleared event
	 */
	protected abstract void selectionClearedFor(int absoluteIndex);
	
	
	// 3. a public Selector - Selection querying/setting APIs for clients and subclasses

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
			if (!options.contains(o))
				throw new IllegalArgumentException(String.format(
						"OrderedDealerPresenter.setSelection: Illegal Argument\n" +
						"option: %s not found in dealer group option list\n", o));
			int absoluteIndex = options.indexOf(o);
			if (!mask.contains(absoluteIndex))
				throw new IllegalArgumentException(String.format(
						"OrderedDealerPresenter.setSelection: Illegal Argument\n" +
						"option: %s not found among this dealer's available options\n", o));
			int pos = mask.indexOf(absoluteIndex);
			view.selectOptionAt(pos);
			selectedOption(pos);
		}, () -> {
			view.selectOptionAt(NO_SELECTION);
			selectionCleared();
		});
	}
	
	/**
	 * allows subclasses to set one of the available options as the selection,
	 * with the operation being local to this selector - akin to the semantics
	 * of {@link #retireOption(int)} and {@link #restoreOption(int)}.
	 * @param absoluteIndex 
	 * 		the index in {@code this.options} of the option to be selected, 
	 * 		or {@link #NO_SELECTION} if one wishes to clear the dealer's selection
	 * @throws IllegalArgumentException if absoluteIndex is not {@link #NO_SELECTION}
	 *		nor corresponds to one of the options available on this selector
	 */
	protected void selectOption(int absoluteIndex) {
		if (!(absoluteIndex == NO_SELECTION || mask.contains(absoluteIndex))) 
			throw new IllegalArgumentException(String.format(
					"OrderedDealerPresenter.selectOption: Illegal Argument\n" +
					"option: %d not found among this dealer's available options: %s\n", 
					absoluteIndex, mask));
		
		currentSelection = absoluteIndex;
		view.selectOptionAt(
				currentSelection != NO_SELECTION ? mask.indexOf(currentSelection) : -1);		
	}
	
	/**
	 * an interface for clients wishing to be notified of <i>selection events</i> occurring
	 * on an {@link OrderedDealerPresenter} instance.

	 * @param <Q> the type for options in the observed {@link OrderedDealerPresenter}
	 */
	public interface OrderedDealerPresenterListener<Q> {

		/**
		 * will be called on {@link OrderedDealerPresenterListener}s when a selection
		 * has been made on an observed {@link OrderedDealerPresenter}.
		 * 
		 * @param selector the observed {@link OrderedDealerPresenter} instance which has
		 *                 received a selection
		 */
		void selectionMadeOn(OrderedDealerPresenter<Q> selector);

		/**
		 * will be called on {@link OrderedDealerPresenterListener}s when the selection
		 * on an observed {@link OrderedDealerPresenter} has been cleared.
		 * 
		 * @param selector the observed {@link OrderedDealerPresenter} instance whose
		 *                 selection has been cleared
		 */
		void selectionClearedOn(OrderedDealerPresenter<Q> selector);
	}

	private List<OrderedDealerPresenterListener<T>> listeners = new ArrayList<>();

	public void attachListener(OrderedDealerPresenterListener<T> listener) {
		listeners.add(listener);
	}
	
	protected List<OrderedDealerPresenterListener<T>> listeners() {
		return listeners;
	}
	
}
