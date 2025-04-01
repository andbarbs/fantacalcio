package businessLogic;

import domainModel.NewsPaper;

public class JournalistSessionBean {
    private NewsPaper newsPaper;
    private String journalist;

    // Getters
    public NewsPaper getNewsPaper() {
        return newsPaper;
    }

    public String getJournalist() {
        return journalist;
    }
}
