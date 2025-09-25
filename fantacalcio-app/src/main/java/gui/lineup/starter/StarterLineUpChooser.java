package gui.lineup.starter;

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
import gui.lineup.chooser.Selector;

public class StarterLineUpChooser implements StarterLineUpChooserController, StarterLineUpChooserDelegate {

	// injected dependencies
	private StarterSelectorDelegate<Goalkeeper> goalieSelector;
	private List<StarterSelectorDelegate<Defender>> defSelectors;
	private List<StarterSelectorDelegate<Midfielder>> midSelectors;
	private List<StarterSelectorDelegate<Forward>> forwSelectors;

	// bookkeeping
	Scheme currentScheme;
	
	public StarterLineUpChooser(StarterSelectorDelegate<Goalkeeper> goalieSelector,
			List<StarterSelectorDelegate<Defender>> defenderSelectors,
			List<StarterSelectorDelegate<Midfielder>> midfielderSelectors,
			List<StarterSelectorDelegate<Forward>> forwardSelectors) {

		this.goalieSelector = Objects.requireNonNull(goalieSelector);
		this.defSelectors = defenderSelectors.stream().map(Objects::requireNonNull).collect(Collectors.toList());
		this.midSelectors = midfielderSelectors.stream().map(Objects::requireNonNull).collect(Collectors.toList());
		this.forwSelectors = forwardSelectors.stream().map(Objects::requireNonNull).collect(Collectors.toList());
	}
	
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
	
	// TODO implement and test these getters!
	@Override
	public Set<Selector<Defender>> getCurrentDefSelectors() {
		return defSelectors.stream().filter(selectorsIn(this.currentScheme)::contains).collect(Collectors.toSet());
	}
	
	@Override
	public Set<Selector<Midfielder>> getCurrentMidSelectors() {
		return midSelectors.stream().filter(selectorsIn(this.currentScheme)::contains).collect(Collectors.toSet());
	}
	
	@Override
	public Set<Selector<Forward>> getCurrentForwSelectors() {
		return forwSelectors.stream().filter(selectorsIn(this.currentScheme)::contains).collect(Collectors.toSet());
	}	
	
	// As a Controller


	// interface for vertical collaborator
	public interface StarterLineUpChooserWidget {
		void switchTo(Scheme scheme);
	}

	private StarterLineUpChooserWidget widget;
	
	public void setWidget(StarterLineUpChooserWidget widget) {
		this.widget = widget;
	}

	private Consumer<Selector<Defender>> entryDefConsumer;
	
	@Override
	public void setEntryDefConsumer(Consumer<Selector<Defender>> entryDefConsumer) {
		this.entryDefConsumer = entryDefConsumer;
	}

	private Consumer<Selector<Midfielder>> entryMidConsumer;
	
	@Override
	public void setEntryMidConsumer(Consumer<Selector<Midfielder>> entryMidConsumer) {
		this.entryMidConsumer = entryMidConsumer;
	}

	private Consumer<Selector<Forward>> entryForwConsumer;
	
	@Override
	public void setEntryForwConsumer(Consumer<Selector<Forward>> entryForwConsumer) {
		this.entryForwConsumer = entryForwConsumer;
	}

	private Consumer<Selector<Defender>> exitDefConsumer;
	
	@Override
	public void setExitDefConsumer(Consumer<Selector<Defender>> exitDefConsumer) {
		this.exitDefConsumer = exitDefConsumer;
	}

	private Consumer<Selector<Midfielder>> exitMidConsumer;
	
	@Override
	public void setExitMidConsumer(Consumer<Selector<Midfielder>> exitMidConsumer) {
		this.exitMidConsumer = exitMidConsumer;
	}

	private Consumer<Selector<Forward>> exitForwConsumer;

	@Override
	public void setExitForwConsumer(Consumer<Selector<Forward>> exitForwConsumer) {
		this.exitForwConsumer = exitForwConsumer;
	}

	@Override
	public void switchToScheme(Scheme newScheme) {
		
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
						.filter(selector -> !selectorsIn(newScheme).contains(selector))
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
						.filter(selector -> !selectorsIn(previousScheme).contains(selector))
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

	@Override
	public void setCurrentScheme(Scheme scheme) {
		switchToScheme(scheme);
	}
}
