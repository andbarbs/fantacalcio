package domainModel;

public class Grade {
	
	private final float mark;
	private final Giocatore giocatore;
	private final int goals;
	private final int assists;
	
	public Grade(float mark, Giocatore giocatore, int goals, int assists) {
		super();
		this.mark = mark;
		this.giocatore = giocatore;
		this.goals = goals;
		this.assists = assists;
	}

	public float getMark() {
		return mark;
	}

	public Giocatore getGiocatore() {
		return giocatore;
	}

	public int getGoals() {
		return goals;
	}

	public int getAssists() {
		return assists;
	}
	
	
	
	
	
}
