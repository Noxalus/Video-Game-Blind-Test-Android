package com.noxalus.vgbt.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.noxalus.vgbt.entities.Game;
import com.noxalus.vgbt.entities.GameSerie;

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

public class GetGamesAsyncTask extends AsyncTask<String, String, ArrayList<Game>>
{
    public GetGamesAsyncResponse delegate = null;

    @Override
    protected void onPostExecute(ArrayList<Game> result) {
        super.onPostExecute(result);
        delegate.processFinish(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<Game> doInBackground(String... params) {

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

            NodeList gameNodes = doc.getElementsByTagName("game");

            if (gameNodes.getLength() > 0)
            {
                ArrayList<Game> gameObjectList = new ArrayList<>();

                for (int i = 0; i < gameNodes.getLength(); i++) {

                    Element game = (Element) gameNodes.item(i);

                    int gameId = Integer.parseInt(game.getAttribute("id"));
                    int gameSerieId = Integer.parseInt(game.getAttribute("game_serie_id"));
                    String gameName = game.getTextContent();

                    Game gameObject = new Game(gameId, gameName, gameSerieId);

                    gameObjectList.add(gameObject);
                }

                return gameObjectList;
            }
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
