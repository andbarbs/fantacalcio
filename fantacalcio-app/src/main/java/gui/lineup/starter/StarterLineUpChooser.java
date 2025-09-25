package gui.lineup.starter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import domainModel.LineUp.LineUpBuilderSteps.StarterLineUp;
import domainModel.Player;
import domainModel.Player.*;
import domainModel.Scheme;
import domainModel.scheme.Scheme343;
import domainModel.scheme.Scheme433;
import domainModel.scheme.Scheme532;
import gui.lineup.chooser.LineUpChooser.StarterLineUpChooserDelegate;
import gui.lineup.chooser.LineUpChooser.StarterSelectorDelegate;
import gui.lineup.chooser.Selector;

public class StarterLineUpChooser implements StarterLineUpChooserController, StarterLineUpChooserDelegate {	

	// injected dependencies
	private StarterSelectorDelegate<Goalkeeper> goalieSelector;
	private List<StarterSelectorDelegate<Defender>> defSelectors;
	private List<StarterSelectorDelegate<Midfielder>> midSelectors;
	private List<StarterSelectorDelegate<Forward>> forwSelectors;
	
	private Collection<Selector<? extends Player>> selectorsIn532;
	private Collection<Selector<? extends Player>> selectorsIn433;
	private Collection<Selector<? extends Player>> selectorsIn343;

	Scheme currentScheme;
	
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
			StarterSelectorDelegate<Forward> forwSelector3) {

		this.goalieSelector = Objects.requireNonNull(goalieSelector);
		this.defSelectors = List.of(
				Objects.requireNonNull(defSelector1), 
				Objects.requireNonNull(defSelector2),
				Objects.requireNonNull(defSelector3), 
				Objects.requireNonNull(defSelector4),
				Objects.requireNonNull(defSelector5));
		this.midSelectors = List.of(
				Objects.requireNonNull(midSelector1), 
				Objects.requireNonNull(midSelector2),
				Objects.requireNonNull(midSelector3), 
				Objects.requireNonNull(midSelector4));
		this.forwSelectors = List.of(
				Objects.requireNonNull(forwSelector1), 
				Objects.requireNonNull(forwSelector2),
				Objects.requireNonNull(forwSelector3));
		
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
	
	// TODO remove this member and its tests
	public boolean hasChoice() {
		var visitor = new Scheme.SchemeVisitor() {
			boolean selectionExists;

			@Override
			public void visitScheme433(Scheme433 scheme433) {
				selectionExists = selectorsIn433.stream().allMatch(
						selector -> selector.getSelection().isPresent());
			}

			@Override
			public void visitScheme343(Scheme343 scheme343) {
				selectionExists = selectorsIn343.stream().allMatch(
						selector -> selector.getSelection().isPresent());
			}

			@Override
			public void visitScheme532(Scheme532 scheme532) {
				selectionExists = selectorsIn532.stream().allMatch(
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void switchToDefaultScheme() {
		// TODO Auto-generated method stub
		
	}
}
