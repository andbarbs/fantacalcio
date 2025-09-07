package swingViews;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import domainModel.Player;
import domainModel.Player.*;
import swingViews.SwingLineUpChooser.StarterLineUpChooser.LineUpScheme;
import swingViews.SwingLineUpChooser.StarterLineUpChooser.LineUpScheme.LineUpSchemeVisitor;
import swingViews.SwingLineUpChooser.StarterLineUpChooser.LineUpSchemes.*;
import swingViews.StarterLineUpChooser.StarterLineUpChooserWidget;

@SuppressWarnings("serial")
public class SwingStarterLineUpChooserWidget extends JPanel implements StarterLineUpChooserWidget {
	
	// injected dependencies
	private StarterLineUpChooserController controller;
	
	private Spring433Scheme panel433;
	private Spring343Scheme panel343;
	private Spring532Scheme panel532;

	private List<JPanel> goalieWidgets;
	private List<JPanel> defWidgets;
	private List<JPanel> midWidgets;
	private List<JPanel> forwWidgets;

	// static PNG dependency
	private static final String FIELD_PNG_PATH = "/gui_images/raster_field.png";
	
	public static Dimension eventualFieldDimension(Dimension availableWindow) throws IOException {
		BufferedImage origField = ImageIO.read(
				SwingStarterLineUpChooserWidget.class.getResourceAsStream(FIELD_PNG_PATH));
		int ow = origField.getWidth(), oh = origField.getHeight();
		double scale = Math.min(availableWindow.width / (double) ow, availableWindow.height / (double) oh);
		return new Dimension((int) (ow * scale), (int) (oh * scale));
	}

	// children refs
	private JPanel switcher, schemesHolder;
	private JLayeredPane layeredPane;
	private JRadioButton b433, b343, b532;
	
	// public instantiation point
	public SwingStarterLineUpChooserWidget(
			boolean isDesignTime,
			
			Dimension availableWindow,
			
			Spring433Scheme panel433, 
			Spring343Scheme panel343, 
			Spring532Scheme panel532,

			JPanel goalieSelectorWidget,
			
			JPanel defSelectorWidget1, 
			JPanel defSelectorWidget2, 
			JPanel defSelectorWidget3,
			JPanel defSelectorWidget4, 
			JPanel defSelectorWidget5,
			
			JPanel midSelectorWidget1, 
			JPanel midSelectorWidget2,
			JPanel midSelectorWidget3, 
			JPanel midSelectorWidget4,
			
			JPanel forwSelectorWidget1, 
			JPanel forwSelectorWidget2, 
			JPanel forwSelectorWidget3) throws IOException {

		this.panel433 = panel433;
		this.panel343 = panel343;
		this.panel532 = panel532;

		this.goalieWidgets = List.of(goalieSelectorWidget);
		this.defWidgets = List.of(defSelectorWidget1, defSelectorWidget2, defSelectorWidget3, 
				defSelectorWidget4, defSelectorWidget5);
		this.midWidgets = List.of(midSelectorWidget1, midSelectorWidget2, 
				midSelectorWidget3, midSelectorWidget4);
		this.forwWidgets = List.of(forwSelectorWidget1, forwSelectorWidget2, forwSelectorWidget3);

		initGraphics(availableWindow);

		// 6) adds all supported scheme panels to CardLayout
		schemesHolder.add(panel433, "433");
		schemesHolder.add(panel343, "343");
		schemesHolder.add(panel532, "532");

		// 7) wires scheme switch-up logic to the radios
		if (!isDesignTime) {
			b433.addActionListener(e -> controller.switchToScheme(new Scheme433()));
			b343.addActionListener(e -> controller.switchToScheme(new Scheme343()));
			b532.addActionListener(e -> controller.switchToScheme(new Scheme532()));
		}
	}

	private void initGraphics(Dimension availableWindow) throws IOException {
		
		// 1) creates a JLayeredPane with GridBag to overlay components
        layeredPane = new JLayeredPane();
        GridBagLayout gbl_layeredPane = new GridBagLayout();
        gbl_layeredPane.rowWeights = new double[]{1.0};
        gbl_layeredPane.rowHeights = new int[]{0};
        gbl_layeredPane.columnWeights = new double[]{1.0};
        gbl_layeredPane.columnWidths = new int[]{0};
        layeredPane.setLayout(gbl_layeredPane);
        
		// 2) creates the background label with the football field image, after scaling it
		BufferedImage origField = ImageIO.read(getClass().getResourceAsStream(FIELD_PNG_PATH));
		Dimension targetDim = eventualFieldDimension(availableWindow);
		JLabel background = new JLabel(new ImageIcon(origField.getScaledInstance(
				targetDim.width, targetDim.height, Image.SCALE_SMOOTH)));
		GridBagConstraints gbc_background = new GridBagConstraints();
		gbc_background.fill = GridBagConstraints.BOTH;
		gbc_background.gridx = 0;
		gbc_background.gridy = 0;
		layeredPane.add(background, gbc_background);
		layeredPane.setLayer(background, 0);
        
        // 3) creates a panel to hold scheme panels
		schemesHolder = new JPanel(new CardLayout());
		schemesHolder.setOpaque(false);
		GridBagConstraints gbc_selectorsPanel = new GridBagConstraints();
		gbc_selectorsPanel.fill = GridBagConstraints.BOTH;
		gbc_selectorsPanel.anchor = GridBagConstraints.NORTH;
		gbc_selectorsPanel.gridx = 0;
		gbc_selectorsPanel.gridy = 0;
		layeredPane.add(schemesHolder, gbc_selectorsPanel);
		layeredPane.setLayer(schemesHolder, 1);

		// 4) builds the radio-button strip for scheme selection
		switcher = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		b433 = new JRadioButton("4-3-3");
		b343 = new JRadioButton("3-4-3");
		b532 = new JRadioButton("5-3-2");
		ButtonGroup group = new ButtonGroup();
		group.add(b433);
		group.add(b343);
		group.add(b532);
		switcher.add(new JLabel("Scheme:"));
		switcher.add(b433);
		switcher.add(b343);
		switcher.add(b532);		

		// 5) composes everything at the root level
		setLayout(new BorderLayout());
		add(switcher, BorderLayout.NORTH);
		add(layeredPane, BorderLayout.CENTER);
	}

	/**
	 * provides this type's <b><i>private</i> design-time instantiation</b>,
	 * directly instantiating one {@linkplain Spring433Scheme} as a token scheme panel
	 * and 11 {@linkplain SwingSubPlayerSelector}s as token widgets.
	 * 
	 * @apiNote This constructor is intended <i>purely</i> for supporting the design
	 *          of this type's visual appearance in WindowBuilder
	 * @throws IOException if {@code SwingSubPlayerSelector}'s sizing-augmented
	 *                     instantiation fails
	 */
	SwingStarterLineUpChooserWidget() throws IOException {
		double widthRatio = 0.25;

		// calls initGraphics on a size acceptable for WB	
		Dimension screenSize = getToolkit().getScreenSize();
		Dimension availableWindow = new Dimension((int) (screenSize.width * widthRatio), screenSize.height);
		initGraphics(availableWindow);

		// 6) instantiates only one token scheme panel and adds it to CardLayout
		this.panel433 = new Spring433Scheme(true);
		schemesHolder.add(panel433, "433");

		// 7) instatiates and adds as many selectors as will fit the token scheme panel
		Dimension slotDims = SpringSchemePanel.recommendedSlotDimensions(
				eventualFieldDimension(availableWindow));

		panel433.getGoalieSlot().add(new SwingSubPlayerSelector<Goalkeeper>(slotDims));
		
		panel433.getDef1().add(new SwingSubPlayerSelector<Defender>(slotDims));
		panel433.getDef2().add(new SwingSubPlayerSelector<Defender>(slotDims));
		panel433.getDef3().add(new SwingSubPlayerSelector<Defender>(slotDims));
		panel433.getDef4().add(new SwingSubPlayerSelector<Defender>(slotDims));
		
		panel433.getMid1().add(new SwingSubPlayerSelector<Midfielder>(slotDims));
		panel433.getMid2().add(new SwingSubPlayerSelector<Midfielder>(slotDims));
		panel433.getMid3().add(new SwingSubPlayerSelector<Midfielder>(slotDims));
		
		panel433.getForw1().add(new SwingSubPlayerSelector<Forward>(slotDims));
		panel433.getForw2().add(new SwingSubPlayerSelector<Forward>(slotDims));
		panel433.getForw3().add(new SwingSubPlayerSelector<Forward>(slotDims));		

		// 8) sets explanatory borders
		setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "BorderLayout", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(184, 207, 229)));
		layeredPane.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2),
				"\"layeredPane\" - GridBagLayout", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));
		schemesHolder.setBorder(
				new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "\"schemesHolder\" - CardLayout",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));
		switcher.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "\"switcher\" - FlowLayout",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));

		// 9) sets JPanel's WB design-time dimensions
		setPreferredSize(getPreferredSize());
	}
	
	public void setController(StarterLineUpChooserController controller) {
		this.controller = controller;
	}



	private void switchToScheme(String targetSchemeKey) {
		
		// i) establish user choice of next scheme given CardLayout's String key
		SpringSchemePanel targetSchemePanel = 
				targetSchemeKey.equals("343") ? panel343 : targetSchemeKey.equals("532") ? panel532 : panel433;

		// ii) re-attach as many selectors as we have slots for each role
		moveWidgets(targetSchemePanel.getGoalieSlots(), goalieWidgets);
		moveWidgets(targetSchemePanel.getDefenderSlots(), defWidgets);
		moveWidgets(targetSchemePanel.getMidfielderSlots(), midWidgets);
		moveWidgets(targetSchemePanel.getForwardSlots(), forwWidgets);

		// iii) show the right card
		((CardLayout) schemesHolder.getLayout()).show(schemesHolder, targetSchemeKey);

		// iv) redraw
		schemesHolder.revalidate();
		schemesHolder.repaint();
	}

	private <T extends Player> void moveWidgets(List<JPanel> slots, List<JPanel> widgets) {
		IntStream.range(0, widgets.size()).forEach(i -> {
			JPanel widget = widgets.get(i);
			Optional.ofNullable(widget.getParent())
					.ifPresent(parent -> parent.remove(widget));
			if (i < slots.size()) {
				slots.get(i).add(widget);
			}
		});
	}

	@Override
	public void switchTo(LineUpScheme scheme) {
		scheme.accept(new LineUpSchemeVisitor() {
			
			@Override
			public void visit532(Scheme532 scheme532) {
				switchToScheme("532");
			}
			
			@Override
			public void visit433(Scheme433 scheme433) {
				switchToScheme("433");
			}
			
			@Override
			public void visit343(Scheme343 scheme343) {
				switchToScheme("343");
			}
		});
	}
}
