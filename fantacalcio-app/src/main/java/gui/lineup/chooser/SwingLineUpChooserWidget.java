package gui.lineup.chooser;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.io.IOException;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.FantaUser;
import domainModel.League;
import domainModel.LineUp;
import domainModel.Match;
import domainModel.Player.*;
import gui.lineup.chooser.LineUpChooser.StarterLineUpChooserDelegate;
import gui.lineup.chooser.LineUpChooser.SubstituteTripletChooserDelegate;
import gui.lineup.selectors.SelectorController;
import gui.lineup.selectors.StarterPlayerSelector;
import gui.lineup.selectors.SubstitutePlayerSelector;
import gui.lineup.selectors.SwingSubPlayerSelector;
import gui.lineup.starter.StarterLineUpChooser;
import gui.lineup.starter.SwingStarterLineUpChooserWidget;
import gui.lineup.triplet.FillableSwappableTriplet;
import gui.lineup.triplet.SwingFillableSwappableTripletWidget;
import gui.utils.schemes.Spring343Scheme;
import gui.utils.schemes.Spring433Scheme;
import gui.utils.schemes.Spring532Scheme;
import gui.utils.schemes.SpringSchemePanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.border.TitledBorder;

import businessLogic.UserService;

import javax.swing.border.LineBorder;
import java.awt.Color;

/**
 * a {@code JPanel} responsible for implementing {@link LineUpChooserWidget}
 * through composition of {@code JPanel} widgets for one
 * {@link StarterLineUpChooserDelegate} and three
 * {@link SubstituteTripletChooserDelegate}s.
 */
@SuppressWarnings("serial")
public class SwingLineUpChooserWidget extends JPanel implements LineUpChooserWidget {

	private JPanel starterChooserWidget, goalieTripletWidget, defTripletWidget, midTripletWidget, forwTripletWidget;
	private JButton saveLineUpButton;

	/**
	 * @param isDesignTime         toggles <b>design-time</b> instantiation
	 * @param starterChooserWidget a {@code JPanel} widget for a
	 *                             {@link StarterLineUpChooserDelegate}
	 * @param goalieTripletWidget  a {@code JPanel} widget for a
	 *                             {@link SubstituteTripletChooserDelegate}
	 *                             responsible for {@link Goalkeeper}s
	 * @param defTripletWidget     a {@code JPanel} widget for a
	 *                             {@link SubstituteTripletChooserDelegate}
	 *                             responsible for {@link Defender}s
	 * @param midTripletWidget     a {@code JPanel} widget for a
	 *                             {@link SubstituteTripletChooserDelegate}
	 *                             responsible for {@link Midfielder}s
	 * @param forwTripletWidget    a {@code JPanel} widget for a
	 *                             {@link SubstituteTripletChooserDelegate}
	 *                             responsible for {@link Forward}s
	 */
	public SwingLineUpChooserWidget(
			boolean isDesignTime,
			JPanel starterChooserWidget,
			JPanel goalieTripletWidget, JPanel defTripletWidget, JPanel midTripletWidget, JPanel forwTripletWidget) {
				
				this.starterChooserWidget = Objects.requireNonNull(starterChooserWidget);
				this.goalieTripletWidget = Objects.requireNonNull(goalieTripletWidget);
				this.defTripletWidget = Objects.requireNonNull(defTripletWidget);
				this.midTripletWidget = Objects.requireNonNull(midTripletWidget);
				this.forwTripletWidget = Objects.requireNonNull(forwTripletWidget);
				
				instantiateOnComposites();
	}

	

	/**
	 * provides this type's <b><i>private</i> design-time instantiation</b>,
	 * directly instantiating dependencies.
	 * 
	 * @apiNote This constructor is intended <i>purely</i> for supporting the design
	 *          of this type's visual appearance in WindowBuilder
	 */
	SwingLineUpChooserWidget() {
		
		Dimension screenSize = getToolkit().getScreenSize();
		Dimension availableWindow = new Dimension((int) (screenSize.width * 0.2), screenSize.height);
		Dimension selectorDims = SpringSchemePanel.recommendedSlotDimensions(
				SwingStarterLineUpChooserWidget.eventualFieldDimension(availableWindow));
		
		// instantiates, initializes and adds a Starter Chooser Widget
		SwingStarterLineUpChooserWidget starterWidget = new SwingStarterLineUpChooserWidget(
				true,				
				availableWindow,				
				List.of(new Spring433Scheme(false), new Spring343Scheme(false), new Spring532Scheme(false)),				
				new SwingSubPlayerSelector<Goalkeeper>(selectorDims),				
				List.of(new SwingSubPlayerSelector<Defender>(selectorDims), 
						new SwingSubPlayerSelector<Defender>(selectorDims), 
						new SwingSubPlayerSelector<Defender>(selectorDims), 
						new SwingSubPlayerSelector<Defender>(selectorDims), 
						new SwingSubPlayerSelector<Defender>(selectorDims)), 				
				List.of(new SwingSubPlayerSelector<Midfielder>(selectorDims),  
						new SwingSubPlayerSelector<Midfielder>(selectorDims),
						new SwingSubPlayerSelector<Midfielder>(selectorDims),
						new SwingSubPlayerSelector<Midfielder>(selectorDims)), 				
				List.of(new SwingSubPlayerSelector<Forward>(selectorDims),
						new SwingSubPlayerSelector<Forward>(selectorDims), 
						new SwingSubPlayerSelector<Forward>(selectorDims)));
//		starterWidget.switchTo(Scheme433.INSTANCE);  // not WB-compatible
		this.starterChooserWidget = starterWidget;	
		
		// instantiates a Substitute Triplet Widget for Defenders
		this.goalieTripletWidget = new SwingFillableSwappableTripletWidget(
				true, 
				new SwingSubPlayerSelector<Goalkeeper>(selectorDims), 
				new SwingSubPlayerSelector<Goalkeeper>(selectorDims), 
				new SwingSubPlayerSelector<Goalkeeper>(selectorDims));
		
		// instantiates a Substitute Triplet Widget for Defenders
		this.defTripletWidget = new SwingFillableSwappableTripletWidget(
				true, 
				new SwingSubPlayerSelector<Defender>(selectorDims), 
				new SwingSubPlayerSelector<Defender>(selectorDims), 
				new SwingSubPlayerSelector<Defender>(selectorDims));
		
		// instantiates a Substitute Triplet Widget for Midfielders
		this.midTripletWidget = new SwingFillableSwappableTripletWidget(
				true, 
				new SwingSubPlayerSelector<Midfielder>(selectorDims), 
				new SwingSubPlayerSelector<Midfielder>(selectorDims), 
				new SwingSubPlayerSelector<Midfielder>(selectorDims));
		
		// instantiates a Substitute Triplet Widget for Forwards
		this.forwTripletWidget = new SwingFillableSwappableTripletWidget(
				true, 
				new SwingSubPlayerSelector<Forward>(selectorDims), 
				new SwingSubPlayerSelector<Forward>(selectorDims), 
				new SwingSubPlayerSelector<Forward>(selectorDims));
		
		instantiateOnComposites();
	
		// sets private design-time dimensions
		setPreferredSize(getPreferredSize());
	}

	private void instantiateOnComposites() {
		
		// initializes own layout
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{409, 335, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		// sets border and adds Starter Chooser widget
		GridBagConstraints gbc_starterWidget = new GridBagConstraints();
		gbc_starterWidget.gridheight = 4;
		gbc_starterWidget.anchor = GridBagConstraints.NORTH;
		gbc_starterWidget.insets = new Insets(0, 0, 5, 5);
		gbc_starterWidget.gridx = 0;
		gbc_starterWidget.gridy = 0;
		this.starterChooserWidget.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "choose starter players",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(starterChooserWidget, gbc_starterWidget);
		
		// sets border and adds Substitute Triplet widget for Defenders
		GridBagConstraints gbc_goalieTripletWidget = new GridBagConstraints();
		gbc_goalieTripletWidget.insets = new Insets(0, 0, 5, 0);
		gbc_goalieTripletWidget.gridx = 1;
		gbc_goalieTripletWidget.gridy = 0;
		goalieTripletWidget.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "choose substitute goalkeepers",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(goalieTripletWidget, gbc_goalieTripletWidget);
		
		// sets border and adds Substitute Triplet widget for Defenders
		GridBagConstraints gbc_defTripletWidget = new GridBagConstraints();
		gbc_defTripletWidget.insets = new Insets(0, 0, 5, 0);
		gbc_defTripletWidget.gridx = 1;
		gbc_defTripletWidget.gridy = 1;
		defTripletWidget.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "choose substitute defenders",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(defTripletWidget, gbc_defTripletWidget);
		
		// sets border and adds Substitute Triplet widget for Defenders
		GridBagConstraints gbc_midTripletWidget = new GridBagConstraints();
		gbc_midTripletWidget.gridwidth = 1;
		gbc_midTripletWidget.insets = new Insets(0, 0, 5, 0);
		gbc_midTripletWidget.gridx = 1;
		gbc_midTripletWidget.gridy = 2;
		midTripletWidget.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "choose substitute midfielders",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(midTripletWidget, gbc_midTripletWidget);
		
		// sets border and adds Substitute Triplet widget for Defenders
		GridBagConstraints gbc_forwTripletWidget = new GridBagConstraints();
		gbc_forwTripletWidget.insets = new Insets(0, 0, 5, 0);
		gbc_forwTripletWidget.gridwidth = 1;
		gbc_forwTripletWidget.gridx = 1;
		gbc_forwTripletWidget.gridy = 3;
		forwTripletWidget.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "choose substitute forwards",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(forwTripletWidget, gbc_forwTripletWidget);	
		
		// instantiates and adds the 'save' button
		saveLineUpButton = new JButton("save");
		saveLineUpButton.setEnabled(false);
		GridBagConstraints gbc_saveLineUpButton = new GridBagConstraints();
		gbc_saveLineUpButton.gridwidth = 2;
		gbc_saveLineUpButton.insets = new Insets(5, 5, 5, 5);
		gbc_saveLineUpButton.gridx = 0;
		gbc_saveLineUpButton.gridy = 4;
		add(saveLineUpButton, gbc_saveLineUpButton);
		saveLineUpButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				controller.saveLineUp();
			}
		});
	}
	
	private LineUpChooserController controller;
	
	public void setController(LineUpChooserController controller) {
		this.controller = controller;
	}	
	
	@Override
	public void enableSavingLineUp() {
		this.saveLineUpButton.setEnabled(true);
	}

	@Override
	public void disableSavingLineUp() {
		this.saveLineUpButton.setEnabled(false);
	}


	public static void main(String[] args) throws IOException {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("starter chooser demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			Dimension screenSize = frame.getToolkit().getScreenSize();
			Dimension availableWindow = new Dimension((int) (screenSize.width * 0.3), screenSize.height);
			Dimension selectorDims = SpringSchemePanel.recommendedSlotDimensions(
					SwingStarterLineUpChooserWidget.eventualFieldDimension(availableWindow));
			
			// I) initializes Selector duos
			int numSubstituteSels = 3;
			
			int numStarterGoalieSels = 1;
			List<SwingSubPlayerSelector<Goalkeeper>> goalieSelWidgets = IntStream
					.rangeClosed(1, numStarterGoalieSels + numSubstituteSels)
					.mapToObj(i -> new SwingSubPlayerSelector<Goalkeeper>(selectorDims)).collect(Collectors.toList());
			List<StarterPlayerSelector<Goalkeeper>> goalieStarterControllers = IntStream.range(0, numStarterGoalieSels)
					.mapToObj(i -> new StarterPlayerSelector<>(goalieSelWidgets.get(i))).collect(Collectors.toList());
			List<SubstitutePlayerSelector<Goalkeeper>> goalieSubsControllers = IntStream.range(0, numSubstituteSels)
					.mapToObj(i -> new SubstitutePlayerSelector<>(goalieSelWidgets.get(i + numStarterGoalieSels)))
					.collect(Collectors.toList());
			IntStream.range(0, goalieSelWidgets.size()).forEach(i -> {
				SelectorController controller = i < goalieStarterControllers.size()
						? goalieStarterControllers.get(i)
						: goalieSubsControllers.get(i - numStarterGoalieSels);
				goalieSelWidgets.get(i).setController(controller);
			});
			
			int numStarterDefSels = 5;
			List<SwingSubPlayerSelector<Defender>> defSelWidgets = IntStream
					.rangeClosed(1, numStarterDefSels + numSubstituteSels)
					.mapToObj(i -> new SwingSubPlayerSelector<Defender>(selectorDims)).collect(Collectors.toList());
			List<StarterPlayerSelector<Defender>> defStarterControllers = IntStream.range(0, numStarterDefSels)
					.mapToObj(i -> new StarterPlayerSelector<>(defSelWidgets.get(i))).collect(Collectors.toList());
			List<SubstitutePlayerSelector<Defender>> defSubsControllers = IntStream.range(0, numSubstituteSels)
					.mapToObj(i -> new SubstitutePlayerSelector<>(defSelWidgets.get(i + numStarterDefSels)))
					.collect(Collectors.toList());
			IntStream.range(0, defSelWidgets.size()).forEach(i -> {
				SelectorController controller = i < defStarterControllers.size()
						? defStarterControllers.get(i)
						: defSubsControllers.get(i - numStarterDefSels);
				defSelWidgets.get(i).setController(controller);
			});
			
			int numStarterMidSels = 4;
			List<SwingSubPlayerSelector<Midfielder>> midSelWidgets = IntStream
					.rangeClosed(1, numStarterMidSels + numSubstituteSels)
					.mapToObj(i -> new SwingSubPlayerSelector<Midfielder>(selectorDims)).collect(Collectors.toList());
			List<StarterPlayerSelector<Midfielder>> midStarterControllers = IntStream.range(0, numStarterMidSels)
					.mapToObj(i -> new StarterPlayerSelector<>(midSelWidgets.get(i))).collect(Collectors.toList());
			List<SubstitutePlayerSelector<Midfielder>> midSubsControllers = IntStream.range(0, numSubstituteSels)
					.mapToObj(i -> new SubstitutePlayerSelector<>(midSelWidgets.get(i + numStarterMidSels)))
					.collect(Collectors.toList());
			IntStream.range(0, midSelWidgets.size()).forEach(i -> {
				SelectorController controller = i < midStarterControllers.size()
						? midStarterControllers.get(i)
						: midSubsControllers.get(i - numStarterMidSels);
				midSelWidgets.get(i).setController(controller);
			});
			
			int numStarterForwSels = 3;
			List<SwingSubPlayerSelector<Forward>> forwSelWidgets = IntStream
					.rangeClosed(1, numStarterForwSels + numSubstituteSels)
					.mapToObj(i -> new SwingSubPlayerSelector<Forward>(selectorDims)).collect(Collectors.toList());
			List<StarterPlayerSelector<Forward>> forwStarterControllers = IntStream.range(0, numStarterForwSels)
					.mapToObj(i -> new StarterPlayerSelector<>(forwSelWidgets.get(i))).collect(Collectors.toList());
			List<SubstitutePlayerSelector<Forward>> forwSubsControllers = IntStream.range(0, numSubstituteSels)
					.mapToObj(i -> new SubstitutePlayerSelector<>(forwSelWidgets.get(i + numStarterForwSels)))
					.collect(Collectors.toList());
			IntStream.range(0, forwSelWidgets.size()).forEach(i -> {
				SelectorController controller = i < forwStarterControllers.size()
						? forwStarterControllers.get(i)
						: forwSubsControllers.get(i - numStarterForwSels);
				forwSelWidgets.get(i).setController(controller);
			});
			
			// II) instantiates StarterChooser duo
			SwingStarterLineUpChooserWidget starterChooserWidget = new SwingStarterLineUpChooserWidget(
					false, 
					availableWindow, 						
					List.of(new Spring433Scheme(false), new Spring343Scheme(false), new Spring532Scheme(false)), 
					goalieSelWidgets.get(0), 
					List.of(defSelWidgets.get(0), defSelWidgets.get(1), defSelWidgets.get(2), defSelWidgets.get(3), defSelWidgets.get(4)), 
					List.of(midSelWidgets.get(0), midSelWidgets.get(1), midSelWidgets.get(2), midSelWidgets.get(3)), 						
					List.of(forwSelWidgets.get(0), forwSelWidgets.get(1), forwSelWidgets.get(2)));
			
			StarterLineUpChooser starterChooser = new StarterLineUpChooser(
					goalieStarterControllers.get(0), 
					List.of(defStarterControllers.get(0), defStarterControllers.get(1), defStarterControllers.get(2), defStarterControllers.get(3), defStarterControllers.get(4)), 
					List.of(midStarterControllers.get(0), midStarterControllers.get(1), midStarterControllers.get(2), midStarterControllers.get(3)), 						
					List.of(forwStarterControllers.get(0), forwStarterControllers.get(1), forwStarterControllers.get(2)));
			
			starterChooserWidget.setController(starterChooser);
			starterChooser.setWidget(starterChooserWidget);
			
			// III) instantiates Triplet duos
			SwingFillableSwappableTripletWidget goalieTripletWidget = new SwingFillableSwappableTripletWidget(
					false, goalieSelWidgets.get(1), goalieSelWidgets.get(2), goalieSelWidgets.get(3));
			FillableSwappableTriplet<Goalkeeper> goalieTripletController = new FillableSwappableTriplet<Goalkeeper>(
					goalieSubsControllers.get(0), goalieSubsControllers.get(1), goalieSubsControllers.get(2));
			goalieTripletWidget.setController(goalieTripletController);
			goalieTripletController.setWidget(goalieTripletWidget);
			
			SwingFillableSwappableTripletWidget defTripletWidget = new SwingFillableSwappableTripletWidget(
					false, defSelWidgets.get(5), defSelWidgets.get(6), defSelWidgets.get(7));
			FillableSwappableTriplet<Defender> defTripletController = new FillableSwappableTriplet<Defender>(
					defSubsControllers.get(0), defSubsControllers.get(1), defSubsControllers.get(2));
			defTripletWidget.setController(defTripletController);
			defTripletController.setWidget(defTripletWidget);
			
			SwingFillableSwappableTripletWidget midTripletWidget = new SwingFillableSwappableTripletWidget(
					false, midSelWidgets.get(4), midSelWidgets.get(5), midSelWidgets.get(6));
			FillableSwappableTriplet<Midfielder> midTripletController = new FillableSwappableTriplet<Midfielder>(
					midSubsControllers.get(0), midSubsControllers.get(1), midSubsControllers.get(2));
			midTripletWidget.setController(midTripletController);
			midTripletController.setWidget(midTripletWidget);
			
			SwingFillableSwappableTripletWidget forwTripletWidget = new SwingFillableSwappableTripletWidget(
					false, forwSelWidgets.get(3), forwSelWidgets.get(4), forwSelWidgets.get(5));
			FillableSwappableTriplet<Forward> forwTripletController = new FillableSwappableTriplet<Forward>(
					forwSubsControllers.get(0), forwSubsControllers.get(1), forwSubsControllers.get(2));
			forwTripletWidget.setController(forwTripletController);
			forwTripletController.setWidget(forwTripletWidget);
			
			// IV) instantiates Chooser duo
			SwingLineUpChooserWidget chooserWidget = new SwingLineUpChooserWidget(
					false, 
					starterChooserWidget, 
					goalieTripletWidget, defTripletWidget, midTripletWidget, forwTripletWidget);
			UserService fakeService = new UserService(null) {
				@Override
				public void saveLineUp(LineUp lineup) {
					System.out.println("saving lineup!!");
				}
			};
			LineUpChooser chooserController = new LineUpChooser(fakeService, 
					starterChooser, 
					goalieTripletController, 
					defTripletController, 
					midTripletController, 
					forwTripletController);
			chooserWidget.setController(chooserController);
			chooserController.setWidget(chooserWidget);
			
			// initializes chooser
	        Set<Contract> contracts = new HashSet<>();
	        FantaUser user = new FantaUser("", "");
	        League league = new League(user, "fakeLeague", null, "");
			FantaTeam team = new FantaTeam("Elite Team", league, 0, user, contracts);
	        contracts.addAll(Stream.of(
	        				new Goalkeeper("Manuel", "Neuer"), 
	        				new Goalkeeper("Alisson", "Becker"), 
	        				new Goalkeeper("David", "de Gea"), 
	        				new Goalkeeper("Ederson", "Motta"), 
	        				new Goalkeeper("Jan", "Oblak"),
	        				
	        				new Defender("Virgil", "van Dijk"), 
	        				new Defender("Sergio", "Ramos"), 
	        				new Defender("Raphael", "Varane"), 
	        				new Defender("Gerard", "Piqué"), 
	        				new Defender("Thiago", "Silva"),
	        				new Midfielder("Luka", "Modrić"), 
	        				
	        				new Midfielder("Andrés", "Iniesta"), 
	        				new Midfielder("Kevin", "De Bruyne"), 
	        				new Midfielder("N'Golo", "Kanté"), 
	        				new Midfielder("Toni", "Kroos"),
	        				
	        				new Forward("Lionel", "Messi"), 
	        				new Forward("Cristiano", "Ronaldo"), 
	        				new Forward("Neymar", "Jr"), 
	        				new Forward("Robert", "Lewandowski"), 
	        				new Forward("Kylian", "Mbappé"))
	        	    .map(player -> new Contract(team, player))
	        	    .collect(Collectors.toSet()));	        
	        Match match = new Match(null, team, team);
	        
	        chooserController.initTo(team, match);			
			
			frame.setContentPane(chooserWidget);		
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}
