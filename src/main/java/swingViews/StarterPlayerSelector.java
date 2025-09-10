package swingViews;

import java.util.Optional;

import domainModel.Player;

/**
 * implements an MVP Presenter for a gadget capable of being part of a
 * {@linkplain CompetitiveOptionDealingGroup <i>competitive dealing group</i>}
 * having as options instances of {@linkplain Player}, or one if its sub-types.
 * 
 * <h1>Listener notification policy</h1> 
 * Once a {@code StarterPlayerSelector}
 * instance is made to participate in a
 * {@linkplain CompetitiveOptionDealingGroup competitive dealing group}, a
 * {@link SelectorListener} attached to it will be notified of
 * <ul>
 * 	<li>a <i>selection-made</i> event whenever an option on the <i>previously
 * 	empty</i> {@code Selector} is selected
 * 	<li>a <i>selection-cleared</i> event whenever the selection on the
 * 	{@code Selector} is cleared
 * </ul>
 * 
 * @param <P> the type for options in the {@code StarterPlayerSelector}
 * @see {@linkplain CompetitiveOptionDealingGroup} for the semantics of
 *      competitive dealing and how to initialize it
 */
public class StarterPlayerSelector<P extends Player> extends OrderedDealerPresenter<P> {

	public StarterPlayerSelector(OrderedDealerView<P> view) {
		super(view);
	}
	
	// 2. Presenter response to selection events

	/**
	 * ensures listeners are only notified when Selector <i>enters</i> "selected"
	 * state, but not during selection updates (Selector <i>remains</i> in
	 * "selected" state)
	 */
	@Override
	public void selectedOption(int position) {
		Optional<P> selection = getSelection();
		super.selectedOption(position);
		if (selection.isEmpty())
			listeners().forEach(l -> l.selectionMadeOn(this));
	}
	
	/**
	 * ensures the group driver is notified every time an option needs to be
	 * withdrawn from competitors
	 */
	@Override
	protected void selectionSetFor(int absoluteIndex) {
		groupDriver.selectionMadeOn(this, absoluteIndex);
	}

	/**
	 * ensures listeners are only notified when Selector <i>enters</i>
	 * "non-selected" state, but not during selection updates (Selector remains in
	 * <i>"selected"</i> state)
	 */
	@Override
	public void selectionCleared() {
		super.selectionCleared();
		listeners().forEach(l -> l.selectionClearedOn(this));
	}

	/**
	 * ensures the group driver is notified every time an option needs to be added
	 * back to competitors
	 */
	@Override
	protected void selectionClearedFor(int absoluteIndex) {
		groupDriver.selectionClearedOn(this, absoluteIndex);
	}	
	
//	// NEW OBSERVABLE INFRASTRUCTURE
//	
//	private List<SelectorListener> newListeners = new ArrayList<>();
//
//	@Override
//	public void add(SelectorListener listener) {
//		newListeners.add(listener);
//	}

}
