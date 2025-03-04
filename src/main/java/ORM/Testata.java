package ORM;

import domainModel.Utente;

import java.util.Iterator;

public interface Testata {
    Iterator<Testata> getAllTestata();
    void save();

}
