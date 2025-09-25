package gui.lineup.chooser;

import java.util.List;
import java.util.Set;
import java.io.IOException;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import domainModel.Player.*;
import domainModel.scheme.Scheme433;
import gui.lineup.dealing.CompetitiveOptionDealingGroup;
import gui.lineup.selectors.StarterPlayerSelector;
import gui.lineup.selectors.SwingSubPlayerSelector;
import gui.lineup.starter.StarterLineUpChooser;
import gui.lineup.starter.SwingStarterLineUpChooserWidget;
import gui.utils.schemes.Spring343Scheme;
import gui.utils.schemes.Spring433Scheme;
import gui.utils.schemes.Spring532Scheme;
import gui.utils.schemes.SpringSchemePanel;

@SuppressWarnings("serial")
public class SwingLineUpChooserWidget extends JPanel {

	private JPanel starterChooserWidget;
	private JPanel goalieTripletWidget;
	private JPanel defTripletWidget;
	private JPanel midTripletWidget;
	private JPanel forwTripletWidget;


	// public instantiation point
	public SwingLineUpChooserWidget(
			boolean isDesignTime,
			JPanel starterChooserWidget,
			JPanel goalieTripletWidget,
			JPanel defTripletWidget,
			JPanel midTripletWidget,
			JPanel forwTripletWidget) {
				
				this.starterChooserWidget = starterChooserWidget;
				this.goalieTripletWidget = goalieTripletWidget;
				this.defTripletWidget = defTripletWidget;
				this.midTripletWidget = midTripletWidget;
				this.forwTripletWidget = forwTripletWidget;
				
				add(this.starterChooserWidget);
				add(this.goalieTripletWidget);
				add(this.defTripletWidget);
				add(this.midTripletWidget);
				add(this.forwTripletWidget);
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
	SwingLineUpChooserWidget() throws IOException {
		Dimension screenSize = getToolkit().getScreenSize();
		Dimension availableWindow = new Dimension((int) (screenSize.width * 0.3), screenSize.height);
		Dimension selectorDims = SpringSchemePanel.recommendedSlotDimensions(
				SwingStarterLineUpChooserWidget.eventualFieldDimension(availableWindow));
		
		this.starterChooserWidget = new SwingStarterLineUpChooserWidget(
				true, 
				
				availableWindow, 
				
				new Spring433Scheme(false), new Spring343Scheme(false), new Spring532Scheme(false), 
				
				new SwingSubPlayerSelector<Goalkeeper>(selectorDims),
				
				new SwingSubPlayerSelector<Defender>(selectorDims), 
				new SwingSubPlayerSelector<Defender>(selectorDims), 
				new SwingSubPlayerSelector<Defender>(selectorDims), 
				new SwingSubPlayerSelector<Defender>(selectorDims), 
				new SwingSubPlayerSelector<Defender>(selectorDims), 
				
				new SwingSubPlayerSelector<Midfielder>(selectorDims),  
				new SwingSubPlayerSelector<Midfielder>(selectorDims),
				new SwingSubPlayerSelector<Midfielder>(selectorDims),
				new SwingSubPlayerSelector<Midfielder>(selectorDims), 
				
				new SwingSubPlayerSelector<Forward>(selectorDims),
				new SwingSubPlayerSelector<Forward>(selectorDims), 
				new SwingSubPlayerSelector<Forward>(selectorDims));
		
		add(starterChooserWidget);		

		// 9) sets private design-time dimensions
		setPreferredSize(starterChooserWidget.getPreferredSize());
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
				StarterPlayerSelector<Goalkeeper> goaliePresenter = new StarterPlayerSelector<>(goalieView);
				goalieView.setController(goaliePresenter);
				
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
								forwView3 = new SwingSubPlayerSelector<Forward>(selectorDims),
										forwView4 = new SwingSubPlayerSelector<Forward>(selectorDims);
				StarterPlayerSelector<Forward> forwPres1 = new StarterPlayerSelector<Forward>(forwView1),
						forwPres2 = new StarterPlayerSelector<Forward>(forwView2),
								forwPres3 = new StarterPlayerSelector<Forward>(forwView3),
										forwPres4 = new StarterPlayerSelector<Forward>(forwView4);
				forwView1.setController(forwPres1);
				forwView2.setController(forwPres2);
				forwView3.setController(forwPres3);
				forwView4.setController(forwPres4);
				
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
				
				// III) instantiates StarterChooser
				SwingStarterLineUpChooserWidget starterWidget = new SwingStarterLineUpChooserWidget(
						false,						
						availableWindow,						
						new Spring433Scheme(false), new Spring343Scheme(false), new Spring532Scheme(false),						
						goalieView,						
						defView1, defView2, defView3, defView4, defView5,						
						midView1, midView2, midView3, midView4,
						forwView1, forwView2, forwView3);
				
				StarterLineUpChooser starterChooser = new StarterLineUpChooser(
						goaliePresenter,						
						defPres1, defPres2, defPres3, defPres4, defPres5,						
						midPres1, midPres2, midPres3, midPres4,						
						forwPres1, forwPres2, forwPres3);
				
				starterWidget.setController(starterChooser);
				starterChooser.setWidget(starterWidget);
				
				starterChooser.switchToScheme(Scheme433.INSTANCE);
				
				// IV) instantiates LineUpChooserWidget
				SwingLineUpChooserWidget chooserWidget = new SwingLineUpChooserWidget(
						false, starterWidget, null, null, null, null);
				
				frame.setContentPane(chooserWidget);		
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
