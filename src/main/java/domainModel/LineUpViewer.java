package domainModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import domainModel.Fielding.*;
import domainModel.Player.*;

public class LineUpViewer {

	private LineUp lineUp;

    public LineUpViewer(LineUp lineUp) {
        this.lineUp = lineUp;
    }
    
    // helpers

    private void visitFieldings(FieldingVisitor visitor) {
    	for (Fielding fielding : lineUp.getFieldings()) {
    		fielding.accept(visitor);
    	}
    }
    
    private void visitStarterPlayers(PlayerVisitor playerVisitor) {
    	FieldingVisitor visitor = new FieldingVisitorAdapter() {
    		@Override
    		public void visitStarterFielding(StarterFielding starterFielding) {
    			starterFielding.getPlayer().accept(playerVisitor);
    		}
    	};
    	visitFieldings(visitor);
    }

 // Extractors for Starters

    public Set<Goalkeeper> starterGoalkeepers() {
        Set<Goalkeeper> result = new HashSet<Goalkeeper>();
        visitStarterPlayers(new PlayerVisitorAdapter() {
            @Override
            public void visitGoalkeeper(Goalkeeper goalkeeper) {
                result.add(goalkeeper);
            }
        });
        return result;
    }


     public Set<Defender> starterDefenders() {
     	Set<Defender> result = new HashSet<Defender>();
 		visitStarterPlayers(new PlayerVisitorAdapter() {
 			@Override
 			public void visitDefender(Defender defender) {
 				result.add(defender);
 			}
 		});
 		return result;
     }

     public Set<Midfielder> starterMidfielders() {
     	Set<Midfielder> result = new HashSet<Midfielder>();
 		visitStarterPlayers(new PlayerVisitorAdapter() {
 			@Override
 			public void visitMidfielder(Midfielder midfielder) {
 				result.add(midfielder);
 			}
 		});
 		return result;
     }

     public Set<Forward> starterForwards() {
     	Set<Forward> result = new HashSet<Forward>();
 		visitStarterPlayers(new PlayerVisitorAdapter() {
 			@Override
 			public void visitForward(Forward forward) {
 				result.add(forward);
 			}
 		});
 		return result;
     }
    
    // Extractors for Substitutes
   
    public List<Goalkeeper> substituteGoalkeepers() {
        // Use a TreeMap to auto-sort by benchPosition.
        Map<Integer, Goalkeeper> benchMap = new TreeMap<>();

        visitFieldings(new FieldingVisitorAdapter() {
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

        visitFieldings(new FieldingVisitorAdapter() {
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

        visitFieldings(new FieldingVisitorAdapter() {
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

        visitFieldings(new FieldingVisitorAdapter() {
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

