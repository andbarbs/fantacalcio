package swingViews;

import javax.swing.JPanel;
import javax.swing.Spring;

import java.awt.Dimension;
import java.beans.Beans;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;
import java.awt.Insets;

@SuppressWarnings("serial")
public class CompetingSubstituteChooser extends JPanel {

	public CompetingSubstituteChooser() {
		setLayout(new SpringLayout());		
		SpringLayout layout = (SpringLayout)getLayout();
		
		Spring panelWidth = Spring.constant(600);
		Spring panelHeight = Spring.constant(225);
		layout.getConstraints(this).setWidth(panelWidth);
		layout.getConstraints(this).setHeight(panelHeight);

		// somehow this is necessary for WB
		if (Beans.isDesignTime())
			setPreferredSize(new Dimension(600, 225));

		CompetingPlayerSelector sel1 = new CompetingPlayerSelector();
		CompetingPlayerSelector sel2 = new CompetingPlayerSelector();
		CompetingPlayerSelector sel3 = new CompetingPlayerSelector();
		
		JButton swap1 = new JButton();
		swap1.setMargin(new Insets(2, 2, 2, 2));
		swap1.setIcon(new ImageIcon(CompetingSubstituteChooser.class.getResource("/gui_icons/swap_verysmall.png")));
		JButton swap2 = new JButton("");
		swap2.setMargin(new Insets(2, 2, 2, 2));
		swap2.setIcon(new ImageIcon(CompetingSubstituteChooser.class.getResource("/gui_icons/swap_verysmall.png")));

		add(sel1); add(sel2); add(sel3);
		add(swap1); add(swap2);
		
//		//lay children out using relative springs <--problem: WB only recognizes putConstraint()!!
//		lay.getConstraints(sel1).setConstraint(SpringLayout.HORIZONTAL_CENTER, Spring.scale(panelWidth, 1f/6f));
//		lay.getConstraints(sel2).setConstraint(SpringLayout.HORIZONTAL_CENTER, Spring.scale(panelWidth, 3f/6f));
//		lay.getConstraints(sel3).setConstraint(SpringLayout.HORIZONTAL_CENTER, Spring.scale(panelWidth, 5f/6f));
//		lay.putConstraint(SpringLayout.VERTICAL_CENTER, sel1, Spring.constant(0),
//				SpringLayout.VERTICAL_CENTER, this);
//		lay.getConstraints(sel2).setY(lay.getConstraints(sel1).getY());
//		lay.getConstraints(sel3).setY(lay.getConstraints(sel1).getY());
//
//		// center swap1 on the boundary after sel1
//		lay.getConstraints(swap1).setConstraint(SpringLayout.HORIZONTAL_CENTER, Spring.scale(panelWidth, 2f/6f));
//		lay.getConstraints(swap2).setConstraint(SpringLayout.HORIZONTAL_CENTER, Spring.scale(panelWidth, 4f/6f));
//		lay.putConstraint(SpringLayout.VERTICAL_CENTER, swap1, Spring.constant(0),
//				SpringLayout.VERTICAL_CENTER, this);
//		lay.getConstraints(swap2).setY(lay.getConstraints(swap1).getY());
		
		// evenly space sel1/sel2/sel3
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, sel1,
		                  Spring.scale(panelWidth, 1f/6f),
		                  SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, sel2,
		                  Spring.scale(panelWidth, 3f/6f),
		                  SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, sel3,
		                  Spring.scale(panelWidth, 5f/6f),
		                  SpringLayout.WEST, this);

		// swap buttons at 2/6 and 4/6
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swap1,
		                  Spring.scale(panelWidth, 2f/6f),
		                  SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swap2,
		                  Spring.scale(panelWidth, 4f/6f),
		                  SpringLayout.WEST, this);
		
		// vertically center all
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, sel1,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, sel2,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, sel3,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, swap1,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, swap2,
				0, SpringLayout.VERTICAL_CENTER, this);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("substitute chooser demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(new CompetingSubstituteChooser());
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
	

}
