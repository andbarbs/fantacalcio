package swingViews;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
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

@SuppressWarnings("serial")
public class SwingStarterLineUpChooser extends JPanel {
	
	public static interface Selector<T> {
		public Optional<T> getSelection();
		public void setSelection(Optional<T> option);
	}

	// inner type for the MVP Pres + View pair
	private static class SelectorWidgetPair<P extends Player> {
		
		Selector<P> selector;
		JPanel widget;
		
		SelectorWidgetPair(Selector<P> presenter, JPanel widget) {
			this.selector = presenter;
			this.widget = widget;			
		}		
	}
	
	// injected dependencies
	private Spring433Scheme panel433;
	private Spring343Scheme panel343;
	private Spring532Scheme panel532;

	private List<SelectorWidgetPair<Goalkeeper>> goalieSelectorPairs;
	private List<SelectorWidgetPair<Defender>> defSelectorPairs;
	private List<SelectorWidgetPair<Midfielder>> midSelectorPairs;
	private List<SelectorWidgetPair<Forward>> forwSelectorPairs;

	// static PNG dependency
	private static final String FIELD_PNG_PATH = "/gui_images/raster_field.png";
	
	public static Dimension eventualFieldDimension(Dimension availableWindow) throws IOException {
		BufferedImage origField = ImageIO.read(
				SwingStarterLineUpChooser.class.getResourceAsStream(FIELD_PNG_PATH));
		int ow = origField.getWidth(), oh = origField.getHeight();
		double scale = Math.min(availableWindow.width / (double) ow, availableWindow.height / (double) oh);
		return new Dimension((int) (ow * scale), (int) (oh * scale));
	}

	// children refs
	private JPanel switcher, schemesHolder;
	private JLayeredPane layeredPane;
	private JRadioButton b433, b343, b532;
	private Consumer<Selector<? extends Player>> onSelectorExlcluded;	
	
	// public instantiation point
	public SwingStarterLineUpChooser(			
			Dimension availableWindow,
			
			Spring433Scheme panel433, Spring343Scheme panel343, Spring532Scheme panel532,

			Selector<Goalkeeper> goalieSelector, JPanel goalieSelectorWidget,
			
			Selector<Defender> defSelector1, JPanel defSelectorWidget1,
			Selector<Defender> defSelector2, JPanel defSelectorWidget2,
			Selector<Defender> defSelector3, JPanel defSelectorWidget3,
			Selector<Defender> defSelector4, JPanel defSelectorWidget4,
			Selector<Defender> defSelector5, JPanel defSelectorWidget5,
			
			Selector<Midfielder> midSelector1, JPanel midSelectorWidget1,
			Selector<Midfielder> midSelector2, JPanel midSelectorWidget2,
			Selector<Midfielder> midSelector3, JPanel midSelectorWidget3,
			Selector<Midfielder> midSelector4, JPanel midSelectorWidget4,
			
			Selector<Forward> forwSelector1, JPanel forwSelectorWidget1,
			Selector<Forward> forwSelector2, JPanel forwSelectorWidget2,
			Selector<Forward> forwSelector3, JPanel forwSelectorWidget3,			
			
			Consumer<Selector<? extends Player>> onSelectorExlcluded) throws IOException {

		this.panel433 = panel433;
		this.panel343 = panel343;
		this.panel532 = panel532;
		this.onSelectorExlcluded = onSelectorExlcluded;

		this.goalieSelectorPairs = List.of(
				new SelectorWidgetPair<Goalkeeper>(goalieSelector, goalieSelectorWidget));
		this.defSelectorPairs = List.of(
				new SelectorWidgetPair<Defender>(defSelector1, defSelectorWidget1),
				new SelectorWidgetPair<Defender>(defSelector2, defSelectorWidget2),
				new SelectorWidgetPair<Defender>(defSelector3, defSelectorWidget3),
				new SelectorWidgetPair<Defender>(defSelector4, defSelectorWidget4),
				new SelectorWidgetPair<Defender>(defSelector5, defSelectorWidget5));
		this.midSelectorPairs = List.of(
				new SelectorWidgetPair<Midfielder>(midSelector1, midSelectorWidget1),
				new SelectorWidgetPair<Midfielder>(midSelector2, midSelectorWidget2),
				new SelectorWidgetPair<Midfielder>(midSelector3, midSelectorWidget3),
				new SelectorWidgetPair<Midfielder>(midSelector4, midSelectorWidget4));
		this.forwSelectorPairs = List.of(
				new SelectorWidgetPair<Forward>(forwSelector1, forwSelectorWidget1),
				new SelectorWidgetPair<Forward>(forwSelector2, forwSelectorWidget2),
				new SelectorWidgetPair<Forward>(forwSelector3, forwSelectorWidget3));

		initGraphics(availableWindow);

		// 6) adds all supported scheme panels to CardLayout
		schemesHolder.add(panel433, "433");
		schemesHolder.add(panel343, "343");
		schemesHolder.add(panel532, "532");

		// 7) wires scheme switch-up logic to the radios
		ActionListener flip = e -> {
			switchToScheme(b433.isSelected() ? "433" : b343.isSelected() ? "343" : "532");
		};
		b433.addActionListener(flip);
		b343.addActionListener(flip);
		b532.addActionListener(flip);
		
		// 8) switch to default scheme
		switchToScheme("433");
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
		b433 = new JRadioButton("4-3-3", true);
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
	SwingStarterLineUpChooser() throws IOException {

		// calls initGraphics on the PNG's native dimensions
		BufferedImage fieldimage = ImageIO.read(getClass().getResourceAsStream(FIELD_PNG_PATH));
		Dimension unscaledDim = new Dimension(fieldimage.getWidth(), fieldimage.getHeight());
		initGraphics(unscaledDim);

		// 6) instantiates only one token scheme panel and adds it to CardLayout
		this.panel433 = new Spring433Scheme(true);
		schemesHolder.add(panel433, "433");

		// 7) instatiates and adds as many selectors as will fit the token scheme panel
		Dimension availableWindow = SpringSchemePanel.recommendedSlotDimensions(unscaledDim);
		Dimension reducedAvailableWindow = // ensures selectors actually fit inside slots after insets
				new Dimension(availableWindow.width - 5, availableWindow.height - 10);

		panel433.getGoalieSlot().add(new SwingSubPlayerSelector<Goalkeeper>(reducedAvailableWindow));
		
		panel433.getDef1().add(new SwingSubPlayerSelector<Defender>(reducedAvailableWindow));
		panel433.getDef2().add(new SwingSubPlayerSelector<Defender>(reducedAvailableWindow));
		panel433.getDef3().add(new SwingSubPlayerSelector<Defender>(reducedAvailableWindow));
		panel433.getDef4().add(new SwingSubPlayerSelector<Defender>(reducedAvailableWindow));
		
		panel433.getMid1().add(new SwingSubPlayerSelector<Midfielder>(reducedAvailableWindow));
		panel433.getMid2().add(new SwingSubPlayerSelector<Midfielder>(reducedAvailableWindow));
		panel433.getMid3().add(new SwingSubPlayerSelector<Midfielder>(reducedAvailableWindow));
		
		panel433.getForw1().add(new SwingSubPlayerSelector<Forward>(reducedAvailableWindow));
		panel433.getForw2().add(new SwingSubPlayerSelector<Forward>(reducedAvailableWindow));
		panel433.getForw3().add(new SwingSubPlayerSelector<Forward>(reducedAvailableWindow));		

		// 8) sets explanatory borders
		setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "BorderLayout", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(184, 207, 229)));
		layeredPane.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2),
				"\"layered\" - GridBagLayout", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));
		schemesHolder.setBorder(
				new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "\"schemesHolder\" - CardLayout",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));
		switcher.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229), 2), "\"switcher\" - FlowLayout",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(184, 207, 229)));

		// 9) sets private design-time dimensions
		setPreferredSize(layeredPane.getPreferredSize());
	}

	private void switchToScheme(String targetSchemeKey) {

//		// i) remove all selectors from old parent slots
//		Stream.concat(
//				Stream.concat(goalieSelectorPairs.stream(), defSelectorPairs.stream()),
//				Stream.concat(midSelectorPairs.stream(), forwSelectorPairs.stream()))
//				.forEach(pair -> Optional.ofNullable(pair.widget.getParent())
//						.ifPresent(parent -> {
//							parent.remove(pair.widget);
//							onSelectorExlcluded.accept(pair.presenter);
//						}));

		// ii) establish user choice of next scheme given CardLayout's String key
		SpringSchemePanel targetSchemePanel = 
				targetSchemeKey.equals("343") ? panel343 : targetSchemeKey.equals("532") ? panel532 : panel433;

		// iii) re-attach as many selectors as we have slots for each role
		System.out.println("attaching goalie");
		attachWidgets(targetSchemePanel.getGoalieSlots(), goalieSelectorPairs);
		System.out.println("attaching defs");
		attachWidgets(targetSchemePanel.getDefenderSlots(), defSelectorPairs);
		System.out.println("attaching mids");
		attachWidgets(targetSchemePanel.getMidfielderSlots(), midSelectorPairs);
		System.out.println("attaching forwds");
		attachWidgets(targetSchemePanel.getForwardSlots(), forwSelectorPairs);

		// iv) show the right card
		((CardLayout) schemesHolder.getLayout()).show(schemesHolder, targetSchemeKey);

		// v) redraw
		schemesHolder.revalidate();
		schemesHolder.repaint();
	}

	private <T extends Player> void attachWidgets(List<JPanel> slots, List<SelectorWidgetPair<T>> pairs) {
//		int i = 0;

//		// add selectors to slots: there could be more selectors than slots
//		for (; i < slots.size(); i++) {
//			slots.get(i).add(pairs.get(i).widget);
//		}
//
//		// empty out selectors that were previously shown but will no longer be
//		for (; i < pairs.size(); i++) {
//			if (pairs.get(i).widget.getParent() != null) {
//				pairs.get(i).widget
//				onSelectorExlcluded.accept(pairs.get(i).presenter);
//			}
//		}
		
		IntStream.range(0, pairs.size()).forEach(i -> {
			Optional.ofNullable(pairs.get(i).widget.getParent()).ifPresent(parent -> {
				parent.remove(pairs.get(i).widget);
				if (i >= slots.size()) {
					System.out.printf("excluding selector %d of type %s with choice: %s\n", 
							i, pairs.get(i).selector.getClass(),
							pairs.get(i).selector.getSelection());
					onSelectorExlcluded.accept(pairs.get(i).selector);
				}
			});
			if (i < slots.size()) {
				slots.get(i).add(pairs.get(i).widget);
			}
		});
		
//		for (int j = 0; j < pairs.size(); j++) {
//			if (pairs.get(j).widget.getParent() != null) {
//				pairs.get(j).widget.getParent().remove(pairs.get(j).widget);
//				if (j >= slots.size()) {
//					System.out.printf("excluding selector %d of type %s with choice: %s\n", 
//							j, pairs.get(j).selector.getClass(),
//							pairs.get(j).selector.getSelection());
//					onSelectorExlcluded.accept(pairs.get(j).selector);
//				}
//			}
//			if (j < slots.size()) {
//				slots.get(j).add(pairs.get(j).widget);
//			}
//		}
		
		
		
		
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("starter chooser demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			try {
				Dimension screenSize = frame.getToolkit().getScreenSize();
				Dimension availableWindow = new Dimension((int) (screenSize.width * 0.3), screenSize.height);
				Dimension selectorDims = SpringSchemePanel.recommendedSlotDimensions(
						SwingStarterLineUpChooser.eventualFieldDimension(availableWindow));
				
				// I) initializes dependencies
				SwingSubPlayerSelector<Goalkeeper> goalieView = new SwingSubPlayerSelector<Goalkeeper>(selectorDims);
				StarterPlayerSelector<Goalkeeper> goaliePresenter = new StarterPlayerSelector<>(goalieView);
				goalieView.setPresenter(goaliePresenter);
				
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
				defView1.setPresenter(defPres1);
				defView2.setPresenter(defPres2);
				defView3.setPresenter(defPres3);
				defView4.setPresenter(defPres4);
				defView5.setPresenter(defPres5);
				
				SwingSubPlayerSelector<Midfielder> midView1 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
						midView2 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
								midView3 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
										midView4 = new SwingSubPlayerSelector<Midfielder>(selectorDims);
				StarterPlayerSelector<Midfielder> midPres1 = new StarterPlayerSelector<Midfielder>(midView1),
						midPres2 = new StarterPlayerSelector<Midfielder>(midView2),
								midPres3 = new StarterPlayerSelector<Midfielder>(midView3),
										midPres4 = new StarterPlayerSelector<Midfielder>(midView4);
				midView1.setPresenter(midPres1);
				midView2.setPresenter(midPres2);
				midView3.setPresenter(midPres3);
				midView4.setPresenter(midPres4);
				
				SwingSubPlayerSelector<Forward> forwView1 = new SwingSubPlayerSelector<Forward>(selectorDims),
						forwView2 = new SwingSubPlayerSelector<Forward>(selectorDims),
								forwView3 = new SwingSubPlayerSelector<Forward>(selectorDims),
										forwView4 = new SwingSubPlayerSelector<Forward>(selectorDims);
				StarterPlayerSelector<Forward> forwPres1 = new StarterPlayerSelector<Forward>(forwView1),
						forwPres2 = new StarterPlayerSelector<Forward>(forwView2),
								forwPres3 = new StarterPlayerSelector<Forward>(forwView3),
										forwPres4 = new StarterPlayerSelector<Forward>(forwView4);
				forwView1.setPresenter(forwPres1);
				forwView2.setPresenter(forwPres2);
				forwView3.setPresenter(forwPres3);
				forwView4.setPresenter(forwPres4);
				
				// II) initializes competition
				CompetitiveOptionDealingGroup.initializeDealing(
						Set.of(goaliePresenter), 
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
				SwingStarterLineUpChooser chooser = new SwingStarterLineUpChooser(
						availableWindow,
						
						new Spring433Scheme(false), new Spring343Scheme(false), new Spring532Scheme(false),
						
						goaliePresenter, goalieView,
						
						defPres1, defView1,
						defPres2, defView2,
						defPres3, defView3,
						defPres4, defView4,
						defPres5, defView5,
						
						midPres1, midView1,
						midPres2, midView2,
						midPres3, midView3,
						midPres4, midView4,
						
						forwPres1, forwView1,
						forwPres2, forwView2,
						forwPres3, forwView3,
						
						presenter -> presenter.setSelection(Optional.empty()));
				
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
