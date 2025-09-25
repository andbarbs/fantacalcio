package gui.lineup.triplet;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import domainModel.Player;
import domainModel.Player.Defender;
import gui.lineup.chooser.LineUpChooser.SubstituteSelectorDelegate;
import gui.lineup.chooser.LineUpChooser.SubstituteTripletChooserDelegate;
import gui.lineup.dealing.CompetitiveOptionDealingGroup;
import gui.lineup.selectors.SubstitutePlayerSelector;
import gui.lineup.selectors.SwingSubPlayerSelector;
import gui.lineup.sequence.FillableSwappableSequence;
import gui.lineup.sequence.FillableSwappableSequence.FillableSwappableSequenceListener;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * implements {@link SubstituteTripletChooserDelegate} as a
 * {@link FillableSwappableTripletController Controller} in a bidirectional
 * collaboration scheme inspired by the <b>MVP pattern</b>.
 * 
 * @param <Q> the type of {@link Player}s up for selection on this chooser
 */
public class FillableSwappableTriplet<Q extends Player> 
		implements  SubstituteTripletChooserDelegate<Q>, FillableSwappableTripletController {
	
	// sequence creation & listening
	private final SubstituteSelectorDelegate<Q> member1, member2, member3;	
	private FillableSwappableSequence<SubstituteSelectorDelegate<Q>> sequenceDriver;
	
	private FillableSwappableTripletWidget widget;
	
	public void setWidget(FillableSwappableTripletWidget widget) {
		this.widget = widget;
	}
	
	/**
	 * 
	 * @param selector1 the first {@link SubstituteSelectorDelegate} in the
	 *                  {@code triplet}
	 * @param selector2 the second {@link SubstituteSelectorDelegate} in the
	 *                  {@code triplet}
	 * @param selector3 the third {@link SubstituteSelectorDelegate} in the
	 *                  {@code triplet}
	 */
	public FillableSwappableTriplet( 
			SubstituteSelectorDelegate<Q> selector1, 
			SubstituteSelectorDelegate<Q> selector2, 
			SubstituteSelectorDelegate<Q> selector3) {

		this.member1 = Objects.requireNonNull(selector1);
		this.member2 = Objects.requireNonNull(selector2);
		this.member3 = Objects.requireNonNull(selector3);
	}

	@Override
	public void swapFirstPair() {
		sequenceDriver.swapRight(member1);
	}

	@Override
	public void swapSecondPair() {
		sequenceDriver.swapRight(member2);
	}

	@Override
	public List<SubstituteSelectorDelegate<Q>> getSelectors() {
		return List.of(member1, member2, member3);
	}

	@Override
	public Optional<SubstituteSelectorDelegate<Q>> getNextFillable() throws IllegalStateException {
		if (this.sequenceDriver == null)
			throw new IllegalStateException(
					"FillableSwappableTriplet.setSelection: Illegal State\n" +
					"this Triplet has not yet been asked to initialize " + 
					"its internal FillableSwappableSequence\n");
		return sequenceDriver.nextFillable();
	}

	@Override
	public void initSequence() {
		
		// creates fillable-swappable sequence and attaches listener
		this.sequenceDriver = FillableSwappableSequence.createSequence(List.of(member1, member2, member3));
		this.sequenceDriver.attachListener(new FillableSwappableSequenceListener<SubstituteSelectorDelegate<Q>>() {
					
					// disables swap buttons according to notifications from the sequence driver
					@Override
					public void becameEmpty(SubstituteSelectorDelegate<Q> emptiedGadget) {
						// System.out.println("content removed from a gadget!");
						if (emptiedGadget == member3)
							widget.setSwappingSecondPair(false);
						else if (emptiedGadget == member2)
							widget.setSwappingFirstPair(false);
					}
					
					// enables swap buttons according to notifications from the sequence driver
					@Override
					public void becameFilled(SubstituteSelectorDelegate<Q> filledGadget) {
						// System.out.println("content added to a gadget!");
						if (filledGadget == member2)
							widget.setSwappingFirstPair(true);
						else if (filledGadget == member3)
							widget.setSwappingSecondPair(true);
					}
				});
		
		// TODO interact with Widget and reset bookkeeping??
		widget.resetSwapping();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("substitute chooser demo");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			Dimension selectorDims = new Dimension(120, 225);
			FillableSwappableTriplet<Defender> triplet;
			try {
				SwingSubPlayerSelector<Defender> view1 = new SwingSubPlayerSelector<Defender>(selectorDims);
				SwingSubPlayerSelector<Defender> view2 = new SwingSubPlayerSelector<Defender>(selectorDims);
				SwingSubPlayerSelector<Defender> view3 = new SwingSubPlayerSelector<Defender>(selectorDims);
				
				SubstitutePlayerSelector<Defender> selPres1 = new SubstitutePlayerSelector<Defender>(view1);
				SubstitutePlayerSelector<Defender> selPres2 = new SubstitutePlayerSelector<Defender>(view2);
				SubstitutePlayerSelector<Defender> selPres3 = new SubstitutePlayerSelector<Defender>(view3);
				
				view1.setController(selPres1);
				view2.setController(selPres2);
				view3.setController(selPres3);
						
				triplet = new FillableSwappableTriplet<Defender>(
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

}
