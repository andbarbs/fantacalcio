package domainModel;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class NewsPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic(optional=false)
    String name;

    public NewsPaper() {}
    public NewsPaper(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NewsPaper newsPaper = (NewsPaper) o;
        return Objects.equals(name, newsPaper.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
