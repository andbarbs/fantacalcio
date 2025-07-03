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
	
	public static Dimension getSlotDimensions(Dimension fieldDimension) {
		return new Dimension(
				(int)Math.floor(fieldDimension.width * slotWidthRatio), 
				(int)Math.floor(fieldDimension.height * slotHeightRatio));
	}
	
	/**
	 * Create the panel.
	 */
	public _433SpringSafe() {
		// creates springs for the Panel dimensions
		Spring panelWidth = Spring.constant(100);
		Spring panelHeight = Spring.constant(100);
		
		// creates springs for the slot dimensions
		Spring slotWidth = Spring.scale(panelWidth, slotWidthRatio);
		Spring slotHeight = Spring.scale(panelHeight, slotHeightRatio);

		// sets panel's own dimensions
		getLayout().getConstraints(this).setWidth(panelWidth);
		getLayout().getConstraints(this).setHeight(panelHeight);

		// creates getGoalie() slot
		Spring goalieYoffset = Spring.scale(panelHeight, 0.02f);
		getLayout().getConstraints(getGoalie()).setConstraint(SpringLayout.NORTH, goalieYoffset);
		getLayout().putConstraint(SpringLayout.WEST, getGoalie(), Spring.scale(slotWidth, -0.5f),
				SpringLayout.HORIZONTAL_CENTER, this);
		getLayout().getConstraints(getGoalie()).setWidth(slotWidth);
		getLayout().getConstraints(getGoalie()).setHeight(slotHeight);
		getGoalie().setBackground(new Color(255, 0, 0));

		// creates Defender slots
		Spring widthAfterDefenders = Spring.sum(panelWidth, Spring.scale(slotWidth, -4f));
		Spring widthNibbleAfterDefenders = Spring.scale(widthAfterDefenders, 0.125f);
		Spring defendersArcOffset = Spring.scale(panelHeight, 0.06f);
		
		getLayout().getConstraints(getDef2()).setY(Spring.sum(slotHeight, Spring.scale(goalieYoffset, 2f)));
		getLayout().putConstraint(SpringLayout.EAST, getDef2(), Spring.scale(widthNibbleAfterDefenders, -1f),
				SpringLayout.HORIZONTAL_CENTER, this);
		getLayout().getConstraints(getDef2()).setWidth(slotWidth);
		getLayout().getConstraints(getDef2()).setHeight(slotHeight);
		getDef2().setBackground(new Color(102, 204, 51));
		
		getLayout().getConstraints(getDef3()).setY(getLayout().getConstraints(getDef2()).getY());
		getLayout().putConstraint(SpringLayout.WEST, getDef3(), widthNibbleAfterDefenders,
				SpringLayout.HORIZONTAL_CENTER, this);
		getLayout().getConstraints(getDef3()).setWidth(slotWidth);
		getLayout().getConstraints(getDef3()).setHeight(slotHeight);
		getDef3().setBackground(new Color(102, 204, 51));
		
		getLayout().getConstraints(getDef1()).setX(Spring.scale(widthAfterDefenders, 0.125f));
		getLayout().getConstraints(getDef1()).setY(Spring.sum(defendersArcOffset, getLayout().getConstraints(getDef2()).getY()));
		getLayout().getConstraints(getDef1()).setWidth(slotWidth);
		getLayout().getConstraints(getDef1()).setHeight(slotHeight);	
		getDef1().setBackground(new Color(102, 204, 51));
		
		getLayout().getConstraints(getDef4()).setWidth(slotWidth);
		getLayout().getConstraints(getDef4()).setHeight(slotHeight);	
		getLayout().putConstraint(SpringLayout.EAST, getDef4(), Spring.scale(widthNibbleAfterDefenders, -1f),
				SpringLayout.EAST, this);
		getLayout().getConstraints(getDef4()).setY(getLayout().getConstraints(getDef1()).getY());
		getDef4().setBackground(new Color(102, 204, 51));
		
		// creates Midfielder slots
		Spring widthNibbleAfterMids = Spring.scale(Spring.sum(panelWidth, Spring.scale(slotWidth, -3f)), 1f / 6f);
		
		getLayout().getConstraints(getMid1()).setX(widthNibbleAfterMids);
		getLayout().putConstraint(SpringLayout.NORTH, getMid1(), goalieYoffset, SpringLayout.VERTICAL_CENTER, this);
		getLayout().getConstraints(getMid1()).setWidth(slotWidth);
		getLayout().getConstraints(getMid1()).setHeight(slotHeight);	
		getMid1().setBackground(new Color(221, 0, 0));
		
		getLayout().getConstraints(getMid2()).setY(getLayout().getConstraints(getMid1()).getY());
		getLayout().putConstraint(SpringLayout.WEST, getMid2(), Spring.scale(widthNibbleAfterMids, 2f), SpringLayout.EAST, getMid1());
		getLayout().getConstraints(getMid2()).setWidth(slotWidth);
		getLayout().getConstraints(getMid2()).setHeight(slotHeight);	
		getMid2().setBackground(new Color(221, 0, 0));
		
		getLayout().getConstraints(getMid3()).setY(getLayout().getConstraints(getMid1()).getY());
		getLayout().putConstraint(SpringLayout.WEST, getMid3(), Spring.scale(widthNibbleAfterMids, 2f), SpringLayout.EAST, getMid2());
		getLayout().getConstraints(getMid3()).setWidth(slotWidth);
		getLayout().getConstraints(getMid3()).setHeight(slotHeight);	
		getMid3().setBackground(new Color(221, 0, 0));
		
		// create Forwards slots
		Spring forwsBaseY = Spring.sum(getLayout().getConstraints(getMid1()).getConstraint(SpringLayout.SOUTH), widthNibbleAfterMids);
		Spring forwArcHeight = goalieYoffset;
		
		getLayout().getConstraints(getForw1()).setX(getLayout().getConstraints(getMid1()).getX());
		getLayout().getConstraints(getForw1()).setY(forwsBaseY);
		getLayout().getConstraints(getForw1()).setWidth(slotWidth);
		getLayout().getConstraints(getForw1()).setHeight(slotHeight);	
		getForw1().setBackground(new Color(0, 0, 0));
		
		getLayout().getConstraints(getForw2()).setX(getLayout().getConstraints(getMid2()).getX());
		getLayout().getConstraints(getForw2()).setY(Spring.sum(forwsBaseY, forwArcHeight));
		getLayout().getConstraints(getForw2()).setWidth(slotWidth);
		getLayout().getConstraints(getForw2()).setHeight(slotHeight);	
		getForw2().setBackground(new Color(0, 0, 0));
		
		getLayout().getConstraints(getForw3()).setX(getLayout().getConstraints(getMid3()).getX());
		getLayout().getConstraints(getForw3()).setY(forwsBaseY);
		getLayout().getConstraints(getForw3()).setWidth(slotWidth);
		getLayout().getConstraints(getForw3()).setHeight(slotHeight);	
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
