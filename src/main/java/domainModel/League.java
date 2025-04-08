package domainModel;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private User admin;

    @Basic(optional=false)
    private String name;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private NewsPaper newsPaper;

    @Basic(optional=false)
    private String leagueCode;

    public League() {}
    public League(User admin, String name, NewsPaper newsPaper, String leagueCode) {
        this.admin = admin;
        this.name = name;
        this.newsPaper = newsPaper;
        this.leagueCode = leagueCode;
    }
    // Getters
    public User getAdmin() {
        return admin;
    }

    public String getName() {
        return name;
    }

    public NewsPaper getNewsPaper() {
        return newsPaper;
    }

    public String getLeagueCode() {
        return leagueCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        League league = (League) o;
        return Objects.equals(admin, league.admin) && Objects.equals(name, league.name) && Objects.equals(newsPaper, league.newsPaper) && Objects.equals(leagueCode, league.leagueCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(admin, name, newsPaper, leagueCode);
    }
}
