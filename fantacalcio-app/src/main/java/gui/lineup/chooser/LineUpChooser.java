package gui.lineup.chooser;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import business.UserService;
import domain.Contract;
import domain.FantaTeam;
import domain.FantaUser;
import domain.League;
import domain.LineUp;
import domain.Match;
import domain.Player;
import domain.Scheme;
import domain.LineUp.LineUpBuilderSteps.StarterLineUp;
import domain.Player.*;
import domain.scheme.Scheme433;
import gui.lineup.chooser.Selector.SelectorListener;
import gui.lineup.dealing.CompetitiveOptionDealingGroup;
import gui.lineup.dealing.CompetitiveOptionDealingGroup.CompetitiveOrderedDealer;
import gui.lineup.selectors.StarterPlayerSelector;
import gui.lineup.selectors.SubstitutePlayerSelector;
import gui.lineup.selectors.SwingSubPlayerSelector;
import gui.lineup.sequence.FillableSwappableSequence;
import gui.lineup.sequence.FillableSwappableSequence.FillableSwappable;
import gui.lineup.starter.StarterLineUpChooser;
import gui.lineup.starter.SwingStarterLineUpChooserWidget;
import gui.lineup.triplet.FillableSwappableTriplet;
import gui.lineup.triplet.SwingFillableSwappableTripletWidget;
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
	 * Specifically, it enables programmatic clients to
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
		 * instructs the {@linkplain StarterSelectorDelegate} to set the provided scheme
		 * as the current one, whether a previous current scheme exists or not
		 * 
		 * @param scheme the {@link Scheme} to be set as current
		 */
		void setCurrentScheme(Scheme scheme);
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
		
		// TODO if sequence wiring is up to the triplet, 
		// this getter could return StarterSelectorDelegates!! <-- obviously, rename this type
		
		/**
		 * @return a {@code List} containing the three composed
		 *         {@link SubstituteSelectorDelegate}s, in the same order as they appear
		 *         to the user
		 */
		List<SubstituteSelectorDelegate<T>> getSelectors();
		
		/**
		 * requests a {@link SubstituteTripletChooserDelegate} to initialize itself on a
		 * new {@link FillableSwappableSequence} containing its tree composed
		 * {@link SubstituteSelectorDelegate}s
		 */
		void initSequence();

		/**
		 * @return an {@code Optional} containing the last non-filled
		 *         {@link SubstituteSelectorDelegate} in the
		 *         {@link SubstituteTripletChooserDelegate triplet}'s internal
		 *         {@link FillableSwappableSequence Sequence}, or an empty one if such a
		 *         {@link SubstituteSelectorDelegate Selector} does not exist
		 */
		Optional<SubstituteSelectorDelegate<T>> getNextFillable();
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
	 * flag consistent with the selection state of a variable {@code Collection} of
	 * {@link Selector}s
	 * 
	 * @param <T>              the role of {@link Player} in {@link Selector}s being
	 *                         listened to
	 * @param flagWrapper      the {@link BooleanWrapper} containing the flag that
	 *                         this listener is responsible for
	 * @param selectorSupplier a {@code Supplier} providing the {@link Selector}s
	 *                         that this listener should monitor
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
				if (hasChoice())
					widget.disableSavingLineUp();
				flagWrapper.flag = false;
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
			Optional<T> exitingSelection = exitingSelector.getSelection();
			if (exitingSelection.isPresent()) {
				exitingSelector.setSelection(Optional.empty());
				triplet.getNextFillable()
						.ifPresent(subSel -> subSel.setSelection(exitingSelection));
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
		starterChooser.setCurrentScheme(Scheme433.INSTANCE);
		
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

			Dimension screenSize = frame.getToolkit().getScreenSize();
			Dimension availableWindow = new Dimension((int) (screenSize.width * 0.3), screenSize.height);
			Dimension selectorDims = SpringSchemePanel.recommendedSlotDimensions(
					SwingStarterLineUpChooserWidget.eventualFieldDimension(availableWindow));
			
			// I) instantiates and wires Starter and Substitute Selectors
			SwingSubPlayerSelector<Goalkeeper> goalieStartView = new SwingSubPlayerSelector<Goalkeeper>(selectorDims),
					goalieSubView1 = new SwingSubPlayerSelector<Goalkeeper>(selectorDims),
					goalieSubView2 = new SwingSubPlayerSelector<Goalkeeper>(selectorDims),
					goalieSubView3 = new SwingSubPlayerSelector<Goalkeeper>(selectorDims);
			StarterPlayerSelector<Goalkeeper> goalieStartPres = new StarterPlayerSelector<>(goalieStartView);
			goalieStartView.setController(goalieStartPres);
			SubstitutePlayerSelector<Goalkeeper> goalieSubPres1 = new SubstitutePlayerSelector<>(goalieSubView1),
					goalieSubPres2 = new SubstitutePlayerSelector<>(goalieSubView2),
					goalieSubPres3 = new SubstitutePlayerSelector<>(goalieSubView3);
			goalieSubView1.setController(goalieSubPres1);
			goalieSubView2.setController(goalieSubPres2);
			goalieSubView3.setController(goalieSubPres3);
			
			SwingSubPlayerSelector<Defender> defStartView1 = new SwingSubPlayerSelector<Defender>(selectorDims),
					defStartView2 = new SwingSubPlayerSelector<Defender>(selectorDims),
					defStartView3 = new SwingSubPlayerSelector<Defender>(selectorDims),
					defStartView4 = new SwingSubPlayerSelector<Defender>(selectorDims),
					defStartView5 = new SwingSubPlayerSelector<Defender>(selectorDims),
					defSubView1 = new SwingSubPlayerSelector<Defender>(selectorDims),
					defSubView2 = new SwingSubPlayerSelector<Defender>(selectorDims),
					defSubView3 = new SwingSubPlayerSelector<Defender>(selectorDims);
			StarterPlayerSelector<Defender> defStartPres1 = new StarterPlayerSelector<Defender>(defStartView1),
					defStartPres2 = new StarterPlayerSelector<Defender>(defStartView2),
					defStartPres3 = new StarterPlayerSelector<Defender>(defStartView3),
					defStartPres4 = new StarterPlayerSelector<Defender>(defStartView4),
					defStartPres5 = new StarterPlayerSelector<Defender>(defStartView5);
			defStartView1.setController(defStartPres1);
			defStartView2.setController(defStartPres2);
			defStartView3.setController(defStartPres3);
			defStartView4.setController(defStartPres4);
			defStartView5.setController(defStartPres5);
			SubstitutePlayerSelector<Defender> defSubPres1 = new SubstitutePlayerSelector<>(defSubView1),
					defSubPres2 = new SubstitutePlayerSelector<>(defSubView2),
					defSubPres3 = new SubstitutePlayerSelector<>(defSubView3);
			defSubView1.setController(defSubPres1);
			defSubView2.setController(defSubPres2);
			defSubView3.setController(defSubPres3);
			
			SwingSubPlayerSelector<Midfielder> midStartView1 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
					midStartView2 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
					midStartView3 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
					midStartView4 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
					midSubView1 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
					midSubView2 = new SwingSubPlayerSelector<Midfielder>(selectorDims),
					midSubView3 = new SwingSubPlayerSelector<Midfielder>(selectorDims);
			StarterPlayerSelector<Midfielder> midStartPres1 = new StarterPlayerSelector<Midfielder>(midStartView1),
					midStartPres2 = new StarterPlayerSelector<Midfielder>(midStartView2),
					midStartPres3 = new StarterPlayerSelector<Midfielder>(midStartView3),
					midStartPres4 = new StarterPlayerSelector<Midfielder>(midStartView4);
			midStartView1.setController(midStartPres1);
			midStartView2.setController(midStartPres2);
			midStartView3.setController(midStartPres3);
			midStartView4.setController(midStartPres4);
			SubstitutePlayerSelector<Midfielder> midSubPres1 = new SubstitutePlayerSelector<>(midSubView1),
					midSubPres2 = new SubstitutePlayerSelector<>(midSubView2),
					midSubPres3 = new SubstitutePlayerSelector<>(midSubView3);
			midSubView1.setController(midSubPres1);
			midSubView2.setController(midSubPres2);
			midSubView3.setController(midSubPres3);
			
			SwingSubPlayerSelector<Forward> forwStartView1 = new SwingSubPlayerSelector<Forward>(selectorDims),
					forwStartView2 = new SwingSubPlayerSelector<Forward>(selectorDims),
					forwStartView3 = new SwingSubPlayerSelector<Forward>(selectorDims),
					forwStartView4 = new SwingSubPlayerSelector<Forward>(selectorDims),
					forwSubView1 = new SwingSubPlayerSelector<Forward>(selectorDims),
					forwSubView2 = new SwingSubPlayerSelector<Forward>(selectorDims),
					forwSubView3 = new SwingSubPlayerSelector<Forward>(selectorDims);
			StarterPlayerSelector<Forward> forwStartPres1 = new StarterPlayerSelector<Forward>(forwStartView1),
					forwStartPres2 = new StarterPlayerSelector<Forward>(forwStartView2),
					forwStartPres3 = new StarterPlayerSelector<Forward>(forwStartView3),
					forwStartPres4 = new StarterPlayerSelector<Forward>(forwStartView4);
			forwStartView1.setController(forwStartPres1);
			forwStartView2.setController(forwStartPres2);
			forwStartView3.setController(forwStartPres3);
			forwStartView4.setController(forwStartPres4);
			SubstitutePlayerSelector<Forward> forwSubPres1 = new SubstitutePlayerSelector<>(forwSubView1),
					forwSubPres2 = new SubstitutePlayerSelector<>(forwSubView2),
					forwSubPres3 = new SubstitutePlayerSelector<>(forwSubView3);
			forwSubView1.setController(forwSubPres1);
			forwSubView2.setController(forwSubPres2);
			forwSubView3.setController(forwSubPres3);
			
			// II) instantiates and wires Starter Chooser
			SwingStarterLineUpChooserWidget starterWidget = new SwingStarterLineUpChooserWidget(
					false, 
					availableWindow, 						
					List.of(new Spring433Scheme(false), new Spring343Scheme(false), new Spring532Scheme(false)), 
					goalieStartView, 
					List.of(defStartView1, defStartView2, defStartView3, defStartView4, defStartView5), 
					List.of(midStartView1, midStartView2, midStartView3, midStartView4), 						
					List.of(forwStartView1, forwStartView2, forwStartView3));
			
			StarterLineUpChooser starterChooser = new StarterLineUpChooser(
					goalieStartPres, 
					List.of(defStartPres1, defStartPres2, defStartPres3, defStartPres4, defStartPres5), 
					List.of(midStartPres1, midStartPres2, midStartPres3, midStartPres4), 
					List.of(forwStartPres1, forwStartPres2, forwStartPres3));
			
			starterWidget.setController(starterChooser);
			starterChooser.setWidget(starterWidget);
			
			// III) instantiates and wires Triplets
			FillableSwappableTriplet<Goalkeeper> goalieTriplet = new FillableSwappableTriplet<Goalkeeper>(goalieSubPres1, goalieSubPres2, goalieSubPres3);
			SwingFillableSwappableTripletWidget goalieTripletWidget = new SwingFillableSwappableTripletWidget(
					false, goalieSubView1, goalieSubView2, goalieSubView3);
			goalieTriplet.setWidget(goalieTripletWidget);
			goalieTripletWidget.setController(goalieTriplet);	
			
			FillableSwappableTriplet<Defender> defTriplet = new FillableSwappableTriplet<Defender>(defSubPres1, defSubPres2, defSubPres3);
			SwingFillableSwappableTripletWidget defTripletWidget = new SwingFillableSwappableTripletWidget(
					false, defSubView1, defSubView2, defSubView3);
			defTriplet.setWidget(defTripletWidget);
			defTripletWidget.setController(defTriplet);	
			
			FillableSwappableTriplet<Midfielder> midTriplet = new FillableSwappableTriplet<Midfielder>(midSubPres1, midSubPres2, midSubPres3);
			SwingFillableSwappableTripletWidget midTripletWidget = new SwingFillableSwappableTripletWidget(
					false, midSubView1, midSubView2, midSubView3);
			midTriplet.setWidget(midTripletWidget);
			midTripletWidget.setController(midTriplet);	
			
			FillableSwappableTriplet<Forward> forwTriplet = new FillableSwappableTriplet<Forward>(forwSubPres1, forwSubPres2, forwSubPres3);
			SwingFillableSwappableTripletWidget forwTripletWidget = new SwingFillableSwappableTripletWidget(
					false, forwSubView1, forwSubView2, forwSubView3);
			forwTriplet.setWidget(forwTripletWidget);
			forwTripletWidget.setController(forwTriplet);
			
			// IV) instantiates and wires Chooser
			SwingLineUpChooserWidget chooserWidget = new SwingLineUpChooserWidget(false, 
					starterWidget, 
					goalieTripletWidget, defTripletWidget, midTripletWidget, forwTripletWidget);
			
			LineUpChooser chooser = new LineUpChooser(null, 
					starterChooser, 
					goalieTriplet, defTriplet, midTriplet, forwTriplet);
			chooserWidget.setController(chooser);
			chooser.setWidget(chooserWidget);
			
			// V) creates a demo FantaTeam and Match
			Goalkeeper keeper1 = new Goalkeeper("Manuel", "Neuer", Club.JUVENTUS),
					keeper2 = new Goalkeeper("Alisson", "Becker", Club.JUVENTUS),
					keeper3 = new Goalkeeper("David", "de Gea", Club.JUVENTUS),
					keeper4 = new Goalkeeper("Ederson", "Motta", Club.JUVENTUS),
					keeper5 = new Goalkeeper("Jan", "Oblak", Club.JUVENTUS);
	        Defender defender1 = new Defender("Virgil", "van Dijk", Club.JUVENTUS),
	        		defender2 = new Defender("Sergio", "Ramos", Club.JUVENTUS),
	        		defender3 = new Defender("Raphael", "Varane", Club.JUVENTUS),
	        		defender4 = new Defender("Gerard", "Piqué", Club.JUVENTUS),
	        		defender5 = new Defender("Thiago", "Silva", Club.JUVENTUS);
	        Midfielder midfielder1 = new Midfielder("Luka", "Modrić", Club.TORINO),
	        		midfielder2 = new Midfielder("Andrés", "Iniesta", Club.TORINO),
	        		midfielder3 = new Midfielder("Kevin", "De Bruyne", Club.TORINO),
	        		midfielder4 = new Midfielder("N'Golo", "Kanté", Club.TORINO),
	        		midfielder5 = new Midfielder("Toni", "Kroos", Club.TORINO);
	        Forward forward1 = new Forward("Lionel", "Messi", Club.TORINO),
	        		forward2 = new Forward("Cristiano", "Ronaldo", Club.TORINO),
	        		forward3 = new Forward("Neymar", "Jr", Club.TORINO),
	        		forward4 = new Forward("Robert", "Lewandowski", Club.TORINO),
	        		forward5 = new Forward("Kylian", "Mbappé", Club.TORINO);

	        Set<Contract> contracts = new HashSet<>();
	        FantaTeam team = new FantaTeam("Elite Team", null, 0, null, contracts);
	        contracts.addAll(Stream.of(
	        				keeper1, keeper2, keeper3, keeper4, keeper5,
	        				defender1, defender2, defender3, defender4, defender5,
	        				midfielder1, midfielder2, midfielder3, midfielder4, midfielder5,
	        				forward1, forward2, forward3, forward4, forward5)
	        	    .map(player -> new Contract(team, player))
	        	    .collect(Collectors.toSet()));
	        
	        Match match = new Match(null, team, team);
			
	        // VI) initializes Chooser to demo Team and Match
	        chooser.initTo(team, match);
			
			frame.setContentPane(chooserWidget);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}
