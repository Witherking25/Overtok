package com.withertech.overtok;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.withertech.overtok.Components.JFilePicker;
import org.ini4j.Ini;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Root
{
    public JPanel rootPanel;
    public JTextField User1TextField;
    public JTextField User2TextField;
    public JList<Object> UserFollowerOverlapList;
    public JLabel User1Label;
    public JLabel User2Label;
    public JScrollBar ScrollBar;
    public JScrollPane ScrollPane;
    public JButton UserFollowerOverlapButton;
    public JPanel constraintPanel;
    public JLabel OverlapListStatus;
    public Ini ini;
    public HttpResponse<JsonNode> response1;
    public HttpResponse<JsonNode> response2;
    public String json1;
    public String json2;
    public JSONObject root1;
    public JSONObject root2;
    public JSONObject root3;
    private JFilePicker OutputPicker;
    private JLabel OutputLabel;
    private JFilePicker OutputFilePicker;

    public Root()
    {
        UserFollowerOverlapButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if ((!User1TextField.getText().isEmpty() && !User2TextField.getText().isEmpty() && OutputFilePicker.outputDirectory != null))
                {
                    try
                    {
                        ini = new Ini(new File(System.getProperty("user.dir") + "/overtok.ini"));
                        response1 = Unirest.get("https://tiktok.p.rapidapi.com/live/user/follower/list?username=" + User1TextField.getText() + "&max_cursor=0&limit=200")
                                .header("x-rapidapi-host", "tiktok.p.rapidapi.com")
                                .header("x-rapidapi-key", ini.get("Keys", "ApiKey"))
                                .asJson();
                        json1 = response1.getBody().toString();
                        root1 = new JSONObject(json1);
                        response2 = Unirest.get("https://tiktok.p.rapidapi.com/live/user/follower/list?username=" + User2TextField.getText() + "&max_cursor=0&limit=200")
                                .header("x-rapidapi-host", "tiktok.p.rapidapi.com")
                                .header("x-rapidapi-key", ini.get("Keys", "ApiKey"))
                                .asJson();
                        json2 = response2.getBody().toString();
                        root2 = new JSONObject(json2);
                        if (root1.has("message") | root2.has("message"))
                        {
                            OverlapListStatus.setText(root1.has("message") ? root1.getString("message") : root2.getString("message"));
                            return;
                        }
                        do
                        {

                            response1 = Unirest.get("https://tiktok.p.rapidapi.com/live/user/follower/list?username=" + User1TextField.getText() + "&max_cursor=" + root1.getString("max_cursor") + "&limit=200")
                                    .header("x-rapidapi-host", "tiktok.p.rapidapi.com")
                                    .header("x-rapidapi-key", ini.get("Keys", "ApiKey"))
                                    .asJson();
                            json1 = response1.getBody().toString();
                            root1 = root1.put("followers", JoinArrays(root1.getJSONArray("followers"), new JSONObject(json1).getJSONArray("followers")));
                            if (root1.has("message"))
                            {
                                OverlapListStatus.setText(root1.getString("message"));
                                return;
                            }

                        } while (root1.getBoolean("has_more"));
                        do
                        {

                            response2 = Unirest.get("https://tiktok.p.rapidapi.com/live/user/follower/list?username=" + User2TextField.getText() + "&max_cursor=" + root2.getString("max_cursor") + "&limit=200")
                                    .header("x-rapidapi-host", "tiktok.p.rapidapi.com")
                                    .header("x-rapidapi-key", ini.get("Keys", "ApiKey"))
                                    .asJson();
                            json2 = response2.getBody().toString();
                            root2 = root2.put("followers", JoinArrays(root2.getJSONArray("followers"), new JSONObject(json2).getJSONArray("followers")));
                            if (root2.has("message"))
                            {
                                OverlapListStatus.setText(root2.getString("message"));
                                return;
                            }

                        } while (root2.getBoolean("has_more"));
                        root3 = new JSONObject("{\n" +
                                "  \"has_more\": false,\n" +
                                "  \"total_followers\": 0,\n" +
                                "  \"followers\": [\n" +
                                "\n" +
                                "  ]\n" +
                                "}");
                        PrintWriter writer1 = new PrintWriter(OutputPicker.outputDirectory.getPath() + "User1.json", "UTF-8");
                        writer1.print(root1.toString(4));
                        writer1.close();
                        PrintWriter writer2 = new PrintWriter(OutputPicker.outputDirectory.getPath() + "User2.json", "UTF-8");
                        writer2.print(root2.toString(4));
                        writer2.close();
                        if (root1.has("followers") && root2.has("followers") && root3.has("followers"))
                        {

                            JSONArray followers1 = root1.getJSONArray("followers");
                            JSONArray followers2 = root2.getJSONArray("followers");
                            List<String> overlappedFollowersList = new ArrayList<>();
                            for (int i1 = 0; i1 < followers1.length(); i1++)
                            {
                                for (int i2 = 0; i2 < followers2.length(); i2++)
                                {
                                    if (followers1.getJSONObject(i1).getString("unique_id").equals(followers2.getJSONObject(i2).getString("unique_id")))
                                    {
                                        overlappedFollowersList.add(followers1.getJSONObject(i1).getString("unique_id"));
                                        if (!root3.getJSONArray("followers").isEmpty())
                                        {
                                            root3 = root3.put("followers", root3.getJSONArray("followers").put(followers1.getJSONObject(i1)));
                                        }
                                        else
                                        {
                                            root3 = root3.put("followers", new JSONArray().put(followers1.getJSONObject(i1)));
                                        }
                                    }
                                }
                            }
                            root3 = root3.put("total_followers", overlappedFollowersList.size());
                            PrintWriter writer3 = new PrintWriter(OutputPicker.outputDirectory.getPath() + "User3.json", "UTF-8");
                            writer3.print(root3.toString(4));
                            writer3.close();
                            UserFollowerOverlapList.setListData(overlappedFollowersList.toArray());
                            OverlapListStatus.setText("Total User1: " + overlappedFollowersList.size() + "/" + root1.getInt("total_followers") + " Total User2: " + overlappedFollowersList.size() + "/" + root2.getInt("total_followers"));
                        }

                    } catch (UnirestException | JSONException | IOException ee)
                    {
                        ee.printStackTrace();
                    }
                }
                else if (OutputFilePicker.outputDirectory == null)
                {
                    OverlapListStatus.setText("Error: Invalid output path");
                }
                else
                {
                    OverlapListStatus.setText("Error: Missing inputs");
                }


            }
        });
    }

    public static JSONArray JoinArrays(JSONArray... jsonArrays)
    {
        JSONArray result = new JSONArray();
        for (JSONArray arr : jsonArrays)
        {
            for (int i = 0; i < arr.length(); i++)
            {
                result.put(arr.get(i));
            }
        }
        return result;
    }

    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //Windows Look and feel
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("Overtok");
        frame.setContentPane(new Root().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
