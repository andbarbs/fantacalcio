package gui.lineup.chooser;

import java.util.Collection;
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
import domainModel.scheme.Scheme433;
import gui.lineup.chooser.Selector.SelectorListener;
import gui.lineup.dealing.CompetitiveOptionDealingGroup;
import gui.lineup.dealing.CompetitiveOptionDealingGroup.CompetitiveOrderedDealer;
import gui.lineup.selectors.StarterPlayerSelector;
import gui.lineup.selectors.SwingSubPlayerSelector;
import gui.lineup.sequence.FillableSwappableSequence;
import gui.lineup.sequence.FillableSwappableSequence.FillableSwappable;
import gui.lineup.starter.StarterLineUpChooser;
import gui.lineup.starter.SwingStarterLineUpChooserWidget;
import gui.utils.schemes.Spring343Scheme;
import gui.utils.schemes.Spring433Scheme;
import gui.utils.schemes.Spring532Scheme;
import gui.utils.schemes.SpringSchemePanel;

public class LineUpChooser implements LineUpChooserController {
	
	// 1. Interfaces and fields for collaborators
	
	/**
	 * a type for a {@linkplain Selector} that can be made to participate in a
	 * {@linkplain CompetitiveOptionDealingGroup} made up of other
	 * {@linkplain StarterSelectorDelegate}s.
	 * 
	 * @param <T> the type for options on this {@code Selector}
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
	 */
	public interface SubstituteSelectorDelegate<T>
			extends StarterSelectorDelegate<T>, FillableSwappable<SubstituteSelectorDelegate<T>> {

	}

	/**
	 * specifies the code-facing side of a component allowing users to pick a choice
	 * of a <i>starter line-up</i> - both scheme and players, the latter through the
	 * use of {@link StarterSelectorDelegate}s.
	 * 
	 * <p>
	 * Specifically, it enables clients to
	 * <ol>
	 * <li>access only {@link Selector}s corresponding to the currently chosen
	 * scheme - effectively encapsulating scheme changes - through
	 * <i>current-scheme</i> getters
	 * <li>configure the processing of {@link Selector}s as they join and leave the
	 * current scheme, through the setting of {@code Consumer}s
	 * <li>retrieve the user's choice of a starter line-up as a
	 * {@link StarterLineUp} instance
	 * <li>access all composed {@link StarterSelectorDelegate}s, through
	 * <i>all-selectors</i> getters
	 * </ol>
	 * 
	 * <h1>Getter-Consumer consistency</h1> At {@code Consumer} execution,
	 * <i>current-scheme</i> getters reflect the composition of the scheme <i>being
	 * transitioned to</i>, and <b>not</b> the old one
	 */
	public interface StarterLineUpChooserDelegate {

		/**
		 * @return the {@linkplain StarterSelectorDelegate} that is responsible for
		 *         the selection of a {@code Goalkeeper} inside the
		 *         {@linkplain StarterSelectorDelegate}
		 */
		StarterSelectorDelegate<Goalkeeper> getGoalieSelector();
		
		/**
		 * @return a {@code Set} containing all {@linkplain StarterSelectorDelegate}s
		 *         that are responsible for the selection of a {@code Defender} inside
		 *         the {@linkplain StarterSelectorDelegate}
		 */
		Set<StarterSelectorDelegate<Defender>> getAllDefSelectors();
		
		/**
		 * @return a {@code Set} containing all {@linkplain StarterSelectorDelegate}s
		 *         that are responsible for the selection of a {@code Midfielder} inside
		 *         the {@linkplain StarterSelectorDelegate}
		 */
		Set<StarterSelectorDelegate<Midfielder>> getAllMidSelectors();
		
		/**
		 * @return a {@code Set} containing all {@linkplain StarterSelectorDelegate}s
		 *         that are responsible for the selection of a {@code Forward} inside
		 *         the {@linkplain StarterSelectorDelegate}
		 */
		Set<StarterSelectorDelegate<Forward>> getAllForwSelectors();

		/**
		 * @return a {@code Set} containing all {@linkplain Selector}s that are
		 *         responsible for the selection of a {@code Defender} in the scheme
		 *         that is current on the {@linkplain StarterSelectorDelegate}
		 * @see {@linkplain StarterSelectorDelegate} for requirements on current-scheme
		 *      getters at {@code Consumer} execution
		 */
		Set<Selector<Defender>> getCurrentDefSelectors();
		
		/**
		 * @return a {@code Set} containing all {@linkplain Selector}s that are
		 *         responsible for the selection of a {@code Midfielder} in the scheme
		 *         that is current on the {@linkplain StarterSelectorDelegate}
		 * @see {@linkplain StarterSelectorDelegate} for requirements on current-scheme
		 *      getters at {@code Consumer} execution
		 */
		Set<Selector<Midfielder>> getCurrentMidSelectors();
		
		/**
		 * @return a {@code Set} containing all {@linkplain Selector}s that are
		 *         responsible for the selection of a {@code Forward} in the scheme that
		 *         is current on the {@linkplain StarterSelectorDelegate}
		 * @see {@linkplain StarterSelectorDelegate} for requirements on current-scheme
		 *      getters at {@code Consumer} execution
		 */
		Set<Selector<Forward>> getCurrentForwSelectors();

		/**
		 * @param entryDefConsumer the {@code Consumer} that
		 *                         {@linkplain StarterSelectorDelegate} should apply to
		 *                         {@linkplain Selector}s responsible for the selection
		 *                         of a {@code Defender} when they <b>enter</b> the
		 *                         current scheme
		 * @see {@linkplain StarterSelectorDelegate} for requirements on current-scheme
		 *      getters at {@code Consumer} execution
		 */
		void setEntryDefConsumer(Consumer<Selector<Defender>> entryDefConsumer);
		
		/**
		 * @param entryMidConsumer the {@code Consumer} that
		 *                         {@linkplain StarterSelectorDelegate} should apply to
		 *                         {@linkplain Selector}s responsible for the selection
		 *                         of a {@code Midfielder} when they <b>enter</b> the
		 *                         current scheme
		 * @see {@linkplain StarterSelectorDelegate} for requirements on current-scheme
		 *      getters at {@code Consumer} execution
		 */
		void setEntryMidConsumer(Consumer<Selector<Midfielder>> entryMidConsumer);
		
		/**
		 * @param entryForwConsumer the {@code Consumer} that
		 *                         {@linkplain StarterSelectorDelegate} should apply to
		 *                         {@linkplain Selector}s responsible for the selection
		 *                         of a {@code Forward} when they <b>enter</b> the
		 *                         current scheme
		 * @see {@linkplain StarterSelectorDelegate} for requirements on current-scheme
		 *      getters at {@code Consumer} execution
		 */
		void setEntryForwConsumer(Consumer<Selector<Forward>> entryForwConsumer);
		
		/**
		 * @param exitDefConsumer the {@code Consumer} that
		 *                         {@linkplain StarterSelectorDelegate} should apply to
		 *                         {@linkplain Selector}s responsible for the selection
		 *                         of a {@code Defender} when they <b>leave</b> the
		 *                         current scheme
		 * @see {@linkplain StarterSelectorDelegate} for requirements on current-scheme
		 *      getters at {@code Consumer} execution
		 */
		void setExitDefConsumer(Consumer<Selector<Defender>> exitDefConsumer);
		
		/**
		 * @param exitMidConsumer the {@code Consumer} that
		 *                         {@linkplain StarterSelectorDelegate} should apply to
		 *                         {@linkplain Selector}s responsible for the selection
		 *                         of a {@code Midfielder} when they <b>leave</b> the
		 *                         current scheme
		 * @see {@linkplain StarterSelectorDelegate} for requirements on current-scheme
		 *      getters at {@code Consumer} execution
		 */
		void setExitMidConsumer(Consumer<Selector<Midfielder>> exitMidConsumer);
		
		/**
		 * @param exitForwConsumer the {@code Consumer} that
		 *                         {@linkplain StarterSelectorDelegate} should apply to
		 *                         {@linkplain Selector}s responsible for the selection
		 *                         of a {@code Forward} when they <b>leave</b> the
		 *                         current scheme
		 * @see {@linkplain StarterSelectorDelegate} for requirements on current-scheme
		 *      getters at {@code Consumer} execution
		 */
		void setExitForwConsumer(Consumer<Selector<Forward>> exitForwConsumer);
		
		/**
		 * @return the {@linkplain StarterLineUp} instance that can be constructed from
		 *         {@code Selectors} in the current scheme, assuming they all bear a
		 *         selection
		 */
		StarterLineUp getCurrentStarterLineUp();
		
		/**
		 * instructs the {@linkplain StarterSelectorDelegate} to set its default scheme
		 * as the current one
		 */
		void switchToDefaultScheme();
	}

	private final StarterLineUpChooserDelegate starterChooser;

	/**
	 * specifies the code-facing side of a component allowing users to pick a choice
	 * of a <i>substitute line-up</i> for a given role, through the use of
	 * <b>three</b> {@link SubstituteSelectorDelegate}s arranged into a
	 * {@link FillableSwappableSequence}.
	 * 
	 * <p>
	 * Specifically, it enables clients to
	 * <ol>
	 * <li>access all composed {@link SubstituteSelectorDelegate}s, in the same
	 * order as they appear to the user
	 * <li>access the last non-filled {@link Selector} in the
	 * {@link FillableSwappableSequence Sequence}, if existing
	 * <li>request that the {@link FillableSwappableSequence Sequence} be
	 * initialized
	 * </ol>
	 * 
	 * @param <T> the role of {@link Player} that this triplet is responsible for
	 */
	public interface SubstituteTripletChooserDelegate<T extends Player> {
		
		/**
		 * @return a {@code List} containing the three composed
		 *         {@link SubstituteSelectorDelegate}s, in the same order as they appear
		 *         to the user
		 */
		List<SubstituteSelectorDelegate<T>> getSelectors();

		/**
		 * @return an {@code Optional} containing the last non-filled {@link Selector}
		 *         in the {@link FillableSwappableSequence Sequence}, or an empty one if
		 *         such a {@link Selector} does not exist
		 */
		Optional<Selector<T>> getNextFillableSelector();

		/**
		 * requests a {@link SubstituteTripletChooserDelegate} to initialize itself on a
		 * new {@link FillableSwappableSequence} containing its tree composed
		 * {@link SubstituteSelectorDelegate}s
		 */
		void initSequence();
	}

	private final SubstituteTripletChooserDelegate<Goalkeeper> goalieTriplet;
	private final SubstituteTripletChooserDelegate<Defender> defTriplet;
	private final SubstituteTripletChooserDelegate<Midfielder> midTriplet;
	private final SubstituteTripletChooserDelegate<Forward> forwTriplet;

	private LineUpChooserWidget widget;

	@Override
	public void setWidget(LineUpChooserWidget widget) {
		this.widget = widget;
	}
	
	private final UserService service;
	
	
	// 2. internal bookkeeping
	
	/**
	 * a type for a wrapper of {@code boolean} that enables
	 * <ul>
	 * <li><i>per-role</i> choice flags, reducing the cost of choice lookups
	 * <li>mutating these flags inside {@code Listener}s and {@code Consumer}s
	 * </ul>
	 * and is non-private for ease of set-up in unit tests
	 */
	static class BooleanWrapper {
		boolean flag;
		
		private boolean flag() {
			return flag;
		}
	}

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

	
	// 3. public instantiation point
	
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

	/**
	 * assembles a {@link SelectorListener} that is responsible for keeping a choice
	 * flag consistent with the selection state of a {@code Collection} of
	 * {@link Selector}s
	 * 
	 * @param <T>              the role of {@link Player} in {@link Selector}s being
	 *                         listened to
	 * @param flagWrapper      the {@link BooleanWrapper} containing the flag that
	 *                         this listener is responsible for
	 * @param selectorSupplier a {@code Supplier} that provides the
	 *                         {@link Selector}s that this listener should monitor
	 * @return a {@link SelectorListener} so construed
	 */
	private <T extends Player> SelectorListener<T> listener(BooleanWrapper flagWrapper,
			Supplier<Collection<? extends Selector<T>>> selectorSupplier) {
		return new SelectorListener<T>() {
	
			@Override
			public void selectionMadeOn(Selector<T> selector) {
				if (flagWrapper.flag = selectorSupplier.get().stream().map(Selector::getSelection)
						.allMatch(Optional::isPresent))
					if (hasChoice())
						widget.enableSavingLineUp();
			}
	
			@Override
			public void selectionClearedOn(Selector<T> selector) {
				if (flagWrapper.flag = selectorSupplier.get().stream().map(Selector::getSelection)
						.allMatch(Optional::isPresent)) {
					if (hasChoice())   // why???
						widget.enableSavingLineUp();
				} else {
						widget.disableSavingLineUp();
				}
			}
		};
	}

	/**
	 * assembles a {@code Consumer} that is responsible for processing
	 * {@link Selector}s as they <b>enter</b> the current scheme on
	 * {@link StarterSelectorDelegate}, by
	 * <ul>
	 * <li>attaching the appropriate {@link SelectorListener} to them
	 * <li>setting the relevant group choice flag to {@code false}, as
	 * {@link Selector}s will always join the current scheme while empty
	 * </ul>
	 * 
	 * @param <T>                the role of {@link Player} in {@link Selector}s
	 *                           being processed
	 * @param starterDefListener the {@link SelectorListener} to be attached
	 * @param hasGroupChoice     the group choice flag that should be negated
	 * @return a {@code Consumer<Selector>} so construed
	 */
	private <T extends Player> Consumer<Selector<T>> entryConsumer(SelectorListener<T> starterDefListener,
			BooleanWrapper hasGroupChoice) {
		return selector -> {
			selector.attachListener(starterDefListener);
			hasGroupChoice.flag = false;
		};
	}

	/**
	 * assembles a {@code Consumer} that is responsible for processing
	 * {@link Selector}s as they <b>exit</b> the current scheme on
	 * {@link StarterSelectorDelegate}, by
	 * <ul>
	 * <li>removing the appropriate {@link SelectorListener} from them
	 * <li>if they bear a selection, transferring it to the last non-filled
	 * {@link Selector} in the relevant {@link SubstituteTripletChooserDelegate}
	 * <li>updating the relevant group choice flag to whatever emerges on the
	 * current scheme following this {@code Selector}'s departure
	 * </ul>
	 * 
	 * @param <T>                    the role of {@link Player} in {@link Selector}s
	 *                               being processed
	 * @param listener               the {@link SelectorListener} to be removed
	 * @param triplet                the {@link SubstituteTripletChooserDelegate}
	 *                               that selections should be transferred to
	 * @param hasGroupChoice         the group choice flag that should be updated
	 * @param currentSchemeSelectors a {@code Supplier} that provides the
	 *                               {@link Selector}s in the scheme that
	 *                               {@link StarterSelectorDelegate} is
	 *                               <b>transitioning to</b>
	 * @return a {@code Consumer<Selector>} so construed
	 */
	private <T extends Player> Consumer<Selector<T>> exitConsumer(SelectorListener<T> listener,
			SubstituteTripletChooserDelegate<T> triplet, BooleanWrapper hasGroupChoice,
			Supplier<Collection<? extends Selector<T>>> currentSchemeSelectors) {
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
	
	
	// 4. public configuration point & bookkeeping

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
					.flatMap(Collection::stream).collect(Collectors.toSet()),
				team.extract().defenders().stream()
						.sorted(Comparator.comparing(Player::getSurname))
						.collect(Collectors.toList()));
		
		CompetitiveOptionDealingGroup.initializeDealing(
				Stream.of(starterChooser.getAllMidSelectors(), midTriplet.getSelectors())
					.flatMap(Collection::stream).collect(Collectors.toSet()),
				team.extract().midfielders().stream()
						.sorted(Comparator.comparing(Player::getSurname))
						.collect(Collectors.toList()));
		
		CompetitiveOptionDealingGroup.initializeDealing(
				Stream.of(starterChooser.getAllForwSelectors(), forwTriplet.getSelectors())
					.flatMap(Collection::stream).collect(Collectors.toSet()),
				team.extract().forwards().stream()
						.sorted(Comparator.comparing(Player::getSurname))
						.collect(Collectors.toList()));
		
		// initializes sequences
		goalieTriplet.initSequence();
		defTriplet.initSequence();
		midTriplet.initSequence();
		forwTriplet.initSequence();
		
		// orders Starter Delegate
		starterChooser.switchToDefaultScheme();
		
		// TODO should reset to false all choice flags?
	}
	
	
	// 5. MVP Widget notification point

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

				starterChooser.switchToScheme(Scheme433.INSTANCE);

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
