package domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class FantaUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic(optional=false)
    public String email;

    @Basic(optional=false)
    public String password;

    protected FantaUser() {}
    public FantaUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FantaUser fantaUser = (FantaUser) o;
        return Objects.equals(email, fantaUser.email) && Objects.equals(password, fantaUser.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
