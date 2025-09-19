package swingViews;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.io.IOException;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import domainModel.LineUp.LineUpBuilderSteps.StarterLineUp;
import domainModel.Player;
import domainModel.Player.*;
import swingViews.LineUpChooser.StarterSelectorDelegate;
import swingViews.LineUpScheme.*;

public class StarterLineUpChooser implements StarterLineUpChooserController, LineUpChooser.StarterLineUpChooserDelegate {	

	// injected dependencies
	private StarterSelectorDelegate<Goalkeeper> goalieSelector;
	private List<StarterSelectorDelegate<Defender>> defPairs;
	private List<StarterSelectorDelegate<Midfielder>> midPairs;
	private List<StarterSelectorDelegate<Forward>> forwPairs;
	
	private Collection<StarterSelectorDelegate<? extends Player>> selectorsIn532;
	private Collection<StarterSelectorDelegate<? extends Player>> selectorsIn433;
	private Collection<StarterSelectorDelegate<? extends Player>> selectorsIn343;
	
	private Consumer<StarterSelectorDelegate<? extends Player>> onSelectorExlcluded;	

	LineUpScheme currentScheme;
	
	// public instantiation point
	public StarterLineUpChooser(
			StarterSelectorDelegate<Goalkeeper> goalieSelector,
			
			StarterSelectorDelegate<Defender> defSelector1,
			StarterSelectorDelegate<Defender> defSelector2,
			StarterSelectorDelegate<Defender> defSelector3,
			StarterSelectorDelegate<Defender> defSelector4,
			StarterSelectorDelegate<Defender> defSelector5,
			
			StarterSelectorDelegate<Midfielder> midSelector1,
			StarterSelectorDelegate<Midfielder> midSelector2,
			StarterSelectorDelegate<Midfielder> midSelector3,
			StarterSelectorDelegate<Midfielder> midSelector4,
			
			StarterSelectorDelegate<Forward> forwSelector1,
			StarterSelectorDelegate<Forward> forwSelector2,
			StarterSelectorDelegate<Forward> forwSelector3,			
			
			Consumer<StarterSelectorDelegate<? extends Player>> onSelectorExlcluded) {

		this.goalieSelector = goalieSelector;
		this.defPairs = List.of(defSelector1, defSelector2, defSelector3, defSelector4, defSelector5);
		this.midPairs = List.of(midSelector1, midSelector2, midSelector3, midSelector4);
		this.forwPairs = List.of(forwSelector1, forwSelector2, forwSelector3);
		
		this.onSelectorExlcluded = onSelectorExlcluded;
		
		// by-role Selector collections
		this.selectorsIn532 = List.of(goalieSelector, 
				defSelector1, defSelector2, defSelector3, defSelector4, defSelector5,
				midSelector1, midSelector2, midSelector3,
				forwSelector1, forwSelector2);
		this.selectorsIn433 = List.of(goalieSelector, 
				defSelector1, defSelector2, defSelector3, defSelector4,
				midSelector1, midSelector2, midSelector3,
				forwSelector1, forwSelector2, forwSelector3);
		this.selectorsIn343 = List.of(goalieSelector, 
				defSelector1, defSelector2, defSelector3,
				midSelector1, midSelector2, midSelector3, midSelector4,
				forwSelector1, forwSelector2, forwSelector3);
	}
	
	// As a StarterLineUpChooser
	
	
	public boolean hasChoice() {
		var visitor = new LineUpScheme.LineUpSchemeVisitor() {
			boolean selectionExists;

			@Override
			public void visit532(Scheme532 scheme532) {
				selectionExists = selectorsIn532.stream().allMatch(
						selector -> selector.getSelection().isPresent());
			}

			@Override
			public void visit433(Scheme433 scheme433) {
				selectionExists = selectorsIn433.stream().allMatch(
						selector -> selector.getSelection().isPresent());
			}

			@Override
			public void visit343(Scheme343 scheme343) {
				selectionExists = selectorsIn343.stream().allMatch(
						selector -> selector.getSelection().isPresent());
			}
		};
		currentScheme.accept(visitor);
		return visitor.selectionExists;
	}
	@Override
	public StarterSelectorDelegate<Goalkeeper> getGoalieSelector() {
		return goalieSelector;
	}

	@Override
	public List<StarterSelectorDelegate<Defender>> getAllDefSelectors() {
		return List.copyOf(defPairs);
	}

	@Override
	public List<StarterSelectorDelegate<Midfielder>> getAllMidSelectors() {
		return List.copyOf(midPairs);
	}
	
	@Override
	public List<StarterSelectorDelegate<Forward>> getAllForwSelectors() {
		return List.copyOf(forwPairs);
	}
	
	@Override
	public List<Selector<Defender>> getCurrentDefSelectors() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Selector<Midfielder>> getCurrentMidSelectors() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Selector<Forward>> getCurrentForwSelectors() {
		// TODO Auto-generated method stub
		return null;
	}	
	
	// As a Controller


	// interface for vertical collaborator
	public interface StarterLineUpChooserWidget {
		void switchTo(LineUpScheme scheme);
	}

	private StarterLineUpChooserWidget widget;

	public void setWidget(StarterLineUpChooserWidget widget) {
		this.widget = widget;
	}
	
	@Override
	public void switchToScheme(LineUpScheme newScheme) {
		
		// 1) processes excluded Selectors
		Optional.ofNullable(currentScheme).ifPresent(scheme -> {
			selectorsIn(scheme).stream()
				.filter(selector -> !selectorsIn(newScheme).contains(selector))
				.forEach(onSelectorExlcluded);
		});
		
		// 2) asks Widget to rearrange Selector widgets
		widget.switchTo(newScheme);

		// 3) updates bookkeeping
		currentScheme = newScheme;	
	}

	private Collection<StarterSelectorDelegate<? extends Player>> selectorsIn(LineUpScheme scheme) {
		return scheme.equals(new Scheme433()) ? selectorsIn433 :
				scheme.equals(new Scheme343()) ? selectorsIn343 : selectorsIn532;
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
						defPres1, defPres2, defPres3, defPres4, defPres5,
						midPres1, midPres2, midPres3, midPres4,
						forwPres1, forwPres2, forwPres3,
						presenter -> presenter.setSelection(Optional.empty()));
				
				SwingStarterLineUpChooserWidget widget = new SwingStarterLineUpChooserWidget(
						false, 
						availableWindow, 						
						new Spring433Scheme(false), new Spring343Scheme(false), new Spring532Scheme(false), 
						goalieView, 
						defView1, defView2, defView3, defView4, defView5, 
						midView1, midView2, midView3, midView4, 						
						forwView1, forwView2, forwView3);
				
				controller.setWidget(widget);
				widget.setController(controller);
				
				controller.switchToScheme(new Scheme433());
				
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

	@Override
	public void setEntryDefConsumer(Consumer<Selector<Defender>> enterDefender) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEntryMidConsumer(Consumer<Selector<Midfielder>> enterMidfielder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEntryForwConsumer(Consumer<Selector<Forward>> enterForward) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExitDefConsumer(Consumer<Selector<Defender>> exitDefender) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExitMidConsumer(Consumer<Selector<Midfielder>> capture) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExitForwConsumer(Consumer<Selector<Forward>> capture) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StarterLineUp getCurrentStarterLineUp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void switchToDefaultScheme() {
		// TODO Auto-generated method stub
		
	}
}
