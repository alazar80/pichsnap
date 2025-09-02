// android-app/app/src/main/java/com/pitchsnap/app/data/SummaryResponse.java
package com.example.pichsnap;

import java.util.List;

public class SummaryResponse {
    public String title;
    public List<String> bullets;
    public Scorecard scorecard;
    public List<String> risks;
    public List<String> questions;
    public Drafts drafts;

    public static class Scorecard {
        public int team;
        public int market;
        public int traction;
        public int clarity;
    }

    public static class Drafts {
        public String email;
        public String linkedin;
    }
}
