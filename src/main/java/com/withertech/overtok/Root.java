package com.withertech.overtok;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Root
{
	private JPanel rootPanel;
	private JTextField User1TextField;
	private JTextField User2TextField;
	private JList UserFollowerOverlapList;
	private JLabel User1Label;
	private JLabel User2Label;
	private JScrollBar ScrollBar;
	private JScrollPane ScrollPane;
	private JButton UserFollowerOverlapButton;
	private JPanel constraintPanel;
	private JLabel OverlapListStatus;
	private Ini ini;
	private HttpResponse<JsonNode> response1;
	private HttpResponse<JsonNode> response2;
	private String json1;
	private String json2;
	private JSONObject root1;
	private JSONObject root2;

	public Root()
	{
		UserFollowerOverlapButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!(User1TextField.getText().isEmpty() && User2TextField.getText().isEmpty()))
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
							OverlapListStatus.setText("Request Quota Reached!");
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
								OverlapListStatus.setText("Request Quota Reached!");
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
								OverlapListStatus.setText("Request Quota Reached!");
								return;
							}

						} while (root2.getBoolean("has_more"));
						PrintWriter writer1 = new PrintWriter("User1.json", "UTF-8");
						writer1.print(root1.toString(4));
						writer1.close();
						PrintWriter writer2 = new PrintWriter("User2.json", "UTF-8");
						writer2.print(root2.toString(4));
						writer2.close();
						if (root1.has("followers") && root2.has("followers"))
						{
							JSONArray followers1 = root1.getJSONArray("followers");
							JSONArray followers2 = root2.getJSONArray("followers");
							List<String> overlappedFollowersList = new ArrayList<String>();
							for (int i1 = 0; i1 < followers1.length(); i1++)
							{
								for (int i2 = 0; i2 < followers1.length(); i2++)
								{
									if (followers1.getJSONObject(i1).getString("unique_id").equals(followers2.getJSONObject(i2).getString("unique_id")))
									{
										overlappedFollowersList.add(followers1.getJSONObject(i1).getString("unique_id"));
									}
								}
							}
							UserFollowerOverlapList.setListData(overlappedFollowersList.toArray());
							OverlapListStatus.setText("Total Amount of Overlapping Followers: " + overlappedFollowersList.size());
						}

					} catch (UnirestException | JSONException | IOException ee)
					{
						ee.printStackTrace();
					}
				} else
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
