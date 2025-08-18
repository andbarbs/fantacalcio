package swingViews;

import javax.swing.JPanel;
import javax.swing.Spring;

import java.awt.Dimension;
import java.beans.Beans;
import java.io.IOException;
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
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;

/**
 * a {@code JPanel} providing access to <b>three</b>
 * {@linkplain SubstitutePlayerSelector}s as members of a left-to-right
 * {@linkplain FillableSwappableSequence <i>fillable-swappable sequence</i>}.
 * 
 * <p>
 * <h1></h1>Specifically, it allows users to
 * <ul>
 * <li>only select options on selectors from left to right
 * <li>swap two adjacent selections
 * </ul>
 * 
 * @param <T> the type for options in the triplet's
 *            {@code SubstitutePlayerSelector}s
 * @apiNote as an inner graphical component with raster contents, instantiation
 *          requires a <b>sizing hint</b>: see the
 *          {@linkplain SubstituteSelectorTriplet#SubstituteSelectorTriplet(boolean, Dimension)
 *          public constructor}
 */
@SuppressWarnings("serial")
public class SubstituteSelectorTriplet<T extends Player> extends JPanel
		implements FillableSwappableSequenceListener<SubstitutePlayerSelector<T>> {
	
	private static final float TRIPLET_W_OVER_SELECTOR_W = 5f;
	private static final float TRIPLET_H_OVER_SELECTOR_H = 1.3f;
	
	private SubstitutePlayerSelector<T> selPres1, selPres2, selPres3;
	private SwingSubPlayerSelector<T> selView1, selView2, selView3;

	private FillableSwappableSequence<SubstitutePlayerSelector<T>> sequenceDriver;

	private JButton swapSelectors1and2, swapSelectors2and3;
	
	/*
	 * WINDOWBUILDER SPRING LAYOUT MEMO
	 * 
	 * designing with SpringLayout using WB has several limitations:
	 * 		- WB only acknowledges the putConstraint() API
	 * 		- it does not relate container width/height springs
	 * 		  to manually set container dimensions
	 * 		- in fact, it seems to ignore Spring's container w/h springs
	 * 		  unless a parent container exists
	 * 
	 * moreover, WB does NOT access superclass members
	 */
	
	/**
	 * provides {@linkplain SubstituteSelectorTriplet}'s <b>public</b> instantiation
	 * point.
	 * 
	 * @param isDesignTime      selects
	 *                          <ul>
	 *                          <li><b><i>design-time</i></b> instantiation when
	 *                          {@code true}
	 *                          <li><b><i>runtime</i></b> instantiation when
	 *                          {@code false}
	 *                          </ul>
	 *                          <p>
	 *                          Graphical clients should consider passing
	 *                          {@linkplain Beans#isDesignTime()}
	 * @param selectorDimension an external <b>sizing indication</b>; the
	 *                          {@code Dimension} that should be used to instantiate
	 *                          {@linkplain SubstitutePlayerSelector}s in the
	 *                          triplet
	 * @throws IOException if {@code SubstitutePlayerSelector} sizing-augmented
	 *                     instantiation fails
	 */
	public SubstituteSelectorTriplet(boolean isDesignTime, Dimension selectorDimension) throws IOException {	

		// creates springs for the Panel based on provided sizing indication
		Spring panelWidth = Spring.scale(Spring.constant(selectorDimension.width), TRIPLET_W_OVER_SELECTOR_W);
		Spring panelHeight = Spring.scale(Spring.constant(selectorDimension.height), TRIPLET_H_OVER_SELECTOR_H);

		if (isDesignTime) {
			// public design-time logic
		}

		instantiateHardComposites(selectorDimension);

		instantiateSwapButtons();
		
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		layout.getConstraints(this).setWidth(panelWidth);
		layout.getConstraints(this).setHeight(panelHeight);
		
		addChildren(layout, panelWidth);    // adds and lays out children		
	}

	/**
	 * provides this type's <b><i>private</i> design-time instantiation</b>.
	 * 
	 * <p>
	 * This constructor is intended <i>purely</i> for supporting the design of this
	 * type in WindowBuilder
	 * @throws IOException 
	 */
	SubstituteSelectorTriplet() throws IOException {

		// chooses arbitrary dimensions
		Dimension selectorDims = new Dimension(120, 225); // appropriate dims for rendering a selector
		Dimension tripletDims = new Dimension(
				(int) (selectorDims.width * TRIPLET_W_OVER_SELECTOR_W),
				(int) (selectorDims.height * TRIPLET_H_OVER_SELECTOR_H)); 
		setPreferredSize(tripletDims);

		instantiateHardComposites(selectorDims);

		instantiateSwapButtons();

		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		addChildren(layout, Spring.constant(tripletDims.width));

		// sets explanatory borders
		setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "SpringLayout", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(184, 207, 229)));
		selView1.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "Selector1",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));
		selView2.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "Selector2",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));
		selView3.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "Selector3",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));
	}

	/*
	 * instantiates the two swap buttons and initially disables them
	 */
	private void instantiateSwapButtons() {
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

		List.of(swapSelectors1and2, swapSelectors2and3).forEach(a -> a.setEnabled(false));
	}

	/**
	 * instantiates and wires up hard composites, i.e.
	 * <ul>
	 * <li>the three {@linkplain SwingSubPlayerSelector Substitute Selector View} instances
	 * <li>the three {@linkplain SubstitutePlayerSelector Substitute Selector Presenter} instances
	 * <li>the {@linkplain FillableSwappableSequence Sequence driver} instance
	 * </ul>
	 * attaching {@code this} as a sequence listener.
	 * @throws IOException 
	 */
	private void instantiateHardComposites(Dimension selectorDimension) throws IOException {
		selView1 = new SwingSubPlayerSelector<T>(selectorDimension);
		selPres1 = new SubstitutePlayerSelector<>(selView1);
		selView1.setName("selector1");
		selView1.setPresenter(selPres1);

		selView2 = new SwingSubPlayerSelector<T>(selectorDimension);
		selPres2 = new SubstitutePlayerSelector<>(selView2);
		selView2.setName("selector2");
		selView2.setPresenter(selPres2);

		selView3 = new SwingSubPlayerSelector<T>(selectorDimension);
		selPres3 = new SubstitutePlayerSelector<>(selView3);
		selView3.setName("selector3");
		selView3.setPresenter(selPres3);

		sequenceDriver = FillableSwappableSequence.createSequence(List.of(selPres1, selPres2, selPres3));
		sequenceDriver.attachListener(this);
	}

	/**
	 * adds {@linkplain SwingSubPlayerSelector Substitute Selector View}s and swap
	 * buttons to {@code this} and lays them out using {@linkplain SpringLayout}
	 * constraints.
	 * <p>
	 * Requires children to have been instantiated.
	 * 
	 * @param layout      the {@code SpringLayout} layout manager for {@code this}
	 * @param parentWidth the width constraint for {@code this}
	 */
	private void addChildren(SpringLayout layout, Spring parentWidth) {
		add(selView1);
		add(selView2);
		add(selView3);
		add(swapSelectors1and2);
		add(swapSelectors2and3);

		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, selView1, Spring.scale(parentWidth, 1f / 6f),
				SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, selView2,
				Spring.scale(parentWidth, 3f / 6f), SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, selView3,
				Spring.scale(parentWidth, 5f / 6f), SpringLayout.WEST, this);

		// swap-buttons at 2/6 and 4/6 horizontally
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swapSelectors1and2,
				Spring.scale(parentWidth, 2f / 6f), SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swapSelectors2and3,
				Spring.scale(parentWidth, 4f / 6f), SpringLayout.WEST, this);

		// vertically center all
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, selView1, 0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, selView2, 0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, selView3, 0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, swapSelectors1and2, 0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, swapSelectors2and3, 0, SpringLayout.VERTICAL_CENTER, this);
	}

	/**
	 * disables swap buttons according to notifications from the
	 * {@linkplain FillableSwappableSequence Sequence driver}
	 */
	@Override
	public void becameEmpty(SubstitutePlayerSelector<T> emptiedGadget) {
		if (emptiedGadget == selPres3)
			swapSelectors2and3.setEnabled(false);
		else if (emptiedGadget == selPres2)
			swapSelectors1and2.setEnabled(false);
	}

	/**
	 * enables swap buttons according to notifications from the
	 * {@linkplain FillableSwappableSequence Sequence driver}
	 */
	@Override
	public void becameFilled(SubstitutePlayerSelector<T> filledGadget) {
		System.out.println("content added to a gadget!");
		if (filledGadget == selPres2)
			swapSelectors1and2.setEnabled(true);
		else if (filledGadget == selPres3)
			swapSelectors2and3.setEnabled(true);
	}

	/**
	 * {@return a {@code List} containing the three {@linkplain
	 * SubstitutePlayerSelector Substitute Selector Presenter} instances composed
	 * inside {@code this}}
	 */
	public List<SubstitutePlayerSelector<T>> getSubstituteSelectors() {
		return List.of(selPres1, selPres2, selPres3);
	}

	/*
	 * these setters are necessary for unit tests to install hard composites
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
			
			Dimension selectorDims = new Dimension(120, 225);
			SubstituteSelectorTriplet<Defender> chooser;
			try {
				chooser = new SubstituteSelectorTriplet<Defender>(false, selectorDims);
				CompetitiveOptionDealingGroup.initializeDealing(Set.copyOf(chooser.getSubstituteSelectors()),
						List.of(new Defender("Giorgio", "Chiellini"), new Defender("Gerard", "Piqué"),
								new Defender("Sergio", "Ramos"), new Defender("Thiago", "Silva"),
								new Defender("Virgil", "van Dijk")));
				frame.setContentPane(chooser);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

}
