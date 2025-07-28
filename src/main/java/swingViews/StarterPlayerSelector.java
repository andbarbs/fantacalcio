package swingViews;

import java.util.ArrayList;
import java.util.List;

import domainModel.Player;

// TODO consider splitting the OrderedOptionDealer interface to allow this class to 
public class StarterPlayerSelector<P extends Player> extends OrderedDealerPresenter<P> {

	public StarterPlayerSelector(OrderedDealerView<P> view) {
		super(view);
	}

	@Override
	protected void selectionSetFor(int absoluteIndex) {
		groupDriver.selectionMadeOn(this, absoluteIndex);
	}

	@Override
	protected void selectionClearedFor(int absoluteIndex) {
		groupDriver.selectionClearedOn(this, absoluteIndex);
	}
	
	// listening API
	
	/**
	 * an interface for clients wishing to be notified of selection
	 * events related to this {@link OrderedDealerPresenter}.
	 */
	public interface StarterPlayerSelectorListener<Q> {
		void selectionMadeOn(OrderedDealerPresenter<Q> selector);
		void selectionClearedOn(OrderedDealerPresenter<Q> selector);
	}
	
	private List<StarterPlayerSelectorListener<P>> listeners = new ArrayList<>();
	
	public void attachListener(StarterPlayerSelectorListener<P> listener) {
		listeners.add(listener);
	}
	
	@Override
	public void selectedOption(int position) {
		super.selectedOption(position);		

		// notifies user selection to listeners
		listeners.forEach(l -> l.selectionMadeOn(this));
	}
	
	@Override
	public void selectionCleared() {
		super.selectionCleared();

		// notifies selection clearance to to listeners
		listeners.forEach(l -> l.selectionClearedOn(this));
	}

}
