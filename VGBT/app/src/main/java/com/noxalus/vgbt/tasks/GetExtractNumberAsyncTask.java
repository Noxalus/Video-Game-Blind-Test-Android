package com.noxalus.vgbt.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.noxalus.vgbt.entities.Question;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class GetExtractNumberAsyncTask extends AsyncTask<String, String, Integer>
{
    public GetExtractNumberAsyncResponse delegate = null;

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        delegate.processFinish(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... params) {

        Log.d("VGBT", "COUCOCUOCUOCUOCU: " + params[0]);

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

            NodeList extractNumberNodes = doc.getElementsByTagName("extract_number");

            if (extractNumberNodes.getLength() == 1)
            {
                Element element = (Element)extractNumberNodes.item(0);
                return Integer.parseInt(element.getTextContent());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
