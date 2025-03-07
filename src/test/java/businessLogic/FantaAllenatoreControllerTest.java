package businessLogic;

import ORM.GiocatoreDAO;
import ORM.TeamDAO;
import View.ClassificaView;
import View.ListoneView;
import domainModel.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FantaAllenatoreControllerTest {

    @Mock
    private GiocatoreDAO giocatoreDAO;

    @Mock
    private ListoneView listoneView;

    @Mock
    private ClassificaView classificaView;

    @Mock
    private TeamDAO teamDAO;

    @InjectMocks
    private FantaAllenatoreController fantaAllenatoreController;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testListone() {
        Iterator<Giocatore> players = asList(new Giocatore()).iterator();
        when(giocatoreDAO.getAllGiocatori()).thenReturn(players);
        fantaAllenatoreController.showListone();
        verify(listoneView).showAll(players);
    }

    @Test
    public void testClassifica() {
        FantaLega lega = new FantaLega(new Utente(), "LeagueName", new Testata(), "code");
        Iterator<FantaTeam> teams = asList(
                new FantaTeam("Team1",lega, 0, new Utente()),
                new FantaTeam("Team2",lega, 0, new Utente())).iterator();
        when(teamDAO.getTeams(lega)).thenReturn(teams);
        fantaAllenatoreController.showTeams(lega);
        verify(classificaView).show(teams);
    }

}