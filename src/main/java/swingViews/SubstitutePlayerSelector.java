package swingViews;

import java.util.Optional;
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
	
	// 2) mandated Presenter hooks

	@Override
	protected void selectionSetFor(int absoluteIndex) {
		// TODO Auto-generated method stub
		System.out.println("about to call driver.selectionMadeOn");
		groupDriver.selectionMadeOn(this, absoluteIndex);
		sequenceDriver.contentAdded(this);
	}

	@Override
	protected void selectionClearedFor(int absoluteIndex) {
		// TODO Auto-generated method stub
		System.out.println("about to call driver.selectionClearedOn");
		groupDriver.selectionClearedOn(this, absoluteIndex);
		sequenceDriver.contentRemoved(this);
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
		int pos = options.indexOf(getSelection().get());
		selectOption(NO_SELECTION);
		retireOption(pos);			
	}
	
	// TODO inserire controlli su appartenenza di other a: stessa sequence, stesso gruppo
	@Override
	public void acquireContentFrom(SubstitutePlayerSelector<P> other) {
		// saves the current selection on this
		Optional<P> thisSelection = this.getSelection();
		
		// makes this acquire other's selection
		P otherSelection = other.getSelection().get();
		this.restoreOption(options.indexOf(otherSelection));
    	this.selectOption(options.indexOf(otherSelection)); 
    	
    	// on the emptied selector it drops nothing, 
    	// on others it ensures correct option propagation across selectors
    	thisSelection.ifPresent(o -> this.retireOption(options.indexOf(o)));
	}

	// TODO inserire controlli su appartenenza di other a: stessa sequence, stesso gruppo
	@Override
	public void swapContentWith(SubstitutePlayerSelector<P> other) {
		P selection = this.getSelection().get();
    	P otherSelection = other.getSelection().get();
    	
    	this.restoreOption(options.indexOf(otherSelection));
    	this.selectOption(options.indexOf(otherSelection));
    	this.retireOption(options.indexOf(selection));
    	
    	other.restoreOption(options.indexOf(selection));
    	other.selectOption(options.indexOf(selection));
    	other.retireOption(options.indexOf(otherSelection));
    	
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
	
}
