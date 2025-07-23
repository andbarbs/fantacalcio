package swingViews;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.beans.Beans;
import java.io.IOException;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.FantaUser;
import domainModel.League;
import domainModel.NewsPaper;
import domainModel.Player;
import domainModel.Player.*;
import swingViews.LineUpChooserPresenter.LineUpChooserView;

@SuppressWarnings("serial")
public class SwingStarterLineUpChooser extends JPanel implements LineUpChooserView {

	// WB-designed panels, each with 11 empty slots (JPanels)
	private final Spring433Scheme panel433;
	private final Spring343Scheme panel343;
	private final Spring532Scheme panel532;

	// The single pool of selectors
	private final List<SwingSubPlayerSelector<Goalkeeper>> goalieSelectorViews;
	private final List<SwingSubPlayerSelector<Defender>> defSelectorViews;
	private final List<SwingSubPlayerSelector<Midfielder>> midSelectorViews;
	private final List<SwingSubPlayerSelector<Forward>> forwSelectorViews;

	// Internal holder that flips schemes
	private final JPanel schemesHolder;

	public SwingStarterLineUpChooser() throws IOException{
		
		// 1) creates a JLayeredPane with GridBag to overlay components
        JLayeredPane layeredPane = new JLayeredPane();
        GridBagLayout gbl_layeredPane = new GridBagLayout();
        gbl_layeredPane.rowWeights = new double[]{1.0};
        gbl_layeredPane.rowHeights = new int[]{0};
        gbl_layeredPane.columnWeights = new double[]{1.0};
        gbl_layeredPane.columnWidths = new int[]{0};
        layeredPane.setLayout(gbl_layeredPane);
        
        // 2) creates the background label with the football field image
        JLabel background = new JLabel(new ImageIcon(getClass().getResource("/gui_images/raster_field.png")));
		GridBagConstraints gbc_background = new GridBagConstraints();
		gbc_background.fill = GridBagConstraints.BOTH;
		gbc_background.gridx = 0;
		gbc_background.gridy = 0;
		layeredPane.add(background, gbc_background);
		layeredPane.setLayer(background, 0);
        
        // 3) creates a panel to hold scheme panels
		schemesHolder = new JPanel(new CardLayout());
		schemesHolder.setOpaque(false);
        schemesHolder.setBorder(new LineBorder(new Color(255, 0, 0), 8));
		GridBagConstraints gbc_selectorsPanel = new GridBagConstraints();
		gbc_selectorsPanel.fill = GridBagConstraints.BOTH;
		gbc_selectorsPanel.anchor = GridBagConstraints.NORTH;
		gbc_selectorsPanel.gridx = 0;
		gbc_selectorsPanel.gridy = 0;
		layeredPane.add(schemesHolder, gbc_selectorsPanel);
		layeredPane.setLayer(schemesHolder, 1);
		
		// 4) creates as many selectors as will fit all schemes
		Dimension availableWindow = SpringSchemePanel.recommendedSlotDimensions(background.getPreferredSize());
		Dimension reducedAvailableWindow = // ensures selectors actually fit inside slots after insets
				new Dimension(availableWindow.width-5, availableWindow.height-10);
		
		goalieSelectorViews = IntStream.range(0, 1).mapToObj(i -> {
			SwingSubPlayerSelector<Goalkeeper> goalieSelectorView;
			try {
				goalieSelectorView = new SwingSubPlayerSelector<Goalkeeper>(reducedAvailableWindow);
			} catch (IOException e) {
				throw new IllegalStateException();
			}
			OrderedDealerPresenter<Goalkeeper> goalieSelectorPres = new OrderedDealerPresenter<>(goalieSelectorView);
			goalieSelectorView.setPresenter(goalieSelectorPres);
			return goalieSelectorView;
		}).collect(Collectors.toList());
		
		defSelectorViews = IntStream.range(0, 5).mapToObj(i -> {
			SwingSubPlayerSelector<Defender> defSelectorView;
			try {
				defSelectorView = new SwingSubPlayerSelector<Defender>(reducedAvailableWindow);
			} catch (IOException e) {
				throw new IllegalStateException();
			}
			OrderedDealerPresenter<Defender> defSelectorPres = new OrderedDealerPresenter<>(defSelectorView);
			defSelectorView.setPresenter(defSelectorPres);
			return defSelectorView;
		}).collect(Collectors.toList());
		
		midSelectorViews = IntStream.range(0, 4).mapToObj(i -> {
			SwingSubPlayerSelector<Midfielder> midSelectorView;
			try {
				midSelectorView = new SwingSubPlayerSelector<Midfielder>(reducedAvailableWindow);
			} catch (IOException e) {
				throw new IllegalStateException();
			}
			OrderedDealerPresenter<Midfielder> midSelectorPres = new OrderedDealerPresenter<>(midSelectorView);
			midSelectorView.setPresenter(midSelectorPres);
			return midSelectorView;
		}).collect(Collectors.toList());
		
		forwSelectorViews = IntStream.range(0, 3).mapToObj(i -> {
			SwingSubPlayerSelector<Forward> forwSelectorView;
			try {
				forwSelectorView = new SwingSubPlayerSelector<Forward>(reducedAvailableWindow);
			} catch (IOException e) {
				throw new IllegalStateException();
			}
			OrderedDealerPresenter<Forward> forwSelectorPres = new OrderedDealerPresenter<>(forwSelectorView);
			forwSelectorView.setPresenter(forwSelectorPres);
			return forwSelectorView;
		}).collect(Collectors.toList());

		// 5) instantiates 3 supported scheme panels and adds them to CardLayout
		panel433 = new Spring433Scheme(Beans.isDesignTime());
		panel343 = new Spring343Scheme(Beans.isDesignTime());
		panel532 = new Spring532Scheme(Beans.isDesignTime());

		schemesHolder.add(panel433, "433");
		schemesHolder.add(panel343, "343");
		schemesHolder.add(panel532, "532");

		// 6) builds the radio-button strip for scheme selection
		JPanel switcher = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		JRadioButton b433 = new JRadioButton("4-3-3", true);
		JRadioButton b343 = new JRadioButton("3-4-3");
		JRadioButton b532 = new JRadioButton("5-3-2");
		ButtonGroup group = new ButtonGroup();
		group.add(b433);
		group.add(b343);
		group.add(b532);
		switcher.add(new JLabel("Scheme:"));
		switcher.add(b433);
		switcher.add(b343);
		switcher.add(b532);

		// 6) composes everything at the root level
		setLayout(new BorderLayout());
		add(switcher, BorderLayout.NORTH);
		add(layeredPane, BorderLayout.CENTER);

		// 7) wires scheme switch-up logic to the radios
		ActionListener flip = e -> {
			String key = b433.isSelected() ? "433" : b343.isSelected() ? "343" : "532";
			switchToScheme(key);
		};
		b433.addActionListener(flip);
		b343.addActionListener(flip);
		b532.addActionListener(flip);
		
		// 8) design-time demo logic
		if (Beans.isDesignTime()) {
			// TODO find a way to make schemes display correctly at design time
			setPreferredSize(background.getPreferredSize());
		}
	}

	@Override
	public void initChoice(FantaTeam team) {
		populateModels(team);
		switchToScheme("433"); // default scheme
	}

	private void populateModels(FantaTeam team) {
		// TODO there is the issue of ordering players in the team:
		// initializeCompetition rightly wants a List!!
		
		OptionDealerGroupDriver.initializeDealing(
				goalieSelectorViews.stream().map(SwingSubPlayerSelector::getPresenter).collect(Collectors.toSet()), 
				List.copyOf(team.extract().goalkeepers()));
		OptionDealerGroupDriver.initializeDealing(
				defSelectorViews.stream().map(SwingSubPlayerSelector::getPresenter).collect(Collectors.toSet()), 
				List.copyOf(team.extract().defenders()));
		OptionDealerGroupDriver.initializeDealing(
				midSelectorViews.stream().map(SwingSubPlayerSelector::getPresenter).collect(Collectors.toSet()), 
				List.copyOf(team.extract().midfielders()));
		OptionDealerGroupDriver.initializeDealing(
				forwSelectorViews.stream().map(SwingSubPlayerSelector::getPresenter).collect(Collectors.toSet()), 
				List.copyOf(team.extract().forwards()));
	}

	private void switchToScheme(String schemeKey) {

		// 1) remove all selectors from old parent slots
		Consumer<? super SwingSubPlayerSelector<? extends Player>> detachSelector = sel -> {
			Container parent = sel.getParent();
			if (parent != null)
				parent.remove(sel);
		};
		
		Stream.concat(
				Stream.concat(goalieSelectorViews.stream(), defSelectorViews.stream()),
				Stream.concat(midSelectorViews.stream(), forwSelectorViews.stream()))
			.forEach(detachSelector);

		// 2) establish user choice of next scheme
		SpringSchemePanel target = schemeKey.equals("343") ? panel343 : schemeKey.equals("532") ? panel532 : panel433;

		// 3) re-attach as many selectors as we have slots for each role
		attachSelectors(target.getGoalieSlots(), goalieSelectorViews);
		attachSelectors(target.getDefenderSlots(), defSelectorViews);
		attachSelectors(target.getMidfielderSlots(), midSelectorViews);
		attachSelectors(target.getForwardSlots(), forwSelectorViews);

		// 4) Show the right card
		((CardLayout) schemesHolder.getLayout()).show(schemesHolder, schemeKey);

		// 5) Redraw
		schemesHolder.revalidate();
		schemesHolder.repaint();
	}

	private <T extends Player> void attachSelectors(List<JPanel> slots, List<SwingSubPlayerSelector<T>> selectors) {
		int i = 0;

		// add selectors to slots: there could be more selectors than slots
		for (; i < slots.size(); i++) {
			slots.get(i).add(selectors.get(i));
		}

		// TODO empty out selectors that won't be shown
		for (; i < selectors.size(); i++) {
			selectors.get(i).getPresenter().setSelection(Optional.empty());
		}
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("starter chooser demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			// Players
			List<Player> players = List.of(
					new Goalkeeper("Gianluigi", "Buffon"), 
					new Goalkeeper("Samir", "Handanović"), 
					new Defender("Paolo", "Maldini"), 
					new Defender("Franco", "Baresi"), 
					new Defender("Alessandro", "Nesta"), 
					new Defender("Giorgio", "Chiellini"), 
					new Defender("Leonardo", "Bonucci"), 
					new Midfielder("Andrea", "Pirlo"), 
					new Midfielder("Daniele", "De Rossi"), 
					new Midfielder("Marco", "Verratti"), 
					new Midfielder("Claudio", "Marchisio"), 
					new Forward("Roberto", "Baggio"), 
					new Forward("Francesco", "Totti"), 
					new Forward("Alessandro", "Del Piero"), 
					new Forward("Lorenzo", "Insigne"));

			// Contracts, FantaTeam and Match
			Set<Contract> contracts = new HashSet<Contract>();
			FantaTeam team = new FantaTeam(
					"Dream Team", 
					new League(
							new FantaUser("manager@example.com", "securePass"), 
							"Serie A", 
							new NewsPaper("Gazzetta"), 
							"code"), 
					30, 
					new FantaUser("manager@example.com", "securePass"), 
					contracts);
			players.stream().map(p -> new Contract(team, p)).forEach(contracts::add);
			
			SwingStarterLineUpChooser chooser;
			try {
				chooser = new SwingStarterLineUpChooser();
				frame.setContentPane(chooser);
				chooser.initChoice(team);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}
