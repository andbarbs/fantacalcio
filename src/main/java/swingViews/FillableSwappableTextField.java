package swingViews;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

import swingViews.FillableSwappablePebbleSequence.ToggleSelectable;
import swingViews.FillableSwappableSequenceDriver.FillableSwappableClient;

@SuppressWarnings("serial")
public class FillableSwappableTextField extends JTextField implements ToggleSelectable, FillableSwappableClient<FillableSwappableTextField>  {

	private FillableSwappableSequenceDriver<FillableSwappableTextField> driver;

	@Override
	public void attachDriver(FillableSwappableSequenceDriver<FillableSwappableTextField> driver) {
		this.driver = driver;
	}

	public FillableSwappableTextField(int columns) {
	    super(columns);
	    addKeyListener(new KeyAdapter() {
	      @Override
	      public void keyReleased(KeyEvent e) {
	        // only on user keys
	        if (getText().isEmpty()) {
	          driver.contentRemoved(FillableSwappableTextField.this);
	        } else {
	          driver.contentAdded(FillableSwappableTextField.this);
	        }
	      }
	    });
	  }

	@Override
	public void acquireContentFrom(FillableSwappableTextField other) {
		setText(other.getText());
	}

	@Override
	public void discardContent() {
		setText("");
	}

	@Override
	public void enableFilling() {
		setEditable(true);
		setBackground(Color.CYAN);
	}

	@Override
	public void disableFilling() {
		setEditable(false);
		setBackground(Color.DARK_GRAY);
	}

	@Override
	public void highlight() {
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	@Override
	public void dehighlight() {
		setBorder(null);		
	}

	@Override
	public void swapContentWith(FillableSwappableTextField other) {
		String temp = getText();
		setText(other.getText());
		other.setText(temp);
	}

	@Override
	public void toggleSelect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toggleDeselect() {
		// TODO Auto-generated method stub
		
	}

}
