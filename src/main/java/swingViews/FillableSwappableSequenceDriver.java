package swingViews;

import java.util.List;
import swingViews.FillableSwappableSequenceDriver.*;

/**
 * Manages a sequence of gadgets in such a way that content in the sequence 
 * grows left‐to‐right, ensuring no gaps are left when a gadget loses its content,
 * and allows swapping the content of two adjacent gadgets.
 *
 * <p><h1>Contracts</h1>
 * In order to be driven by {@code FillableSwappableSequenceDriver}, gadgets must
 * <ul>
 * 	<li>fulfill the contract in {@link FillableSwappableGadget}&lt;T&gt; which ensures they are able to
 *   	<ul>
 *   		 <li> toggle their ability to receive user input
 *  	 	 <li> take over the content of a fellow gadget
 *   		 <li> swap contents with a fellow gadget
 *  	 	 <li> attach an instance of {@code FillableSwappableSequenceDriver} 
 *  				for sending content-related notifications
 *   	</ul></li>
 *   <li>notify the attached {@code FillableSwappableSequenceDriver} instance of changes to their content,
 *   passing their instance to methods {@link FillableSwappableSequenceDriver#contentAdded(FillableSwappableGadget)} and 
 *   {@link FillableSwappableSequenceDriver#contentRemoved(FillableSwappableGadget)} of the driver. 
 *   Gadgets should take care that programmatic addition/removal of their content, such as that induced by the driver, 
 *   be <i>not</i> notified back to the driver
 * </ul>
 *
 * <p><h1>Limitations</h1>
 * <ul>
 *   <li>Not thread‐safe</li>
 *   <li>Does not defend itself against event feedback loops</li>
 * </ul>
 *
 * @param <T> the client gadget type; must implement
 *           {@link FillableSwappableGadget}&lt;T&gt;
 * @see FillableSwappableGadget
 * @apiNote This driver is GUI-agnostic and relieves clients of using class inheritance
 */
public class FillableSwappableSequenceDriver<T extends FillableSwappableGadget<T>> {

	private interface RightwardFillable<P> {
		void acquireContentFrom(P other);
		
		/**
		 * is called on a 
		 * as part of a <i>collapse operation</i> orchestrated by the
		 * {@linkplain FillableSwappableSequenceDriver}
		 */
		void discardContent();
		void enableFilling();
		void disableFilling();

		// allow gadgets to customize rightmost-fillable status
		void highlight();
		void dehighlight();
	}

	private interface Swappable<Q> {
		void swapContentWith(Q other);
	}
	
	/**
	 * public interface gadgets must implement 
	 * so that a {@code FillableSwappableSequenceDriver} can drive them
	 * 
	 * <p><h1>Limitations</h1>
	 * no mechanism is in place to ensure a gadget correctly
	 * binds &lt;R&gt; to its own type</p>
	 *
	 * @param <R> the client type; must implement
	 *           {@link FillableSwappableGadget}&lt;R&gt;
	 * @see FillableSwappableSequenceDriver
	 * @implNote a collapsed interface for various private gadget interfaces
	 */
	public interface FillableSwappableGadget<R extends FillableSwappableGadget<R>>
			extends RightwardFillable<R>, Swappable<R> {
		void attachDriver(FillableSwappableSequenceDriver<R> driver);
	}
	
	/**
	 * public interface a visual must implement 
	 * so that a {@code FillableSwappableSequenceDriver} can update it
	 *
	 * @param <S> the gadget type; must implement
	 *           {@link FillableSwappableGadget}&lt;S&gt;
	 * @see FillableSwappableSequenceDriver
	 */
	public interface FillableSwappableVisual<S extends FillableSwappableGadget<S>> {
		void becameEmpty(S emptiedGadget);
		void becameFilled(S filledGadget);
	}

	// internal bookkeeping
	// TODO consider using a bi-chained wrapper to avoid indexOf() calls
	// and make utility methods more intuitive
	private List<T> sequence;
	private int rightmostFillablePosition;
	private static final int RIGHTMOST_FILLABLE_OVERFLOW = -1;
	private FillableSwappableVisual<T> view;

	// internal utility methods for querying client status
	private boolean isRightmostFillable(T client) {
		return sequence.indexOf(client) == rightmostFillablePosition;
	}

	private boolean isFillable(T client) {
		return sequence.indexOf(client) <= rightmostFillablePosition || 
				rightmostFillablePosition == RIGHTMOST_FILLABLE_OVERFLOW;
	}

	public FillableSwappableSequenceDriver(List<T> gadgets, FillableSwappableVisual<T> view) {
		this.sequence = gadgets;
		this.view = view;
		this.sequence.forEach(c -> {
			c.attachDriver(this);
			c.disableFilling();
		});
		sequence.getFirst().enableFilling();
		updateRightmostFillable(0);
	}

	// notification methods: update sequence state and collapse
	public void contentRemoved(T emptied) {
		if (!isFillable(emptied))
			throw new IllegalStateException("Content removal reported for non-fillable client");

		// 1) collapses content in the sequence
		int nextRightmostFillable = (rightmostFillablePosition == RIGHTMOST_FILLABLE_OVERFLOW) ? 
				sequence.size() - 1 : rightmostFillablePosition - 1;
		for (int i = sequence.indexOf(emptied); i < nextRightmostFillable; i++) {
			sequence.get(i).acquireContentFrom(sequence.get(i + 1));
		}
		
		// 2) shifts rightmostFillable client backwards
		
		// avoids emptying if emptied == nextRightmostFillable
		if (sequence.indexOf(emptied) != nextRightmostFillable)
			sequence.get(nextRightmostFillable).discardContent();
		view.becameEmpty(sequence.get(nextRightmostFillable));
		if (rightmostFillablePosition != RIGHTMOST_FILLABLE_OVERFLOW)
			sequence.get(rightmostFillablePosition).disableFilling();
		updateRightmostFillable(nextRightmostFillable);
	}

	public void contentAdded(T client) {
		if (!isFillable(client))
			throw new IllegalStateException("Content addition reported for non-fillable client");

		// updates sequence status moving rightmostFillable rightwards
		if (isRightmostFillable(client)) {
			if (rightmostFillablePosition < sequence.size() - 1)
				sequence.get(updateRightmostFillable(rightmostFillablePosition + 1)).enableFilling();
			else
				updateRightmostFillable(RIGHTMOST_FILLABLE_OVERFLOW); // signals RF overflow
			view.becameFilled(client);
		}
	}

	// triggers gadget highlighting for rightmost-fillable status
	private int updateRightmostFillable(int newValue) {
		if (rightmostFillablePosition != RIGHTMOST_FILLABLE_OVERFLOW)
			sequence.get(rightmostFillablePosition).dehighlight();
		rightmostFillablePosition = newValue;
		if (newValue != RIGHTMOST_FILLABLE_OVERFLOW)
			sequence.get(rightmostFillablePosition).highlight();
		return newValue;
	}

	// swapping gadget content
	public boolean hasContent(T client) {
		return sequence.indexOf(client) < rightmostFillablePosition ||
				rightmostFillablePosition == RIGHTMOST_FILLABLE_OVERFLOW;
	}
	
	public boolean canSwapLeft(T client) {
		return hasContent(client) && sequence.indexOf(client) != 0;
	}

	public boolean canSwapRight(T client) {
		return hasContent(client) && 
				sequence.indexOf(client) < sequence.size() - 1 &&
					hasContent(sequence.get(sequence.indexOf(client) + 1));
	}

	// TODO insert checks using canSwap (like Iterator does: next() -> hasNext())
	public void swapLeft(T client) {
		client.swapContentWith(sequence.get(sequence.indexOf(client) - 1));
	}

	public void swapRight(T client) {
		client.swapContentWith(sequence.get(sequence.indexOf(client) + 1));
	}

}
