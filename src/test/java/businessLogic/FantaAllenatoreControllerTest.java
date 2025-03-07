package businessLogic;

import ORM.GiocatoreDAO;
import ORM.TeamDAO;
import View.ClassificaView;
import View.ListoneView;
import domainModel.Giocatore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
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




}