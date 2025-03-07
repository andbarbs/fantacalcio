package businessLogic;

import ORM.GiocatoreDAO;
import ORM.TeamDAO;
import View.ListoneView;

public class FantaAllenatoreController {
    private ListoneView listoneView;
    private GiocatoreDAO giocatoreDAO;
    private TeamDAO teamDAO;


    public FantaAllenatoreController(ListoneView listoneView, GiocatoreDAO giocatoreDAO) {
        this.listoneView = listoneView;
        this.giocatoreDAO = giocatoreDAO;
    }

    public void getMatchesCurrentLeague() {}
    public void getSchieramenti(String match, String team) {}
    public void getTeamsCurrentLeague() {}
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

