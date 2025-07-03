package swingViews;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;

public class _433SpringSafe extends SchemeSpringPanel {

	private static final long serialVersionUID = 1L;
	
	private static final float slotWidthRatio = 0.217f;
	private static final float slotHeightRatio = 0.193f;
	
	/**
	 * Create the panel.
	 */
	public _433SpringSafe() {
		setOpaque(false);
		SpringLayout layout = new SpringLayout();
		setLayout(layout);

		// creates springs for the Panel dimensions
		Spring panelWidth = Spring.constant(100);
		Spring panelHeight = Spring.constant(100);
		
		// creates springs for the slot dimensions
		Spring slotWidth = Spring.scale(panelWidth, slotWidthRatio);
		Spring slotHeight = Spring.scale(panelHeight, slotHeightRatio);

		// sets panel's own dimensions
		layout.getConstraints(this).setWidth(panelWidth);
		layout.getConstraints(this).setHeight(panelHeight);

		// creates Goalie slot
		goalie = new JPanel();
		add(goalie);
		Spring goalieYoffset = Spring.scale(panelHeight, 0.02f);
		layout.getConstraints(goalie).setConstraint(SpringLayout.NORTH, goalieYoffset);
		layout.putConstraint(SpringLayout.WEST, goalie, Spring.scale(slotWidth, -0.5f),
				SpringLayout.HORIZONTAL_CENTER, this);
		layout.getConstraints(goalie).setWidth(slotWidth);
		layout.getConstraints(goalie).setHeight(slotHeight);
		goalie.setBackground(new Color(255, 0, 0));

		// creates Defender slots
		Spring widthAfterDefenders = Spring.sum(panelWidth, Spring.scale(slotWidth, -4f));
		Spring widthNibbleAfterDefenders = Spring.scale(widthAfterDefenders, 0.125f);
		Spring defendersArcOffset = Spring.scale(panelHeight, 0.06f);
		
		def2 = new JPanel();
		add(def2);
		layout.getConstraints(def2).setY(Spring.sum(slotHeight, Spring.scale(goalieYoffset, 2f)));
		layout.putConstraint(SpringLayout.EAST, def2, Spring.scale(widthNibbleAfterDefenders, -1f),
				SpringLayout.HORIZONTAL_CENTER, this);
		layout.getConstraints(def2).setWidth(slotWidth);
		layout.getConstraints(def2).setHeight(slotHeight);
		def2.setBackground(new Color(102, 204, 51));
		
		def3 = new JPanel();
		add(def3);
		layout.getConstraints(def3).setY(layout.getConstraints(def2).getY());
		layout.putConstraint(SpringLayout.WEST, def3, widthNibbleAfterDefenders,
				SpringLayout.HORIZONTAL_CENTER, this);
		layout.getConstraints(def3).setWidth(slotWidth);
		layout.getConstraints(def3).setHeight(slotHeight);
		def3.setBackground(new Color(102, 204, 51));
		
		def1 = new JPanel();
		add(def1);
		layout.getConstraints(def1).setX(Spring.scale(widthAfterDefenders, 0.125f));
		layout.getConstraints(def1).setY(Spring.sum(defendersArcOffset, layout.getConstraints(def2).getY()));
		layout.getConstraints(def1).setWidth(slotWidth);
		layout.getConstraints(def1).setHeight(slotHeight);	
		def1.setBackground(new Color(102, 204, 51));
		
		def4 = new JPanel();
		add(def4);
		layout.getConstraints(def4).setWidth(slotWidth);
		layout.getConstraints(def4).setHeight(slotHeight);	
		layout.putConstraint(SpringLayout.EAST, def4, Spring.scale(widthNibbleAfterDefenders, -1f),
				SpringLayout.EAST, this);
		layout.getConstraints(def4).setY(layout.getConstraints(def1).getY());
		def4.setBackground(new Color(102, 204, 51));
		
		// creates Midfielder slots
		Spring widthNibbleAfterMids = Spring.scale(Spring.sum(panelWidth, Spring.scale(slotWidth, -3f)), 1f / 6f);
		
		mid1 = new JPanel();
		add(mid1);
		layout.getConstraints(mid1).setX(widthNibbleAfterMids);
		layout.putConstraint(SpringLayout.NORTH, mid1, goalieYoffset, SpringLayout.VERTICAL_CENTER, this);
		layout.getConstraints(mid1).setWidth(slotWidth);
		layout.getConstraints(mid1).setHeight(slotHeight);	
		mid1.setBackground(new Color(221, 0, 0));
		
		mid2 = new JPanel();
		add(mid2);
		layout.getConstraints(mid2).setY(layout.getConstraints(mid1).getY());
		layout.putConstraint(SpringLayout.WEST, mid2, Spring.scale(widthNibbleAfterMids, 2f), SpringLayout.EAST, mid1);
		layout.getConstraints(mid2).setWidth(slotWidth);
		layout.getConstraints(mid2).setHeight(slotHeight);	
		mid2.setBackground(new Color(221, 0, 0));
		
		mid3 = new JPanel();
		add(mid3);
		layout.getConstraints(mid3).setY(layout.getConstraints(mid1).getY());
		layout.putConstraint(SpringLayout.WEST, mid3, Spring.scale(widthNibbleAfterMids, 2f), SpringLayout.EAST, mid2);
		layout.getConstraints(mid3).setWidth(slotWidth);
		layout.getConstraints(mid3).setHeight(slotHeight);	
		mid3.setBackground(new Color(221, 0, 0));
		
		// create Forwards slots
		Spring forwsBaseY = Spring.sum(layout.getConstraints(mid1).getConstraint(SpringLayout.SOUTH), widthNibbleAfterMids);
		Spring forwArcHeight = goalieYoffset;
		
		forw1 = new JPanel();
		add(forw1);
		layout.getConstraints(forw1).setX(layout.getConstraints(mid1).getX());
		layout.getConstraints(forw1).setY(forwsBaseY);
		layout.getConstraints(forw1).setWidth(slotWidth);
		layout.getConstraints(forw1).setHeight(slotHeight);	
		forw1.setBackground(new Color(0, 0, 0));
		
		forw2 = new JPanel();
		add(forw2);
		layout.getConstraints(forw2).setX(layout.getConstraints(mid2).getX());
		layout.getConstraints(forw2).setY(Spring.sum(forwsBaseY, forwArcHeight));
		layout.getConstraints(forw2).setWidth(slotWidth);
		layout.getConstraints(forw2).setHeight(slotHeight);	
		forw2.setBackground(new Color(0, 0, 0));
		
		forw3 = new JPanel();
		add(forw3);
		layout.getConstraints(forw3).setX(layout.getConstraints(mid3).getX());
		layout.getConstraints(forw3).setY(forwsBaseY);
		layout.getConstraints(forw3).setWidth(slotWidth);
		layout.getConstraints(forw3).setHeight(slotHeight);	
		forw3.setBackground(new Color(0, 0, 0));
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
