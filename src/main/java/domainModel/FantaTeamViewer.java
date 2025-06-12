package domainModel;

import java.util.ArrayList;
import java.util.List;

import domainModel.Player.Defender;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel.Player.Midfielder;
import domainModel.Player.PlayerVisitor;
import domainModel.Player.PlayerVisitorAdapter;

public class FantaTeamViewer {

	private FantaTeam fantaTeam;

	public FantaTeamViewer(FantaTeam fantaTeam) {
		this.fantaTeam = fantaTeam;
	}
	
	// helper
	private void visitPlayers(PlayerVisitor playerVisitor) {
		for(Contract contract : fantaTeam.getContracts()) {
			contract.getPlayer().accept(playerVisitor);
		}
	}	   

    // Extractors
    
	public List<Goalkeeper> goalkeepers() {
		List<Goalkeeper> result = new ArrayList<Goalkeeper>();
		visitPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitGoalkeeper(Goalkeeper goalkeeper) {
				result.add(goalkeeper);
			}
		});
		return result;
	}

	public List<Defender> defenders() {
		List<Defender> result = new ArrayList<Defender>();
		visitPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitDefender(Defender defender) {
				result.add(defender);
			}
		});
		return result;
	}
	
	public List<Midfielder> midfielders() {
		List<Midfielder> result = new ArrayList<Midfielder>();
		visitPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitMidfielder(Midfielder midfielder) {
				result.add(midfielder);
			}
		});
		return result;
	}
	
	public List<Forward> forwards() {
		List<Forward> result = new ArrayList<Forward>();
		visitPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitForward(Forward forward) {
				result.add(forward);
			}
		});
		return result;
	}




}
