package com.noxalus.vgbt.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.noxalus.vgbt.entities.Question;

public class GetQuizAsyncTask extends AsyncTask<String, String, ArrayList<Question>>
{
    public GetQuizAsyncResponse delegate = null;

    @Override
    protected void onPostExecute(ArrayList<Question> result) {
        super.onPostExecute(result);
        delegate.processFinish(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<Question> doInBackground(String... params) {

        HttpGet uri = new HttpGet(params[0]);

        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse resp = null;
        try {
            resp = client.execute(uri);

            HttpEntity entity = resp.getEntity();

            StatusLine status = resp.getStatusLine();
            if (status.getStatusCode() != 200) {
                Log.d("VGBT", "HTTP error, invalid server status code: " + resp.getStatusLine());
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = entity.getContent();

            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    Log.e("VGBT", exception.getMessage());
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    Log.e("VGBT", exception.getMessage());
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    Log.e("VGBT", exception.getMessage());
                }
            });
            Document doc = builder.parse(is);

            ArrayList<Question> questionObjectList = new ArrayList<>();
            NodeList questions = doc.getElementsByTagName("question");
            for (int i = 0; i < questions.getLength(); i++)
            {
                Question questionObject = new Question();

                Element question = (Element)questions.item(i);

                try {
                    questionObject.setAnswerIndex(Short.parseShort(question.getAttribute("answer")));
                    questionObject.setExtractId(Integer.parseInt(question.getAttribute("id")));
                }
                catch (Exception e)
                {
                    return null;
                }

                NodeList answers = question.getElementsByTagName("answer");

                ArrayList<String> answersString = new ArrayList<String>();
                for (int j = 0; j < answers.getLength(); j++)
                {
                    String currentAnswer = answers.item(j).getTextContent();
                    answersString.add(currentAnswer);
                }

                questionObject.setAnswers(answersString);

                questionObjectList.add(questionObject);
            }

            return questionObjectList;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return null;
    }
}
