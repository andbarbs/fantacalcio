package gui.lineup.starter;

import static java.util.function.Predicate.not;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import domainModel.LineUp.LineUpBuilderSteps.StarterLineUp;
import domainModel.Player;
import domainModel.Player.*;
import domainModel.Scheme;
import gui.lineup.chooser.LineUpChooser.StarterLineUpChooserDelegate;
import gui.lineup.chooser.LineUpChooser.StarterSelectorDelegate;
import gui.lineup.chooser.LineUpChooser.SubstituteSelectorDelegate;
import gui.lineup.chooser.Selector;

/**
 * implements {@link StarterLineUpChooserDelegate} as a
 * {@link StarterLineUpChooserController Controller} in a bidirectional
 * collaboration scheme inspired by the <b>MVP pattern</b>.
 * 
 * <p>
 * <h1>Dynamic {@link Scheme} support</h1> This type does not lock down the
 * number of {@link StarterSelectorDelegate}s it composes in order to define at
 * compile-time the {@link Scheme}s it can support. Instead, it
 * <ol>
 * <li>is instantiated on a {@code List<StarterSelectorDelegate<>>} of
 * <i>arbitrary size</i> for each role
 * <li>seeks to realize whatever {@link Scheme} is <i>dynamically requested</i>
 * by tapping into those {@code List}s, lower-indices first, throwing an error
 * if not enough {@link Selector}s are available
 * </ol>
 */
public class StarterLineUpChooser implements StarterLineUpChooserDelegate, StarterLineUpChooserController {

	// composed StarterSelectorDelegate instances
	private StarterSelectorDelegate<Goalkeeper> goalieSelector;
	private List<StarterSelectorDelegate<Defender>> defSelectors;
	private List<StarterSelectorDelegate<Midfielder>> midSelectors;
	private List<StarterSelectorDelegate<Forward>> forwSelectors;
	
	// Widget ref
	private StarterLineUpChooserWidget widget;

	public void setWidget(StarterLineUpChooserWidget widget) {
		this.widget = widget;
	}

	// bookkeeping, non-private for setup in unit tests
	Scheme currentScheme;
	
	/**
	 * @see {@link StarterLineUpChooser} for an explanation of this type's policy
	 *      for dynamically supporting {@link Scheme}s with only a fixed number of
	 *      composed {@link SubstituteSelectorDelegate} instances.
	 * 
	 * @param goalieSelector      a {@link SubstituteSelectorDelegate} for a
	 *                            {@link Goalkeeper}
	 * @param defenderSelectors   a {@code List} containing
	 *                            {@link SubstituteSelectorDelegate}s for a
	 *                            {@link Defender}
	 * @param midfielderSelectors a {@code List} containing
	 *                            {@link SubstituteSelectorDelegate}s for a
	 *                            {@link Midfielder}
	 * @param forwardSelectors    a {@code List} containing
	 *                            {@link SubstituteSelectorDelegate}s for a
	 *                            {@link Defender}
	 */
	public StarterLineUpChooser(StarterSelectorDelegate<Goalkeeper> goalieSelector,
			List<StarterSelectorDelegate<Defender>> defenderSelectors,
			List<StarterSelectorDelegate<Midfielder>> midfielderSelectors,
			List<StarterSelectorDelegate<Forward>> forwardSelectors) {

		this.goalieSelector = Objects.requireNonNull(goalieSelector);
		this.defSelectors = Objects.requireNonNull(defenderSelectors).stream().map(Objects::requireNonNull)
				.collect(Collectors.toList());
		this.midSelectors = Objects.requireNonNull(midfielderSelectors).stream().map(Objects::requireNonNull)
				.collect(Collectors.toList());
		this.forwSelectors = Objects.requireNonNull(forwardSelectors).stream().map(Objects::requireNonNull)
				.collect(Collectors.toList());
	}
	
	// 1. StarterLineUpChooserDelegate: all-selectors getters
	
	@Override
	public StarterSelectorDelegate<Goalkeeper> getGoalieSelector() {
		return goalieSelector;
	}

	@Override
	public Set<StarterSelectorDelegate<Defender>> getAllDefSelectors() {
		return Set.copyOf(defSelectors);
	}

	@Override
	public Set<StarterSelectorDelegate<Midfielder>> getAllMidSelectors() {
		return Set.copyOf(midSelectors);
	}
	
	@Override
	public Set<StarterSelectorDelegate<Forward>> getAllForwSelectors() {
		return Set.copyOf(forwSelectors);
	}
	
	// 2. StarterLineUpChooserDelegate: current-scheme getters
	
	@Override
	public Set<Selector<Defender>> getCurrentDefSelectors() {
		return defSelectors.stream()
				.limit(this.currentScheme == null ? 0 : this.currentScheme.getNumDefenders())
				.collect(Collectors.toSet());
	}
	
	@Override
	public Set<Selector<Midfielder>> getCurrentMidSelectors() {
		return midSelectors.stream()
				.limit(this.currentScheme == null ? 0 : this.currentScheme.getNumMidfielders())
				.collect(Collectors.toSet());
	}
	
	@Override
	public Set<Selector<Forward>> getCurrentForwSelectors() {
		return forwSelectors.stream()
				.limit(this.currentScheme == null ? 0 : this.currentScheme.getNumForwards())
				.collect(Collectors.toSet());
	}	
	
	// 3. StarterLineUpChooserDelegate: Consumer setters

	private Consumer<Selector<Defender>> entryDefConsumer, exitDefConsumer;
	private Consumer<Selector<Midfielder>> entryMidConsumer, exitMidConsumer;
	private Consumer<Selector<Forward>> entryForwConsumer, exitForwConsumer;

	@Override
	public void setEntryDefConsumer(Consumer<Selector<Defender>> entryDefConsumer) {
		this.entryDefConsumer = entryDefConsumer;
	}
	
	@Override
	public void setEntryMidConsumer(Consumer<Selector<Midfielder>> entryMidConsumer) {
		this.entryMidConsumer = entryMidConsumer;
	}
	
	@Override
	public void setEntryForwConsumer(Consumer<Selector<Forward>> entryForwConsumer) {
		this.entryForwConsumer = entryForwConsumer;
	}

	@Override
	public void setExitDefConsumer(Consumer<Selector<Defender>> exitDefConsumer) {
		this.exitDefConsumer = exitDefConsumer;
	}

	@Override
	public void setExitMidConsumer(Consumer<Selector<Midfielder>> exitMidConsumer) {
		this.exitMidConsumer = exitMidConsumer;
	}

	@Override
	public void setExitForwConsumer(Consumer<Selector<Forward>> exitForwConsumer) {
		this.exitForwConsumer = exitForwConsumer;
	}
	
	// 4. StarterLineUpChooserDelegate: StarterLineUp choice retrieval
	
	@Override
	public StarterLineUp getCurrentStarterLineUp() {
		return new StarterLineUp(
				currentScheme, 
				goalieSelector.getSelection().get(), 
				defSelectors.stream()
						.limit(currentScheme.getNumDefenders())
						.map(Selector::getSelection).map(Optional::get).collect(Collectors.toSet()),
				midSelectors.stream()
						.limit(currentScheme.getNumMidfielders())
						.map(Selector::getSelection).map(Optional::get).collect(Collectors.toSet()), 
				forwSelectors.stream()
						.limit(currentScheme.getNumForwards())
						.map(Selector::getSelection).map(Optional::get).collect(Collectors.toSet()));
	}
	
	// 5. StarterLineUpChooserDelegate: current scheme setting

	/**
	 * @implNote {@link StarterLineUpChooser} enforces consistency at runtime
	 *           between the requested {@link Scheme} and the number of
	 *           {@link StarterSelectorDelegate} instances it composes. If a
	 *           {@link Scheme} is requested which is infeasible, an {@code Error}
	 *           is thrown and no modifications or interactions ensue
	 */
	@Override
	public void setCurrentScheme(Scheme scheme) {
		switchToScheme(scheme);
	}
	
	// 6. StarterLineUpChooserController: scheme switching Widget notification point


	/**
	 * @implNote {@link StarterLineUpChooser} enforces consistency at runtime
	 *           between the requested {@link Scheme} and the number of
	 *           {@link StarterSelectorDelegate} instances it composes. If a
	 *           {@link Scheme} is requested which is infeasible, an {@code Error}
	 *           is thrown and no modifications or interactions ensue
	 */
	@Override
	public void switchToScheme(Scheme newScheme) {
		
		// TODO insert a NonNull check on Consumers (ensure full instantiation)
		
		// checks new Scheme is compatible with existing Selector numbers
		String format = "StarterLineUpChooser.switchToScheme: Unsatisfiable Request\n" +
				"requested Scheme has %d %ss, this composes only %d instances of Selector<%s>";
		if (newScheme.getNumDefenders() > defSelectors.size())
			throw new IllegalArgumentException(String.format(format, 
					newScheme.getNumDefenders(), Defender.class.getSimpleName(), 
					defSelectors.size(), Defender.class.getSimpleName()));
		if (newScheme.getNumMidfielders() > midSelectors.size())
			throw new IllegalArgumentException(String.format(format, 
					newScheme.getNumMidfielders(), Midfielder.class.getSimpleName(), 
					midSelectors.size(), Midfielder.class.getSimpleName()));
		if (newScheme.getNumForwards() > forwSelectors.size())
			throw new IllegalArgumentException(String.format(format, 
					newScheme.getNumForwards(), Forward.class.getSimpleName(), 
					forwSelectors.size(), Forward.class.getSimpleName()));
		
		// 1) saves old scheme
		Scheme previousScheme = currentScheme;		
		
		// 2) updates current scheme to ensure Consumer-getter consistency
		currentScheme = newScheme;
		
		// 3) processes exiting Selectors
		class ProcessExitingSelectors<T extends Player>
				implements BiConsumer<List<? extends Selector<T>>, Consumer<Selector<T>>> {

			@Override
			public void accept(List<? extends Selector<T>> selectors, Consumer<Selector<T>> consumer) {
				selectors.stream()
						.filter(selectorsIn(previousScheme)::contains)
						.filter(not(selectorsIn(newScheme)::contains))
						.forEach(consumer);
			}
		};
		
		new ProcessExitingSelectors<Defender>().accept(defSelectors, exitDefConsumer);
		new ProcessExitingSelectors<Midfielder>().accept(midSelectors, exitMidConsumer);
		new ProcessExitingSelectors<Forward>().accept(forwSelectors, exitForwConsumer);
		
		// 4) processes entering Selectors
		class ProcessEnteringSelectors<T extends Player>
				implements BiConsumer<List<? extends Selector<T>>, Consumer<Selector<T>>> {

			@Override
			public void accept(List<? extends Selector<T>> selectors, Consumer<Selector<T>> consumer) {
				selectors.stream()
						.filter(selectorsIn(newScheme)::contains)
						.filter(not(selectorsIn(previousScheme)::contains))
						.forEach(consumer);
			}
		};
		
		new ProcessEnteringSelectors<Defender>().accept(defSelectors, entryDefConsumer);
		new ProcessEnteringSelectors<Midfielder>().accept(midSelectors, entryMidConsumer);
		new ProcessEnteringSelectors<Forward>().accept(forwSelectors, entryForwConsumer);
		
		// 2) asks Widget to rearrange Selector widgets
		widget.switchTo(newScheme);
	}

	/**
	 * @param scheme the {@link Scheme} singleton for which {@link Selector}s are
	 *               queried, or {@code null}
	 * @return a {@code Collection} containing {@link Selector}s that make up the
	 *         given scheme on this {@link StarterLineUpChooserDelegate}, or an
	 *         empty one if {@code scheme} is {@code null}
	 */
	private Collection<Selector<? extends Player>> selectorsIn(Scheme scheme) {		
		return scheme == null ? List.of() : 
			Stream.of(defSelectors.stream().limit(scheme.getNumDefenders()),
				    midSelectors.stream().limit(scheme.getNumMidfielders()),
				    forwSelectors.stream().limit(scheme.getNumForwards()))
				.flatMap(s -> s)
				.collect(Collectors.toList());
	}
}
