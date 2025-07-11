package domainModel;

import java.util.HashSet;
import java.util.Set;

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
    
	public Set<Goalkeeper> goalkeepers() {
		Set<Goalkeeper> result = new HashSet<Goalkeeper>();
		visitPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitGoalkeeper(Goalkeeper goalkeeper) {
				result.add(goalkeeper);
			}
		});
		return result;
	}
	
	public Set<Defender> defenders() {
		Set<Defender> result = new HashSet<Defender>();
		visitPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitDefender(Defender defender) {
				result.add(defender);
			}
		});
		return result;
	}
	
	public Set<Midfielder> midfielders() {
		Set<Midfielder> result = new HashSet<Midfielder>();
		visitPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitMidfielder(Midfielder midfielder) {
				result.add(midfielder);
			}
		});
		return result;
	}
	
	public Set<Forward> forwards() {
		Set<Forward> result = new HashSet<Forward>();
		visitPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitForward(Forward forward) {
				result.add(forward);
			}
		});
		return result;
	}
}
