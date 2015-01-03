package vgbt.noxalus.com.videogameblindtest.entities;

import java.util.ArrayList;

public class Question
{
    ArrayList<String> answers;
    int extractId;
    short answerIndex;

    public Question()
    {
        answers = null;
        extractId = -1;
        answerIndex = 0;
    }

    public Question(int extractId, short answerIndex, ArrayList<String> answers)
    {
        this.extractId = extractId;
        this.answerIndex = answerIndex;
        this.answers = answers;
    }

    public int getExtractId() {
        return extractId;
    }

    public void setExtractId(int extractId) {
        this.extractId = extractId;
    }

    public short getAnswerIndex() {
        return answerIndex;
    }

    public void setAnswerIndex(short answerIndex) {
        this.answerIndex = answerIndex;
    }

    public ArrayList<String> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<String> answers) {
        this.answers = answers;
    }
}
