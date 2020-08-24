package app.jam.jam.data;

import androidx.annotation.NonNull;

public class HelpItem {

    private String question, answer;

    public HelpItem(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    @NonNull
    @Override
    public String toString() {
        return "ItemModel{" +
                "question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                '}';
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

}