/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package edu.cs65.caregiver.backend;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.GsonBuilder;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Named;


import static edu.cs65.caregiver.backend.OfyService.ofy;

/**
 * An endpoint to send messages to devices registered with the backend
 *
 * For more information, see
 * https://developers.google.com/appengine/docs/java/endpoints/
 *
 * NOTE: This endpoint does not use any form of authorization or
 * authentication! If this app is deployed, anyone can access this endpoint! If
 * you'd like to add authentication, take a look at the documentation.
 */
@Api(
  name = "messaging",
  version = "v1",
  namespace = @ApiNamespace(
    ownerDomain = "backend.caregiver.cs65.edu",
    ownerName = "backend.caregiver.cs65.edu",
    packagePath=""
  )
)
public class MessagingEndpoint {
    private static final Logger log = Logger.getLogger(MessagingEndpoint.class.getName());

    /** Api Keys can be obtained from the google cloud console */
    private static final String API_KEY = System.getProperty("gcm.api.key");

    /**
     * Send to the first 10 devices (You can modify this to send to any number of devices or a specific device)
     *
     * @param message The message to send
     */
    public void sendMessage(@Named("message") String message, @Nullable AccountObject user) throws IOException {
        if(message == null || message.trim().length() == 0) {
            log.warning("Not sending message because it is empty");
            return;
        }
        // crop longer messages
        if (message.length() > 1000) {
            message = message.substring(0, 1000) + "[...]";
        }
        Sender sender = new Sender(API_KEY);
        Message msg = new Message.Builder().addData("message", message).build();
        List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).limit(10).list();
        for(RegistrationRecord record : user.getRegistrations()) {
        //for(RegistrationRecord record : records){
            Result result = sender.send(msg, record.getRegId(), 5);
            if (result.getMessageId() != null) {
                log.info("Message sent to " + record.getRegId());
                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    // if the regId changed, we have to update the datastore
                    log.info("Registration Id changed for " + record.getRegId() + " updating to " + canonicalRegId);
                    record.setRegId(canonicalRegId);
                    ofy().save().entity(record).now();
                }
            } else {
                String error = result.getErrorCodeName();
                if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                    log.warning("Registration Id " + record.getRegId() + " no longer registered with GCM, removing from datastore");
                    // if the device is no longer registered with Gcm, remove it from the datastore
                    ofy().delete().entity(record).now();
                }
                else {
                    log.warning("Error when sending message : " + error);
                }
            }
        }
    }

    /**
     * This method should be called whenever a change in the caregiver object has been made on the
     * caregiver's end.
     * @param registration Registration ID of the phone
     * @param email Email address of account
     * @param json New serialized data of caregiver object
     */
    @ApiMethod(name = "updateEntry", httpMethod = ApiMethod.HttpMethod.PUT)
    public void updateCaregiverObject(@Named("registration") String registration,
                                      @Named("email") String email, @Named("json") String json) {
        // if registration ID matches one that is found in account's list of registration records
        // then update the caregiver object
        AccountObject account =
                ofy().load().type(AccountObject.class).filter("email", email).first().now();
        for (RegistrationRecord reg : account.getRegistrations()) {
            if (reg.getRegId().equals(registration)) {
                // registration matches, update the record
                CaregiverObject co = ofy().load().type(CaregiverObject.class)
                        .filter("email", email).first().now();
                co.setData(json);
                ofy().save().entity(co).now();
            }
        }
        // TODO: Throw error message to calling class if update does not go through
    }

    /**
     * Send notification to all caregiver registered devices for account
     * @param registration Registration ID of phone
     * @param email Email address associated with account
     * @param message Message to send via notification
     */
    @ApiMethod(name = "sendNotificationToCaregiver")
    public void sendNotificationToCaregiver(@Named("registration") String registration,
                                 @Named("email") String email, @Named("message") String message) {
        AccountObject account =
                ofy().load().type(AccountObject.class).filter("email", email).first().now();
        for (RegistrationRecord reg : account.getRegistrations()) {
            if (reg.getRegId().equals(registration)) {
                // registration matches, send notification to all other phones associated
                // with account
                AccountObject temp = new AccountObject(account.getEmail(), account.getHashedPw());
                ArrayList<RegistrationRecord> tempRegs = new ArrayList<>();
                for (RegistrationRecord record : account.getRegistrations()) {
                    if ("caregiver".equals(record.getRole())) {
                        tempRegs.add(record);
                    }
                }
                temp.setRegistrations(tempRegs);
                try {
                    //sendMessage(message); // change back!
                    sendMessage(message, temp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Return account info associated with an email address
     * @param email Email address of account
     * @return Caregiver object
     */
    @ApiMethod(name = "getAccountInfo", httpMethod = ApiMethod.HttpMethod.GET)
    public CaregiverEndpointsObject getAccountInfo(@Named("email") String email) {
        // get caregiver object from email address
        CaregiverObject caregiverObject =  ofy().load().type(CaregiverObject.class)
                .filter("email", email).first().now();
        CaregiverEndpointsObject caregiverEndpointsObject = new CaregiverEndpointsObject();
        caregiverEndpointsObject.setData(caregiverObject.getData());
        return caregiverEndpointsObject;
    }

    /**
     * Register patient to the caregiver's account
     * @param email Email address of caregiver's account
     * @param regId Registration ID of the android device
     */
    @ApiMethod(name = "registerPatientAccount", httpMethod = ApiMethod.HttpMethod.POST)
    public void registerPatientAccount(@Named("email") String email,
                                       @Named("regId") String regId) {
        // add registration ID to the caregiver account so push notifications can be
        // delivered
        AccountObject account =
                ofy().load().type(AccountObject.class).filter("email", email).first().now();
        RegistrationRecord record =
                ofy().load().type(RegistrationRecord.class).filter("regId", regId).first().now();
        record.setRole("patient");
        account.addRegistration(record);
    }


//    @ApiMethod(name = "logIn")
//    public void logIn(@Named("email") String email,
//                                                     @Named("password") String password,
//                                                     @Named("regId") String regId,
//                                                     @Named("json") String json) {
//        // return the user's data model when they log in.
//        String hashedPw = computeMD5Hash(password);
//        AccountObject account = new AccountObject(email, hashedPw);
//        // get registration record associated with the id
//        RegistrationRecord record =
//                ofy().load().type(RegistrationRecord.class).filter("regId", regId).first().now();
//        account.addRegistration(record);
//        if (ofy().load().type(AccountObject.class).filter("email", email).first().now() == null) {
//            // does not yet exist, create account
//            ofy().save().entity(account).now();
//            // create the caregiver object to be stored in the database
//            CaregiverObject co = new CaregiverObject(email, json);
//            ofy().save().entity(co).now();
//        } else {
//        }
//    }

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
