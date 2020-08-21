package app.jam.jam.data;

public class UserState {

    String status, date, time;

    public UserState() {
    }

    public UserState(String status, String date, String time) {
        this.status = status;
        this.date = date;
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
