package swingViews;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.beans.Beans;

@SuppressWarnings("serial")
public class Spring433Scheme extends SpringSchemePanel {	
	
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
	public Spring433Scheme() {

		if (Beans.isDesignTime())
			setPreferredSize(new Dimension(100, 100));

		// creates springs for the Panel dimensions
		Spring panelWidth = Spring.constant(100);
		Spring panelHeight = Spring.constant(100);// sets panel's own dimensions
		getLayout().getConstraints(this).setWidth(panelWidth);
		getLayout().getConstraints(this).setHeight(panelHeight);

		// goalie slot
		Spring goalieYoffset = Spring.scale(panelHeight, 0.02f);
		getLayout().putConstraint(
				SpringLayout.NORTH, slot1, 
				goalieYoffset, 
				SpringLayout.NORTH, this);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot1, 
				Spring.constant(0),
				SpringLayout.HORIZONTAL_CENTER, this);
		slot1.setBackground(GOALIE_COLOR);

		// def2 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot3, 
				Spring.scale(panelHeight, 0.34f),
				SpringLayout.NORTH, this);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot3, 
				Spring.scale(panelWidth, -0.1f),
				SpringLayout.HORIZONTAL_CENTER, this);
		slot3.setBackground(DEFENDER_COLOR);

		// def3 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot4, 
				Spring.constant(0), SpringLayout.VERTICAL_CENTER,
				slot3);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot4, 
				Spring.scale(panelWidth, 0.1f),
				SpringLayout.HORIZONTAL_CENTER, this);
		slot4.setBackground(DEFENDER_COLOR);

		// def1 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot2, 
				Spring.scale(panelHeight, 0.4f),
				SpringLayout.NORTH, this);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot2, 
				Spring.scale(panelHeight, 0.125f),
				SpringLayout.WEST, this);
		slot2.setBackground(DEFENDER_COLOR);

		// def4 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot5, 
				Spring.constant(0), SpringLayout.VERTICAL_CENTER,
				slot2);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot5, 
				Spring.scale(panelHeight, -0.125f),
				SpringLayout.EAST, this);
		slot5.setBackground(DEFENDER_COLOR);

		// mid2 slot
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot7, 
				Spring.constant(0),
				SpringLayout.HORIZONTAL_CENTER, this);
		getLayout().putConstraint(
				SpringLayout.NORTH, slot7, 
				goalieYoffset, 
				SpringLayout.VERTICAL_CENTER, this);
		slot7.setBackground(MIDFIELDER_COLOR);

		// mid1 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot6, 
				Spring.constant(0), 
				SpringLayout.VERTICAL_CENTER,
				slot7);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot6, 
				Spring.scale(panelWidth, 0.2f),
				SpringLayout.WEST, this);
		slot6.setBackground(MIDFIELDER_COLOR);

		// mid3 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot8, 
				Spring.constant(0), SpringLayout.VERTICAL_CENTER,
				slot7);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot8, 
				Spring.scale(panelWidth, -0.2f),
				SpringLayout.EAST, this);
		slot8.setBackground(MIDFIELDER_COLOR);

		// forw2 slot
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot10, 
				Spring.constant(0),
				SpringLayout.HORIZONTAL_CENTER, this);
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot10, 
				Spring.scale(panelHeight, -0.12f),
				SpringLayout.SOUTH, this);
		slot10.setBackground(FORWARD_COLOR);

		// forw1 slot
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot9, 
				Spring.constant(0),
				SpringLayout.HORIZONTAL_CENTER, slot6);
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot9, 
				Spring.scale(panelHeight, 0.86f),
				SpringLayout.NORTH, this);
		slot9.setBackground(FORWARD_COLOR);

		// forw3 slot
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot11, 
				Spring.constant(0),
				SpringLayout.HORIZONTAL_CENTER, slot8);
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot11, 
				Spring.scale(panelHeight, 0.86f),
				SpringLayout.NORTH, this);
		slot11.setBackground(FORWARD_COLOR);
	}
	
	public JPanel getGoalie() { return slot1; }

	public JPanel getDef1() {   return slot2;   }

	public JPanel getDef2() {   return slot3;   }

	public JPanel getDef3() {   return slot4;   }

	public JPanel getDef4() {   return slot5;   }

	public JPanel getMid1() {   return slot6;   }

	public JPanel getMid2() {   return slot7;   }

	public JPanel getMid3() {   return slot8;   }

	public JPanel getForw1() {  return slot9;  }

	public JPanel getForw2() {  return slot10;  }

	public JPanel getForw3() {  return slot11;  }

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			JFrame window = new JFrame("testing Safe Spring");

			// Set the position to screen center & size to half screen size
			Dimension wndSize = window.getToolkit().getScreenSize(); // Get screen size
			window.setLocation(new Point(wndSize.width / 4, wndSize.height / 4));
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			window.setContentPane(new Spring433Scheme());

			window.pack();
			window.setVisible(true);

		});
	}
}
