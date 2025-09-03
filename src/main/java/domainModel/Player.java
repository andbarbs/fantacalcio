package domainModel;

import java.util.Objects;

import jakarta.persistence.*;

@Entity
public abstract class Player {
	public static enum Club {ATALANTA, BOLOGNA, CAGLIARI, COMO, CREMONESE, FIORENTINA, GENOA, INTER, JUVENTUS, LAZIO, LECCE, MILAN, NAPOLI, PARMA, PISA, ROMA, SASSUOLO, TORINO, UDINESE, VERONA}
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Basic(optional=false)
    private String name;
    
    @Basic(optional=false)
    private String surname;

    @Basic(optional=false)
	@Enumerated(EnumType.STRING)
    private Club club;
    
    protected Player() {}

    Player(String name, String surname, Club team) {
        this.name = name;
        this.surname = surname;
		this.club = team;
    }

    // Getter

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Player player = (Player) o;
		return Objects.equals(id, player.id) && Objects.equals(name, player.name) && Objects.equals(surname, player.surname) && Objects.equals(club, player.club);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, surname, club);
	}

	public static interface PlayerVisitor {
		void visitGoalkeeper(Goalkeeper goalkeeper);
		void visitDefender(Defender defender);
		void visitMidfielder(Midfielder midfielder);
		void visitForward(Forward forward);
	}
	
	public static class PlayerVisitorAdapter implements PlayerVisitor {

		@Override
		public void visitGoalkeeper(Goalkeeper goalkeeper) {}

		@Override
		public void visitDefender(Defender defender) {}

		@Override
		public void visitMidfielder(Midfielder midfielder) {}

		@Override
		public void visitForward(Forward forward) {}
		
	}
    
    public abstract void accept(PlayerVisitor visitor);
	
	@Entity
    public static class Goalkeeper extends Player {
		
		protected Goalkeeper () {}
		
		public Goalkeeper(String name, String surname, Club team) {
			super(name, surname, team);
	    }

		@Override
		public void accept(PlayerVisitor visitor) {
			visitor.visitGoalkeeper(this);
		}
    }
	
	@Entity
    public static class Defender extends Player {
		
		protected Defender () {}
		
		public Defender(String name, String surname, Club team) {
			super(name, surname, team);
	    }

		@Override
		public void accept(PlayerVisitor visitor) {
			visitor.visitDefender(this);
		}
    }
	
	@Entity
    public static class Midfielder extends Player {
		
		protected Midfielder() {}

		public Midfielder(String name, String surname, Club team) {
			super(name, surname, team);
	    }

		@Override
		public void accept(PlayerVisitor visitor) {
			visitor.visitMidfielder(this);
		}
    }
	
	@Entity
    public static class Forward extends Player {
		
		protected Forward() {}
		
		public Forward(String name, String surname, Club team) {
			super(name, surname, team);
	    }

		@Override
		public void accept(PlayerVisitor visitor) {
			visitor.visitForward(this);
		}
    }

}

