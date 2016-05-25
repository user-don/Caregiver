package edu.cs65.caregiver.backend;

import com.google.appengine.repackaged.com.google.api.client.util.StringUtils;
import com.google.appengine.repackaged.com.google.common.base.StringUtil;
import com.googlecode.objectify.Key;

import java.io.IOException;
import java.security.MessageDigest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static edu.cs65.caregiver.backend.OfyService.ofy;

/**
 * Call when creating a new caregiver account
 *
 * Created by don on 5/22/16.
 */
public class CreateAccountServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //super.doPost(req, resp);

//        resp.setHeader("result", "successful_account_register");

        // Here we are passed an email and password for creating a new account.
        String email = req.getParameter("email").trim();
        String password = req.getParameter("password");
        String regId = req.getParameter("registrationId");
        String caregiverJson = req.getParameter("caregiver");
        String hashedPw = computeMD5Hash(password);
        //AccountsDatastore.add(email, hashedPw);
        AccountObject account = new AccountObject(email, hashedPw);

        // get registration record associated with the id
        RegistrationRecord record =
                ofy().load().type(RegistrationRecord.class).filter("regId", regId).first().now();
        record.setRole("caregiver");
        account.addRegistration(record);
        if (ofy().load().type(AccountObject.class).filter("email", email).first().now() == null) {
            // does not yet exist, create account
            ofy().save().entity(account).now();
            // create the caregiver object to be stored in the database
            CaregiverObject co = new CaregiverObject();
            co.setData(caregiverJson);
            co.setEmail(email);
            ofy().save().entity(co).now();
            resp.getWriter().write("result: successful account register");
            resp.setHeader("result", "successful_account_register");
        } else {
            // throw an error toast message back to the client.
            // TODO: Send message: account already exists
            resp.setHeader("result", "failed_account_exists");
        }


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
