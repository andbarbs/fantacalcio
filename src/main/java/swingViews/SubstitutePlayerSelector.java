package swingViews;

import java.util.Optional;
import java.util.function.Consumer;

import domainModel.Player;
import swingViews.FillableSwappableSequenceDriver.FillableSwappableGadget;

public class SubstitutePlayerSelector<P extends Player> extends OrderedDealerPresenter<P> 
			implements FillableSwappableGadget<SubstitutePlayerSelector<P>> {	
	
	// TODO somewhat ugly: there will exist two references to the view,
	// one in super and one in this 
	private final SubstitutePlayerSelectorView<P> view;

	public interface SubstitutePlayerSelectorView<T> extends OrderedDealerView<T> {
		void highlight();
		void dehighlight();
		void setControlsEnabled(boolean b);
	}	
	
	// constructor mandated by superclass
	public SubstitutePlayerSelector(SubstitutePlayerSelectorView<P> view) {
		super(view);
		this.view = view;
	}

	/**************** FillableSwappableGadget ***************/
	
	private FillableSwappableSequenceDriver<SubstitutePlayerSelector<P>> sequenceDriver;
	
	private boolean allowGroupDriverFeedback = true;
	
	private void executeWithoutDriverFeedback(Consumer<SubstitutePlayerSelector<P>> action) {
		allowGroupDriverFeedback = false;
		action.accept(this);
		allowGroupDriverFeedback = true;
	}
	
	// 2) mandated Presenter hooks

	@Override
	protected void selectionSetFor(int absoluteIndex) {
		// TODO Auto-generated method stub
		if (allowGroupDriverFeedback) {
			System.out.println("about to call driver.selectionMadeOn");
			groupDriver.selectionMadeOn(this, absoluteIndex);
			sequenceDriver.contentAdded(this);
		}		
	}

	@Override
	protected void selectionClearedFor(int absoluteIndex) {
		// TODO Auto-generated method stub
		if (allowGroupDriverFeedback) {
			System.out.println("about to call driver.selectionClearedOn");
			groupDriver.selectionClearedOn(this, absoluteIndex);
			sequenceDriver.contentRemoved(this);
		}
	}
	
	// 2) mandated FillableSwappableGadget methods
	
	@Override
	public void attachDriver(FillableSwappableSequenceDriver<SubstitutePlayerSelector<P>> driver) {
		this.sequenceDriver = driver;		
	}
	
	/**
	 * causes the {@code SubstitutePlayerSelector} to clear its selection
	 * and retire the corresponding option <b>without</b> informing the
	 * {@code OptionDealerGroupDriver} group driver.
	 * @apiNote this is a local operation
	 */
	@Override
	public void discardContent() {
		this.silentlyDrop(this.getSelection());
	}
	
	// TODO inserire controlli su appartenenza di other a: stessa sequence, stesso gruppo
	@Override
	public void acquireContentFrom(SubstitutePlayerSelector<P> other) {
		Optional<P> selection = this.getSelection();
		Optional<P> otherSelection = other.getSelection();
    	this.silentlyAdd(otherSelection);
    	this.silentlySelect(otherSelection);
		
    	// on the cleared selector it drops nothing, 
    	// on others it ensures correct option propagation across selectors
    	this.silentlyDrop(selection);  
	}

	// TODO inserire controlli su appartenenza di other a: stessa sequence, stesso gruppo
	@Override
	public void swapContentWith(SubstitutePlayerSelector<P> other) {
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

	// TODO remove these or make them private, we don't want to test them!!
	// 3) local Selection operators	
	
	void silentlySelect(Optional<P> option) {
		
		option.ifPresent(o -> {
			int pos = options.indexOf(o);
			if (pos == -1)
				throw new IllegalArgumentException("option must belong to group option pool");
			if (!mask.contains(pos))
				throw new IllegalArgumentException("option for selecting is not present");
		});
		executeWithoutDriverFeedback(selector -> selector.setSelection(option));
	}
	
	void silentlyDrop(Optional<P> option) {
		option.ifPresent(o -> {
			int pos = options.indexOf(o);
			if (pos == -1)
				throw new IllegalArgumentException("option must belong to group option pool");
			if (!mask.contains(pos))
				throw new IllegalArgumentException("option for dropping is already missing");
			getSelection().ifPresent(sel -> {
				if (sel.equals(o)) {
					executeWithoutDriverFeedback(selector -> {
						selector.setSelection(Optional.empty());
					});
				}
			});
			retireOption(pos);
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
