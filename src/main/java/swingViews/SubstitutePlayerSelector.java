package swingViews;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.border.LineBorder;

import domainModel.Player;
import swingViews.FillableSwappableSequenceDriver.FillableSwappableGadget;

@SuppressWarnings("serial")
public class SubstitutePlayerSelector<T extends Player> extends StarterPlayerSelector<T> 
			implements FillableSwappableGadget<SubstitutePlayerSelector<T>> {	
	
	// constructors inherited from superclass
	public SubstitutePlayerSelector() {
		super();
	}

	public SubstitutePlayerSelector(Dimension availableWindow) throws IOException {
		super(availableWindow);
	}
	
	private FillableSwappableSequenceDriver<SubstitutePlayerSelector<T>> driver;

	@Override
	public void attachDriver(FillableSwappableSequenceDriver<SubstitutePlayerSelector<T>> driver) {
		this.driver = driver;		
	}
	
	
	// implements notifications to the driver by augmenting these protected hooks
	@Override
	protected void onUserSelectionSet() {
		driver.contentAdded(this);
	}
	
	@Override
	protected void onSelectionCleared() {
		super.onSelectionCleared();
		driver.contentRemoved(this);
	}

	@Override
	public void acquireContentFrom(SubstitutePlayerSelector<T> other) {
		super.acquireSelectionFrom(other);
	}
	
	@Override
	public void discardContent() {
		super.discardContent();
	}

	@Override
	public void swapContentWith(SubstitutePlayerSelector<T> other) {
		super.swapSelectionWith(other);
	}
	
	@Override
	public void highlight() {
		setBorder(new LineBorder(Color.CYAN, 5));
	}
	
	@Override
	public void dehighlight() {
		setBorder(null);
	}

	@Override
	public void enableFilling() {
		super.enableUserInteraction();
	}

	@Override
	public void disableFilling() {
		super.disableUserInteraction();
	}
	
}
