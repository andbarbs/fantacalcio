package swingViews;

import java.util.ArrayList;
import java.util.List;

import domainModel.Player;

/** 
 * implements an MVP Presenter for a gadget capable of being part of 
 * a <i>group</i> where each gadget allows <i>competitive selection</i> of one instance 
 * of {@linkplain Player}, or one if its sub-types, from a global list.
 * 
 * <p>This functionality is fully realized when a <i>{@code Set}</i> of 
 * {@code StarterPlayerSelector} instances are made to collaborate with 
 * an {@linkplain CompetitiveOptionDealingGroup}, through the facilities defined thereby.</p>
 * 
 * <p>Additionally, {@code StarterPlayerSelector} offers a Subject API 
 * for clients wanting to be notified of <i>selection-set</i> and <i>selection-cleared</i> 
 * events on a {@code StarterPlayerSelector} instance.
 * 
 * @param <P> the type for options in the {@code StarterPlayerSelector}
 * @see {@linkplain CompetitiveOptionDealingGroup} for the semantics of competitive selection
 */
public class StarterPlayerSelector<P extends Player> extends OrderedDealerPresenter<P> 
			implements SwingLineUpChooser.StarterLineUpChooser.Selector<P> {

	public StarterPlayerSelector(OrderedDealerView<P> view) {
		super(view);
	}
	
	// 1. Listener interface and Subject infrastructure 
	
	/**
	 * an interface for clients wishing to be notified of selection
	 * events occurring on a {@link StarterPlayerSelector} instance.
	 */
	public interface StarterPlayerSelectorListener<Q extends Player> {
		
		/**
		 * will be called on {@code StarterPlayerSelectorListener} s when
		 * a selection has been made on an observed {@code StarterPlayerSelector}.
		 * 
		 * <p>In the event of the selection being <i>updated</i>, only this method 
		 * will be invoked - i.e., no {@linkplain #selectionClearedOn(StarterPlayerSelector)}
		 * notification shall be sent
		 * @param selector the observed {@code StarterPlayerSelector} instance which
		 * 		has received a selection
		 */
		void selectionMadeOn(StarterPlayerSelector<Q> selector);
		
		/**
		 * will be called on {@code StarterPlayerSelectorListener} s when
		 * the selection on an observed {@code StarterPlayerSelector} has been cleared.
		 * 
		 * <p>In the event of the selection being <i>updated</i>, only 
		 * {@linkplain #selectionMadeOn(StarterPlayerSelector)} will be invoked - 
		 * i.e., this method shall not be called
		 * @param selector the observed {@code StarterPlayerSelector} instance whose
		 * 		selection has been cleared
		 */
		void selectionClearedOn(StarterPlayerSelector<Q> selector);
	}
	
	private List<StarterPlayerSelectorListener<P>> listeners = new ArrayList<>();
	
	public void attachListener(StarterPlayerSelectorListener<P> listener) {
		listeners.add(listener);
	}
	
	// 2. Presenter response to selection events

	/**
	 * notifies the {@code OptionDealerGroupDriver} and
	 * {@code StarterPlayerSelectorListener} s of every <i>selection-set</i> 
	 * event occurring on this {@code StarterPlayerSelector}.
	 */
	@Override
	protected void selectionSetFor(int absoluteIndex) {
		groupDriver.selectionMadeOn(this, absoluteIndex);
		listeners.forEach(l -> l.selectionMadeOn(this));
	}

	/**
	 * notifies the {@code OptionDealerGroupDriver} of every <i>selection-cleared</i> 
	 * event occurring on this {@code StarterPlayerSelector}.
	 * 
	 * @apiNote {@code StarterPlayerSelectorListener} s are only notified on <i>pure</i> 
	 * selection-cleared events, i.e. not in the context of a selection update
	 */
	@Override
	protected void selectionClearedFor(int absoluteIndex) {
		groupDriver.selectionClearedOn(this, absoluteIndex);
	}
	
	/**
	 * ensures {@code StarterPlayerSelectorListener} s are only notified on <i>pure</i> 
	 * selection-cleared events, i.e. not in the context of a selection update
	 */
	@Override
	public void selectionCleared() {
		super.selectionCleared();

		listeners.forEach(l -> l.selectionClearedOn(this));
	}
	
	// NEW OBSERVABLE INFRASTRUCTURE
	
	private List<SelectorListener> newListeners = new ArrayList<>();

	@Override
	public void add(SelectorListener listener) {
		newListeners.add(listener);
	}

}
