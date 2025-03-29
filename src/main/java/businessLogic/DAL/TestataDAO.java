package businessLogic.DAL;

import java.util.Iterator;

import domainModel.Testata;

public interface TestataDAO {
    Iterator<Testata> getAllTestata();
    void save();

}
