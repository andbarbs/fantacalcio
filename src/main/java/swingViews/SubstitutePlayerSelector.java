package swingViews;

import java.util.Optional;
import domainModel.Player;
import swingViews.FillableSwappableSequence.FillableSwappable;
import swingViews.OrderedDealerPresenter.OrderedDealerView;

/**
 * <h1></h1>implements an MVP Presenter for a gadget capable of being part of
 * <ol>
 * 		<li>a <i>group</i> where each gadget allows <i>competitive dealing</i> of one instance 
 * 			of {@linkplain Player}, or one if its sub-types, from a group-wide list
 * 		<li>an <i>ordered group</i> of gadgets which only permits the user 
 * 			to enter selections <i>sequentially</i>
 * </ol>
 * 
 * <p>These two functionalities are fully realized when, respectively,
 * <ol>
 * 		<li>a <i>{@code Set}</i> of {@code SubstitutePlayerSelector} instances 
 * 			are made to collaborate with a {@linkplain CompetitiveOptionDealingGroup}
 * 		<li>a <i>{@code List}</i> of {@code SubstitutePlayerSelector} instances 
 * 			are made to collaborate with a {@linkplain FillableSwappableSequence}
 * </ol> 
 * through the facilities defined by those types.</p>
 * 
 * @param <P> the type for options in the {@code StarterPlayerSelector}
 * @see {@linkplain CompetitiveOptionDealingGroup} for the semantics of competitive dealing, 
 * 		{@linkplain FillableSwappableSequence}
 */
public class SubstitutePlayerSelector<P extends Player> extends OrderedDealerPresenter<P> 
			implements FillableSwappable<SubstitutePlayerSelector<P>> {
	
	// 1. MVP Presenter: additional View interface 

		/**
		 * an interface for Views wishing to collaborate with a
		 * {@linkplain SubstitutePlayerSelector}.
		 * 
		 * <p> Functionally, a {@code SubstitutePlayerSelectorView} is supposed to
		 * implement all capabilities in {@linkplain OrderedDealerView} and also
		 * be able to
		 * <ul> 
		 * 		<li>toggle its ability to receive user input
		 * 		<li>toggle visual decoration of its status as the <i>next-fillable</i> 
		 * 			gadget in a {@code FillableSwappableSequenceDriver}
		 * </ul>
		 * @param <T> the type for options in the View's option list
		 */	
		public interface SubstitutePlayerSelectorView<T> extends OrderedDealerView<T> {
			
			//TODO introdurre un setNextFillable(boolean)
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

	private FillableSwappableSequence<SubstitutePlayerSelector<P>> sequenceDriver;
	
	@Override
	public void attachDriver(FillableSwappableSequence<SubstitutePlayerSelector<P>> driver) {
		this.sequenceDriver = driver;		
	}
	
	/** 
	 * causes a {@code SubstitutePlayerSelector} to clear its selection 
	 * and retire the corresponding option , as requested by the 
	 * {@linkplain FillableSwappableSequence} during a <i>collapse operation</i>,
	 * <b>without</b> notifying back the sequence driver.
	 * @implNote this is a local operation with respect to the dealer group, 
	 * 		i.e. it does not notify the {@code OptionDealerGroupDriver}
	 */
	@Override
	public void discardContent() {
		int pos = options.indexOf(getSelection().get());
		selectOption(NO_SELECTION);
		retireOption(pos);			
	}
	
	/**
	 * causes a {@code SubstitutePlayerSelector} to "equalize" to another
	 * {@code SubstitutePlayerSelector} instance, <i><b>{@code other}</i></b>, as requested
	 * by the {@linkplain FillableSwappableSequence} during a <i>collapse operation</i>,
	 * <b>without</b> notifying back the sequence driver.
	 * <p>
	 * In the context of the dealer group, this means
	 * <ul>
	 * 		<li>restoring <i><b>{@code other}</i></b>'s selected option
	 * 		<li>setting the newly restored option as the selected one
	 * 		<li>retiring the previously selected option, if applicable
	 * </ul>
	 * 
	 * @implNote 
	 * <ol>
	 * 		<li>this is a local operation with respect to the dealer group, 
	 * 			i.e. it does not notify the {@linkplain CompetitiveOptionDealingGroup}
	 * 		<li>this operation does not rely on temporarily clearing the View's selection
	 * </ol>
	 */
	@Override
	public void acquireContentFrom(SubstitutePlayerSelector<P> other) {
		// saves the current selection on this
		Optional<P> thisSelection = this.getSelection();
		
		// makes this acquire other's selection
		P otherSelection = other.getSelection().get();
		this.restoreOption(options.indexOf(otherSelection));
    	this.selectOption(options.indexOf(otherSelection)); 
    	
    	// on the emptied selector it drops nothing, 
    	// on others it ensures correct option propagation across selectors
    	thisSelection.ifPresent(o -> this.retireOption(options.indexOf(o)));
	}

	/**
	 * causes a {@code SubstitutePlayerSelector} to swap contents with another
	 * {@code SubstitutePlayerSelector} instance, <i><b>{@code other}</i></b>, 
	 * as requested by the {@linkplain FillableSwappableSequence} during a <i>swap operation</i>, 
	 * <b>without</b> notifying back the sequence driver.
	 * <p>
	 * In the context of the dealer group, this means
	 * <ul>
	 * 		<li>restoring <i><b>{@code other}</i></b>'s selected option
	 * 		<li>setting the newly restored option as the selected one
	 * 		<li>retiring the previously selected option
	 * </ul>
	 * and reciprocally on <i><b>{@code other}</i></b> with <i><b>{@code this}</i></b>.<p>
	 * 
	 * @implNote 
	 * <ol>
	 * 		<li>this is a local operation with respect to the dealer group, 
	 * 			i.e. it does not notify the {@linkplain CompetitiveOptionDealingGroup}
	 * 		<li>this operation does not rely on temporarily clearing the View's selection
	 * </ol>
	 */
	@Override
	public void swapContentWith(SubstitutePlayerSelector<P> other) {
		P selection = this.getSelection().get();
    	P otherSelection = other.getSelection().get();
    	
    	this.restoreOption(options.indexOf(otherSelection));
    	this.selectOption(options.indexOf(otherSelection));
    	this.retireOption(options.indexOf(selection));
    	
    	other.restoreOption(options.indexOf(selection));
    	other.selectOption(options.indexOf(selection));
    	other.retireOption(options.indexOf(otherSelection));    	
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
	
	@Override
	protected void selectionSetFor(int absoluteIndex) {
		System.out.println("about to call driver.selectionMadeOn");
		groupDriver.selectionMadeOn(this, absoluteIndex);
		sequenceDriver.contentAdded(this);
	}

	@Override
	protected void selectionClearedFor(int absoluteIndex) {
		System.out.println("about to call driver.selectionClearedOn");
		groupDriver.selectionClearedOn(this, absoluteIndex);
		sequenceDriver.contentRemoved(this);
	}
	
}
