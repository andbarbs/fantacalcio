package swingViews;

import java.awt.Color;
import java.util.stream.Stream;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

@SuppressWarnings("serial")
public abstract class SpringSchemePanel extends JPanel {
	
	protected static final Color FORWARD_COLOR = new Color(0, 0, 0);
	protected static final Color MIDFIELDER_COLOR = new Color(221, 0, 0);
	protected static final Color DEFENDER_COLOR = new Color(102, 204, 51);
	protected static final Color GOALIE_COLOR = new Color(255, 0, 0);

	protected JPanel slot1;
	protected JPanel slot2;
	protected JPanel slot3;
	protected JPanel slot4;
	protected JPanel slot5;
	protected JPanel slot6;
	protected JPanel slot7;
	protected JPanel slot8;
	protected JPanel slot9;
	protected JPanel slot10;
	protected JPanel slot11;

	public SpringSchemePanel() {
		setOpaque(false);
		setLayout(new SpringLayout());
		
		slot1 = new JPanel();
		slot2 = new JPanel(); 
		slot3  = new JPanel(); 
		slot4 = new JPanel(); 
		slot5 = new JPanel();
		slot6 = new JPanel();
		slot7 = new JPanel();
		slot8 = new JPanel();
		slot9 = new JPanel();
		slot10 = new JPanel();
		slot11 = new JPanel();

		Stream.of(slot1,
				slot2, slot3, slot4, slot5,
				slot6, slot7, slot8,
				slot9, slot10, slot11
				).forEach(this::add);
	}
	
	@Override
	public SpringLayout getLayout() {
		return (SpringLayout) super.getLayout();
	}
	
	

}