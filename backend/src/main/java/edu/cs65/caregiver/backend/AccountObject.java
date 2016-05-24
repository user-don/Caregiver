package edu.cs65.caregiver.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by don on 5/22/16.
 */
@Entity
public class AccountObject {

    @Id
    Long id;

    @Index
    private String email;
    private String hashedPw;

    private List<RegistrationRecord> registrations;

    public AccountObject(String email, String hashedPw) {
        this.email = email;
        this.hashedPw = hashedPw;
        this.registrations = new ArrayList<>();
    }

    public AccountObject() {
        this.email = "";
        this.hashedPw = "";
        this.registrations = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashedPw() {
        return hashedPw;
    }

    public void setHashedPw(String hashedPw) {
        this.hashedPw = hashedPw;
    }

    public List<RegistrationRecord> getRegistrations() {
        return registrations;
    }

    public void addRegistration(RegistrationRecord registrationRecord) {
        this.registrations.add(registrationRecord);
    }

    public void setRegistrations(List<RegistrationRecord> registrations) {
        this.registrations = registrations;
    }
}
