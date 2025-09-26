package gui.utils.schemes;

import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import domainModel.scheme.Scheme532;

import java.awt.Dimension;
import java.util.List;
import java.awt.Color;

@SuppressWarnings("serial")
public class Spring532Scheme extends SpringSchemePanel {	
	
	/*
	 * SpringLayout MEMO
	 * 
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
	
	/**
	 * provides {@linkplain Spring532Scheme}'s <b>public</b> instantiation point.
	 * 
	 * @param isDesignTime selects
	 *                     <ul>
	 *                     <li><b><i>design-time</i></b> instantiation when {@code true}
	 *                     <li><b><i>runtime</i></b> instantiation when {@code false}
	 *                     </ul>
	 *                     <p>
	 *                     Graphical clients should consider passing
	 *                     {@linkplain Beans#isDesignTime()}
	 */
	public Spring532Scheme(boolean isDesignTime) {
		
		super(Scheme532.INSTANCE);
		
		// creates springs for the Panel dimensions
		Spring panelWidth = Spring.constant(100);
		Spring panelHeight = Spring.constant(100);

		if (isDesignTime) {
			// public design-time logic
		}
		
		getLayout().getConstraints(this).setWidth(panelWidth);
		getLayout().getConstraints(this).setHeight(panelHeight);			

		layDownSlots(panelWidth, panelHeight);
	}

	/**
	 * provides this type's <b><i>private</i> design-time instantiation</b>.
	 * 
	 * <p>
	 * This constructor is intended <i>purely</i> for supporting the design of this
	 * type in WindowBuilder
	 */
	Spring532Scheme() {
		
		super(Scheme532.INSTANCE);
		
		/*
		 * designing with SpringLayout using WB has several limitations:
		 * 		- WB only acknowledges the putConstraint() API
		 * 		- it does not relate container width/height springs
		 * 		  to manually set container dimensions
		 * 		- in fact, it seems to ignore Spring's container w/h springs
		 * 		  unless a parent container exists
		 * 
		 * moreover, WB does NOT access superclass members
		 */

		// a typical aspect ratio for a football field
		Dimension footballField = new Dimension(463, 648);
		setPreferredSize(footballField);
		
		// this has to be done so scaled springs will
		// work well with the absolute dimensions employed
		Spring panelWidth = Spring.constant(footballField.width);
		Spring panelHeight = Spring.constant(footballField.height); 
		
		layDownSlots(panelWidth, panelHeight);
		
		fillSlotsWithDummyPanels(recommendedSlotDimensions(footballField));
	}

	/**
	 * lays out slot Panels inside {@code this} containing Panel, in terms of 
	 * {@link Spring#constant(int) Spring.constant}(0) Springs or 
	 * {@link Spring#scale(Spring, float) Spring.scale} self-adjusting multiples of
	 * {@code panelWidth} and {@code panelHeight}. 
	 * 
	 * @param panelWidth the containing Panel's constant width Spring
	 * @param panelHeight the containing Panel's constant height Spring
	 */
	private void layDownSlots(Spring panelWidth, Spring panelHeight) {
		// goalie slot
		getLayout().putConstraint(
				SpringLayout.NORTH, slot1, 
				Spring.scale(panelHeight, 0.005f), 
				SpringLayout.NORTH, this);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot1, 
				Spring.constant(0),
				SpringLayout.HORIZONTAL_CENTER, this);
		slot1.setBackground(GOALIE_COLOR);
		
		// def3 slot
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot4, 
				Spring.constant(0), 
				SpringLayout.HORIZONTAL_CENTER, this);
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot4, 
				Spring.scale(panelWidth, 0.43f),
				SpringLayout.NORTH, this);
		slot4.setBackground(DEFENDER_COLOR);

		// def2 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot3, 
				Spring.constant(0),
				SpringLayout.VERTICAL_CENTER, slot4);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot3, 
				Spring.scale(panelWidth, 0.25f),
				SpringLayout.WEST, this);
		slot3.setBackground(DEFENDER_COLOR);
		
		// def4 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot5, 
				Spring.constant(0), 
				SpringLayout.VERTICAL_CENTER, slot3);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot5, 
				Spring.scale(panelWidth, -0.25f),
				SpringLayout.EAST, this);
		slot5.setBackground(DEFENDER_COLOR);

		// def1 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot2, 
				Spring.scale(panelHeight, 0f),
				SpringLayout.VERTICAL_CENTER, this);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot2, 
				Spring.scale(panelWidth, 0.125f),
				SpringLayout.WEST, this);
		slot2.setBackground(DEFENDER_COLOR);
		
		// def5 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot6, 
				Spring.constant(0), 
				SpringLayout.VERTICAL_CENTER, slot2);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot6, 
				Spring.scale(panelWidth, -0.125f),
				SpringLayout.EAST, this);
		slot6.setBackground(DEFENDER_COLOR);
		
		// mid2 slot
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot8, 
				Spring.constant(0), 
				SpringLayout.HORIZONTAL_CENTER, this);
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot8, 
				Spring.scale(panelHeight, 0.7f),
				SpringLayout.NORTH, this);
		slot8.setBackground(MIDFIELDER_COLOR);

		// mid1 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot7, 
				Spring.constant(0),
				SpringLayout.VERTICAL_CENTER, slot8);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot7, 
				Spring.scale(panelWidth, 0.16f),
				SpringLayout.WEST, this);
		slot7.setBackground(MIDFIELDER_COLOR);

		// mid3 slot
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot9, 
				Spring.constant(0),
				SpringLayout.VERTICAL_CENTER, slot8);
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot9, 
				Spring.scale(panelWidth, -0.16f),
				SpringLayout.EAST, this);
		slot9.setBackground(MIDFIELDER_COLOR);

		// forw1 slot
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot10, 
				Spring.scale(panelHeight, 0.25f),
				SpringLayout.WEST, this);
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot10, 
				Spring.scale(panelHeight, 0.9f),
				SpringLayout.NORTH, this);
		slot10.setBackground(FORWARD_COLOR);

		// forw2 slot
		getLayout().putConstraint(
				SpringLayout.HORIZONTAL_CENTER, slot11, 
				Spring.scale(panelHeight, -0.25f),
				SpringLayout.EAST, this);
		getLayout().putConstraint(
				SpringLayout.VERTICAL_CENTER, slot11, 
				Spring.constant(0),
				SpringLayout.VERTICAL_CENTER, slot10);
		slot11.setBackground(FORWARD_COLOR);
	}

	private void fillSlotsWithDummyPanels(Dimension designContentSize) {
		JPanel content1 = new JPanel();
		content1.setPreferredSize(designContentSize);
		content1.setBackground(Color.MAGENTA);
		slot1.add(content1);
		
		JPanel content2 = new JPanel();
		content2.setPreferredSize(designContentSize);
		content2.setBackground(Color.ORANGE);
		slot2.add(content2);
		
		JPanel content3 = new JPanel();
		content3.setPreferredSize(designContentSize);
		content3.setBackground(Color.ORANGE);
		slot3.add(content3);
		
		JPanel content4 = new JPanel();
		content4.setPreferredSize(designContentSize);
		content4.setBackground(Color.ORANGE);
		slot4.add(content4);
		
		JPanel content5 = new JPanel();
		content5.setPreferredSize(designContentSize);
		content5.setBackground(Color.ORANGE);
		slot5.add(content5);
		
		JPanel content6 = new JPanel();
		content6.setPreferredSize(designContentSize);
		content6.setBackground(Color.ORANGE);
		slot6.add(content6);
		
		JPanel content7 = new JPanel();
		content7.setPreferredSize(designContentSize);
		content7.setBackground(Color.GREEN);
		slot7.add(content7);
		
		JPanel content8 = new JPanel();
		content8.setPreferredSize(designContentSize);
		content8.setBackground(Color.GREEN);
		slot8.add(content8);
		
		JPanel content9 = new JPanel();
		content9.setPreferredSize(designContentSize);
		content9.setBackground(Color.GREEN);
		slot9.add(content9);
		
		JPanel content10= new JPanel();
		content10.setPreferredSize(designContentSize);
		content10.setBackground(Color.YELLOW);
		slot10.add(content10);		
		
		JPanel content11 = new JPanel();
		content11.setPreferredSize(designContentSize);
		content11.setBackground(Color.YELLOW);
		slot11.add(content11);
	}
	
	public JPanel getGoalie() { return slot1; }

	public JPanel getDef1() {   return slot2;   }

	public JPanel getDef2() {   return slot3;   }

	public JPanel getDef3() {   return slot4;   }

	public JPanel getDef4() {   return slot5;   }

	public JPanel getDef5() {   return slot6;   }

	public JPanel getMid1() {   return slot7;   }

	public JPanel getMid2() {   return slot8;   }

	public JPanel getMid3() {   return slot9;  }

	public JPanel getForw1() {  return slot10;  }

	public JPanel getForw2() {  return slot11;  }
	
	@Override
	public List<JPanel> getDefenderSlots() {
		return List.of(slot2, slot3, slot4, slot5, slot6);
	}

	@Override
	public List<JPanel> getMidfielderSlots() {
		return List.of(slot7, slot8, slot9);
	}

	@Override
	public List<JPanel> getForwardSlots() {
		return List.of(slot10, slot11);
	}

	@Override
	public void accept(SpringSchemeVisitor visitor) {
		visitor.visit532Scheme(this);
	}
}
