package swingViews;

import java.util.List;
import swingViews.FillableSwappableSequenceDriver.*;

public class FillableSwappableSequenceDriver<T extends FillableSwappableClient<T>> {

	/*
	 * arranges suitable clients in a sequence such that content in the sequence
	 * grows from left to right, and clients that have content can have it swapped
	 * 
	 * To this end, it 
	 * 		> stores client instances in an ordered structure 
	 * 		> enables filling for clients in a left-to-right fashion 
	 * 		> allows clients to notify it of content addition/removal 
	 * 		> updates its internal notion of sequence state 
	 * 		> permits an actor to request content swap on filled clients
	 * 
	 * CAVEATS:
	 * clients should ensure that programmatic content addition/removal
	 * (such as those induced by the driver) be NOT notified back to the driver
	 */

	private interface RightwardFillable<S> {
		void acquireContentFrom(S other);
		void discardContent();
		void enableFilling();
		void disableFilling();

		// allow client to augment rightmost-fillable status
		void highlight();
		void dehighlight();
	}

	private interface Swappable<Q> {
		void swapContentWith(Q other);
	}
	
	// public interfaces clients must implement
	// so RightwardFillableSequenceDriver can drive them
	public interface FillableSwappableClient<T extends FillableSwappableClient<T>>
			extends RightwardFillable<T>, Swappable<T> {
		void attachDriver(FillableSwappableSequenceDriver<T> driver);
	}

	// internal bookkeeping
	// TODO consider using a bi-chained wrapper to avoid indexOf() calls
	private List<T> sequence;
	private int rightmostFillablePosition;
	private static final int RIGHTMOST_FILLABLE_OVERFLOW = -1;

	// internal utility methods for querying client status
	private boolean isRightmostFillable(T client) {
		return sequence.indexOf(client) == rightmostFillablePosition;
	}

	private boolean isFillable(T client) {
		return sequence.indexOf(client) <= rightmostFillablePosition || 
				rightmostFillablePosition == RIGHTMOST_FILLABLE_OVERFLOW;
	}

	public FillableSwappableSequenceDriver(List<T> clients) {
		this.sequence = clients;
		this.sequence.forEach(c -> {
			c.attachDriver(this);
			c.disableFilling();
		});
		sequence.getFirst().enableFilling();
		updateRightmostFillable(0);
	}

	// notification methods: manage sequence state
	public void contentRemoved(T client) {
		if (!isFillable(client))
			throw new IllegalStateException("Content removal reported for non-fillable client");

		// collapses content in the sequence
		int nextRightmostFillable = (rightmostFillablePosition == RIGHTMOST_FILLABLE_OVERFLOW) ? 
				sequence.size() - 1 : rightmostFillablePosition - 1;
		for (int i = sequence.indexOf(client); i < nextRightmostFillable; i++) {
			sequence.get(i).acquireContentFrom(sequence.get(i + 1));
		}
		// shifts rightmostFillable client backwards
		sequence.get(nextRightmostFillable).discardContent(); //
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
		}
	}

	// triggers client highlighting for rightmost-fillable status
	private int updateRightmostFillable(int newValue) {
		if (rightmostFillablePosition != RIGHTMOST_FILLABLE_OVERFLOW)
			sequence.get(rightmostFillablePosition).dehighlight();
		rightmostFillablePosition = newValue;
		if (newValue != RIGHTMOST_FILLABLE_OVERFLOW)
			sequence.get(rightmostFillablePosition).highlight();
		return newValue;
	}

	// swapping api
	public boolean hasContent(T client) {
		System.out.printf("sequence.indexOf(client) = %d, rightmostFillablePosition = %d\n", sequence.indexOf(client), rightmostFillablePosition);
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
