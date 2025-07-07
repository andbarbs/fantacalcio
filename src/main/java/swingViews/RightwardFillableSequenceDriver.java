package swingViews;

import java.util.List;
import swingViews.RightwardFillableSequenceDriver.*;

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
	public interface RightwardFillable<T> {
		  void acquireContentFrom(T other);
		  void discardContent();
		  void enableFilling();
		  void disableFilling();
//		  void attachDriver(RightwardFillableSequenceDriver<T> driver);
		}

	// TODO consider using a bi-chained wrapper to avoid indexOf() calls
	private List<T> sequence;
	private int rightmostFillablePosition = 0;
	
	private boolean isRightmostFillable(T client) {
		return sequence.indexOf(client) == rightmostFillablePosition;
	}
	private boolean isFillable(T client) {
		return sequence.indexOf(client) <= rightmostFillablePosition;
	}

	public RightwardFillableSequenceDriver(List<T> clients) {
		this.sequence = clients;		
		this.sequence.forEach(c -> c.disableFilling());
		sequence.getFirst().enableFilling();
	}
	
	public void contentRemoved(T client) {
		if (!isFillable(client)) {
			throw new IllegalStateException("Content removal reported for non-fillable client");
		}
		
		// collapses content in the sequence
		int i = sequence.indexOf(client);
		for (; i < rightmostFillablePosition - 1; i++) {
			sequence.get(i).acquireContentFrom(sequence.get(i+1));
		}
		// shifts rightmostFillable status backwards
		sequence.get(i).discardContent();  //
		sequence.get(rightmostFillablePosition).disableFilling();
		rightmostFillablePosition = i;		
	}

	public void contentAdded(T client) {
		if (!isFillable(client)) {
			throw new IllegalStateException("Content addition reported for non-fillable client");
		}
		
		// updates sequence status moving rightmostFillable rightwards
		if (isRightmostFillable(client)) {
			rightmostFillablePosition = rightmostFillablePosition < sequence.size() ? 
					rightmostFillablePosition++ : -1;    // signals sequence completely filled
		}
	}

}
