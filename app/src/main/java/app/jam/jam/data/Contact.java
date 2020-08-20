package app.jam.jam.data;

public class Contact {

    public String userName, about, imageUri;

    public Contact() {

    }

    public Contact(String userName, String about, String imageUri) {
        this.userName = userName;
        this.about = about;
        this.imageUri = imageUri;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

}
