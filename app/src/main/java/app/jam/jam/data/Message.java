package app.jam.jam.data;

import java.text.DateFormat;
import java.util.Calendar;

public class Message {

    private String from, body, type, to, time, date, name;

    public Message() {
    }

    public Message(String type) {
        this.type = type;
        Calendar calendar = Calendar.getInstance();
        this.date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
        this.time = DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime());
    }

    public Message(String from, String body, String type, String to, String time, String date, String name) {
        this.from = from;
        this.body = body;
        this.type = type;
        this.to = to;
        this.time = time;
        this.date = date;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Message{" +
                "from='" + from + '\'' +
                ", message='" + body + '\'' +
                ", type='" + type + '\'' +
                ", to='" + to + '\'' +
                ", time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}