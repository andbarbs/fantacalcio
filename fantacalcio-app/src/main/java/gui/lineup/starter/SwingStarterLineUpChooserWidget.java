package gui.lineup.starter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import domainModel.Player;
import domainModel.Player.*;
import domainModel.Scheme;
import domainModel.scheme.Scheme433;
import gui.lineup.dealing.CompetitiveOptionDealingGroup;
import gui.lineup.selectors.StarterPlayerSelector;
import gui.lineup.selectors.SwingSubPlayerSelector;
import gui.utils.schemes.Spring343Scheme;
import gui.utils.schemes.Spring433Scheme;
import gui.utils.schemes.Spring532Scheme;
import gui.utils.schemes.SpringSchemePanel;

@SuppressWarnings("serial")
public class SwingStarterLineUpChooserWidget extends JPanel implements StarterLineUpChooserWidget {
	
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
	private List<JRadioButton> radios = new ArrayList<>();

	// injected dependencies
	private List<SpringSchemePanel> schemePanels;
	private List<JPanel> goalieSelectorWidget;
	private List<JPanel> defSelectorWidgets;
	private List<JPanel> midSelectorWidgets;
	private List<JPanel> forwSelectorWidgets;
	
	// Controller ref
	private StarterLineUpChooserController controller;
	
	public void setController(StarterLineUpChooserController controller) {
		this.controller = controller;
	}
	
	// public instantiation point
	public SwingStarterLineUpChooserWidget(
			boolean isDesignTime,			
			Dimension availableWindow,			
			List<SpringSchemePanel> schemePanels,
			JPanel goalieSelectorWidget,			
			List<JPanel> defSelectorWidgets,			
			List<JPanel> midSelectorWidgets,			
			List<JPanel> forwSelectorWidgets) throws IOException {

		this.schemePanels = Objects.requireNonNull(schemePanels).stream().map(Objects::requireNonNull)
				.collect(Collectors.toList());
		this.goalieSelectorWidget = List.of(Objects.requireNonNull(goalieSelectorWidget));
		this.defSelectorWidgets = Objects.requireNonNull(defSelectorWidgets).stream().map(Objects::requireNonNull)
				.collect(Collectors.toList());
		this.midSelectorWidgets = Objects.requireNonNull(midSelectorWidgets).stream().map(Objects::requireNonNull)
				.collect(Collectors.toList());
		this.forwSelectorWidgets = Objects.requireNonNull(forwSelectorWidgets).stream().map(Objects::requireNonNull)
				.collect(Collectors.toList());
		
		initGraphics(availableWindow);

		// 6) adds all supported scheme panels to CardLayout
		this.schemePanels.forEach(schemePanel -> this.schemesHolder.add(schemePanel, schemePanel.scheme().toString()));

		// 7) wires scheme switch-up logic to the radios
		if (!isDesignTime) {
			IntStream.range(0, schemePanels.size()).forEach(
					i -> radios.get(i).addActionListener(e -> controller.switchToScheme(schemePanels.get(i).scheme())));
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
		switcher.add(new JLabel("Scheme:"));
		ButtonGroup group = new ButtonGroup();
		schemePanels.forEach(schemePanel -> {
			JRadioButton radioButton = new JRadioButton(schemePanel.scheme().toString());
			radios.add(radioButton);
			group.add(radioButton);
			switcher.add(radioButton);
		});
		
		// 5) composes everything at the root level
		setLayout(new BorderLayout());
		add(switcher, BorderLayout.NORTH);
		add(layeredPane, BorderLayout.CENTER);
	}

	/**
	 * provides this type's <b><i>private</i> design-time instantiation</b>, using
	 * direct instantiation in place of DI for the aim of aiding in design.
	 * 
	 * <p>
	 * This constructor also attaches {@link StarterPlayerSelector}s to the
	 * {@link Scheme433Panel} to demonstrate this type's look when operational.
	 * 
	 * <p>
	 * <h1>Limitations</h1> Due to WindowBuilder's parsing limitations on
	 * first-level constructors
	 * <ul>
	 * <li>the radio-button strip is improperly filled
	 * <li>{@link SprinhSchemePanel} slot attachment must be performed with single
	 * {@link JPanel#add} calls
	 * </ul>
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
		Spring433Scheme panel433 = new Spring433Scheme(true);
		this.schemesHolder.add(panel433, panel433.scheme().toString());

		// 7) instatiates and adds as many selectors as will fit the token scheme panel,
		//    effectively simulating a call switchTo(Scheme433.INSTANCE)
		Dimension slotDims = SpringSchemePanel.recommendedSlotDimensions(
				eventualFieldDimension(availableWindow));	
		
		panel433.getGoalieSlot().add((new SwingSubPlayerSelector<Goalkeeper>(slotDims)));
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

	private void switchToScheme(String targetSchemeKey) {
		
		// i) establish user choice of next scheme given CardLayout's String key
		SpringSchemePanel targetSchemePanel = schemePanels.stream()
				.filter(schemePanel -> schemePanel.scheme().toString().equals(targetSchemeKey))
				.findFirst().get();

		// ii) re-attach as many selectors as we have slots for each role
		moveWidgets(targetSchemePanel.getGoalieSlots(), goalieSelectorWidget);
		moveWidgets(targetSchemePanel.getDefenderSlots(), defSelectorWidgets);
		moveWidgets(targetSchemePanel.getMidfielderSlots(), midSelectorWidgets);
		moveWidgets(targetSchemePanel.getForwardSlots(), forwSelectorWidgets);

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
	public void switchTo(Scheme newScheme) {		
		radios.stream()
				.filter(radio -> radio.getText().equals(newScheme.toString()))
				.findFirst().ifPresentOrElse(radioButton -> {
					radioButton.setSelected(true);
					switchToScheme(radioButton.getText());
				}, () -> {
					String format = "SwingStarterLineUpChooserWidget.switchTo: Unsatisfiable Request\n" +
							"requested Scheme has %d %ss, this composes only %d widgets for %s";
					if (newScheme.getNumDefenders() > defSelectorWidgets.size())
						throw new IllegalArgumentException(String.format(format, 
								newScheme.getNumDefenders(), Defender.class.getSimpleName(), 
								defSelectorWidgets.size(), Defender.class.getSimpleName()));
					if (newScheme.getNumMidfielders() > midSelectorWidgets.size())
						throw new IllegalArgumentException(String.format(format, 
								newScheme.getNumMidfielders(), Midfielder.class.getSimpleName(), 
								midSelectorWidgets.size(), Midfielder.class.getSimpleName()));
					if (newScheme.getNumForwards() > forwSelectorWidgets.size())
						throw new IllegalArgumentException(String.format(format, 
								newScheme.getNumForwards(), Forward.class.getSimpleName(), 
								forwSelectorWidgets.size(), Forward.class.getSimpleName()));
				});
	}
	

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("starter chooser demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			try {
				Dimension screenSize = frame.getToolkit().getScreenSize();
				Dimension availableWindow = new Dimension((int) (screenSize.width * 0.3), screenSize.height);
				Dimension selectorDims = SpringSchemePanel.recommendedSlotDimensions(
						SwingStarterLineUpChooserWidget.eventualFieldDimension(availableWindow));
				
				// I) initializes dependencies
				SwingSubPlayerSelector<Goalkeeper> goalieView = new SwingSubPlayerSelector<Goalkeeper>(selectorDims);
				StarterPlayerSelector<Goalkeeper> goalieSelector = new StarterPlayerSelector<>(goalieView);
				goalieView.setController(goalieSelector);
				
				SwingSubPlayerSelector<Defender> defView1 = new SwingSubPlayerSelector<Defender>(selectorDims),
						defView2 = new SwingSubPlayerSelector<Defender>(selectorDims),
								defView3 = new SwingSubPlayerSelector<Defender>(selectorDims),
										defView4 = new SwingSubPlayerSelector<Defender>(selectorDims),
												defView5 = new SwingSubPlayerSelector<Defender>(selectorDims);
				StarterPlayerSelector<Defender> defPres1 = new StarterPlayerSelector<Defender>(defView1),
						defPres2 = new StarterPlayerSelector<Defender>(defView2),
								defPres3 = new StarterPlayerSelector<Defender>(defView3),
										defPres4 = new StarterPlayerSelector<Defender>(defView4),
												defPres5 = new StarterPlayerSelector<Defender>(defView5);
				defView1.setController(defPres1);
				defView2.setController(defPres2);
				defView3.setController(defPres3);
				defView4.setController(defPres4);
				defView5.setController(defPres5);
				
				SwingSubPlayerSelector<Midfielder> midView1 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
						midView2 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
								midView3 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
										midView4 = new SwingSubPlayerSelector<Midfielder>(selectorDims);
				StarterPlayerSelector<Midfielder> midPres1 = new StarterPlayerSelector<Midfielder>(midView1),
						midPres2 = new StarterPlayerSelector<Midfielder>(midView2),
								midPres3 = new StarterPlayerSelector<Midfielder>(midView3),
										midPres4 = new StarterPlayerSelector<Midfielder>(midView4);
				midView1.setController(midPres1);
				midView2.setController(midPres2);
				midView3.setController(midPres3);
				midView4.setController(midPres4);
				
				SwingSubPlayerSelector<Forward> forwView1 = new SwingSubPlayerSelector<Forward>(selectorDims),
						forwView2 = new SwingSubPlayerSelector<Forward>(selectorDims),
								forwView3 = new SwingSubPlayerSelector<Forward>(selectorDims);
				StarterPlayerSelector<Forward> forwPres1 = new StarterPlayerSelector<Forward>(forwView1),
						forwPres2 = new StarterPlayerSelector<Forward>(forwView2),
								forwPres3 = new StarterPlayerSelector<Forward>(forwView3);
				forwView1.setController(forwPres1);
				forwView2.setController(forwPres2);
				forwView3.setController(forwPres3);
				
				// II) initializes competition
				CompetitiveOptionDealingGroup.initializeDealing(
						Set.of(goalieSelector), 
						List.of(new Goalkeeper("Gianluigi", "Buffon")));
				CompetitiveOptionDealingGroup.initializeDealing(
						Set.of(defPres1, defPres2, defPres3, defPres4, defPres5), 
						List.of(new Defender("Paolo", "Maldini"), 
								new Defender("Franco", "Baresi"), 
								new Defender("Alessandro", "Nesta"), 
								new Defender("Giorgio", "Chiellini"), 
								new Defender("Leonardo", "Bonucci")));
				CompetitiveOptionDealingGroup.initializeDealing(
						Set.of(midPres1, midPres2, midPres3, midPres4), 
						List.of(new Midfielder("Andrea", "Pirlo"), 
								new Midfielder("Daniele", "De Rossi"), 
								new Midfielder("Marco", "Verratti"), 
								new Midfielder("Claudio", "Marchisio")));
				CompetitiveOptionDealingGroup.initializeDealing(
						Set.of(forwPres1, forwPres2, forwPres3), 
						List.of(new Forward("Roberto", "Baggio"), 
								new Forward("Francesco", "Totti"), 
								new Forward("Alessandro", "Del Piero"), 
								new Forward("Lorenzo", "Insigne")));
				
				// III) instantiates Chooser
				StarterLineUpChooser controller = new StarterLineUpChooser(
						goalieSelector, 
						List.of(defPres1, defPres2, defPres3, defPres4, defPres5), 
						List.of(midPres1, midPres2, midPres3, midPres4), 
						List.of(forwPres1, forwPres2, forwPres3));
				
				controller.setEntryDefConsumer(sel -> {});
				controller.setEntryMidConsumer(sel -> {});
				controller.setEntryForwConsumer(sel -> {});
				controller.setExitDefConsumer(sel -> sel.setSelection(Optional.empty()));
				controller.setExitMidConsumer(sel -> sel.setSelection(Optional.empty()));
				controller.setExitForwConsumer(sel -> sel.setSelection(Optional.empty()));
				
				SwingStarterLineUpChooserWidget widget = new SwingStarterLineUpChooserWidget(
						false, 
						availableWindow, 						
						List.of(new Spring433Scheme(false), new Spring343Scheme(false), new Spring532Scheme(false)), 
						goalieView, 
						List.of(defView1, defView2, defView3, defView4, defView5), 
						List.of(midView1, midView2, midView3, midView4), 						
						List.of(forwView1, forwView2, forwView3));
				
				controller.setWidget(widget);
				widget.setController(controller);
				
				controller.switchToScheme(Scheme433.INSTANCE);
				
				frame.setContentPane(widget);		
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
