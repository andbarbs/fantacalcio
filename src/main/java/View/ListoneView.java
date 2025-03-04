package View;

import domainModel.FantaTeam;
import domainModel.Giocatore;

import java.util.Iterator;

public interface ListoneView {
    void showAll(Iterator<Giocatore> giocatori);
}
