package gui;

import java.util.List;

import gui.RightwardFillableSequenceDriver.*;

public class RightwardFillableSequenceDriver<T extends RightwardFillable<T>> {
	
	/* 
	 * arranges suitable clients in a sequence such that content
	 * in the sequence grows from the left to the right.
	 * 
	 * To this end, it
	 * 	> stores client instances in an ordered structure
	 *  > assigns fillable status to clients according to sequence state
	 *  > allows clients to notify it of content addition/removal 
	 *    and thereupon updates sequence state 
	 */

	// public interface clients must implement
	// so RightwardFillableSequenceDriver can drive them
	public interface RightwardFillable<S extends RightwardFillable<S>> {
		  void acquireContentFrom(S other);
		  void discardContent();
		  void enableFilling();
		  void disableFilling();
		  
		  void highlight(); void dehighlight();
		  void attachDriver(RightwardFillableSequenceDriver<S> driver);
		}
	
	private static final int RIGHTMOST_FILLABLE_OVERFLOW = -1;

	// TODO consider using a bi-chained wrapper to avoid indexOf() calls
	private List<T> sequence;
	private int rightmostFillablePosition;
	
	private boolean isRightmostFillable(T client) {
		return sequence.indexOf(client) == rightmostFillablePosition;
	}
	private boolean isFillable(T client) {
		return (sequence.indexOf(client) <= rightmostFillablePosition || 
				rightmostFillablePosition == RIGHTMOST_FILLABLE_OVERFLOW);
	}

	public RightwardFillableSequenceDriver(List<T> clients) {
		this.sequence = clients;
		this.sequence.forEach(c -> {
			c.attachDriver(this);
			c.disableFilling();
		});
		sequence.getFirst().enableFilling();
		updateRightmostFillable(0);
	}
	
	public void contentRemoved(T client) {
		if (!isFillable(client)) {
			throw new IllegalStateException("Content removal reported for non-fillable client");
		}
		
		// collapses content in the sequence
		int nextRightmostFillable = (rightmostFillablePosition == RIGHTMOST_FILLABLE_OVERFLOW) ?
				sequence.size() - 1 : rightmostFillablePosition - 1;
		for (int i = sequence.indexOf(client); i < nextRightmostFillable; i++) {
			sequence.get(i).acquireContentFrom(sequence.get(i+1));
		}
		// shifts rightmostFillable client backwards
		sequence.get(nextRightmostFillable).discardContent();  //
		if (rightmostFillablePosition != RIGHTMOST_FILLABLE_OVERFLOW) {
			sequence.get(rightmostFillablePosition).disableFilling();
		}
		updateRightmostFillable(nextRightmostFillable);		
	}

	public void contentRemoved2(T client) {
		int idx = sequence.indexOf(client);
		if (idx <= rightmostFillablePosition) {
			// collapse left
			for (int i = idx; i < rightmostFillablePosition - 1; i++) {
				sequence.get(i).acquireContentFrom(sequence.get(i + 1));
			}
			// reset far right
			sequence.get(rightmostFillablePosition).discardContent();
			sequence.get(rightmostFillablePosition).disableFilling();
			updateRightmostFillable(idx);
		}
	}

	public void contentAdded(T client) {
		if (!isFillable(client)) {
			throw new IllegalStateException("Content addition reported for non-fillable client");
		}

		// updates sequence status moving rightmostFillable rightwards
		if (isRightmostFillable(client)) {
			if (rightmostFillablePosition < sequence.size() - 1) {
				sequence.get(updateRightmostFillable(rightmostFillablePosition + 1)).enableFilling();
			}
			else {
				updateRightmostFillable(RIGHTMOST_FILLABLE_OVERFLOW);   // signals RF overflow
			}
		}
	}
	
	private int updateRightmostFillable(int newValue) {
		if (rightmostFillablePosition != RIGHTMOST_FILLABLE_OVERFLOW) {
			sequence.get(rightmostFillablePosition).dehighlight();
		}
		rightmostFillablePosition = newValue;
		if (newValue != RIGHTMOST_FILLABLE_OVERFLOW) {
			sequence.get(rightmostFillablePosition).highlight();
		}
		return newValue;
	}


}
