package com.noxalus.vgbt.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Xml;

import com.noxalus.vgbt.entities.Game;
import com.noxalus.vgbt.entities.GameSerie;
import com.noxalus.vgbt.entities.GameSeries;
import com.noxalus.vgbt.entities.Title;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Config
{
    private final static String FILENAME = "data.xml";
    private GameSeries gameSeries;

    public enum ExcludeType
    {
        GAME_SERIE,
        GAME,
        TITLE
    };

    private Config()
    {
    }

    private static Config INSTANCE = new Config();

    public static Config getInstance()
    {
        return INSTANCE;
    }

    public GameSeries getGameSeries()
    {
        return gameSeries;
    }

    public void setGameSeries(GameSeries value)
    {
        gameSeries = value;
    }

    public ArrayList<Title> getTitlesFromGameId(int gameId)
    {
        for (GameSerie gameSerie : gameSeries)
        {
            for (Game game : gameSerie.getGames())
            {
                if (game.getId() == gameId)
                    return game.getTitles();
            }
        }

        return null;
    }

    public ArrayList<Game> getGamesFromGameSerieId(int gameSerieId)
    {
        for (GameSerie gameSerie : gameSeries)
        {
            if (gameSerie.getId() == gameSerieId)
                return gameSerie.getGames();
        }

        return null;
    }

    public boolean isThereEnoughSelectedTitles(SharedPreferences settings, int minimumNumber, int idToExclude, ExcludeType excludeType)
    {
        Set<String> excludeGameSeries = settings.getStringSet("excludeGameSeries", null);
        Set<String> excludeGames = settings.getStringSet("excludeGames", null);
        Set<String> excludeTitles = settings.getStringSet("excludeTitles", null);

        int selectedTitleCounter = 0;
        for (GameSerie gameSerie : gameSeries)
        {
            if ((gameSerie.getId() != idToExclude || excludeType != ExcludeType.GAME_SERIE) &&
                !excludeGameSeries.contains(gameSerie.getId().toString()))
            {
                for (Game game : gameSerie.getGames())
                {
                    if ((game.getId() != idToExclude || excludeType != ExcludeType.GAME) &&
                        !excludeGames.contains(game.getId().toString()))
                    {
                        for (Title title : game.getTitles())
                        {
                            if ((title.getId() != idToExclude || excludeType != ExcludeType.TITLE) &&
                                !excludeTitles.contains(title.getId().toString()))
                            {
                                selectedTitleCounter++;

                                if (selectedTitleCounter >= minimumNumber)
                                    return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public void loadGameSeries(Context context)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        try
        {
            builder = factory.newDocumentBuilder();

            File file = new File(context.getFilesDir(), FILENAME);
            InputStream is = null;
            is = new FileInputStream(file);

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

            Document doc = null;
            doc = builder.parse(is);

            NodeList gameSerieNodes = doc.getElementsByTagName("game_serie");

            if (gameSerieNodes.getLength() > 0)
            {
                GameSeries gameSerieObjectList = new GameSeries();

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

                this.gameSeries = gameSerieObjectList;
            }
        } catch (ParserConfigurationException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveGameSeries(Context context)
    {
        if (gameSeries == null)
            return;

        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "game_series");
            for (GameSerie gameSerie : gameSeries)
            {
                serializer.startTag("", "game_serie");
                serializer.attribute("", "id", gameSerie.getId().toString());
                serializer.attribute("", "name", gameSerie.getName());
                serializer.startTag("", "games");
                for (Game game : gameSerie.getGames())
                {
                    serializer.startTag("", "game");
                    serializer.attribute("", "id", game.getId().toString());
                    serializer.attribute("", "name", game.getName());
                    serializer.startTag("", "extracts");
                    for (Title title : game.getTitles())
                    {
                        serializer.startTag("", "extract");
                        serializer.attribute("", "id", title.getId().toString());
                        serializer.text(title.getName());
                        serializer.endTag("", "extract");
                    }
                    serializer.endTag("", "extracts");
                    serializer.endTag("", "game");
                }
                serializer.endTag("", "games");
                serializer.endTag("", "game_serie");
            }
            serializer.endTag("", "game_series");
            serializer.endDocument();

            File file = new File(context.getFilesDir(), FILENAME);
            OutputStreamWriter outputStream;

            try {
                outputStream = new OutputStreamWriter(context.openFileOutput(FILENAME, Context.MODE_PRIVATE));
                outputStream.write(writer.toString());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
