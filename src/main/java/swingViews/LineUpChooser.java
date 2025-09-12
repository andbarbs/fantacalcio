package swingViews;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.io.IOException;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import domainModel.Player;
import domainModel.Player.*;
import swingViews.LineUpScheme.Scheme433;
import swingViews.Selector.SelectorListener;

public class LineUpChooser {

	// a type for the Starter Chooser delegate
	public interface StarterLineUpChooserDelegate {

		Selector<Goalkeeper> getGoalieSelector();
		List<Selector<Defender>> getAllDefSelectors();
		List<Selector<Midfielder>> getAllMidSelectors();
		List<Selector<Forward>> getAllForwSelectors();

		List<Selector<Defender>> getCurrentDefSelectors();
		List<Selector<Midfielder>> getCurrentMidSelectors();
		List<Selector<Forward>> getCurrentForwSelectors();

		void setEntryDefConsumer(Consumer<Selector<Defender>> enterDefender);
		void setEntryMidConsumer(Consumer<Selector<Midfielder>> enterMidfielder);
		void setEntryForwConsumer(Consumer<Selector<Forward>> enterForward);
	}

	private StarterLineUpChooserDelegate starterChooser;

	// a type for the Substitute Triplet Chooser delegate
	public interface SubstituteTripletChooserDelegate<T extends Player> {
		List<Selector<T>> getSelectors();
	}

	private SubstituteTripletChooserDelegate<Goalkeeper> goalieTriplet;
	private SubstituteTripletChooserDelegate<Defender> defTriplet;
	private SubstituteTripletChooserDelegate<Midfielder> midTriplet;
	private SubstituteTripletChooserDelegate<Forward> forwTriplet;

	// a type for the Widget collaborator
	public interface LineUpChooserWidget {
		void enableSavingLineUp();

		void disableSavingLineUp();
	}

	private LineUpChooserWidget widget;

	public void setWidget(LineUpChooserWidget mockWidget) {
		this.widget = mockWidget;
	}
	
	static class BooleanWrapper {
		boolean flag;
		
		private boolean flag() {
			return flag;
		}
	}

	// visible to unit tests for set-up
	BooleanWrapper hasStarterGoalieChoice = new BooleanWrapper();
	BooleanWrapper hasStarterDefChoice = new BooleanWrapper();
	BooleanWrapper hasStarterMidChoice = new BooleanWrapper();
	BooleanWrapper hasStarterForwChoice = new BooleanWrapper();
	BooleanWrapper hasSubsGoaliesChoice = new BooleanWrapper();
	BooleanWrapper hasSubsDefsChoice = new BooleanWrapper();
	BooleanWrapper hasSubsMidsChoice = new BooleanWrapper();
	BooleanWrapper hasSubsForwsChoice = new BooleanWrapper();
	
	private boolean hasChoice() {
		return Stream
				.of(hasStarterGoalieChoice, hasStarterDefChoice, hasStarterMidChoice, hasStarterForwChoice,
						hasSubsGoaliesChoice, hasSubsDefsChoice, hasSubsMidsChoice, hasSubsForwsChoice)
				.map(BooleanWrapper::flag).allMatch(t -> t.equals(Boolean.TRUE));
	}

	// public instantiation point
	public LineUpChooser(StarterLineUpChooserDelegate starterChooser,
			SubstituteTripletChooserDelegate<Goalkeeper> goalieTriplet,
			SubstituteTripletChooserDelegate<Defender> defTriplet,
			SubstituteTripletChooserDelegate<Midfielder> midTriplet,
			SubstituteTripletChooserDelegate<Forward> forwTriplet) {

		this.starterChooser = starterChooser;
		this.goalieTriplet = goalieTriplet;
		this.defTriplet = defTriplet;
		this.midTriplet = midTriplet;
		this.forwTriplet = forwTriplet;

		// 1. attaches Listener to starter Goalie Selector
		this.starterChooser.getGoalieSelector().attachListener(
				listener(hasStarterGoalieChoice, () -> List.of(starterChooser.getGoalieSelector())));

		// 2. sets Consumers into the Starter Delegate for the other three roles
		SelectorListener<Defender> starterDefListener = listener(hasStarterDefChoice, starterChooser::getCurrentDefSelectors);		
		this.starterChooser.setEntryDefConsumer(selector -> selector.attachListener(starterDefListener));

		SelectorListener<Midfielder> starterMidListener = listener(hasStarterMidChoice, starterChooser::getCurrentMidSelectors);
		this.starterChooser.setEntryMidConsumer(selector -> selector.attachListener(starterMidListener));

		SelectorListener<Forward> starterForwListener = listener(hasStarterForwChoice, starterChooser::getCurrentForwSelectors);
		this.starterChooser.setEntryForwConsumer(selector -> selector.attachListener(starterForwListener));

		// 3. attaches Listeners to substitute Selectors
		SelectorListener<Goalkeeper> substituteGoalieListener = listener(hasSubsGoaliesChoice, goalieTriplet::getSelectors);
		this.goalieTriplet.getSelectors().forEach(sel -> sel.attachListener(substituteGoalieListener));

		SelectorListener<Defender> substituteDefListener = listener(hasSubsDefsChoice, defTriplet::getSelectors);
		this.defTriplet.getSelectors().forEach(sel -> sel.attachListener(substituteDefListener));

		SelectorListener<Midfielder> substituteMidListener = listener(hasSubsMidsChoice, midTriplet::getSelectors);
		this.midTriplet.getSelectors().forEach(sel -> sel.attachListener(substituteMidListener));

		SelectorListener<Forward> substituteForwListener = listener(hasSubsForwsChoice, forwTriplet::getSelectors);
		this.forwTriplet.getSelectors().forEach(sel -> sel.attachListener(substituteForwListener));
	}

	private <T extends Player> SelectorListener<T> listener(BooleanWrapper flagWrapper,
			Supplier<List<Selector<T>>> listSupplier) {
		return new SelectorListener<T>() {

			@Override
			public void selectionMadeOn(Selector<T> selector) {
				if (flagWrapper.flag = listSupplier.get().stream().map(Selector::getSelection)
						.allMatch(Optional::isPresent))
					if (hasChoice())
						widget.enableSavingLineUp();
			}

			@Override
			public void selectionClearedOn(Selector<T> selector) {
				if (flagWrapper.flag = listSupplier.get().stream().map(Selector::getSelection)
						.allMatch(Optional::isPresent))
					if (hasChoice())
						widget.enableSavingLineUp();
					else
						widget.disableSavingLineUp();
			}
		};
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
				CompetitiveOptionDealingGroup.initializeDealing(Set.of(goaliePresenter),
						List.of(new Goalkeeper("Gianluigi", "Buffon")));
				CompetitiveOptionDealingGroup.initializeDealing(
						Set.of(defPres1, defPres2, defPres3, defPres4, defPres5),
						List.of(new Defender("Paolo", "Maldini"), new Defender("Franco", "Baresi"),
								new Defender("Alessandro", "Nesta"), new Defender("Giorgio", "Chiellini"),
								new Defender("Leonardo", "Bonucci")));
				CompetitiveOptionDealingGroup.initializeDealing(Set.of(midPres1, midPres2, midPres3, midPres4),
						List.of(new Midfielder("Andrea", "Pirlo"), new Midfielder("Daniele", "De Rossi"),
								new Midfielder("Marco", "Verratti"), new Midfielder("Claudio", "Marchisio")));
				CompetitiveOptionDealingGroup.initializeDealing(Set.of(forwPres1, forwPres2, forwPres3),
						List.of(new Forward("Roberto", "Baggio"), new Forward("Francesco", "Totti"),
								new Forward("Alessandro", "Del Piero"), new Forward("Lorenzo", "Insigne")));

				// III) instantiates StarterChooser
				SwingStarterLineUpChooserWidget starterWidget = new SwingStarterLineUpChooserWidget(false,
						availableWindow, new Spring433Scheme(false), new Spring343Scheme(false),
						new Spring532Scheme(false), goalieView, defView1, defView2, defView3, defView4, defView5,
						midView1, midView2, midView3, midView4, forwView1, forwView2, forwView3);

				StarterLineUpChooser starterChooser = new StarterLineUpChooser(goaliePresenter, defPres1, defPres2,
						defPres3, defPres4, defPres5, midPres1, midPres2, midPres3, midPres4, forwPres1, forwPres2,
						forwPres3, presenter -> presenter.setSelection(Optional.empty()));

				starterWidget.setController(starterChooser);
				starterChooser.setWidget(starterWidget);

				starterChooser.switchToScheme(new Scheme433());

				// IV) instantiates LineUpChooserWidget
				SwingLineUpChooserWidget chooserWidget = new SwingLineUpChooserWidget(false, starterWidget, null, null,
						null, null);

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
