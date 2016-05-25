package edu.cs65.caregiver.backend;

import com.google.appengine.repackaged.com.google.gson.Gson;

import java.io.IOException;
import java.security.MessageDigest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static edu.cs65.caregiver.backend.OfyService.ofy;

/**
 * Call when attempting to log in a caregiver
 *
 * Created by don on 5/22/16.
 */
public class LogInServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /* shouldn't handle get requests */
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        //super.doPost(req, resp);

        // Here we are passed an email and password for logging in
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String regId = req.getParameter("registrationId");
        String hashedPw = computeMD5Hash(password);

        AccountObject account =
                ofy().load().type(AccountObject.class).filter("email", email).first().now();

        if (account != null && account.getHashedPw() != null) {
            // account exists, see if hashed password matches
            boolean passCheck = hashedPw.equals(account.getHashedPw());
            if (hashedPw.equals(account.getHashedPw())) {
                // associate registration ID with the account - we are logged in
                RegistrationRecord record =
                        ofy().load().type(RegistrationRecord.class).filter("regId", regId).first().now();
                record.setRole("caregiver");
                account.addRegistration(record);
                // update account in google database
                ofy().save().entity(account).now();
                CaregiverObject co = ofy().load().type(CaregiverObject.class)
                        .filter("email", email).first().now();
                // write out the serialized CaregiverObject entry
                resp.getWriter().write(co.getData());
                resp.setHeader("result", "successful_account_login");
            }
            resp.setHeader("result", "incorrect_password");
        }
        resp.setHeader("result", "unknown_email_address");
    }

    /**
     * Computes MD5 Hash for password. Taken from tutorial here: http://bit.ly/1R9lIZc
     *
     * @param password password to hash
     */
    private String computeMD5Hash(String password) {
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
            return MD5Hash.toString();

        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
