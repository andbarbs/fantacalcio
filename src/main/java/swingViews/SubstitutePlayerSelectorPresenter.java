package swingViews;

import java.util.Optional;

import domainModel.Player;
import swingViews.FillableSwappableSequenceDriver.FillableSwappableGadget;

public class SubstitutePlayerSelectorPresenter<T extends Player> extends PlayerSelectorPresenter<T> 
			implements FillableSwappableGadget<SubstitutePlayerSelectorPresenter<T>> {	
	
	private final SubstitutePlayerSelectorView<T> view;

	public interface SubstitutePlayerSelectorView<T> extends PlayerSelectorView<T> {
		void highlight();
		void dehighlight();
		void setControlsEnabled(boolean b);
	}	
	
	// TODO somewhat ugly: there will exist two references to the view,
	// one in super and one in this 
	
	// constructor inherited from superclass
	public SubstitutePlayerSelectorPresenter(SubstitutePlayerSelectorView<T> view) {
		super(view);
		this.view = view;
	}
	
	private FillableSwappableSequenceDriver<SubstitutePlayerSelectorPresenter<T>> driver;

	@Override
	public void attachDriver(FillableSwappableSequenceDriver<SubstitutePlayerSelectorPresenter<T>> driver) {
		this.driver = driver;		
	}
	
	
	// implements notifications to the driver by overriding these superclass methods
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

	@Override
	public void acquireContentFrom(SubstitutePlayerSelectorPresenter<T> other) {
		Optional<T> selection = this.getSelectedOption();
		Optional<T> otherSelection = other.getSelectedOption();
    	this.silentlyAdd(otherSelection);
    	this.silentlySelect(otherSelection);
		this.silentlyDrop(selection);
	}
	
	@Override
	public void discardContent() {
		this.silentlyDrop(this.getSelectedOption());
	}

	@Override
	public void swapContentWith(SubstitutePlayerSelectorPresenter<T> other) {
		Optional<T> selection = this.getSelectedOption();
    	Optional<T> otherSelection = other.getSelectedOption();
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
	
}
