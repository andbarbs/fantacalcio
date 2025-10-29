package gui.lineup.triplet;

import javax.swing.JPanel;
import javax.swing.Spring;

import java.awt.Dimension;
import java.beans.Beans;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import gui.lineup.dealing.CompetitiveOptionDealingGroup;
import gui.lineup.selectors.SubstitutePlayerSelector;
import gui.lineup.selectors.SwingSubPlayerSelector;
import gui.lineup.sequence.FillableSwappableSequence;

import javax.swing.ImageIcon;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.TitledBorder;

import domain.Player;
import domain.Player.Defender;
import domain.Player.Forward;

import javax.swing.border.LineBorder;
import java.awt.Color;

// TODO: are JButton icons considered raster components? If so, address their scaling!

/**
 * a {@code JPanel} responsible for implementing
 * {@link FillableSwappableTripletWidget} through composition of {@code JPanel}
 * widgets for members of the {@link FillableSwappableSequence}.
 * 
 * @implNote sizing of this {@code JPanel} is computed as a function of member
 *           widget's {@linkplain JPanel#getPreferredSize()} dimensions. See the
 *           {@linkplain SwingFillableSwappableTripletWidget#SwingFillableSwappableTripletWidget (boolean, JPanel, JPanel, JPanel)
 *           public constructor}
 */
@SuppressWarnings("serial")
public class SwingFillableSwappableTripletWidget extends JPanel implements FillableSwappableTripletWidget {
	
	// Controller ref
	private FillableSwappableTripletController controller;
	
	public void setController(FillableSwappableTripletController controller) {
		this.controller = controller;
	}

	// graphical appearance
	private static final float TRIPLET_W_OVER_VIEW_W = 5f, TRIPLET_H_OVER_VIEW_H = 1.2f;
	private JButton swapMembers1and2, swapMembers2and3;	
	
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
	 * provides {@linkplain SwingFillableSwappableTripletWidget}'s <b>public</b>
	 * instantiation point.
	 * 
	 * <p><h1>Relative sizing</h1>
	 * Widgets can have varying dimensions. Sizing for the
	 * {@code SwingFillableSwappableTriplet} is computed as a function of widgets'
	 * {@linkplain JPanel#getPreferredSize()} dimensions so as to accommodate all of
	 * them.</p>
	 * 
	 * @param isDesignTime selects
	 *                     <ul>
	 *                     <li><b><i>design-time</i></b> instantiation when
	 *                     {@code true}
	 *                     <li><b><i>runtime</i></b> instantiation when
	 *                     {@code false}
	 *                     </ul>
	 *                     <p>
	 *                     Graphical clients should consider passing
	 *                     {@linkplain Beans#isDesignTime()}
	 * @param fillable1    the first sequence member
	 * @param widget1      the first member's widget
	 * @param fillable2    the second sequence member
	 * @param widget2      the second member's widget
	 * @param fillable3    the third sequence member
	 * @param widget3      the third member's widget
	 */
	public SwingFillableSwappableTripletWidget(boolean isDesignTime, 
					JPanel widget1,
					JPanel widget2, 
					JPanel widget3) {

		// creates springs for the Panel based on widgets' preferred dimensions
		Dimension maxWidgetSize = new Dimension(
				Stream.of(widget1, widget2, widget3).map(widget -> widget.getPreferredSize().width).max(Math::max).get(),
				Stream.of(widget1, widget2, widget3).map(widget -> widget.getPreferredSize().height).max(Math::max).get());
		Spring panelWidth = Spring.constant((int)(maxWidgetSize.width * TRIPLET_W_OVER_VIEW_W));
		Spring panelHeight = Spring.constant((int)(maxWidgetSize.height * TRIPLET_H_OVER_VIEW_H));

		// sets visual appearance
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		layout.getConstraints(this).setWidth(panelWidth);
		layout.getConstraints(this).setHeight(panelHeight);

		addAndLayoutSwapButtons(layout, panelWidth);
		addAndLayoutWidgets(layout, panelWidth, 
				Objects.requireNonNull(widget1), 
				Objects.requireNonNull(widget2), 
				Objects.requireNonNull(widget3));
	}

	/**
	 * provides this type's <b><i>private</i> design-time instantiation</b>,
	 * directly instantiating {@linkplain SwingSubPlayerSelector} as a token widget.
	 * 
	 * @apiNote This constructor is intended <i>purely</i> for supporting the design
	 *          of this type's visual appearance in WindowBuilder
	 * @throws IOException if {@code SwingSubPlayerSelector}'s sizing-augmented
	 *                     instantiation fails
	 */
	SwingFillableSwappableTripletWidget() throws IOException {

		// chooses arbitrary dimensions for the triplet
		Dimension selectorDims = new Dimension(120, 225); // appropriate dims for rendering a selector
		Dimension tripletDims = new Dimension(
				(int) (selectorDims.width * TRIPLET_W_OVER_VIEW_W),
				(int) (selectorDims.height * TRIPLET_H_OVER_VIEW_H)); 
		setPreferredSize(tripletDims);

		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		Spring parentWidth = Spring.constant(tripletDims.width);

		addAndLayoutSwapButtons(layout, parentWidth);

		SwingSubPlayerSelector<Forward> selectorView1 = new SwingSubPlayerSelector<Forward>(selectorDims);
		SwingSubPlayerSelector<Forward> selectorView2 = new SwingSubPlayerSelector<Forward>(selectorDims);
		SwingSubPlayerSelector<Forward> selectorView3 = new SwingSubPlayerSelector<Forward>(selectorDims);
		
		addAndLayoutWidgets(layout, parentWidth, selectorView1, selectorView2, selectorView3);

		// adds explanatory borders
		setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "SpringLayout", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(184, 207, 229)));
		selectorView1.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "Selector1",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));
		selectorView2.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "Selector2",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));
		selectorView3.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "Selector3",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));
	}
	
	/**
	 * instantiates, adds Listeners to, adds to this, lays out and initially
	 * disables the two swap buttons
	 * 
	 * @param layout      the {@code SpringLayout} layout manager for {@code this}
	 * @param parentWidth the width constraint for {@code this}
	 */
	private void addAndLayoutSwapButtons(SpringLayout layout, Spring parentWidth) {
		swapMembers1and2 = new JButton();
		swapMembers1and2.setName("swap1_2");
		swapMembers1and2.setMargin(new Insets(2, 2, 2, 2));
		swapMembers1and2.setIcon(new ImageIcon(getClass().getResource("/gui_icons/swap_verysmall.png")));
		swapMembers1and2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// System.out.println("asking driver to swap 1 and 2");
				System.out.println("**************** FLAKINESS POINT A *****************");
				System.out.println("SwingFillableSwappableTripletWidget's MouseAdapter: ");
				System.out.println("              about to call controller.swapFirstPair");
				System.out.println("****************************************************");
				controller.swapFirstPair();
			}
		});

		swapMembers2and3 = new JButton();
		swapMembers2and3.setName("swap2_3");
		swapMembers2and3.setMargin(new Insets(2, 2, 2, 2));
		swapMembers2and3.setIcon(new ImageIcon(getClass().getResource("/gui_icons/swap_verysmall.png")));
		swapMembers2and3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// System.out.println("asking driver to swap 2 and 3");
				System.out.println("**************** FLAKINESS POINT B *****************");
				System.out.println("SwingFillableSwappableTripletWidget's MouseAdapter: ");
				System.out.println("             about to call controller.swapSecondPair");
				System.out.println("****************************************************");
				controller.swapSecondPair();
			}
		});

		swapMembers1and2.setEnabled(false);
		swapMembers2and3.setEnabled(false);
		
		add(swapMembers1and2);
		add(swapMembers2and3);

		// swap-buttons at 2/6 and 4/6 horizontally and centered vertically
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swapMembers1and2, Spring.scale(parentWidth, 2f / 6f),
				SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, swapMembers2and3, Spring.scale(parentWidth, 4f / 6f),
				SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, swapMembers1and2, 0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, swapMembers2and3, 0, SpringLayout.VERTICAL_CENTER, this);
	}

	/**
	 * adds, sets names to and lays out widgets using {@linkplain SpringLayout} constraints.
	 * <p>
	 * Requires widgets to have been instantiated.
	 *
	 * @param layout      the {@code SpringLayout} layout manager for {@code this}
	 * @param parentWidth the width constraint for {@code this}
	 * @param widget1       the leftmost widget
	 * @param widget2       the center widget
	 * @param widget3       the rightmost widget
	 */
	private void addAndLayoutWidgets(SpringLayout layout, Spring parentWidth, 
			JPanel widget1, JPanel widget2, JPanel widget3) {
		add(widget1);
		add(widget2);
		add(widget3);
		
		widget1.setName("widget1");
		widget2.setName("widget2");
		widget3.setName("widget3");
		
		// horizontally places widgets at 1/6, 3/6 and 5/6
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, widget1, Spring.scale(parentWidth, 1f / 6f),
				SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, widget2, Spring.scale(parentWidth, 3f / 6f),
				SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, widget3, Spring.scale(parentWidth, 5f / 6f),
				SpringLayout.WEST, this);

		// vertically center all
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, widget1, 0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, widget2, 0, SpringLayout.VERTICAL_CENTER, this);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, widget3, 0, SpringLayout.VERTICAL_CENTER, this);
	}

	

	@Override
	public void setSwappingFirstPair(boolean enabled) {
		swapMembers1and2.setEnabled(enabled);
	}

	@Override
	public void setSwappingSecondPair(boolean enabled) {
		swapMembers2and3.setEnabled(enabled);
	}

	@Override
	public void resetSwapping() {
		swapMembers1and2.setEnabled(false);
		swapMembers2and3.setEnabled(false);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("substitute chooser demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			Dimension selectorDims = new Dimension(120, 225);
			SwingSubPlayerSelector<Defender> view1 = new SwingSubPlayerSelector<Defender>(selectorDims);
			SwingSubPlayerSelector<Defender> view2 = new SwingSubPlayerSelector<Defender>(selectorDims);
			SwingSubPlayerSelector<Defender> view3 = new SwingSubPlayerSelector<Defender>(selectorDims);
			
			SubstitutePlayerSelector<Defender> selPres1 = new SubstitutePlayerSelector<Defender>(view1);
			SubstitutePlayerSelector<Defender> selPres2 = new SubstitutePlayerSelector<Defender>(view2);
			SubstitutePlayerSelector<Defender> selPres3 = new SubstitutePlayerSelector<Defender>(view3);
			
			view1.setController(selPres1);
			view2.setController(selPres2);
			view3.setController(selPres3);
			
			SwingFillableSwappableTripletWidget tripletWidget = 
					new SwingFillableSwappableTripletWidget(false, view1, view2, view3);
			CompetitiveOptionDealingGroup.initializeDealing(Set.of(selPres1, selPres2, selPres3),
					List.of(new Defender("Giorgio", "Chiellini", Player.Club.ATALANTA), new Defender("Gerard", "Piqué", Player.Club.ATALANTA),
							new Defender("Sergio", "Ramos", Player.Club.ATALANTA), new Defender("Thiago", "Silva", Player.Club.ATALANTA),
							new Defender("Virgil", "van Dijk", Player.Club.ATALANTA)));
			frame.setContentPane(tripletWidget);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

}
