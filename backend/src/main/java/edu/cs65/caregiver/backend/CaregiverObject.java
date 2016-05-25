package edu.cs65.caregiver.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by don on 5/23/16.
 */
@Entity
public class CaregiverObject {

    @Id
    Long id;

    @Index
    private String email;
    private String data;

    public CaregiverObject() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
