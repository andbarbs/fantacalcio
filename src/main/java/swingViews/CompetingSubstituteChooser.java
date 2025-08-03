package swingViews;

import javax.swing.JPanel;
import javax.swing.Spring;

import java.awt.Dimension;
import java.beans.Beans;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import domainModel.Player;
import domainModel.Player.Defender;
import swingViews.FillableSwappableSequence.FillableSwappableSequenceListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.Insets;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class CompetingSubstituteChooser<T extends Player> extends JPanel 
				implements FillableSwappableSequenceListener<SubstitutePlayerSelector<T>> {

	private FillableSwappableSequence<SubstitutePlayerSelector<T>> driver;
	private SubstitutePlayerSelector<T> selPres1;
	private SubstitutePlayerSelector<T> selPres2;
	private SubstitutePlayerSelector<T> selPres3;
	private Action swapSelectors1and2, swapSelectors2and3;
	
	public List<SubstitutePlayerSelector<T>> getSubstituteSelectors() {
		return List.of(selPres1, selPres2, selPres3);
	}

	public CompetingSubstituteChooser() {
		setLayout(new SpringLayout());		
		SpringLayout layout = (SpringLayout)getLayout();
		
		Spring panelWidth = Spring.constant(600);
		Spring panelHeight = Spring.constant(250);
		layout.getConstraints(this).setWidth(panelWidth);
		layout.getConstraints(this).setHeight(panelHeight);

		// somehow this is necessary for WB
		if (Beans.isDesignTime())
			setPreferredSize(new Dimension(600, 225));

		// initialize components and add them
		SwingSubPlayerSelector<T> selView1 = new SwingSubPlayerSelector<T>();
		selPres1 = new SubstitutePlayerSelector<>(selView1);
		selView1.setName("selector1");
		selView1.setPresenter(selPres1);
		
		SwingSubPlayerSelector<T> selView2 = new SwingSubPlayerSelector<T>();
		selPres2 = new SubstitutePlayerSelector<>(selView2);
		selView2.setName("selector2");
		selView2.setPresenter(selPres2);
		
		SwingSubPlayerSelector<T> selView3 = new SwingSubPlayerSelector<T>();
		selPres3 = new SubstitutePlayerSelector<>(selView3);
		selView3.setName("selector3");
		selView3.setPresenter(selPres3);
		
		swapSelectors1and2 = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("asking driver to swap 1 and 2");
				driver.swapRight(selPres1);
			}
		};

		swapSelectors2and3 = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("asking driver to swap 2 and 3");
				driver.swapRight(selPres2);
			}
		};
		
		JButton swap1 = new JButton(swapSelectors1and2);
		swap1.setName("swap1_2");
		swap1.setMargin(new Insets(2, 2, 2, 2));
		swap1.setIcon(new ImageIcon(getClass().getResource("/gui_icons/swap_verysmall.png")));
		JButton swap2 = new JButton(swapSelectors2and3);
		swap2.setName("swap2_3");
		swap2.setMargin(new Insets(2, 2, 2, 2));
		swap2.setIcon(new ImageIcon(getClass().getResource("/gui_icons/swap_verysmall.png")));

		add(selView1); add(selView2); add(selView3);
		add(swap1); add(swap2);
		
		// evenly space sel1/sel2/sel3 horizontally
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, selView1,
		                  Spring.scale(panelWidth, 1f/6f),
		                  SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, selView2,
		                  Spring.scale(panelWidth, 3f/6f),
		                  SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, selView3,
		                  Spring.scale(panelWidth, 5f/6f),
		                  SpringLayout.WEST, this);

		// swap buttons at 2/6 and 4/6 horizontally
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swap1,
		                  Spring.scale(panelWidth, 2f/6f),
		                  SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swap2,
		                  Spring.scale(panelWidth, 4f/6f),
		                  SpringLayout.WEST, this);
		
		// vertically center all
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, selView1,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, selView2,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, selView3,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, swap1,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, swap2,
				0, SpringLayout.VERTICAL_CENTER, this);
		
		// wire up substitute driver
		driver = FillableSwappableSequence.createSequence(
				List.of(selPres1, selPres2, selPres3));
		driver.attachListener(this);
		
		// initially disable swapping
		List.of(swapSelectors1and2, swapSelectors2and3).forEach(a -> a.setEnabled(false));
	}
	
	
	@Override
	public void becameEmpty(SubstitutePlayerSelector<T> emptiedGadget) {
		if (emptiedGadget == selPres3)
			swapSelectors2and3.setEnabled(false);
		else if (emptiedGadget == selPres2)
			swapSelectors1and2.setEnabled(false);
	}
	
	@Override
	public void becameFilled(SubstitutePlayerSelector<T> filledGadget) {
		System.out.println("content added to a gadget!");
		if (filledGadget == selPres2)
			swapSelectors1and2.setEnabled(true);
		else if (filledGadget == selPres3)
			swapSelectors2and3.setEnabled(true);
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("substitute chooser demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			CompetingSubstituteChooser<Defender> chooser = new CompetingSubstituteChooser<Defender>();
			CompetitiveOptionDealingGroup.initializeDealing(
					Set.copyOf(chooser.getSubstituteSelectors()), 
					List.of(
							new Defender("Giorgio", "Chiellini"),
		            	    new Defender("Gerard", "Piqué"),
		            	    new Defender("Sergio", "Ramos"),
		            	    new Defender("Thiago", "Silva"),
		            	    new Defender("Virgil", "van Dijk")));
			frame.setContentPane(chooser);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	

}
