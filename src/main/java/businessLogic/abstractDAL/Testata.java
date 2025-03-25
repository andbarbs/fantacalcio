package businessLogic.abstractDAL;

import java.util.Iterator;

public interface Testata {
    Iterator<Testata> getAllTestata();
    void save();

}
