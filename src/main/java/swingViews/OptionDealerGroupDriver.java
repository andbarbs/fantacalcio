package swingViews;

import java.util.List;
import java.util.Set;

import swingViews.OptionDealerGroupDriver.*;

/**
 * manages a group of implementors of {@link OrderedOptionDealer} in such a way that each dealer offers all original
 * options barred those that are currently selected in the other dealers.
 *
 * <p><h1>Contracts</h1>
 * In order to be driven by {@link OrderedOptionDealer}, dealers must
 * <ul>
 * 	<li>fulfill the contract in {@link OrderedOptionDealer}&lt;D, O&gt; which ensures they are able to
 *   	<ul>
 *   		 <li> retire and restore one of the common options, based on its index
 *  	 	 <li> store an instance of {@code OptionDealerGroupDriver} 
 *  				for sending selection-related notifications
 *  		 <li> store the common options, ordered
 *   	</ul></li>
 *   <li>notify the attached {@code OptionDealerGroupDriver} instance when a selection is made or cleared, 
 *   according to whatever semantic "selection" has for the dealer. The dealer should
 *   pass their instance to methods {@link OptionDealerGroupDriver#selectionMadeOn(OrderedOptionDealer, int)} and 
 *   {@link OptionDealerGroupDriver#selectionClearedOn(OrderedOptionDealer, int)} of the driver. 
 *   Dealers should take care that driver-induced addition/removal of their options via 
 *   {@link OrderedOptionDealer#restoreOption(int)} and {@link OrderedOptionDealer#retireOption(int)}
 *   be <i>not</i> notified back to the driver
 * </ul>
 *
 * <p><h1>Limitations</h1>
 * <ul>
 *   <li>Not thread‐safe</li>
 *   <li>Does not defend itself against event feedback loops</li>
 * </ul>
 *
 * @param <D> the client dealer type; must implement
 *           {@link OrderedOptionDealer}&lt;D, O&gt;
 * @param <O> the option type
 * @see OrderedOptionDealer
 * @apiNote This driver is GUI-agnostic and does not force class inheritance on clients
 */

public class OptionDealerGroupDriver<D extends OrderedOptionDealer<D, O>, O> {	

	/**
	 * public interface clients must implement 
	 * so that an {@link OptionDealerGroupDriver} can drive them
	 * 
	 * <p><h1>Limitations</h1>
	 * no mechanism is in place to ensure a client correctly
	 * binds &lt;D&gt; to its own type</p>
	 *
	 * @param <D> the client type; must implement
	 *           {@link OrderedOptionDealer}&lt;D, O&gt;
	 * @see OptionDealerGroupDriver
	 */
	public interface OrderedOptionDealer<D extends OrderedOptionDealer<D, O>, O> {
		void attachDriver(OptionDealerGroupDriver<D, O> driver);
		void attachOptions(List<O> options);
		void retireOption(int index);
		void restoreOption(int index);
	}
	
	// needed for spying in tests
	OptionDealerGroupDriver() {
	}
	
	private Set<D> dealers;
	
	// used by tests for spying
	OptionDealerGroupDriver(Set<D> dealers) {
		this.dealers = dealers;
	}

	// public factory method
	public static <D extends OrderedOptionDealer<D, O>, O> 
			OptionDealerGroupDriver<D, O> initializeDealing(Set<D> dealers, List<O> options) {
		OptionDealerGroupDriver<D, O> driver = new OptionDealerGroupDriver<D, O>(dealers);
		driver.dealers.forEach(d -> d.attachDriver(driver));
		driver.dealers.forEach(d -> d.attachOptions(options));
		return driver;
	}
	
	public void selectionMadeOn(D dealer, int index) {
		dealers.stream().filter(d -> !d.equals(dealer)).forEach(d -> d.retireOption(index));
	}
	
	public void selectionClearedOn(D dealer, int index) {
		dealers.stream().filter(d -> !d.equals(dealer)).forEach(d -> d.restoreOption(index));
	}

	

}
