package swingViews;

import java.util.Optional;

import domainModel.Player;
import swingViews.FillableSwappableSequenceDriver.FillableSwappableGadget;

public class SubstitutePlayerSelectorPresenter<P extends Player> extends OrderedDealerPresenter<P> 
			implements FillableSwappableGadget<SubstitutePlayerSelectorPresenter<P>> {	
	
	private final SubstitutePlayerSelectorView<P> view;

	public interface SubstitutePlayerSelectorView<T> extends OrderedDealerView<T> {
		void highlight();
		void dehighlight();
		void setControlsEnabled(boolean b);
	}	
	
	// TODO somewhat ugly: there will exist two references to the view,
	// one in super and one in this 
	
	// constructor inherited from superclass
	public SubstitutePlayerSelectorPresenter(SubstitutePlayerSelectorView<P> view) {
		super(view);
		this.view = view;
	}

	/**************** FillableSwappableGadget ***************/
	
	private FillableSwappableSequenceDriver<SubstitutePlayerSelectorPresenter<P>> driver;
	
	// 1) notifications to the driver: implements them by overriding these superclass methods
	@Override
	public void selectedOption(int position) {
		super.selectedOption(position);
		driver.contentAdded(this);
	}
	
	@Override
	public void selectionCleared() {
		super.selectionCleared();
		driver.contentRemoved(this);
	}

	// 2) mandated FillableSwappableGadget methods
	
	@Override
	public void attachDriver(FillableSwappableSequenceDriver<SubstitutePlayerSelectorPresenter<P>> driver) {
		this.driver = driver;		
	}
	
	@Override
	public void acquireContentFrom(SubstitutePlayerSelectorPresenter<P> other) {
		Optional<P> selection = this.getSelection();
		Optional<P> otherSelection = other.getSelection();
    	this.silentlyAdd(otherSelection);
    	this.silentlySelect(otherSelection);
		this.silentlyDrop(selection);
	}
	
	@Override
	public void discardContent() {
		this.silentlyDrop(this.getSelection());
	}

	@Override
	public void swapContentWith(SubstitutePlayerSelectorPresenter<P> other) {
		Optional<P> selection = this.getSelection();
    	Optional<P> otherSelection = other.getSelection();
    	this.silentlyAdd(otherSelection);
    	this.silentlySelect(otherSelection);
    	this.silentlyDrop(selection);
    	other.silentlyAdd(selection);
    	other.silentlySelect(selection);
    	other.silentlyDrop(otherSelection);
	}
	
	@Override
	public void highlight() {
		view.highlight();
	}
	
	@Override
	public void dehighlight() {
		view.dehighlight();
	}

	@Override
	public void enableFilling() {
		view.setControlsEnabled(true);
	}

	@Override
	public void disableFilling() {
		view.setControlsEnabled(false);
	}
	
	/************* package-private local option operators **************/

	
	// 3) local Selection operators	
	
	// TODO consider completely overhauling these operators in favor of
	// opening up bookkeeping to subclasses, given that this class
	// no longer has to worry about being the (sole) originator of
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
	
	void silentlySelect(Optional<P> option) {
		option.ifPresentOrElse(o -> {
			int pos = options.indexOf(o);
			if (pos == -1)
				throw new IllegalArgumentException("option must belong to group option pool");
			if (!mask.contains(pos))
				throw new IllegalArgumentException("option for selecting is not present");
			currentSelection = pos;
			view.selectOptionAt(mask.indexOf(pos));
		}, () -> {
			currentSelection = NO_SELECTION;
			view.selectOptionAt(NO_SELECTION);
		});
	}
	
	void silentlyDrop(Optional<P> option) {
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
	
	void silentlyAdd(Optional<P> option) {
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
