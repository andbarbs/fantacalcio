package swingViews;

import java.util.Optional;
import domainModel.Player;
import swingViews.FillableSwappableSequence.FillableSwappable;
import swingViews.FillableSwappableSequence.FillableSwappableSequenceListener;
import swingViews.LineUpChooser.SubstituteSelectorDelegate;
import swingViews.OrderedDealerPresenter.OrderedDealerView;

/**
 * <h1></h1>implements an MVP Presenter for a gadget capable of being part of
 * <ol>
 * <li>a {@linkplain CompetitiveOptionDealingGroup <i>competitive dealing
 * group</i>} having as options instances of {@linkplain Player}, or one if its
 * sub-types
 * <li>an <i>ordered group</i> of gadgets which
 * {@linkplain FillableSwappableSequence only permits selections to be entered
 * <i>sequentially</i>}
 * </ol>
 * 
 * <h1>Listener notification policy</h1> 
 * Once a {@code SubstitutePlayerSelector}
 * instance is made to participate in a
 * {@linkplain CompetitiveOptionDealingGroup competitive dealing group}
 * <b>and</b> in a {@linkplain FillableSwappableSequence fillable-swappable
 * sequence}, a {@link SelectorListener} attached to it will be notified of
 * <ul>
 * 	<li>a <i>selection-made</i> event whenever an option on the <i>previously
 * 	empty</i> {@code Selector} is selected
 * 	<li>a <i>selection-cleared</i> event whenever the selection on the
 * 	{@code Selector} is cleared, after <i>{@linkplain FillableSwappableSequence
 * 	sequence} operations</i> have taken place
 * </ul>
 * 
 * @param <P> the type for options in the {@code SubstitutePlayerSelector}
 * @see
 *      <ul>
 *      <li>{@linkplain CompetitiveOptionDealingGroup} for the semantics of
 *      <i>competitive dealing</i> and how to initialize it
 *      <li>{@linkplain FillableSwappableSequence} for the semantics of a
 *      <i>fillable-swappable sequence</i> and how to initialize it
 */
public class SubstitutePlayerSelector<P extends Player> extends OrderedDealerPresenter<P>
		implements SubstituteSelectorDelegate<P> {

	// 1. MVP Presenter: additional View interface

	/**
	 * an interface for Views wishing to collaborate with a
	 * {@linkplain SubstitutePlayerSelector}.
	 * 
	 * <p>
	 * Functionally, a {@code SubstitutePlayerSelectorView} is supposed to implement
	 * all capabilities in {@linkplain OrderedDealerView} and also be able to
	 * <ul>
	 * <li>toggle its ability to receive user input
	 * <li>toggle visual decoration of its status as the <i>next-fillable</i> gadget
	 * in a {@code FillableSwappableSequenceDriver}
	 * </ul>
	 * 
	 * @param <T> the type for options in the View's option list
	 */
	public interface SubstitutePlayerSelectorView<T> extends OrderedDealerView<T> {

		// TODO rimpiazzare con un setNextFillable(boolean)
		/**
		 * requests the {@linkplain SubstitutePlayerSelectorView} to take on
		 * <i>next-fillable</i> status.
		 */
		void highlight();

		/**
		 * requests the {@linkplain SubstitutePlayerSelectorView} to relinquish
		 * <i>next-fillable</i> status.
		 */
		void dehighlight();

		/**
		 * requests the {@link OrderedDealerView} to toggle the visual availability of
		 * its controls to the user.
		 */
		void setControlsEnabled(boolean b);
	}

	/**
	 * a separate reference to the view in the more specific type
	 * {@linkplain SubstitutePlayerSelectorView} is necessary for this Presenter to
	 * implement additional interaction with the View, as per its
	 * {@linkplain FillableSwappable} duties
	 */
	private final SubstitutePlayerSelectorView<P> view;

	public SubstitutePlayerSelector(SubstitutePlayerSelectorView<P> view) {
		super(view);
		this.view = view;
	}

	// 2. FillableSwappable: mandated functions

	private FillableSwappableSequence<SubstituteSelectorDelegate<P>> sequenceDriver;

	@Override
	public void attachDriver(FillableSwappableSequence<SubstituteSelectorDelegate<P>> driver) {
		this.sequenceDriver = driver;
		this.sequenceDriver.attachListener(
				new FillableSwappableSequenceListener<SubstituteSelectorDelegate<P>>() {

			@Override
			public void becameEmpty(SubstituteSelectorDelegate<P> emptiedMember) {
				if (emptiedMember == SubstitutePlayerSelector.this)
					listeners().forEach(listener -> listener.selectionClearedOn(SubstitutePlayerSelector.this));
			}

			@Override
			public void becameFilled(SubstituteSelectorDelegate<P> filledMember) {
				if (filledMember == SubstitutePlayerSelector.this)
					listeners().forEach(listener -> listener.selectionMadeOn(SubstitutePlayerSelector.this));
			}
		});
	}

	/**
	 * causes a {@code SubstitutePlayerSelector} to clear its selection and retire
	 * the corresponding option, as requested by the
	 * {@linkplain FillableSwappableSequence} during a <i>collapse operation</i>,
	 * <b>without</b> notifying back the sequence driver.
	 * 
	 * @implNote this is a local operation with respect to the dealer group, i.e. it
	 *           does not notify the {@code OptionDealerGroupDriver}
	 */
	@Override
	public void discardContent() {
		int pos = options.indexOf(getSelection().get());
		selectOption(NO_SELECTION);
		retireOption(pos);
	}

	/**
	 * causes a {@code SubstitutePlayerSelector} to "equalize" to another
	 * {@code SubstitutePlayerSelector} instance, <i><b>{@code other}</i></b>, as
	 * requested by the {@linkplain FillableSwappableSequence} during a <i>collapse
	 * operation</i>, <b>without</b> notifying back the sequence driver.
	 * <p>
	 * In the context of the dealer group, this means
	 * <ul>
	 * <li>restoring <i><b>{@code other}</i></b>'s selected option
	 * <li>setting the newly restored option as the selected one
	 * <li>retiring the previously selected option, if existing
	 * </ul>
	 * 
	 * @implNote
	 *           <ol>
	 *           <li>this is a local operation with respect to the dealer group,
	 *           i.e. it does not notify the
	 *           {@linkplain CompetitiveOptionDealingGroup}
	 *           <li>this operation does <b>not</b> rely on temporarily clearing the
	 *           View's selection
	 *           </ol>
	 */
	@Override
	public void acquireContentFrom(SubstituteSelectorDelegate<P> other) {
		// saves the current selection on this
		Optional<P> thisOldSelection = this.getSelection();

		// makes this acquire other's selection
		P otherSelection = other.getSelection().get();
		this.restoreOption(options.indexOf(otherSelection));
		this.selectOption(options.indexOf(otherSelection));

		// on the emptied selector it drops nothing,
		// on others it ensures correct option propagation across selectors
		thisOldSelection.ifPresent(o -> this.retireOption(options.indexOf(o)));
	}

	/**
	 * causes a {@code SubstitutePlayerSelector} to swap contents with another
	 * {@code SubstitutePlayerSelector} instance, <i><b>{@code other}</i></b>, as
	 * requested by the {@linkplain FillableSwappableSequence} during a <i>swap
	 * operation</i>, <b>without</b> notifying back the sequence driver.
	 * <p>
	 * In the context of the dealer group, this means
	 * <ul>
	 * <li>restoring <i><b>{@code other}</i></b>'s selected option
	 * <li>setting the newly restored option as the selected one
	 * <li>retiring the previously selected option
	 * </ul>
	 * and reciprocally on <i><b>{@code other}</i></b> with
	 * <i><b>{@code this}</i></b>.
	 * <p>
	 * 
	 * @implNote
	 *           <ol>
	 *           <li>this is a local operation with respect to the dealer group,
	 *           i.e. it does not notify the
	 *           {@linkplain CompetitiveOptionDealingGroup}
	 *           <li>this operation does <b>not</b> rely on temporarily clearing the
	 *           View's selection
	 *           </ol>
	 */
	@Override
	public void swapContentWith(SubstituteSelectorDelegate<P> other) {
		P otherOldSelection = other.getSelection().get();
		other.acquireContentFrom(this);

		P thisOldSelection = this.getSelection().get();
		this.restoreOption(options.indexOf(otherOldSelection));
		this.selectOption(options.indexOf(otherOldSelection));
		this.retireOption(options.indexOf(thisOldSelection));
	}

	@Override
	public void setNextFillable(boolean flag) {
		if (flag)
			view.highlight();
		else
			view.dehighlight();
	}

	@Override
	public void setFillingEnabled(boolean flag) {
		view.setControlsEnabled(flag);
	}

	// 3. OrderedDealerPresenter: response to selection events

	/**
	 * ensures the sequence driver - and thereby listeners - are only notified when
	 * Selector <i>enters</i> "filled" state, but not during selection updates
	 * (Selector <i>remains</i> in "filled" state)
	 */
	@Override
	public void selectedOption(int position) {
		Optional<P> selection = getSelection();
		super.selectedOption(position);
		if (selection.isEmpty())
			sequenceDriver.contentAdded(this);
	}

	/**
	 * ensures the group driver is notified every time an option needs to be
	 * withdrawn from competitors
	 */
	@Override
	protected void selectionSetFor(int absoluteIndex) {
		groupDriver.selectionMadeOn(this, absoluteIndex);
	}

	/**
	 * ensures the sequence driver - and thereby listeners - are only notified when
	 * Selector <i>enters</i> "empty" state, but not during selection updates
	 * (Selector remains in <i>"filled"</i> state)
	 */
	@Override
	public void selectionCleared() {
		super.selectionCleared();
		sequenceDriver.contentRemoved(this);
	}

	/**
	 * ensures the group driver is notified every time an option needs to be added
	 * back to competitors
	 */
	@Override
	protected void selectionClearedFor(int absoluteIndex) {
		groupDriver.selectionClearedOn(this, absoluteIndex);
	}

}
