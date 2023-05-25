package hcmute.edu.project_06_fishclassification.model;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class User {
    private String name;
    private Timestamp birth;

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getBirth() {
        return birth;
    }

    public void setBirth(Timestamp birth) {
        this.birth = birth;
    }
}
