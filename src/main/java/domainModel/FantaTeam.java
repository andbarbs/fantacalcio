package domainModel;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;

@Entity
public class FantaTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic(optional=false)
    private String name;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private League league;

    @Basic(optional=false)
    private int points;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private FantaUser fantaManager;
    
    // qui va inserito  CASCADING!!!
    @OneToMany(mappedBy = Contract_.TEAM)
    private Set<Contract> contracts;

    protected FantaTeam() {}
    public FantaTeam(String name, League league, int points, FantaUser fantaManager, Set<Contract> contracts) {
        this.name = name;
        this.league = league;
        this.points = points;
        this.fantaManager = fantaManager;
		this.contracts = contracts;
    }

    // Getters
    public League getLeague() {
        return league;
    }

    public int getPoints() {
        return points;
    }

    public FantaUser getFantaManager() {
        return fantaManager;
    }

    public String getName() {
        return name;
    }
    
    public Set<Contract> getContracts() {
		return contracts;
	}

    public void setContracts(Set<Contract> contracts) {
        this.contracts = contracts;
    }

    // entry point for strongly-typed Player lookup
    public FantaTeamViewer extract() {
    	return new FantaTeamViewer(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FantaTeam fantaTeam = (FantaTeam) o;
        return points == fantaTeam.points && Objects.equals(name, fantaTeam.name) && Objects.equals(league, fantaTeam.league) && Objects.equals(fantaManager, fantaTeam.fantaManager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, league, points, fantaManager);
    }
	
}


