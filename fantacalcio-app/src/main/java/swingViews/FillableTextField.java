package swingViews;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import swingViews.RightwardFillableSequenceDriver.RightwardFillable;

@SuppressWarnings("serial")
public class FillableTextField extends JTextField implements RightwardFillable<FillableTextField> {

	private RightwardFillableSequenceDriver<FillableTextField> driver;

	@Override
	public void attachDriver(RightwardFillableSequenceDriver<FillableTextField> driver) {
		this.driver = driver;
	}

	public FillableTextField(int columns) {
	    super(columns);
	    addKeyListener(new KeyAdapter() {
	      @Override
	      public void keyReleased(KeyEvent e) {
	        // only on user keys
	        if (getText().isEmpty()) {
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

}
