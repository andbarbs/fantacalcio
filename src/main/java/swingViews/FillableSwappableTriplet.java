package swingViews;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import domainModel.Player;
import domainModel.Player.Defender;

import java.awt.Dimension;
import java.beans.Beans;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import swingViews.FillableSwappableSequence.FillableSwappable;
import swingViews.FillableSwappableSequence.FillableSwappableSequenceListener;
import swingViews.LineUpChooser.SubstituteTripletChooserDelegate;

// TODO: javadoc is pre-splitting!!

/**
 * a {@code JPanel} responsible for wiring up and providing graphical access to
 * a <b>three-member</b>, left-to-right {@linkplain FillableSwappableSequence
 * <i>fillable-swappable sequence</i>} through members' {@code JPanel} widgets.
 * 
 * @param <T> the type for sequence members
 * @implNote sizing of this {@code JPanel} is computed as a function of member
 *           widget's {@linkplain JPanel#getPreferredSize()} dimensions. See the
 *           {@linkplain FillableSwappableTriplet#SwingFillableSwappableTriplet
 *           (boolean, FillableSwappable, JPanel, FillableSwappable, JPanel, FillableSwappable, JPanel)
 *           public constructor}
 */
public class FillableSwappableTriplet<Q extends Player, T extends FillableSwappable<T> & Selector<Q>> 
		implements FillableSwappableTripletController, SubstituteTripletChooserDelegate<Q> {
	
	// sequence creation & listening
	private T member1, member2, member3;	
	private FillableSwappableSequence<T> sequenceDriver;
	
	// Widget ref
	public interface FillableSwappableTripletWidget {
		void setSwappingFirstPair(boolean enabled);
		void setSwappingSecondPair(boolean enabled);
	}
	
	private FillableSwappableTripletWidget widget;
	
	public void setWidget(FillableSwappableTripletWidget widget) {
		this.widget = widget;
	}
	
	/**
	 * provides {@linkplain FillableSwappableTriplet}'s <b>public</b>
	 * instantiation point.
	 * 
	 * <p><h1>Relative sizing</h1>
	 * Widgets can have varying dimensions. Sizing for the
	 * {@code SwingFillableSwappableTriplet} is computed as a function of widgets'
	 * {@linkplain JPanel#getPreferredSize()} dimensions so as to accommodate all of
	 * them.</p>
	 * 
	 * @param isDesignTime selects
	 *                     <ul>
	 *                     <li><b><i>design-time</i></b> instantiation when
	 *                     {@code true}
	 *                     <li><b><i>runtime</i></b> instantiation when
	 *                     {@code false}
	 *                     </ul>
	 *                     <p>
	 *                     Graphical clients should consider passing
	 *                     {@linkplain Beans#isDesignTime()}
	 * @param fillable1    the first sequence member
	 * @param widget1      the first member's widget
	 * @param fillable2    the second sequence member
	 * @param widget2      the second member's widget
	 * @param fillable3    the third sequence member
	 * @param widget3      the third member's widget
	 */
	public FillableSwappableTriplet(FillableSwappableSequence<T> sequence, T fillable1, T fillable2, T fillable3) {

		this.member1 = Objects.requireNonNull(fillable1);
		this.member2 = Objects.requireNonNull(fillable2);
		this.member3 = Objects.requireNonNull(fillable3);

		// creates fillable-swappable sequence and attaches listener
		this.sequenceDriver = sequence;
		sequenceDriver.attachListener(new FillableSwappableSequenceListener<T>() {
			
			// disables swap buttons according to notifications from the sequence driver
			@Override
			public void becameEmpty(T emptiedGadget) {
				// System.out.println("content removed from a gadget!");
				if (emptiedGadget == member3)
					widget.setSwappingSecondPair(false);
				else if (emptiedGadget == member2)
					widget.setSwappingFirstPair(false);
			}
			
			// enables swap buttons according to notifications from the sequence driver
			@Override
			public void becameFilled(T filledGadget) {
				// System.out.println("content added to a gadget!");
				if (filledGadget == member2)
					widget.setSwappingFirstPair(true);
				else if (filledGadget == member3)
					widget.setSwappingSecondPair(true);
			}
		});
	}

	@Override
	public void swapFirstPair() {
		sequenceDriver.swapRight(member1);
	}

	@Override
	public void swapSecondPair() {
		sequenceDriver.swapRight(member2);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("substitute chooser demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			Dimension selectorDims = new Dimension(120, 225);
			FillableSwappableTriplet<Defender, SubstitutePlayerSelector<Defender>> triplet;
			try {
				SwingSubPlayerSelector<Defender> view1 = new SwingSubPlayerSelector<Defender>(selectorDims);
				SwingSubPlayerSelector<Defender> view2 = new SwingSubPlayerSelector<Defender>(selectorDims);
				SwingSubPlayerSelector<Defender> view3 = new SwingSubPlayerSelector<Defender>(selectorDims);
				
				SubstitutePlayerSelector<Defender> selPres1 = new SubstitutePlayerSelector<Defender>(view1);
				SubstitutePlayerSelector<Defender> selPres2 = new SubstitutePlayerSelector<Defender>(view2);
				SubstitutePlayerSelector<Defender> selPres3 = new SubstitutePlayerSelector<Defender>(view3);
				
				view1.setPresenter(selPres1);
				view2.setPresenter(selPres2);
				view3.setPresenter(selPres3);
						
				triplet = new FillableSwappableTriplet<Defender, SubstitutePlayerSelector<Defender>>(
						FillableSwappableSequence.createSequence(List.of(selPres1, selPres2, selPres3)),
						selPres1, selPres2, selPres3);
				
				SwingFillableSwappableTripletWidget widget = new SwingFillableSwappableTripletWidget(
						false, view1, view2, view3);
				
				triplet.setWidget(widget);
				widget.setController(triplet);
				
				CompetitiveOptionDealingGroup.initializeDealing(Set.of(selPres1, selPres2, selPres3),
						List.of(new Defender("Giorgio", "Chiellini"), new Defender("Gerard", "Piqué"),
								new Defender("Sergio", "Ramos"), new Defender("Thiago", "Silva"),
								new Defender("Virgil", "van Dijk")));
				frame.setContentPane(widget);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	@Override
	public List<Selector<Q>> getSelectors() {
		return List.of(member1, member2, member3);
	}

	@Override
	public Optional<Selector<Q>> getNextFillableSelector() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

}
