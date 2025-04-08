package domainModel;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class LineUp {
    public static enum Module{_343, _433, _352}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private Module module;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    Match match;

    public LineUp() {}

    public LineUp(Module module, Match match) {
        this.module = module;
        this.match = match;
    }

    public Module getModule() {
        return module;
    }

    public Match getMatch() {
        return match;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LineUp lineUp = (LineUp) o;
        return module == lineUp.module && Objects.equals(match, lineUp.match);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, match);
    }
}
