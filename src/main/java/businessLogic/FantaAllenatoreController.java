package businessLogic;

import ORM.GiocatoreDAO;
import ORM.TeamDAO;
import View.ClassificaView;
import View.ListoneView;
import domainModel.FantaLega;

public class FantaAllenatoreController {
    private final ListoneView listoneView;
    private final GiocatoreDAO giocatoreDAO;
    private final TeamDAO teamDAO;
    private final ClassificaView classificaView;


    public FantaAllenatoreController(ListoneView listoneView, GiocatoreDAO giocatoreDAO, TeamDAO teamDAO, ClassificaView classificaView) {
        this.listoneView = listoneView;
        this.giocatoreDAO = giocatoreDAO;
        this.teamDAO = teamDAO;
        this.classificaView = classificaView;
    }

    public void getMatchesCurrentLeague() {}
    public void getSchieramenti(String match, String team) {}
    public void showTeams(FantaLega lega) {
        classificaView.show(teamDAO.getTeams(lega));
    }
    public void getRosa(String team) {}
    public void getCurrentProssimoMatch() {}
    public void getMyGiocatori(String ruolo) {}
    public void salvaProssimaFormazione(String modulo, String... giocatori) {}
    public void joinLega(String nomeLega, String codiceLega, String nomeTeam) {}
    public void getProposte(String ricevuteFatte) {}
    public void creaProposta(String giocatoreProposto, String giocatoreRichiesto) {}
    public void accettaProposta(String proposta) {}
    public void creaLega(String nomeLega) {}
    public void showListone() {
        listoneView.showAll(giocatoreDAO.getAllGiocatori());
    }
}

