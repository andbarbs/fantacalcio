package swingViews;

import java.util.List;
import static java.util.stream.Collectors.*;

import java.awt.CardLayout;
import java.awt.Container;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.JPanel;

import domainModel.FantaTeam;
import domainModel.Player;
import domainModel.Player.*;
import swingViews.LineUpChooserPresenter.LineUpChooserView;

public class SwingStarterLineUpChooser implements LineUpChooserView {

	private SpringSchemePanel panel433;
	private SpringSchemePanel panel343;
	private SpringSchemePanel panel532;
	private List<CompetingPlayerSelector<Goalkeeper>> gkSelectors;
	private List<CompetingPlayerSelector<Defender>> dfSelectors;
	private List<CompetingPlayerSelector<Midfielder>> mfSelectors;
	private List<CompetingPlayerSelector<Forward>> fwSelectors;
	private JPanel schemesHolder;

	public SwingStarterLineUpChooser() {

		// 1) Build your 11 selectors up front, organized by role
		gkSelectors = List.of(new CompetingPlayerSelector<Goalkeeper>());
		dfSelectors = IntStream.range(0, 5) // max defenders = 5
				.mapToObj(i -> new CompetingPlayerSelector<Defender>()).collect(toList());
		mfSelectors = IntStream.range(0, 4) // e.g. max mids
				.mapToObj(i -> new CompetingPlayerSelector<Midfielder>()).collect(toList());
		fwSelectors = IntStream.range(0, 3).mapToObj(i -> new CompetingPlayerSelector<Forward>()).collect(toList());

		// 2) Instantiate all scheme panels and CardLayout‐wrap them
		panel433 = new Spring433Scheme();
		panel343 = new Spring343Scheme();
		panel532 = new Spring532Scheme();

		schemesHolder = new JPanel(new CardLayout());
		schemesHolder.add(panel433, "433");
		schemesHolder.add(panel343, "343");
		schemesHolder.add(panel532, "532");
	}

	@Override
	public void initChoice(FantaTeam team) {
		populateModels(team);
		switchScheme("433"); // default scheme
	}

	private void populateModels(FantaTeam team) {
		// TODO use team.extract() and CompetingCombo.initializeCompetition()
	}

	private void switchScheme(String schemeKey) {

		// clear all selectors from old parent slots
		Stream.concat(Stream.concat(gkSelectors.stream(), dfSelectors.stream()),
				Stream.concat(mfSelectors.stream(), fwSelectors.stream())).forEach(sel -> {
					Container parent = sel.getParent();
					if (parent != null)
						parent.remove(sel);
				});

		// establish user choice for next scheme
		SpringSchemePanel target;
		switch (schemeKey) {
		case "343":
			target = panel343;
			break;
		case "532":
			target = panel532;
			break;
		default:
			target = panel433;
		}

		// Re-attach as many as we have slots for each role
		attachSelectors(target.getGoalieSlots(), gkSelectors);
		attachSelectors(target.getDefenderSlots(), dfSelectors);
		attachSelectors(target.getMidfielderSlots(), mfSelectors);
		attachSelectors(target.getForwardSlots(), fwSelectors);

		// Show the right card
		((CardLayout) schemesHolder.getLayout()).show(schemesHolder, schemeKey);

		// Redraw
		schemesHolder.revalidate();
		schemesHolder.repaint();
	}

	private <T extends Player> void attachSelectors(List<JPanel> slots, List<CompetingPlayerSelector<T>> selectors) {
		int i = 0;

		// add selectors to slots: there could be more selectors than slots
		for (; i < slots.size(); i++) {
			slots.get(i).add(selectors.get(i));
		}

		// TODO empty out selectors that didn't make it
		for (; i < selectors.size(); i++) {
			selectors.get(i).getCompetingComboBox().setSelectedIndex(-1);
		}
	}

}
