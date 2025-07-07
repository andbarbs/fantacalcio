package swingViews;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;

import swingViews.RightwardFillableSequenceDriver.RightwardFillable;

@SuppressWarnings("serial")
public class FillableTextField extends JTextField implements RightwardFillable<FillableTextField> {

	private RightwardFillableSequenceDriver<FillableTextField> driver;	

	public void attachDriver(RightwardFillableSequenceDriver<FillableTextField> driver) {
		this.driver = driver;
	}	

	public FillableTextField() {
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (FillableTextField.this.getText().isEmpty()) {
					driver.contentRemoved(FillableTextField.this);
				} else {
					driver.contentAdded(FillableTextField.this);
				}
			}
		});
	}

	@Override
	public void acquireContentFrom(FillableTextField other) {
		setText(other.getText());
	}

	@Override
	public void discardContent() {
		setText("");
	}

	@Override
	public void enableFilling() {
		setEditable(true);
	}

	@Override
	public void disableFilling() {
		setEditable(false);
	}


}
