package swingViews;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import domainModel.Player;
import swingViews.FillableSwappableSequenceDriver.FillableSwappableGadget;

@SuppressWarnings("serial")
public class FillableSwappableCompetingPlayerSelector<T extends Player> extends CompetingPlayerSelector<T> 
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
		super.clearSelection();
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
	
	
	// implements notifications to the driver by augmenting these protected methods
	// the superclass calls inside listeners
	@Override
	protected void onSelectionSet() {
		super.onSelectionSet();
		// a user selection has been made on the combo
		if (comboBox.isPopupVisible() && 
				comboBox.getSelectedIndex() > -1) { 
			driver.contentAdded(this);
		}
	}
	
	@Override
	protected void clearSelection() {
		super.clearSelection();
		driver.contentRemoved(this);
	}
	
}
