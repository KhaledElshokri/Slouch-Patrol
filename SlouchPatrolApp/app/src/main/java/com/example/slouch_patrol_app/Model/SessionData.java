package com.example.slouch_patrol_app.Model;

public class SessionData {

    private final String sessionType;
    private final String sessionName;
    private final String sessionNotes;
    private final int[] postureScores;

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
