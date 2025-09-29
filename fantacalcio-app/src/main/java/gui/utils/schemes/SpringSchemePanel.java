package gui.utils.schemes;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

import domainModel.Scheme;

@SuppressWarnings("serial")
public abstract class SpringSchemePanel extends JPanel {
	
	private final Scheme scheme;
	
	private static final float slotWidthRatio = 0.217f;
	private static final float slotHeightRatio = 0.193f;
	
	public static Dimension recommendedSlotDimensions(Dimension fieldDimension) {
		return new Dimension(
				(int)Math.floor(fieldDimension.width * slotWidthRatio), 
				(int)Math.floor(fieldDimension.height * slotHeightRatio));
	}
	
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

	protected SpringSchemePanel(Scheme scheme) {
		this.scheme = scheme;
		
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
	
	public final Scheme scheme() {
		return this.scheme;
	}
	
	@Override
	public SpringLayout getLayout() {
		return (SpringLayout) super.getLayout();
	}
	
	public final JPanel getGoalieSlot() {
		return slot1;
	}
	
	public final List<JPanel> getGoalieSlots() {
		return List.of(slot1);
	}
	
	public final List<JPanel> getDefenderSlots() {
		return Stream.of(slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8, slot9, slot10, slot11)
				.skip(1)
				.limit(scheme.getNumDefenders())
				.collect(Collectors.toList());
	}
	
	public final List<JPanel> getMidfielderSlots() {
		return Stream.of(slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8, slot9, slot10, slot11)
				.skip(1 + scheme.getNumDefenders())
				.limit(scheme.getNumMidfielders())
				.collect(Collectors.toList());
	}
	public final List<JPanel> getForwardSlots() {
		return Stream.of(slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8, slot9, slot10, slot11)
				.skip(1 + scheme.getNumDefenders() + scheme.getNumMidfielders())
				.limit(scheme.getNumForwards())
				.collect(Collectors.toList());
	}
	
	public interface SpringSchemeVisitor {
		void visit433Scheme(Spring433Scheme scheme433);
		void visit343Scheme(Spring343Scheme scheme343);
		void visit532Scheme(Spring532Scheme scheme532);
	}
	
	public abstract void accept(SpringSchemeVisitor visitor);

}