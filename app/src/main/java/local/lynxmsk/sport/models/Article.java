package local.lynxmsk.sport.models;

public class Article {
    private String team1;
    private String team2;
    private String time;
    private String tournament;
    private String place;
    private SubArticle[] article;

    public String getTeam1() {
        return team1;
    }

    public String getTeam2() {
        return team2;
    }

    public String getTime() {
        return time;
    }

    public String getTournament() {
        return tournament;
    }

    public String getPlace() {
        return place;
    }

    public SubArticle[] getArticle() {
        return article;
    }
}
