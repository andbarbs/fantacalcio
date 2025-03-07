package domainModel;

public class FantaLega {
    private final Utente admin;
    private final String nomeLega;
    private final Testata testata;
    private final String codiceLega;

    public FantaLega(Utente admin, String nomeLega, Testata testata, String codiceLega) {
        this.admin = admin;
        this.nomeLega = nomeLega;
        this.testata = testata;
        this.codiceLega = codiceLega;
    }
    // Getters
    public Utente getAdmin() {
        return admin;
    }

    public String getNomeLega() {
        return nomeLega;
    }

    public Testata getTestata() {
        return testata;
    }

    public String getCodiceLega() {
        return codiceLega;
    }
}
