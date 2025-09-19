package swingViews;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import swingViews.CompetitiveOptionDealingGroup.CompetitiveOrderedDealer;
import swingViews.LineUpChooser.StarterSelectorDelegate;

/**
 * partially implements members of {@linkplain StarterSelectorDelegate} as a
 * Controller in a bidirectional collaboration scheme inspired by the <b>MVP
 * pattern</b>.
 * 
 * <p>
 * Subclasses are free to implement the behavioral responsibilities of
 * {@linkplain CompetitiveOrderedDealer} and {@linkplain Selector}, namely:
 * <ol>
 * <li>their policy for notifying the {@linkplain CompetitiveOptionDealingGroup
 * group driver}, through the {@link #groupDriver} field and hooks
 * {@link #selectionSetFor(int)} and {@link #selectionClearedFor(int)}
 * <li>their policy for notifying {@linkplain SelectorListener listener}s,
 * through the {@link #listeners()} getter
 * </ol>
 * 
 * @implNote implementations for {@linkplain CompetitiveOrderedDealer} members
 *           do not leak back into the driver, as long as the
 *           {@linkplain OrderedDealerView} collaborator does <i>not</i> notify
 *           back its {@code OrderedDealerPresenter} for mutations induced by
 *           the {@code OrderedDealerPresenter} itself
 * @param <T> the type for options in this {@code Selector} / {@code Dealer}
 */
public abstract class OrderedDealerPresenter<T> 
				implements StarterSelectorDelegate<T>, SelectorController {	

	// 1. OrderedOptionDealer: bookkeeping & mandated functions
	
	protected CompetitiveOptionDealingGroup<StarterSelectorDelegate<T>, T> groupDriver;
	
	@Override
	public final void attachDriver(CompetitiveOptionDealingGroup<StarterSelectorDelegate<T>, T> driver) {
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
	public final void attachOptions(List<T> options) {
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
	public final void retireOption(int absoluteIndex) {
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
	public final void restoreOption(int absoluteIndex) {
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
	 * <p><h1>Notification Redundancy</h1>
	 * {@code OrderedDealerView} implementors should be aware that
	 * {@code OrderedDealerPresenter} has no mechanism for detecting a
	 * redundant {@code selectedOption(int)} notification.
	 */
	@Override
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
	 * <p><h1>Notification Redundancy</h1>
	 * {@code OrderedDealerView} implementors should be aware that
	 * {@code OrderedDealerPresenter} has no mechanism for detecting a
	 * redundant {@code selectionCleared()} notification.
	 */
	@Override
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
	
	
	// 3. a public Selector

	@Override
	public final Optional<T> getSelection() {
		return Optional.ofNullable(
				currentSelection != NO_SELECTION ? options.get(currentSelection) : null);
	}
	
	/**
	 * @throws IllegalArgumentException if the option provided is not
	 *                                  <ul>
	 *                                  <li>among the options available in this
	 *                                  {@code dealer}'s group
	 *                                  <li>among those available on this
	 *                                  {@code dealer}
	 *                                  </ul>
	 */
	@Override
	public final void setSelection(Optional<T> option) {
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

	private Collection<SelectorListener<T>> listeners = new ArrayList<>();

	@Override
	public final void attachListener(SelectorListener<T> listener) {
		this.listeners.add(listener);		
	}
	
	@Override
	public final void removeListener(SelectorListener<T> listener) {
		this.listeners.remove(listener);
	}
	
	protected Collection<SelectorListener<T>> listeners() {
		return listeners;
	}
	
	
	// Selection setting API for subclasses
	
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
	protected final void selectOption(int absoluteIndex) {
		if (!(absoluteIndex == NO_SELECTION || mask.contains(absoluteIndex))) 
			throw new IllegalArgumentException(String.format(
					"OrderedDealerPresenter.selectOption: Illegal Argument\n" +
							"option: %d not found among this dealer's available options: %s\n", 
							absoluteIndex, mask));
		
		currentSelection = absoluteIndex;
		view.selectOptionAt(
				currentSelection != NO_SELECTION ? mask.indexOf(currentSelection) : -1);		
	}
}
