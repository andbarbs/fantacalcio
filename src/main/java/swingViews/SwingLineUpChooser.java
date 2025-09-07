package swingViews;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.io.IOException;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import domainModel.Player.*;
import swingViews.StarterLineUpChooserController.LineUpScheme;
import swingViews.StarterLineUpChooserController.LineUpSchemes;
import swingViews.StarterLineUpChooserController.LineUpScheme.LineUpSchemeVisitor;

@SuppressWarnings("serial")
public class SwingLineUpChooser extends JPanel {
	
	public interface StarterLineUpChooser {
		
		// interface for Selector collaborator (will belong in their own file)
		
		public interface Selector<T> {
			public interface SelectorListener {
				void selectionMade();
				void selectionCleared();
			}
			void add(SelectorListener listener);
			Optional<T> getSelection();
			void setSelection(Optional<T> option);
		}
		
		Selector<Goalkeeper> getGoalieSelector();
		List<Selector<Defender>> getDefenderSelectors();
		List<Selector<Midfielder>> getMidfielderSelectors();		
		List<Selector<Forward>> getForwardSelectors();
		
		// Visitable scheme type
		public static abstract class LineUpScheme {

			public interface LineUpSchemeVisitor {
				void visit433(LineUpSchemes.Scheme433 scheme433);
				void visit343(LineUpSchemes.Scheme343 scheme343);
				void visit532(LineUpSchemes.Scheme532 scheme532);
			}

			abstract void accept(LineUpSchemeVisitor visitor);

			@Override
			public boolean equals(Object obj) {
				return getClass().equals(obj.getClass());
			}
		}

		public static abstract class LineUpSchemes {
			public static class Scheme433 extends LineUpScheme {
				@Override
				void accept(LineUpSchemeVisitor visitor) {
					visitor.visit433(this);
				}
			}

			public static class Scheme343 extends LineUpScheme {
				@Override
				void accept(LineUpSchemeVisitor visitor) {
					visitor.visit343(this);
				}
			}

			public static class Scheme532 extends LineUpScheme {
				@Override
				void accept(LineUpSchemeVisitor visitor) {
					visitor.visit532(this);
				}
			}
		}
		
		interface StarterLineUpChooserListener {
			void schemeChangedOn(StarterLineUpChooser chooser);
		}
		
		void attachListener(StarterLineUpChooserListener listener);
		LineUpScheme getCurrentScheme();
	}
	

	private SwingStarterLineUpChooserWidget starterChooser;
	private FillableSwappableTriplet<SubstitutePlayerSelector<Goalkeeper>> goalieTriplet;
	private FillableSwappableTriplet<SubstitutePlayerSelector<Defender>> defTriplet;
	private FillableSwappableTriplet<SubstitutePlayerSelector<Goalkeeper>> midTriplet;
	private FillableSwappableTriplet<SubstitutePlayerSelector<Goalkeeper>> forwTriplet;


	// public instantiation point
	public SwingLineUpChooser(
			boolean isDesignTime,
			SwingStarterLineUpChooserWidget starterChooser,
			FillableSwappableTriplet<SubstitutePlayerSelector<Goalkeeper>> goalieTriplet,
			FillableSwappableTriplet<SubstitutePlayerSelector<Defender>> defTriplet,
			FillableSwappableTriplet<SubstitutePlayerSelector<Goalkeeper>> midTriplet,
			FillableSwappableTriplet<SubstitutePlayerSelector<Goalkeeper>> forwTriplet) {
		
				this.starterChooser = starterChooser;
				this.goalieTriplet = goalieTriplet;
				this.defTriplet = defTriplet;
				this.midTriplet = midTriplet;
				this.forwTriplet = forwTriplet;

		
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
	SwingLineUpChooser() throws IOException {
		Dimension screenSize = getToolkit().getScreenSize();
		Dimension availableWindow = new Dimension((int) (screenSize.width * 0.3), screenSize.height);
		Dimension selectorDims = SpringSchemePanel.recommendedSlotDimensions(
				SwingStarterLineUpChooserWidget.eventualFieldDimension(availableWindow));
		
		this.starterChooser = new SwingStarterLineUpChooserWidget(
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
		
		add(starterChooser);		

		// 9) sets private design-time dimensions
		setPreferredSize(starterChooser.getPreferredSize());
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
//				SwingLineUpChooser chooser = new SwingLineUpChooser(
//						false,
//						
//						availableWindow,
//						
//						new Spring433Scheme(false), new Spring343Scheme(false), new Spring532Scheme(false),
//						
//						goaliePresenter, goalieView,
//						
//						defPres1, defView1,
//						defPres2, defView2,
//						defPres3, defView3,
//						defPres4, defView4,
//						defPres5, defView5,
//						
//						midPres1, midView1,
//						midPres2, midView2,
//						midPres3, midView3,
//						midPres4, midView4,
//						
//						forwPres1, forwView1,
//						forwPres2, forwView2,
//						forwPres3, forwView3,
//						
//						presenter -> presenter.setSelection(Optional.empty()));
//				
//				frame.setContentPane(chooser);		
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
