package swingViews;

import javax.swing.JFrame;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;

public class _433SpringSafe extends SpringSchemePanel {

	private static final long serialVersionUID = 1L;
	
	private static final float slotWidthRatio = 0.217f;
	private static final float slotHeightRatio = 0.193f;
	
	public static Dimension recommendedSlotDimensions(Dimension fieldDimension) {
		return new Dimension(
				(int)Math.floor(fieldDimension.width * slotWidthRatio), 
				(int)Math.floor(fieldDimension.height * slotHeightRatio));
	}
	
	/*
	 * 1) static methods of Spring:
	 * From the docs at https://docs.oracle.com/en/java/javase/24/docs/api/java.desktop/javax/swing/Spring.html:
	 * 
	 * "the static methods create a new Spring instance containing references 
	 * to the method's arguments so that the characteristics of the new spring 
	 * track the potentially changing characteristics of the springs from which it was made"
	 * 
	 * Thus, calls to Spring.scale() cannot be replaced by uses of Spring.constant()
	 * 
	 * 2) placement of slots:
	 * placing slots by their HORIZONTAL_CENTER and VERTICAL_CENTER and NOT constraining their size
	 * allows contents to grow/retract symmetrically around the slot's geometric center.
	 * In particular, we always use HORIZONTAL_CENTER to allow symmetric expansion horizontally, however
	 * we're not as strict with VERTICAL_CENTER as we don't forecast the need for symmetric vertical growth.
	 */
	public _433SpringSafe() {
		// creates springs for the Panel dimensions
		Spring panelWidth = Spring.constant(100);
		Spring panelHeight = Spring.constant(100);// sets panel's own dimensions
		getLayout().getConstraints(this).setWidth(panelWidth);
		getLayout().getConstraints(this).setHeight(panelHeight);

		// installs Goalie slot
		Spring goalieYoffset = Spring.scale(panelHeight, 0.02f);
		getLayout().getConstraints(getGoalie()).setConstraint(SpringLayout.NORTH, goalieYoffset);
		getLayout().putConstraint(SpringLayout.HORIZONTAL_CENTER, getGoalie(), Spring.constant(0),
				SpringLayout.HORIZONTAL_CENTER, this);
		getGoalie().setBackground(new Color(255, 0, 0));

		// installs Defender slots		
		getLayout().getConstraints(getDef2()).setConstraint(SpringLayout.VERTICAL_CENTER, Spring.scale(panelHeight, 0.34f));
		getLayout().putConstraint(SpringLayout.HORIZONTAL_CENTER, getDef2(), Spring.scale(panelWidth, -0.1f),
				SpringLayout.HORIZONTAL_CENTER, this);
		getDef2().setBackground(new Color(102, 204, 51));
		
		getLayout().getConstraints(getDef3()).setY(getLayout().getConstraints(getDef2()).getY());
		getLayout().putConstraint(SpringLayout.HORIZONTAL_CENTER, getDef3(), Spring.scale(panelWidth, 0.1f),
				SpringLayout.HORIZONTAL_CENTER, this);
		getDef3().setBackground(new Color(102, 204, 51));
		
		getLayout().getConstraints(getDef1()).setConstraint(SpringLayout.VERTICAL_CENTER, Spring.scale(panelHeight, 0.4f));
		getLayout().getConstraints(getDef1()).setConstraint(SpringLayout.HORIZONTAL_CENTER, Spring.scale(panelHeight, 0.125f));
		getDef1().setBackground(new Color(102, 204, 51));
		
		getLayout().putConstraint(SpringLayout.VERTICAL_CENTER, getDef4(), Spring.constant(0),
				SpringLayout.VERTICAL_CENTER, getDef1());
		getLayout().putConstraint(SpringLayout.HORIZONTAL_CENTER, getDef4(), Spring.scale(panelHeight, -0.125f),
				SpringLayout.EAST, this);	
		getDef4().setBackground(new Color(102, 204, 51));
		
		// installs Midfielder slots
		getLayout().putConstraint(SpringLayout.HORIZONTAL_CENTER, getMid2(), Spring.constant(0), SpringLayout.HORIZONTAL_CENTER, this);
		getLayout().putConstraint(SpringLayout.NORTH, getMid2(), goalieYoffset, SpringLayout.VERTICAL_CENTER, this);
		getMid2().setBackground(new Color(221, 0, 0));
		
		getLayout().getConstraints(getMid1()).setY(getLayout().getConstraints(getMid2()).getY());
		getLayout().getConstraints(getMid1()).setConstraint(SpringLayout.HORIZONTAL_CENTER, Spring.scale(panelWidth, 0.2f));
		getMid1().setBackground(new Color(221, 0, 0));
		
		getLayout().getConstraints(getMid3()).setY(getLayout().getConstraints(getMid2()).getY());
		getLayout().putConstraint(SpringLayout.HORIZONTAL_CENTER, getMid3(), Spring.scale(panelWidth, -0.2f), SpringLayout.EAST, this);
		getMid3().setBackground(new Color(221, 0, 0));
		
		// installs Forwards slots		
		getLayout().putConstraint(SpringLayout.HORIZONTAL_CENTER, getForw2(), Spring.constant(0), SpringLayout.HORIZONTAL_CENTER, this);
		getLayout().putConstraint(SpringLayout.VERTICAL_CENTER, getForw2(), Spring.scale(panelHeight, -0.12f), SpringLayout.SOUTH, this);
		getForw2().setBackground(new Color(0, 0, 0));
		
		getLayout().putConstraint(SpringLayout.HORIZONTAL_CENTER, getForw1(), Spring.constant(0), SpringLayout.HORIZONTAL_CENTER, getMid1());
		getLayout().getConstraints(getForw1()).setConstraint(SpringLayout.VERTICAL_CENTER, Spring.scale(panelHeight, 0.86f));
		getForw1().setBackground(new Color(0, 0, 0));
		
		getLayout().putConstraint(SpringLayout.HORIZONTAL_CENTER, getForw3(), Spring.constant(0), SpringLayout.HORIZONTAL_CENTER, getMid3());
		getLayout().getConstraints(getForw3()).setConstraint(SpringLayout.VERTICAL_CENTER, Spring.scale(panelHeight, 0.86f));
		getForw3().setBackground(new Color(0, 0, 0));
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			JFrame window = new JFrame("testing Safe Spring");

			// Set the position to screen center & size to half screen size
			Dimension wndSize = window.getToolkit().getScreenSize(); // Get screen size
			window.setLocation(new Point(wndSize.width / 4, wndSize.height / 4));
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			window.setContentPane(new _433SpringSafe());

			window.pack();
			window.setVisible(true);

		});
	}
}
