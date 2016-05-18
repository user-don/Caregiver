package edu.cs65.caregiver.backend.models;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.UUID;

/**
 * UserAccount model for each individual user
 *
 * Created by don on 5/18/16.
 */
public class UserAccount {

    private String hashedPw;
    private String email;
    private UUID id;
    private ArrayList<Patient> patients;

    public UserAccount(String email, String password) {
        setHashedPw(password);
        this.email = email;
        // Create unique identifier
        this.id = UUID.fromString(this.hashedPw);
        this.patients = new ArrayList<>();
    }

    public UUID getId() {
        return id;
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

    /**
     * Computes MD5 Hash for password. Taken from tutorial here: http://bit.ly/1R9lIZc
     *
     * @param password password to hash
     */
    public void computeMD5Hash(String password) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(password.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder MD5Hash = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                MD5Hash.append(h);
            }
            hashedPw = MD5Hash.toString();

        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
