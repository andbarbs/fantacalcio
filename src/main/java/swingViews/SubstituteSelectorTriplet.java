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

import javax.swing.ImageIcon;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
public class SubstituteSelectorTriplet<T extends Player> extends JPanel 
				implements FillableSwappableSequenceListener<SubstitutePlayerSelector<T>> {

	private FillableSwappableSequence<SubstitutePlayerSelector<T>> sequenceDriver;
	private SubstitutePlayerSelector<T> selPres1;
	private SubstitutePlayerSelector<T> selPres2;
	private SubstitutePlayerSelector<T> selPres3;
	
	private JButton swapSelectors1and2, swapSelectors2and3;
	
	public List<SubstitutePlayerSelector<T>> getSubstituteSelectors() {
		return List.of(selPres1, selPres2, selPres3);
	}

	public SubstituteSelectorTriplet() {
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
		
		swapSelectors1and2 = new JButton();
		swapSelectors1and2.setName("swap1_2");
		swapSelectors1and2.setMargin(new Insets(2, 2, 2, 2));
		swapSelectors1and2.setIcon(new ImageIcon(getClass().getResource("/gui_icons/swap_verysmall.png")));
		swapSelectors1and2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("asking driver to swap 1 and 2");
				sequenceDriver.swapRight(selPres1);
			}
		});
		
		swapSelectors2and3 = new JButton();
		swapSelectors2and3.setName("swap2_3");
		swapSelectors2and3.setMargin(new Insets(2, 2, 2, 2));
		swapSelectors2and3.setIcon(new ImageIcon(getClass().getResource("/gui_icons/swap_verysmall.png")));
		swapSelectors2and3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("asking driver to swap 2 and 3");
				sequenceDriver.swapRight(selPres2);
			}
		});

		add(selView1); add(selView2); add(selView3);
		add(swapSelectors1and2); add(swapSelectors2and3);
		
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

		// swap-buttons at 2/6 and 4/6 horizontally
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swapSelectors1and2,
		                  Spring.scale(panelWidth, 2f/6f),
		                  SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swapSelectors2and3,
		                  Spring.scale(panelWidth, 4f/6f),
		                  SpringLayout.WEST, this);
		
		// vertically center all
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, selView1,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, selView2,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, selView3,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, swapSelectors1and2,
				0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, swapSelectors2and3,
				0, SpringLayout.VERTICAL_CENTER, this);
		
		// wire up substitute driver
		sequenceDriver = FillableSwappableSequence.createSequence(
				List.of(selPres1, selPres2, selPres3));
		sequenceDriver.attachListener(this);
		
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
	
	/*
	 * will have a getSelectors() for a composing client to wire up competition
	 */
	
	/*
	 * setters needed for unit tests to install hard composites
	 */
	void setSelectors(SubstitutePlayerSelector<T> selector1, SubstitutePlayerSelector<T> selector2,
			SubstitutePlayerSelector<T> selector3) {
		selPres1 = selector1;
		selPres2 = selector2;
		selPres3 = selector3;
	}
	
	void setSequenceDriver(FillableSwappableSequence<SubstitutePlayerSelector<T>> mockSequence) {
		this.sequenceDriver = mockSequence;
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("substitute chooser demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			SubstituteSelectorTriplet<Defender> chooser = new SubstituteSelectorTriplet<Defender>();
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
