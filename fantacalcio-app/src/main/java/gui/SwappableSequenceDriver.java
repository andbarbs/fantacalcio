package gui;

import java.util.ArrayList;
import java.util.List;

import gui.SwappableSequenceDriver.*;

public class SwappableSequenceDriver<T extends Swappable<T>> {
	
	/* 
	 * arranges suitable clients in a sequence such that the contents 
	 * of one client can be swapped with its left and right neighbors.
	 * 
	 * To this end, it
	 * 	> stores client instances in an ordered structure
	 * 	> provides an API for requesting a content swap to the left/right
	 * 	  for a given client instance
	 */

	// public interface clients must implement 
	// so SwappableSequenceDriver can drive them
	public static interface Swappable<S> {
		void swapContentWith(S other); // Swap the content of this with another
	}

	private List<T> clients = new ArrayList<T>();

	// TODO consider using a bi-chained wrapper to avoid indexOf() calls
	public SwappableSequenceDriver(List<T> clients) {
		this.clients = clients;
	}
	
	public void appendRight(T newClient) {
		clients.add(newClient);
	}

	public boolean canSwapLeft(T client) {
		return clients.indexOf(client) != 0;
	}

	public boolean canSwapRight(T client) {
		return clients.indexOf(client) < clients.size() - 1;
	}

	// TODO insert checks using canSwap (like Iterator does: next() -> hasNext())
	public void swapLeft(T client) {
		client.swapContentWith(clients.get(clients.indexOf(client) - 1));
	}

	public void swapRight(T client) {
		client.swapContentWith(clients.get(clients.indexOf(client) + 1));
	}

}
