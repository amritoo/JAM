package app.jam.jam.data;

import java.util.UUID;

import app.jam.jam.offline.BluetoothChatService;

/**
 * Defines several constants used between {@link BluetoothChatService} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    // Intent request codes
    int REQUEST_CONNECT_DEVICE_SECURE = 1;
    int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    int REQUEST_ENABLE_BT = 3;

    int ONLINE_MENU_ID = 601;
    int OFFLINE_MENU_ID = 795;

    /**
     * Return Intent extra
     */
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Name for the SDP record when creating server socket
    String NAME_SECURE = "BluetoothChatSecure";
    String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Constants that indicate the current connection state
    int STATE_NONE = 0;       // we're doing nothing
    int STATE_LISTEN = 1;     // now listening for incoming connections
    int STATE_CONNECTING = 2; // now initiating an outgoing connection
    int STATE_CONNECTED = 3;  // now connected to a remote device

    String TRUE = Boolean.TRUE.toString();
    String FALSE = Boolean.FALSE.toString();

    long SPLASH_OUT_TIME = 3000;


    // Preference file name
    String PREFERENCE_FILE_NAME = "login_activity";
    String PREFERENCE_EMAIL = "email";
    String PREFERENCE_PASSWORD = "password";


    String CURRENT_USER = "current_user";

    int CREATE_ACCOUNT_CODE = 1;
    int UPDATE_PROFILE_CODE = 2;
    int SELECT_PICTURE_CODE = 3;

    String EMAIL = "email";
    String PASSWORD = "password";
    String REMEMBER_ME = "remember";

    // Firebase paths. Very important! *** Don't change them **
    String ROOT_USER = "users";
    String ROOT_CONTACTS = "contacts";
    String ROOT_USER_STATES = "userStates";
    String ROOT_REQUESTS = "friendRequests";
    String ROOT_PROFILE_IMAGE = "profileImages";
    String CHILD_REQUEST_TYPE = "requestType";
    String CHILD_USERNAME = "userName";
    String CHILD_USER_ABOUT = "about";
    String CHILD_USER_IMAGE = "imageUri";
    String CHILD_REQUEST_TYPE_SENT = "sent";
    String CHILD_REQUEST_TYPE_RECEIVED = "received";
    String CHILD_CONTACT = "contacts";
    String CHILD_CONTACT_SAVED = "saved";

    String RECEIVER_USER_ID = "receiver_user_id";
    String USER_STATE_NEW = "new";

}
