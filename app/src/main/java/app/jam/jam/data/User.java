package app.jam.jam.data;


public class User {

    private String userName;
    private String about;
    private String work;
    private String address;
    private String birthDate;

    public User() {
    }

    public User(String userName, String about, String work, String address, String birthDate) {
        this.userName = userName;
        this.about = about;
        this.work = work;
        this.address = address;
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", about='" + about + '\'' +
                ", work='" + work + '\'' +
                ", address='" + address + '\'' +
                ", birthDate='" + birthDate + '\'' +
                '}';
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

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}
