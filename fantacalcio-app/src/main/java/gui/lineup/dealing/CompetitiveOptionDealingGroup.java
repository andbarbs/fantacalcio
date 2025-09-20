package gui.lineup.dealing;

import java.util.List;
import java.util.Set;

import gui.lineup.dealing.CompetitiveOptionDealingGroup.*;

/*
 * TODO 
 * 		1. consider moving the global option list entirely within this driver
 * 			attualmente, i test usano una option list vuota ed esercitano 
 * 			il driver su indici arbitrari, senza che venga sollevato errore
 */

/**
 * drives a {@code Set} of implementors of {@linkplain CompetitiveOrderedDealer} 
 * in such a way as to realize a <i>competitive dealing</i> group.
 * 
 * Competitive dealing is defined as a collective behavior where
 * <ol>
 * 	<li>dealers are initialized on a global list of options
 * 	<li>when an option is selected on a dealer, it is withdrawn
 * 		from other dealers
 *	<li>when a selection is cleared on a dealer, the corresponding options is
 * 		added back to other dealers
 * </ol>
 * with the ordering of options on a dealer always being 
 * consistent with that of the global list.
 *
 * <p>In order to realize competitive dealing, dealers must collaborate 
 * with the {@code CompetitiveOptionDealerGroup} as specified under their interface.
 *
 * <p><h1>Limitations</h1>
 * <ul>
 * 	<li>Not thread‐safe
 * 	<li>Vulnerable to feedback loops from dealers
 * </ul>
 *
 * @param <D> the type for dealers in the group; must implement
 *            {@link CompetitiveOrderedDealer CompetitiveOrderedDealer&lt;D, O&gt;}
 * @param <O> the type for options in the dealing
 */
public class CompetitiveOptionDealingGroup<D extends CompetitiveOrderedDealer<D, O>, O> {	

	/** 
	 * an interface for a type that wants to be part of a 
	 * {@linkplain CompetitiveOptionDealingGroup <i>competitive dealing</i>} group.
	 * 
	 * <p>Implicitly, a {@link CompetitiveOrderedDealer} is supposed to:
	 * <ul>
	 * 	<li>provide clients with an <i>ordered choice</i> of options
	 *  <li>allow clients to <i>select</i> some such options
	 * 	<li>allow clients to <i>clear</i> a previously set selection
	 * </ul>
	 * 
	 * <p>Competitive dealing will then ensue from an implementor additionally 
	 * satisfying the following <i><b>behavioral requirements</i></b>:
	 * <ol>
	 * 	<li>reporting a selection being made and selection clearance to the 
	 * 		{@linkplain CompetitiveOptionDealingGroup} driver, respectively,
	 * 		through the latter's {@linkplain #selectionMadeOn <i>selection-made</i>} 
	 * 		and {@linkplain #selectionClearedOn <i>selection-cleared</i>} notification points
	 * 	<li>ensuring that driver requests to 
	 * 		{@linkplain CompetitiveOrderedDealer#restoreOption add back} or 
	 * 		{@linkplain CompetitiveOrderedDealer#retireOption remove} an option 
	 * 		do <b>not</b> produce a notification back to the driver
	 * </ol>
	 *
	 * @param <D> intended as a self-bound type; must implement
	 *            {@link CompetitiveOrderedDealer CompetitiveOrderedDealer&lt;D, O&gt;}
	 * @param <O> the type for options in the dealing
	 */
	public interface CompetitiveOrderedDealer<D extends CompetitiveOrderedDealer<D, O>, O> {
		
		/**
		 * encourages the {@linkplain CompetitiveOrderedDealer} to initialize a 
		 * composed {@linkplain CompetitiveOptionDealingGroup} reference,
		 * as part of the establishment of a competitive dealing group.
		 * @apiNote effectively a mandated {@code setter}
		 * @param driver the driver for the group being established
		 */
		void attachDriver(CompetitiveOptionDealingGroup<D, O> driver);
		
		/**
		 * requires the {@linkplain CompetitiveOrderedDealer} to initialize its
		 * available options to the global list, as part of the establishment 
		 * of a competitive dealing group.
		 * 
		 * <p><h1>Driver feedback avoidance</h1>
		 * This operation is driver-induced and must <i><b>not</b></i> notify back the driver</p>
		 * @param options the global option list
		 */
		void attachOptions(List<O> options);
		
		/**
		 * requires the {@linkplain CompetitiveOrderedDealer} to remove the
		 * given option from its available options, as part of competitive dealing
		 * within the {@code dealer}'s competitive dealing group.
		 * 
		 * <p><h1>Driver feedback avoidance</h1>
		 * This operation is driver-induced and must <i><b>not</b></i> notify back the driver</p>
		 * @param index the index of the option to be retired relative to the global option list
		 */
		void retireOption(int index);
		
		/**
		 * requires the {@linkplain CompetitiveOrderedDealer} to add back the
		 * given option to its available options, keeping their ordering consistent 
		 * with that of the global list, as part of competitive dealing 
		 * within the {@code dealer}'s competitive dealing group.
		 * 
		 * <p><h1>Driver feedback avoidance</h1>
		 * This operation is driver-induced and must <i><b>not</b></i> notify back the driver</p>
		 * @param index the index of the option to be restored relative to the global option list
		 */
		void restoreOption(int index);
	}
	
	// needed for spying in tests
	CompetitiveOptionDealingGroup() {
	}
	
	private Set<D> dealers;
	
	// used by tests for spying
	CompetitiveOptionDealingGroup(Set<D> dealers) {
		this.dealers = dealers;
	}

	/**
	 * A compact API for initializing a {@linkplain CompetitiveOptionDealingGroup
	 * <i>competitive ordered dealing</i> group}.
	 * 
	 * @param <D>     the type for dealers in the group; must implement
	 *                {@link CompetitiveOrderedDealer CompetitiveOrderedDealer&lt;D, O&gt;}
	 * @param <O>     the type for options in the group
	 * @param dealers the {@code Set} of {@code dealers} to include in the group
	 * @param options the global option list for this group
	 * @return the instance of {@linkplain CompetitiveOptionDealingGroup}
	 *         orchestrating the new group
	 */
	public static <D extends CompetitiveOrderedDealer<D, O>, O> 
			CompetitiveOptionDealingGroup<D, O> initializeDealing(Set<D> dealers, List<O> options) {
		CompetitiveOptionDealingGroup<D, O> driver = new CompetitiveOptionDealingGroup<D, O>(dealers);
		driver.dealers.forEach(d -> {
			d.attachDriver(driver);
			d.attachOptions(options);
		});
		return driver;
	}
	
	/**
	 * notification point for <i>selection-made</i> events on
	 * {@linkplain CompetitiveOrderedDealer dealer}s in a
	 * {@linkplain CompetitiveOrderedDealer <i>competitive-dealing</i> group}.
	 * 
	 * @param dealer the dealer for which the event is being reported
	 * @param index  the index of the option for which the event is being reported,
	 *               relative to the global option list
	 * @throws IllegalArgumentException if the provided dealer is not part of the
	 *                                  group managed by this {@code driver}
	 */
	public void selectionMadeOn(D dealer, int index) {
		if (!dealers.contains(dealer))
			throw new IllegalArgumentException(String.format(
					"CompetitiveOptionDealingGroup.selectionMadeOn: Illegal Argument\n" +
					"dealer: %s not a member of this group\n", dealer));
		dealers.stream().filter(d -> !d.equals(dealer)).forEach(d -> d.retireOption(index));
	}
	
	//TODO add check "does the dealer belong to this group?"
	/**
	 * notification point for <i>selection-cleared</i> events on
	 * {@linkplain CompetitiveOrderedDealer dealer}s in a
	 * {@linkplain CompetitiveOrderedDealer <i>competitive-dealing</i> group}.
	 * 
	 * @param dealer the dealer for which the event is being reported
	 * @param index  the index of the option for which the event is being reported,
	 *               relative to the global option list
	 * @throws IllegalArgumentException if the provided dealer is not part of the
	 *                                  group managed by this {@code driver}
	 */
	public void selectionClearedOn(D dealer, int index) {
		if (!dealers.contains(dealer))
			throw new IllegalArgumentException(String.format(
					"CompetitiveOptionDealingGroup.selectionMadeOn: Illegal Argument\n" +
					"dealer: %s not a member of this group\n", dealer));
		dealers.stream().filter(d -> !d.equals(dealer)).forEach(d -> d.restoreOption(index));
	}

	

}
