package domainModel;

import java.util.Objects;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public abstract class Player {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Basic(optional=false)
    private String name;
    
    @Basic(optional=false)
    private String surname;
    
    protected Player() {}

    Player(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    // Getter

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

	@Override
	public int hashCode() {
		return Objects.hash(name, surname);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		return Objects.equals(name, other.name) && Objects.equals(surname, other.surname);
	}
	
	@Entity
    public static class Goalkeeper extends Player {
		
		protected Goalkeeper () {}
		
		public Goalkeeper(String name, String surname) {
			super(name, surname);
	    }
    }
	
	@Entity
    public static class Defender extends Player {
		
		protected Defender () {}
		
		public Defender(String name, String surname) {
			super(name, surname);
	    }
    }
	
	@Entity
    public static class Midfielder extends Player {
		
		protected Midfielder() {}

		public Midfielder(String name, String surname) {
			super(name, surname);
	    }
    }
	
	@Entity
    public static class Forward extends Player {
		
		protected Forward() {}
		
		public Forward(String name, String surname) {
			super(name, surname);
	    }
    }
    
    
}

