package com.noxalus.vgbt.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.noxalus.vgbt.entities.Game;
import com.noxalus.vgbt.entities.GameSerie;
import com.noxalus.vgbt.entities.Title;

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

public class GetGameSeriesAsyncTask extends AsyncTask<String, String, ArrayList<GameSerie>>
{
    public GetGameSeriesAsyncResponse delegate = null;

    @Override
    protected void onPostExecute(ArrayList<GameSerie> result) {
        super.onPostExecute(result);
        delegate.processFinish(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<GameSerie> doInBackground(String... params) {

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

            NodeList gameSerieNodes = doc.getElementsByTagName("game_serie");

            if (gameSerieNodes.getLength() > 0)
            {
                ArrayList<GameSerie> gameSerieObjectList = new ArrayList<>();

                for (int i = 0; i < gameSerieNodes.getLength(); i++) {

                    Element gameSerie = (Element) gameSerieNodes.item(i);

                    int gameSerieId = Integer.parseInt(gameSerie.getAttribute("id"));
                    String gameSerieName = gameSerie.getAttribute("name");

                    GameSerie gameSerieObject = new GameSerie(gameSerieId, gameSerieName);

                    // Get the list of games
                    NodeList gameNodes = ((Element) gameSerieNodes.item(i)).getElementsByTagName("game");

                    if (gameNodes.getLength() > 0) {
                        for (int j = 0; j < gameNodes.getLength(); j++) {

                            Element game = (Element) gameNodes.item(j);

                            int gameId = Integer.parseInt(game.getAttribute("id"));
                            String gameName = game.getAttribute("name");

                            Game gameObject = new Game(gameId, gameName, gameSerieId);

                            // Get the list of extracts
                            NodeList extractNodes = ((Element) gameNodes.item(j)).getElementsByTagName("extract");

                            if (extractNodes.getLength() > 0) {
                                for (int k = 0; k < extractNodes.getLength(); k++) {

                                    Element extract = (Element) extractNodes.item(k);

                                    int extractId = Integer.parseInt(extract.getAttribute("id"));
                                    String extractName = extract.getTextContent();

                                    Title extractObject = new Title(extractId, extractName, gameId);

                                    gameObject.addTitle(extractObject);
                                }
                            }

                            gameSerieObject.addGame(gameObject);
                        }
                    }

                    gameSerieObjectList.add(gameSerieObject);
                }

                return gameSerieObjectList;
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
