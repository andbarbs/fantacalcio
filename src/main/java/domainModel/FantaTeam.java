package domainModel;

import jakarta.persistence.*;

import java.util.Objects;

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
    private User fantaManager;

    protected FantaTeam() {}
    public FantaTeam(String name, League league, int points, User fantaManager) {
        this.name = name;
        this.league = league;
        this.points = points;
        this.fantaManager = fantaManager;
    }

    // Getters
    public League getLeague() {
        return league;
    }

    public int getPoints() {
        return points;
    }

    public User getFantaManager() {
        return fantaManager;
    }

    public String getName() {
        return name;
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


