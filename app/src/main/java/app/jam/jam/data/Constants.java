package app.jam.jam.data;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.UUID;

/**
 * Defines several constants used throughout the project.
 */
public interface Constants {

    /* Firebase paths. Very important! *** DON'T CHANGE THEM *** */
    /**
     * Root child of all users in {@link FirebaseDatabase#getReference()}
     */
    String ROOT_USERS = "users";

    /**
     * Root child of all friend requests in {@link FirebaseDatabase#getReference()}
     */
    String ROOT_REQUESTS = "friend_requests";

    /**
     * Root child of all contacts in {@link FirebaseDatabase#getReference()}
     */
    String ROOT_CONTACTS = "contacts";

    /**
     * Root child of all messages in {@link FirebaseDatabase#getReference()}
     */
    String ROOT_MESSAGES = "messages";

    /**
     * Root child of all users' profile images in {@link FirebaseStorage#getReference()}
     */
    String ROOT_PROFILE_IMAGES = "profile_images";

    /**
     * Root child of all users' profile images in {@link FirebaseStorage#getReference()}
     */
    String ROOT_IMAGES = "images";

    /**
     * The user id of admin user at {@link FirebaseDatabase#getReference()}
     */
    String CHILD_ADMIN_USER_ID = "CwcuHnHBGOPBqr7NddjbsEYFJCF3";

    /**
     * The message that will be sent to all users after successfully adding admin as contact.
     */
    String ADMIN_DEFAULT_MESSAGE = "Hello!\nThis is JAM admin.\nYou can give any feedback or review here. You can also reach us via email:\njamapp1234@gmail.com.";

    /**
     * Friend request type
     */
    String CHILD_REQUEST_TYPE = "requestType";
    String CHILD_USERNAME = "userName";
    String CHILD_USER_ABOUT = "about";
    String CHILD_USER_IMAGE = "imageUri";
    String CHILD_REQUEST_TYPE_SENT = "sent";
    String CHILD_REQUEST_TYPE_RECEIVED = "received";
    String CHILD_CONTACT = "contacts";
    String CHILD_CONTACT_SAVED = "saved";
    String CHILD_MESSAGE_SEEN = "seen";

    String MESSAGE_SEEN_DEFAULT = "unseen";
    String MESSAGE_SEEN = "seen";
    String MESSAGE_TYPE_TEXT = "text";
    String MESSAGE_TYPE_IMAGE = "image";

    String RECEIVER_USER_ID = "receiver_user_id";
    String RECEIVER_USER_NAME = "receiver_username";
    String RECEIVER_USER_IMAGE = "receiver_user_image";

    String CONNECTION_FLAG = "connection_type";
    String CONNECTION_ONLINE = "online";
    String CONNECTION_OFFLINE = "offline";

    String LOGIN_TO_ONLINE = "from_login";

    // Preferences
    String PREFERENCE_FILE_NAME = "login_activity";
    String PREFERENCE_EMAIL = "email";
    String PREFERENCE_PASSWORD = "password";
    String CURRENT_THEME = "theme";

    String REMEMBER_ME = "remember";
    String EMAIL = "email";

    String CURRENT_USER = "current_user";
    int CREATE_ACCOUNT_CODE = 1;

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
    int REQUEST_ENABLE_BT = 3;

    /**
     * To show paired status after device name in offline
     */
    String TITLE_PAIRED = "Paired";

    /**
     * Return Intent extra
     */
    String EXTRA_DEVICE_ADDRESS = "device_address";
    String EXTRA_DEVICE_NAME = "device_name";

    // Name for the SDP record when creating server socket
    String NAME_SECURE = "JAMOfflineChatSecure";
    String NAME_INSECURE = "JAMOfflineChatInsecure";

    // Unique UUID for this application
    UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Constants that indicate the current connection state
    int STATE_NONE = 0;       // we're doing nothing
    int STATE_LISTEN = 1;     // now listening for incoming connections
    int STATE_CONNECTING = 2; // now initiating an outgoing connection
    int STATE_CONNECTED = 3;  // now connected to a remote device

    /* Times */
    long SPLASH_OUT_TIME = 3000;
    long HELP_TOGGLE_ANIMATION_DURATION = 300;
    long CONNECTION_OUT_TIME = 300;
    long BLUETOOTH_DISCOVERABLE_TIME = 300;

}
