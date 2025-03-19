package businessLogic.DAL;

import domainModel.Utente;

import java.util.Iterator;

public interface Testata {
    Iterator<Testata> getAllTestata();
    void save();

}
