package swingViews;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import domainModel.Player;
import swingViews.FillableSwappableSequenceDriver.FillableSwappableGadget;

@SuppressWarnings("serial")
public class FillableSwappableCompetingPlayerSelector<T extends Player> extends StarterPlayerSelector<T> 
			implements FillableSwappableGadget<FillableSwappableCompetingPlayerSelector<T>> {	
	
	// constructors inherited from superclass
	public FillableSwappableCompetingPlayerSelector() {
		super();
	}

	public FillableSwappableCompetingPlayerSelector(Dimension availableWindow) throws IOException {
		super(availableWindow);
	}	
	
	@Override
	public void acquireContentFrom(FillableSwappableCompetingPlayerSelector<T> other) {
		Object otherContent = other.comboBox.getSelectedItem();
		other.comboBox.setSelectedIndex(-1);
		comboBox.setSelectedItem(otherContent);
	}

	@Override
	public void discardContent() {
		// avoids feeding back to the driver
		super.onSelectionCleared();
	}

	@Override
	public void enableFilling() {
		System.out.println("driver enabled me");
		comboBox.setEnabled(true);
		figureLabel.setEnabled(true);
	}

	@Override
	public void disableFilling() {
		comboBox.setEnabled(false);
		figureLabel.setEnabled(false);
	}

	@Override
	public void highlight() {
		comboBox.setBackground(Color.BLUE);
	}

	@Override
	public void dehighlight() {
		comboBox.setBackground(Color.WHITE);
	}

	@Override
	public void swapContentWith(FillableSwappableCompetingPlayerSelector<T> other) {
		Object tempMine = comboBox.getSelectedItem();
		Object tempOther = other.comboBox.getSelectedItem();
		
		comboBox.setSelectedIndex(-1);
		other.comboBox.setSelectedItem(tempMine);
		comboBox.setSelectedItem(tempOther);
	}
	
	private FillableSwappableSequenceDriver<FillableSwappableCompetingPlayerSelector<T>> driver;

	@Override
	public void attachDriver(FillableSwappableSequenceDriver<FillableSwappableCompetingPlayerSelector<T>> driver) {
		this.driver = driver;		
	}
	
	
	// implements notifications to the driver by augmenting these protected hooks
	@Override
	protected void onUserSelectionSet(int selectedIndex) {
		// a user selection has been made on the selector
		driver.contentAdded(this);
	}
	
	@Override
	protected void onSelectionCleared() {
		super.onSelectionCleared();
		driver.contentRemoved(this);
	}
	
}
