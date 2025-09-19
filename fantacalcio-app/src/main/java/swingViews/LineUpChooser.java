package swingViews;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.IOException;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import businessLogic.UserService;
import domainModel.FantaTeam;
import domainModel.LineUp;
import domainModel.LineUp.LineUpBuilderSteps.StarterLineUp;
import domainModel.Match;
import domainModel.Player;
import domainModel.Player.*;
import swingViews.CompetitiveOptionDealingGroup.CompetitiveOrderedDealer;
import swingViews.FillableSwappableSequence.FillableSwappable;
import swingViews.LineUpScheme.Scheme433;
import swingViews.Selector.SelectorListener;

public class LineUpChooser implements LineUpChooserController {
	
	/**
	 * a type for a {@linkplain Selector} that can be made to participate in a
	 * {@linkplain CompetitiveOptionDealingGroup} made up of other
	 * {@linkplain StarterSelectorDelegate}s.
	 * 
	 * @param <T> the type for options on this {@code Selector}
	 * @see {@link CompetitiveOptionDealingGroup} for the semantics of
	 *      <i>competitive dealing</i> and how to initialize it
	 */
	public interface StarterSelectorDelegate<T> extends Selector<T>, 
										CompetitiveOrderedDealer<StarterSelectorDelegate<T>, T> {
		
	}
	
	/**
	 * a type for a {@linkplain StarterSelectorDelegate} that can be made to
	 * participate in a {@linkplain FillableSwappableSequence} made up of other
	 * {@linkplain SubstituteSelectorDelegate}s.
	 * 
	 * @param <T> the type for options on this {@code Selector}
	 * @see {@link FillableSwappableSequence} for the semantics of a
	 *      <i>fillable-swappable sequence</i> and how to initialize one
	 */
	public interface SubstituteSelectorDelegate<T>
			extends StarterSelectorDelegate<T>, FillableSwappable<SubstituteSelectorDelegate<T>> {

	}

	// a type for the Starter Chooser delegate
	public interface StarterLineUpChooserDelegate {

		StarterSelectorDelegate<Goalkeeper> getGoalieSelector();
		List<StarterSelectorDelegate<Defender>> getAllDefSelectors();
		List<StarterSelectorDelegate<Midfielder>> getAllMidSelectors();
		List<StarterSelectorDelegate<Forward>> getAllForwSelectors();

		List<Selector<Defender>> getCurrentDefSelectors();
		List<Selector<Midfielder>> getCurrentMidSelectors();
		List<Selector<Forward>> getCurrentForwSelectors();

		void setEntryDefConsumer(Consumer<Selector<Defender>> enterDefender);
		void setEntryMidConsumer(Consumer<Selector<Midfielder>> enterMidfielder);
		void setEntryForwConsumer(Consumer<Selector<Forward>> enterForward);
		void setExitDefConsumer(Consumer<Selector<Defender>> exitDefender);
		void setExitMidConsumer(Consumer<Selector<Midfielder>> exitMidfielder);
		void setExitForwConsumer(Consumer<Selector<Forward>> exitForward);
		StarterLineUp getCurrentStarterLineUp();
		void switchToDefaultScheme();
	}

	private StarterLineUpChooserDelegate starterChooser;

	// a type for the Substitute Triplet Chooser delegate
	public interface SubstituteTripletChooserDelegate<T extends Player> {
		List<SubstituteSelectorDelegate<T>> getSelectors();

		Optional<Selector<T>> getNextFillableSelector();
	}

	private final SubstituteTripletChooserDelegate<Goalkeeper> goalieTriplet;
	private final SubstituteTripletChooserDelegate<Defender> defTriplet;
	private final SubstituteTripletChooserDelegate<Midfielder> midTriplet;
	private final SubstituteTripletChooserDelegate<Forward> forwTriplet;

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

	private final UserService service;
	
	// public instantiation point
	public LineUpChooser(
			UserService service,
			StarterLineUpChooserDelegate starterChooser,
			SubstituteTripletChooserDelegate<Goalkeeper> goalieTriplet,
			SubstituteTripletChooserDelegate<Defender> defTriplet,
			SubstituteTripletChooserDelegate<Midfielder> midTriplet,
			SubstituteTripletChooserDelegate<Forward> forwTriplet) {

		this.service = service;
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
		this.starterChooser.setEntryDefConsumer(entryConsumer(starterDefListener, hasStarterDefChoice));
		this.starterChooser.setExitDefConsumer(exitConsumer(starterDefListener, defTriplet, hasStarterDefChoice,
				starterChooser::getCurrentDefSelectors));

		SelectorListener<Midfielder> starterMidListener = listener(hasStarterMidChoice, starterChooser::getCurrentMidSelectors);
		this.starterChooser.setEntryMidConsumer(entryConsumer(starterMidListener, hasStarterMidChoice));		
		this.starterChooser.setExitMidConsumer(exitConsumer(starterMidListener, midTriplet, hasStarterMidChoice,
				starterChooser::getCurrentMidSelectors));

		SelectorListener<Forward> starterForwListener = listener(hasStarterForwChoice, starterChooser::getCurrentForwSelectors);
		this.starterChooser.setEntryForwConsumer(entryConsumer(starterForwListener, hasStarterForwChoice));
		this.starterChooser.setExitForwConsumer(exitConsumer(starterForwListener, forwTriplet, hasStarterForwChoice,
				starterChooser::getCurrentForwSelectors));

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
			Supplier<List<? extends Selector<T>>> listSupplier) {
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
						.allMatch(Optional::isPresent)) {
					if (hasChoice())   // why???
						widget.enableSavingLineUp();
				} else {
						widget.disableSavingLineUp();
				}
			}
		};
	}

	private <T extends Player> Consumer<Selector<T>> entryConsumer(SelectorListener<T> starterDefListener,
			BooleanWrapper hasGroupChoice) {
		return selector -> {
			selector.attachListener(starterDefListener);
			hasGroupChoice.flag = false;
		};
	}

	private <T extends Player> Consumer<Selector<T>> exitConsumer(SelectorListener<T> listener,
			SubstituteTripletChooserDelegate<T> triplet, BooleanWrapper hasGroupChoice,
			Supplier<List<? extends Selector<T>>> currentSchemeSelectors) {
		return exitingSelector -> {
			exitingSelector.removeListener(listener);
			if (exitingSelector.getSelection().isPresent()) {
				triplet.getNextFillableSelector()
						.ifPresent(subSel -> subSel.setSelection(exitingSelector.getSelection()));
				exitingSelector.setSelection(Optional.empty());
			}
			hasGroupChoice.flag = currentSchemeSelectors.get().stream().map(Selector::getSelection)
					.allMatch(Optional::isPresent);
		};
	}

	FantaTeam team;
	Match match;

	public void initTo(FantaTeam team, Match match) {
		
		// captures inside internal bookkeeping
		this.team = Objects.requireNonNull(team);
		this.match = Objects.requireNonNull(match);
		
		// initializes dealing
		CompetitiveOptionDealingGroup.initializeDealing(
				Stream.of(List.of(starterChooser.getGoalieSelector()), goalieTriplet.getSelectors())
					.flatMap(List::stream).collect(Collectors.toSet()),
				team.extract().goalkeepers().stream()
						.sorted(Comparator.comparing(Player::getSurname))
						.collect(Collectors.toList()));
		
		CompetitiveOptionDealingGroup.initializeDealing(
				Stream.of(starterChooser.getAllDefSelectors(), defTriplet.getSelectors())
					.flatMap(List::stream).collect(Collectors.toSet()),
				team.extract().defenders().stream()
						.sorted(Comparator.comparing(Player::getSurname))
						.collect(Collectors.toList()));
		
		CompetitiveOptionDealingGroup.initializeDealing(
				Stream.of(starterChooser.getAllMidSelectors(), midTriplet.getSelectors())
					.flatMap(List::stream).collect(Collectors.toSet()),
				team.extract().midfielders().stream()
						.sorted(Comparator.comparing(Player::getSurname))
						.collect(Collectors.toList()));
		
		CompetitiveOptionDealingGroup.initializeDealing(
				Stream.of(starterChooser.getAllForwSelectors(), forwTriplet.getSelectors())
					.flatMap(List::stream).collect(Collectors.toSet()),
				team.extract().forwards().stream()
						.sorted(Comparator.comparing(Player::getSurname))
						.collect(Collectors.toList()));
		
		// initializes sequences
		FillableSwappableSequence.createSequence(goalieTriplet.getSelectors());
		FillableSwappableSequence.createSequence(defTriplet.getSelectors());
		FillableSwappableSequence.createSequence(midTriplet.getSelectors());
		FillableSwappableSequence.createSequence(forwTriplet.getSelectors());
		
		// orders Starter Delegate
		starterChooser.switchToDefaultScheme();
	}

	@Override
	public void saveLineUp() {
		if (hasChoice()) {
			service.saveLineUp(LineUp.build().forTeam(team).inMatch(match)
					.withStarterLineUp(starterChooser.getCurrentStarterLineUp())
					.withSubstituteGoalkeepers(
							goalieTriplet.getSelectors().get(0).getSelection().get(),
							goalieTriplet.getSelectors().get(1).getSelection().get(),
							goalieTriplet.getSelectors().get(2).getSelection().get())
					.withSubstituteDefenders(
							defTriplet.getSelectors().get(0).getSelection().get(),
							defTriplet.getSelectors().get(1).getSelection().get(),
							defTriplet.getSelectors().get(2).getSelection().get())
					.withSubstituteMidfielders(
							midTriplet.getSelectors().get(0).getSelection().get(),
							midTriplet.getSelectors().get(1).getSelection().get(),
							midTriplet.getSelectors().get(2).getSelection().get())
					.withSubstituteForwards(
							forwTriplet.getSelectors().get(0).getSelection().get(),
							forwTriplet.getSelectors().get(1).getSelection().get(),
							forwTriplet.getSelectors().get(2).getSelection().get()));
		}
		else
			throw new IllegalStateException(String.format(
					"LineUpChooserController.saveLineUp: Untimely Request\n" +
					"no choice of LineUp is present on this Controller"));
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
