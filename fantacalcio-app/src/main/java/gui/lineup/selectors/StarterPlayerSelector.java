package gui.lineup.selectors;

import java.util.Optional;

/**
 * implements {@linkplain StarterSelectorDelegate} as a {@code Controller} in a
 * bidirectional collaboration scheme inspired by the <b>MVP pattern</b>.
 * 
 * <h1>Listener notification policy</h1> Once a {@code StarterPlayerSelector}
 * instance is made to participate in a
 * {@linkplain CompetitiveOptionDealingGroup competitive dealing group}, a
 * {@link SelectorListener} attached to it will be notified of
 * <ul>
 * <li>a <i>selection-made</i> event whenever an option on the <i>previously
 * empty</i> {@code Selector} is selected
 * <li>a <i>selection-cleared</i> event whenever the selection on the
 * {@code Selector} is cleared
 * </ul>
 * 
 * @param <T> the type for options in this {@code Selector}
 */
public final class StarterPlayerSelector<T> extends OrderedDealerPresenter<T> {

	public StarterPlayerSelector(SelectorWidget<T> view) {
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
		Optional<T> selection = getSelection();
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
}
