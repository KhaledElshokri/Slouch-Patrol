package com.example.slouch_patrol_app.Model;

public class SessionData {

    private String sessionType;
    private String sessionName;
    private String sessionNotes;
    private int[] postureScores;

    public SessionData(
            String sessionType,
            String sessionName,
            String sessionNotes,
            int[] postureScores) {

        this.sessionType = sessionType;
        this.sessionName = sessionName;
        this.sessionNotes = sessionNotes;
        this.postureScores = postureScores;
    }

}
