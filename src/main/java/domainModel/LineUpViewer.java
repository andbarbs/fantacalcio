package domainModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import domainModel.Fielding.*;
import domainModel.Player.*;

public class LineUpViewer {

	private LineUp lineUp;

    public LineUpViewer(LineUp lineUp) {
        this.lineUp = lineUp;
    }
    
    // Getters for starters
    
    // helper
    private void visitStarterPlayers(PlayerVisitorAdapter playerVisitorAdapter) {
    	FieldingVisitor visitor = new FieldingVisitorAdapter() {
    		@Override
    		public void visitStarterFielding(StarterFielding starterFielding) {
    			starterFielding.getPlayer().accept(playerVisitorAdapter);
    		}
    	};
    	visitSubstituteFieldings(visitor);
    }

	public List<Goalkeeper> starterGoalkeepers() {
		List<Goalkeeper> result = new ArrayList<Goalkeeper>();
		visitStarterPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitGoalkeeper(Goalkeeper goalkeeper) {
				result.add(goalkeeper);
			}
		});
		return result;
	}


    public List<Defender> starterDefenders() {
    	List<Defender> result = new ArrayList<Defender>();
		visitStarterPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitDefender(Defender defender) {
				result.add(defender);
			}
		});
		return result;
    }

    public List<Midfielder> starterMidfielders() {
    	List<Midfielder> result = new ArrayList<Midfielder>();
		visitStarterPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitMidfielder(Midfielder midfielder) {
				result.add(midfielder);
			}
		});
		return result;
    }

    public List<Forward> starterForwards() {
    	List<Forward> result = new ArrayList<Forward>();
		visitStarterPlayers(new PlayerVisitorAdapter() {
			@Override
			public void visitForward(Forward forward) {
				result.add(forward);
			}
		});
		return result;
    }
    
    // Getters for substitutes

    // helper
    private void visitSubstituteFieldings(FieldingVisitor visitor) {
    	for (Fielding fielding : lineUp.getFieldings()) {
    		fielding.accept(visitor);
    	}
    }
   
    public List<Goalkeeper> substituteGoalkeepers() {
        // Use a TreeMap to auto-sort by benchPosition.
        Map<Integer, Goalkeeper> benchMap = new TreeMap<>();

        visitSubstituteFieldings(new FieldingVisitorAdapter() {
            @Override
            public void visitSubstituteFielding(SubstituteFielding substituteFielding) {
                substituteFielding.getPlayer().accept(new PlayerVisitorAdapter() {
                    @Override
                    public void visitGoalkeeper(Goalkeeper goalkeeper) {
                        benchMap.put(substituteFielding.getBenchPosition(), goalkeeper);
                    }
                });
            }
        });

        // Flatten the sorted map values into a List.
        return new ArrayList<>(benchMap.values());
    }

    public List<Defender> substituteDefenders() {
        Map<Integer, Defender> benchMap = new TreeMap<>();

        visitSubstituteFieldings(new FieldingVisitorAdapter() {
            @Override
            public void visitSubstituteFielding(SubstituteFielding substituteFielding) {
                substituteFielding.getPlayer().accept(new PlayerVisitorAdapter() {
                    @Override
                    public void visitDefender(Defender defender) {
                        benchMap.put(substituteFielding.getBenchPosition(), defender);
                    }
                });
            }
        });

        return new ArrayList<>(benchMap.values());
    }

    public List<Midfielder> substituteMidfielders() {
        // Use a TreeMap to auto-sort by benchPosition.
        Map<Integer, Midfielder> benchMap = new TreeMap<>();

        visitSubstituteFieldings(new FieldingVisitorAdapter() {
            @Override
            public void visitSubstituteFielding(SubstituteFielding substituteFielding) {
                substituteFielding.getPlayer().accept(new PlayerVisitorAdapter() {
                    @Override
                    public void visitMidfielder(Midfielder midfielder) {
                        benchMap.put(substituteFielding.getBenchPosition(), midfielder);
                    }
                });
            }
        });

        return new ArrayList<>(benchMap.values());
    }

    public List<Forward> substituteForwards() {
        // Use a TreeMap to auto-sort by benchPosition.
        Map<Integer, Forward> benchMap = new TreeMap<>();

        visitSubstituteFieldings(new FieldingVisitorAdapter() {
            @Override
            public void visitSubstituteFielding(SubstituteFielding substituteFielding) {
                substituteFielding.getPlayer().accept(new PlayerVisitorAdapter() {
                    @Override
                    public void visitForward(Forward forward) {
                        benchMap.put(substituteFielding.getBenchPosition(), forward);
                    }
                });
            }
        });

        return new ArrayList<>(benchMap.values());
    }
}

