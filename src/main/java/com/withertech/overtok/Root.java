package com.withertech.overtok;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.github.skjolber.jackson.jsh.DefaultSyntaxHighlighter;
import com.github.skjolber.jackson.jsh.SyntaxHighlightingJsonGenerator;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.withertech.overtok.Components.JColorPane;
import com.withertech.overtok.Components.JDefaultContextMenu;
import com.withertech.overtok.Components.JFilePicker;
import org.ini4j.Ini;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Root
{
    public final boolean debug = false;
    public JPanel rootPanel;
    public JTextField User1TextField;
    public JTextField User2TextField;
    public JList<Object> UserFollowerOverlapList;
    public JLabel User1Label;
    public JLabel User2Label;
    public JScrollBar UserFollowerOverlapListScrollBar;
    public JScrollPane UserFollowerOverlapListScrollPane;
    public JButton UserFollowerOverlapButton;
    public JLabel OverlapListStatus;
    public Ini ini;
    public HttpResponse<JsonNode> response1;
    public HttpResponse<JsonNode> response2;
    public String json1;
    public String json2;
    public JSONObject root1;
    public JSONObject root2;
    public JSONObject root3;
    private JLabel OutputLabel;
    private JFilePicker OutputFilePicker;
    private JPanel UserFollowerOverlapListPanel;
    private JTabbedPane OutputPane;
    private JPanel UserFollowerOverlapJsonPanel;
    private JScrollPane UserFollowerOverlapJsonTextScrollPane;
    private JScrollPane UserFollowerOverlapJsonTreeScrollPane;
    private JTree UserFollowerOverlapJsonTree;
    private JColorPane UserFollowerOverlapJsonTextPane;
    private JPanel constraintPanel;
    private JButton UserFollowerOverlapJsonTreeToggle;
    private boolean Expanded = false;

    public Root()
    {
        UserFollowerOverlapJsonTree.setModel(null);
        JDefaultContextMenu.addDefaultContextMenu(UserFollowerOverlapJsonTextPane);
        UserFollowerOverlapButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if ((!User1TextField.getText().isEmpty() && !User2TextField.getText().isEmpty() && OutputFilePicker.outputDirectory != null))
                {
                    try
                    {
                        if (!debug)
                        {
                            ini = new Ini(new File(System.getProperty("user.dir") + "/overtok.ini"));
                            response1 = Unirest.get("https://tiktok.p.rapidapi.com/live/user/follower/list?username=" + User1TextField.getText() + "&max_cursor=0&limit=200")
                                    .header("x-rapidapi-host", "tiktok.p.rapidapi.com")
                                    .header("x-rapidapi-key", ini.get("Keys", "ApiKey"))
                                    .asJson();
                            json1 = response1.getBody().toString();
                            root1 = new JSONObject(json1);
                            System.out.println(root1.toString(4));
                            response2 = Unirest.get("https://tiktok.p.rapidapi.com/live/user/follower/list?username=" + User2TextField.getText() + "&max_cursor=0&limit=200")
                                    .header("x-rapidapi-host", "tiktok.p.rapidapi.com")
                                    .header("x-rapidapi-key", ini.get("Keys", "ApiKey"))
                                    .asJson();
                            json2 = response2.getBody().toString();
                            root2 = new JSONObject(json2);
                            System.out.println(root2.toString(4));
                            if (root1.has("message") | root2.has("message"))
                            {
                                OverlapListStatus.setText(root1.has("message") ? root1.getString("message") : root2.getString("message"));
                                return;
                            }
                            while (true)
                            {

                                response1 = Unirest.get("https://tiktok.p.rapidapi.com/live/user/follower/list?username=" + User1TextField.getText() + "&max_cursor=" + root1.getString("max_cursor") + "&limit=200")
                                        .header("x-rapidapi-host", "tiktok.p.rapidapi.com")
                                        .header("x-rapidapi-key", ini.get("Keys", "ApiKey"))
                                        .asJson();
                                json1 = response1.getBody().toString();
                                if (root1.getJSONArray("followers").getJSONObject(0).getString("unique_id").equals(new JSONObject(json1).getJSONArray("followers").getJSONObject(0).getString("unique_id")))
                                {
                                    break;
                                }
                                root1 = root1.put("followers", JoinArrays(root1.getJSONArray("followers"), new JSONObject(json1).getJSONArray("followers")));
                                root1 = root1.put("max_cursor", new JSONObject(json1).getString("max_cursor"));
                                root1 = root1.put("has_more", new JSONObject(json1).getBoolean("has_more"));
                                System.out.println(root1.toString(4));
                                if (root1.has("message"))
                                {
                                    OverlapListStatus.setText(root1.getString("message"));
                                    return;
                                }

                            }
                            while (true)
                            {

                                response2 = Unirest.get("https://tiktok.p.rapidapi.com/live/user/follower/list?username=" + User2TextField.getText() + "&max_cursor=" + root2.getString("max_cursor") + "&limit=200")
                                        .header("x-rapidapi-host", "tiktok.p.rapidapi.com")
                                        .header("x-rapidapi-key", ini.get("Keys", "ApiKey"))
                                        .asJson();

                                json2 = response2.getBody().toString();
                                if (root2.getJSONArray("followers").getJSONObject(0).getString("unique_id").equals(new JSONObject(json2).getJSONArray("followers").getJSONObject(0).getString("unique_id")))
                                {
                                    break;
                                }
                                root2 = root2.put("followers", JoinArrays(root2.getJSONArray("followers"), new JSONObject(json2).getJSONArray("followers")));
                                root2 = root2.put("max_cursor", new JSONObject(json2).getString("max_cursor"));
                                root2 = root2.put("has_more", new JSONObject(json2).getBoolean("has_more"));
                                System.out.println(root2.toString(4));
                                if (root2.has("message"))
                                {
                                    OverlapListStatus.setText(root2.getString("message"));
                                    return;
                                }

                            }
                        }
                        if (debug)
                        {
                            root1 = new JSONObject("{\"has_more\":true,\"total_followers\":41005493,\"max_cursor\":\"1589812335\",\"followers\":[{\"unique_id\":\"userys5w39ir9d\",\"uid\":\"6795797315329410054\",\"nickname\":\"Noor love 🥺💞\",\"sec_uid\":\"MS4wLjABAAAAaj6ErXUJxHlUNPnoqjY9a_JqChrSpDkkfoXsri2ihkI8ohoPklApwhHSjn7JKEx8\",\"is_verified\":false,\"is_private\":false,\"country\":\"SY\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/dd23f72bcfb81df0a4218669c53b00fe~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/dd23f72bcfb81df0a4218669c53b00fe~c5_1080x1080.webp\"},{\"unique_id\":\"anmol_bajwa\",\"uid\":\"6784475674066650118\",\"nickname\":\"Anmol Bajwa\",\"sec_uid\":\"MS4wLjABAAAAhDHDyTal_DNH7KGmEs2-smHYLGsekHUHC3Kk3dFsAsbNQREcvueAIPNzcXimmgM-\",\"is_verified\":false,\"is_private\":false,\"country\":\"IT\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1664709691333637~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1664709691333637~c5_1080x1080.webp\"},{\"unique_id\":\"bannarajputana62\",\"uid\":\"6740281747408225285\",\"nickname\":\"Dipu banna rajputana\",\"sec_uid\":\"MS4wLjABAAAAqHefheEKEu9PN17Ircs1Zey2r4VnmWIB1_2me3BgQW05HyHnSRNt3qqs4mnwWAMo\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666305310438401.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666305310438401.webp\"},{\"unique_id\":\"aparajitakarar123\",\"uid\":\"6734943305442280453\",\"nickname\":\"Disha\",\"sec_uid\":\"MS4wLjABAAAAr6HwIjupNIzNfeYiiUy2SxdcbO-dSb21q8kivYQqFuFtXQRZjgSPBxaSGefuhoMa\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/ba66cec5126bc84f9663a24c154fb6b4.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/ba66cec5126bc84f9663a24c154fb6b4.webp\"},{\"unique_id\":\"yogisahu143\",\"uid\":\"6820636904968127490\",\"nickname\":\"user6818505925754\",\"sec_uid\":\"MS4wLjABAAAAtf4TpBuWu2Ot_iwHCfbZ0BIWJ_w4WC_QZCzA9CYZxAzT8SY_SIblhz6VbvCJcDIC\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666037128489985.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666037128489985.webp\"},{\"unique_id\":\"r_g_01\",\"uid\":\"6760851150940144645\",\"nickname\":\"Rushikesh Gosavi 🔥\",\"sec_uid\":\"MS4wLjABAAAAIfQlVutgNB3O-0hMa8niOVLCpXZkfFd30fPf3463yY-svs26VaH9-58nJDTBzs9x\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/a719781f4a08acb24bada3bb0baba289.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/a719781f4a08acb24bada3bb0baba289.webp\"},{\"unique_id\":\"mrgourav88000\",\"uid\":\"6700517557092713478\",\"nickname\":\"🤡 GOURAV 🤡\",\"sec_uid\":\"MS4wLjABAAAAbKFskqTSidUFRdCnrh0obtHfhbNsKvh1clmjGj5fjoD3vogIqKiGYk1ofBUO-SB7\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1665316872150017.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1665316872150017.webp\"},{\"unique_id\":\"jp5232\",\"uid\":\"6668306929166614533\",\"nickname\":\"JoHn Player (JP)\",\"sec_uid\":\"MS4wLjABAAAAEZBNGJnU5r1m9uQuO4ugf-pgkJPgAch9hrr2Rh9VYdaA5cXxR3mi2eX60H3PmXMU\",\"is_verified\":false,\"is_private\":false,\"country\":\"PK\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1656059634692098.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1656059634692098.webp\"},{\"unique_id\":\"gymnast_1404\",\"uid\":\"182824553411239936\",\"nickname\":\"Emily\",\"sec_uid\":\"MS4wLjABAAAARajZWZ9I-GjmuO2NRU6sSWsYjzsLST3PrHTo5Yg6JW7VQKThGNKuuCAYEt3FuIzV\",\"is_verified\":false,\"is_private\":false,\"country\":\"US\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1657099413699590~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1657099413699590~c5_1080x1080.webp\"},{\"unique_id\":\"manishjodhpuri\",\"uid\":\"6826009669494113281\",\"nickname\":\"Manish rajasthani888\",\"sec_uid\":\"MS4wLjABAAAAqSC26revXs2ErWEOBP9TunoZiny8ZJec_EDBzwTSljZd_lonLDXbksvsLDnSaRyl\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666545123391489.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666545123391489.webp\"},{\"unique_id\":\"sameershah5704\",\"uid\":\"6809840121132106754\",\"nickname\":\"Sameer Shah\",\"sec_uid\":\"MS4wLjABAAAA8YHsbunPAeW5yZcMhSOvJIOMGli9MmnK0vB8D0EPAJws-61FWJrRd5udyr8p7wPd\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1663683778428929.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1663683778428929.webp\"},{\"unique_id\":\"anushachakrabortyme\",\"uid\":\"6806581955343320069\",\"nickname\":\"Anusha\",\"sec_uid\":\"MS4wLjABAAAAfY-LIlhruVc7WPEmlRnQ15r_hyoPX3BiDGNxDibPoaiGvpIWPJYYf_sKG227K1I8\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1662033775653890.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1662033775653890.webp\"},{\"unique_id\":\"kritikumari070\",\"uid\":\"6806282902557574149\",\"nickname\":\"😗cute girl 😙😍\",\"sec_uid\":\"MS4wLjABAAAA3pYaof6cibU3r6krXKpZ6BmlTo6DUC-P4AmRBHZpFFANZRnny86lWbGTw13Y0Nrm\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/ae9fef9a872d24f7e074b17ef66841e3.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/ae9fef9a872d24f7e074b17ef66841e3.webp\"},{\"unique_id\":\"30661328297\",\"uid\":\"6612125363920257026\",\"nickname\":\"sameer khan\",\"sec_uid\":\"MS4wLjABAAAATDdc8Mc9-IvhUELKgEbwfOC13LPxFACTivEadaVK6lnHf9oep483a8Y3EpCXagUj\",\"is_verified\":false,\"is_private\":true,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1665841391168514.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1665841391168514.webp\"},{\"unique_id\":\"idalduzel\",\"uid\":\"6818979128353375237\",\"nickname\":\"user7362466564220\",\"sec_uid\":\"MS4wLjABAAAABUh-4_P1Xz7jr1WbYda5qcAReXR2IjxVSS4kDcJS7MsQ9Dsk2VM6lqOI2ysuSuKk\",\"is_verified\":false,\"is_private\":false,\"country\":\"TR\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/6cb8e946e0f4ddecc1b3cfb7d551307f~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/6cb8e946e0f4ddecc1b3cfb7d551307f~c5_1080x1080.webp\"},{\"unique_id\":\"supriya.durgam\",\"uid\":\"6772555347955713030\",\"nickname\":\"user3756147110308\",\"sec_uid\":\"MS4wLjABAAAAsmH6syzVj0BX1k7boAUY6TgP_sEHrdy752AcQ65gQxTzTQFTVKvjsajDWQaJThG1\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1663394762586113.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1663394762586113.webp\"},{\"unique_id\":\"karibasava04\",\"uid\":\"6745601552915186690\",\"nickname\":\"Kari Basava\",\"sec_uid\":\"MS4wLjABAAAARwy4RdRAeicQsBoB_yXc35bMDIkjO3qPLQd_m2sx6npxccLvgrwqteY0pBgQzjU1\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/8fe6529b7208c3b605cdb01bf4ff8dee.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/8fe6529b7208c3b605cdb01bf4ff8dee.webp\"},{\"unique_id\":\"kajalgngstr\",\"uid\":\"6736076635793490946\",\"nickname\":\"kajal verma\",\"sec_uid\":\"MS4wLjABAAAA3WASZhi-1UKdqscuK10aCJkYOYtExQLsL14PlkJb3uLIlMp-e7bHP32P0fLvyRYO\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1662461782103041.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1662461782103041.webp\"},{\"unique_id\":\"little_honey_singh\",\"uid\":\"6657525471100502021\",\"nickname\":\"Honey singh\",\"sec_uid\":\"MS4wLjABAAAA08XqS8HzdcWw7R8fCEoHWEZH8MLP3d2b117tgEeZoaZYgfBus9-qvrC6_9A0RZ6D\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1664058012628994.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1664058012628994.webp\"},{\"unique_id\":\"bts_army3024\",\"uid\":\"6652473811592380422\",\"nickname\":\"taetae💜💜💜\",\"sec_uid\":\"MS4wLjABAAAAx9hOUozpvoYcmYuugFc0Oj90_27aGlglIqhmtEWiQNU8QtlmRbsTYzJU85zVXQJE\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/d93e6d55e26e4a4e51e8b3fb1a3a6698.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/d93e6d55e26e4a4e51e8b3fb1a3a6698.webp\"},{\"unique_id\":\"nurulejamkecil\",\"uid\":\"6824472746024010754\",\"nickname\":\"Nurul Ejamkecil\",\"sec_uid\":\"MS4wLjABAAAA9EAddzCPRfF11IW_0mNR27nVaswtxyzImXuP-e4EI0l_qoTIbK2OSWsIi19bOQVw\",\"is_verified\":false,\"is_private\":false,\"country\":\"MY\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666131301439489.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666131301439489.webp\"},{\"unique_id\":\"styleboy70\",\"uid\":\"6817304475226588165\",\"nickname\":\"☘ Sohan☘\",\"sec_uid\":\"MS4wLjABAAAA8lJFpmsN5Am0alKSktdViBkqFHL3EEoCE7Gh6oIzZrb2oGN0tGZHdbzAotCfLH06\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/6df20e288cb0b577f844c62756ee29d2.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/6df20e288cb0b577f844c62756ee29d2.webp\"},{\"unique_id\":\"sardarawais49\",\"uid\":\"6817072187795588101\",\"nickname\":\"Awais Sardar\",\"sec_uid\":\"MS4wLjABAAAAIDSazLIp6ERUPgRgxCSPgbz_rA1Gta_rLa1fhwNWkAJ17i6odtjpHPTk051iNdBu\",\"is_verified\":false,\"is_private\":false,\"country\":\"PK\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1665965571046402.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1665965571046402.webp\"},{\"unique_id\":\"raaz.29\",\"uid\":\"6811815279699018754\",\"nickname\":\"I m raaz☑️\",\"sec_uid\":\"MS4wLjABAAAAWVLr6JGtdkRT-CwchC3j0Ew_wzgd8mu4Nteo6sSgyTQSCkae_VzP0Q4Eym50XSnc\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1663042882426881.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1663042882426881.webp\"},{\"unique_id\":\"_ali_lhaji_fazl_\",\"uid\":\"6658295192203902981\",\"nickname\":\"💪💙SHEX_ALI💙💪\",\"sec_uid\":\"MS4wLjABAAAA3e010VaC71SBhJtxGLnBtFFJkedRN9G5pCYhAkfKdu3J3VvkaNkcsEUplUoukSLx\",\"is_verified\":false,\"is_private\":false,\"country\":\"IQ\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1666453255109638~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1666453255109638~c5_1080x1080.webp\"},{\"unique_id\":\"pujjuofficial_1\",\"uid\":\"6522783549391639552\",\"nickname\":\"_Miss_tata_wat\",\"sec_uid\":\"MS4wLjABAAAA9qxyYtIytUkAqgyMnGyQps0sjPYnOBmHXlT0rMIdi2cH8r2zbtNfkBc2YgiRzupn\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666120975977474.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666120975977474.webp\"},{\"unique_id\":\"senjamwrn\",\"uid\":\"6795493741164692481\",\"nickname\":\"Senjakd✨\",\"sec_uid\":\"MS4wLjABAAAAmjyT_y_hJaWU0r-eAK5fYp6Fk56v2IpzAA4EPdPWdMYcNuGs_RcsTmV3ngK7caUP\",\"is_verified\":false,\"is_private\":false,\"country\":\"ID\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/a1fb2e8920c4bde4263d0411f852b8d3.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/a1fb2e8920c4bde4263d0411f852b8d3.webp\"},{\"unique_id\":\"brigh.tonperry\",\"uid\":\"6788319027938264070\",\"nickname\":\"Brighton 🤗\",\"sec_uid\":\"MS4wLjABAAAAjlVBi9v5IfPUMbEUM-LALlaHAHa-tGQ7wlim6lcH3azqpl6Lkh6z3K0kDdmrFW_f\",\"is_verified\":false,\"is_private\":false,\"country\":\"US\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/e9d10dddeba0db415ee5c863ccf3c778~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/e9d10dddeba0db415ee5c863ccf3c778~c5_1080x1080.webp\"},{\"unique_id\":\"djnoushad25\",\"uid\":\"6785462901191050245\",\"nickname\":\"NOUSHAD 786 786🇳\",\"sec_uid\":\"MS4wLjABAAAAPgeYR5THGfEVEBTOlNXzDbcRLnpg0uxDA876czFLC2tJeqqun6KtVSxG_0Wo1LXu\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/7cc812aa5b067a02070c8236d15454b6.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/7cc812aa5b067a02070c8236d15454b6.webp\"},{\"unique_id\":\"user2346292839367\",\"uid\":\"6767388684780471301\",\"nickname\":\"user2346292839367\",\"sec_uid\":\"MS4wLjABAAAAR0yDjzfY-G-sUGcUKDLSa8w7UkLmv_nDzOT55RLxLwEBGUBurEryBlh8dfaQYYdl\",\"is_verified\":false,\"is_private\":false,\"country\":\"RU\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1652345197624325~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1652345197624325~c5_1080x1080.webp\"},{\"unique_id\":\"tokiewokiepokie\",\"uid\":\"6724596550418301958\",\"nickname\":\"Tokiewokie\",\"sec_uid\":\"MS4wLjABAAAAIYXmuMMg_vvtk-LzLosvhitRfavs8eEmzgJMXp9_JTxVcnGkLTreBZlZKKq1z5qD\",\"is_verified\":false,\"is_private\":true,\"country\":\"BE\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1659350221722629~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1659350221722629~c5_1080x1080.webp\"},{\"unique_id\":\"mohdrashid1874\",\"uid\":\"6597296860623290374\",\"nickname\":\"mohdrashid1874\",\"sec_uid\":\"MS4wLjABAAAAJKfvcBHVaq_h-nrAFKGnwBiAG4ZVqfzQ0-yHGf-epxctUJWbmtbQ5ftFfstg6DZs\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1665066286704642.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1665066286704642.webp\"},{\"unique_id\":\"jagirdar_hr\",\"uid\":\"308654005453406209\",\"nickname\":\"Hitesh Rajpurohit\",\"sec_uid\":\"MS4wLjABAAAAjEr1FMX327ac-Yjih7XWR16_nQkkEeo8vYswI35CFZcAN3YwDTuyV7OMCkXvMYXw\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1660800620906501~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1660800620906501~c5_1080x1080.webp\"},{\"unique_id\":\"mosquerayuliana0\",\"uid\":\"6826905444301407237\",\"nickname\":\"Mosquera Yuliana\",\"sec_uid\":\"MS4wLjABAAAAWZ9oCAgOlEqn1VEKqIM0r_3xR3vj9xKPymi_jhGpxBl8pU--rWlQqntHV5sTvBed\",\"is_verified\":false,\"is_private\":true,\"country\":\"VE\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1666725259213830~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1666725259213830~c5_1080x1080.webp\"},{\"unique_id\":\"pijun_the_bird\",\"uid\":\"6696485623928390662\",\"nickname\":\"Fexhri Papenbrock\",\"sec_uid\":\"MS4wLjABAAAA5B_lNTwvO-bYPDMmwQLM7_JPnnxvjZO1UU6oND7NKgn959zqxzypXOJv5Gl8VGms\",\"is_verified\":false,\"is_private\":false,\"country\":\"DE\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1665623334303750~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1665623334303750~c5_1080x1080.webp\"},{\"unique_id\":\"devidattawniya0\",\"uid\":\"6608063554304491525\",\"nickname\":\"Devidat Tawniya\",\"sec_uid\":\"MS4wLjABAAAAAqNoEkyT6sdekDuz0Uon1El7ZojkWCVbwq9bSsEmJq35ec0ZJUjd2l1fMGDqcBW0\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1667038865236994.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1667038865236994.webp\"},{\"unique_id\":\"tushardassagor\",\"uid\":\"6546042046962996224\",\"nickname\":\"Tushar Das Sagor\",\"sec_uid\":\"MS4wLjABAAAAcT8B_R9TJeaC-DQC-oOuAmRTj8I12cXa8uYxU8GuBMsuUc16GtZZSiUK-ExPDKML\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1664836985919490.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1664836985919490.webp\"},{\"unique_id\":\"elijah_edward\",\"uid\":\"6827091738361349122\",\"nickname\":\"user4642448258824\",\"sec_uid\":\"MS4wLjABAAAAdJPs7FHQSbYdpLWhwnQaIGKnIYD2Z8HUGtKSJs_zH5d-9C19Q40YyKFaNpVjdRvC\",\"is_verified\":false,\"is_private\":false,\"country\":\"PH\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/a163539241c5aec94d8f98ccd6bef9a7.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/a163539241c5aec94d8f98ccd6bef9a7.webp\"},{\"unique_id\":\"danishjain775\",\"uid\":\"6821856402261492737\",\"nickname\":\"Prakash\",\"sec_uid\":\"MS4wLjABAAAA4BSXCy9fR2j_hQOUsghx-eGZzpfgzRbA_abTCgNcXm8JMVY1A-H2hXQuWMieLAmX\",\"is_verified\":false,\"is_private\":true,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666032144072706.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666032144072706.webp\"},{\"unique_id\":\"abhishek.patil1\",\"uid\":\"6816365519139046402\",\"nickname\":\"Abhishek Abhi\",\"sec_uid\":\"MS4wLjABAAAAWb8sAYP0214iyt2ocBoW7Y8BMTyPwSe2Gxs2SWjMalszf7sJWinJgcEYnFr8KN_8\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1664152046779393.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1664152046779393.webp\"}]}");
                            root2 = new JSONObject("{\"has_more\":true,\"total_followers\":41005493,\"max_cursor\":\"1589812335\",\"followers\":[{\"unique_id\":\"userys5w39ir9d\",\"uid\":\"6795797315329410054\",\"nickname\":\"Noor love 🥺💞\",\"sec_uid\":\"MS4wLjABAAAAaj6ErXUJxHlUNPnoqjY9a_JqChrSpDkkfoXsri2ihkI8ohoPklApwhHSjn7JKEx8\",\"is_verified\":false,\"is_private\":false,\"country\":\"SY\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/dd23f72bcfb81df0a4218669c53b00fe~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/dd23f72bcfb81df0a4218669c53b00fe~c5_1080x1080.webp\"},{\"unique_id\":\"anmol_bajwa\",\"uid\":\"6784475674066650118\",\"nickname\":\"Anmol Bajwa\",\"sec_uid\":\"MS4wLjABAAAAhDHDyTal_DNH7KGmEs2-smHYLGsekHUHC3Kk3dFsAsbNQREcvueAIPNzcXimmgM-\",\"is_verified\":false,\"is_private\":false,\"country\":\"IT\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1664709691333637~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1664709691333637~c5_1080x1080.webp\"},{\"unique_id\":\"bannarajputana62\",\"uid\":\"6740281747408225285\",\"nickname\":\"Dipu banna rajputana\",\"sec_uid\":\"MS4wLjABAAAAqHefheEKEu9PN17Ircs1Zey2r4VnmWIB1_2me3BgQW05HyHnSRNt3qqs4mnwWAMo\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666305310438401.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666305310438401.webp\"},{\"unique_id\":\"aparajitakarar123\",\"uid\":\"6734943305442280453\",\"nickname\":\"Disha\",\"sec_uid\":\"MS4wLjABAAAAr6HwIjupNIzNfeYiiUy2SxdcbO-dSb21q8kivYQqFuFtXQRZjgSPBxaSGefuhoMa\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/ba66cec5126bc84f9663a24c154fb6b4.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/ba66cec5126bc84f9663a24c154fb6b4.webp\"},{\"unique_id\":\"yogisahu143\",\"uid\":\"6820636904968127490\",\"nickname\":\"user6818505925754\",\"sec_uid\":\"MS4wLjABAAAAtf4TpBuWu2Ot_iwHCfbZ0BIWJ_w4WC_QZCzA9CYZxAzT8SY_SIblhz6VbvCJcDIC\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666037128489985.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666037128489985.webp\"},{\"unique_id\":\"r_g_01\",\"uid\":\"6760851150940144645\",\"nickname\":\"Rushikesh Gosavi 🔥\",\"sec_uid\":\"MS4wLjABAAAAIfQlVutgNB3O-0hMa8niOVLCpXZkfFd30fPf3463yY-svs26VaH9-58nJDTBzs9x\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/a719781f4a08acb24bada3bb0baba289.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/a719781f4a08acb24bada3bb0baba289.webp\"},{\"unique_id\":\"mrgourav88000\",\"uid\":\"6700517557092713478\",\"nickname\":\"🤡 GOURAV 🤡\",\"sec_uid\":\"MS4wLjABAAAAbKFskqTSidUFRdCnrh0obtHfhbNsKvh1clmjGj5fjoD3vogIqKiGYk1ofBUO-SB7\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1665316872150017.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1665316872150017.webp\"},{\"unique_id\":\"jp5232\",\"uid\":\"6668306929166614533\",\"nickname\":\"JoHn Player (JP)\",\"sec_uid\":\"MS4wLjABAAAAEZBNGJnU5r1m9uQuO4ugf-pgkJPgAch9hrr2Rh9VYdaA5cXxR3mi2eX60H3PmXMU\",\"is_verified\":false,\"is_private\":false,\"country\":\"PK\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1656059634692098.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1656059634692098.webp\"},{\"unique_id\":\"gymnast_1404\",\"uid\":\"182824553411239936\",\"nickname\":\"Emily\",\"sec_uid\":\"MS4wLjABAAAARajZWZ9I-GjmuO2NRU6sSWsYjzsLST3PrHTo5Yg6JW7VQKThGNKuuCAYEt3FuIzV\",\"is_verified\":false,\"is_private\":false,\"country\":\"US\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1657099413699590~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1657099413699590~c5_1080x1080.webp\"},{\"unique_id\":\"manishjodhpuri\",\"uid\":\"6826009669494113281\",\"nickname\":\"Manish rajasthani888\",\"sec_uid\":\"MS4wLjABAAAAqSC26revXs2ErWEOBP9TunoZiny8ZJec_EDBzwTSljZd_lonLDXbksvsLDnSaRyl\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666545123391489.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666545123391489.webp\"},{\"unique_id\":\"sameershah5704\",\"uid\":\"6809840121132106754\",\"nickname\":\"Sameer Shah\",\"sec_uid\":\"MS4wLjABAAAA8YHsbunPAeW5yZcMhSOvJIOMGli9MmnK0vB8D0EPAJws-61FWJrRd5udyr8p7wPd\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1663683778428929.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1663683778428929.webp\"},{\"unique_id\":\"anushachakrabortyme\",\"uid\":\"6806581955343320069\",\"nickname\":\"Anusha\",\"sec_uid\":\"MS4wLjABAAAAfY-LIlhruVc7WPEmlRnQ15r_hyoPX3BiDGNxDibPoaiGvpIWPJYYf_sKG227K1I8\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1662033775653890.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1662033775653890.webp\"},{\"unique_id\":\"kritikumari070\",\"uid\":\"6806282902557574149\",\"nickname\":\"😗cute girl 😙😍\",\"sec_uid\":\"MS4wLjABAAAA3pYaof6cibU3r6krXKpZ6BmlTo6DUC-P4AmRBHZpFFANZRnny86lWbGTw13Y0Nrm\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/ae9fef9a872d24f7e074b17ef66841e3.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/ae9fef9a872d24f7e074b17ef66841e3.webp\"},{\"unique_id\":\"30661328297\",\"uid\":\"6612125363920257026\",\"nickname\":\"sameer khan\",\"sec_uid\":\"MS4wLjABAAAATDdc8Mc9-IvhUELKgEbwfOC13LPxFACTivEadaVK6lnHf9oep483a8Y3EpCXagUj\",\"is_verified\":false,\"is_private\":true,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1665841391168514.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1665841391168514.webp\"},{\"unique_id\":\"idalduzel\",\"uid\":\"6818979128353375237\",\"nickname\":\"user7362466564220\",\"sec_uid\":\"MS4wLjABAAAABUh-4_P1Xz7jr1WbYda5qcAReXR2IjxVSS4kDcJS7MsQ9Dsk2VM6lqOI2ysuSuKk\",\"is_verified\":false,\"is_private\":false,\"country\":\"TR\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/6cb8e946e0f4ddecc1b3cfb7d551307f~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/6cb8e946e0f4ddecc1b3cfb7d551307f~c5_1080x1080.webp\"},{\"unique_id\":\"supriya.durgam\",\"uid\":\"6772555347955713030\",\"nickname\":\"user3756147110308\",\"sec_uid\":\"MS4wLjABAAAAsmH6syzVj0BX1k7boAUY6TgP_sEHrdy752AcQ65gQxTzTQFTVKvjsajDWQaJThG1\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1663394762586113.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1663394762586113.webp\"},{\"unique_id\":\"karibasava04\",\"uid\":\"6745601552915186690\",\"nickname\":\"Kari Basava\",\"sec_uid\":\"MS4wLjABAAAARwy4RdRAeicQsBoB_yXc35bMDIkjO3qPLQd_m2sx6npxccLvgrwqteY0pBgQzjU1\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/8fe6529b7208c3b605cdb01bf4ff8dee.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/8fe6529b7208c3b605cdb01bf4ff8dee.webp\"},{\"unique_id\":\"kajalgngstr\",\"uid\":\"6736076635793490946\",\"nickname\":\"kajal verma\",\"sec_uid\":\"MS4wLjABAAAA3WASZhi-1UKdqscuK10aCJkYOYtExQLsL14PlkJb3uLIlMp-e7bHP32P0fLvyRYO\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1662461782103041.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1662461782103041.webp\"},{\"unique_id\":\"little_honey_singh\",\"uid\":\"6657525471100502021\",\"nickname\":\"Honey singh\",\"sec_uid\":\"MS4wLjABAAAA08XqS8HzdcWw7R8fCEoHWEZH8MLP3d2b117tgEeZoaZYgfBus9-qvrC6_9A0RZ6D\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1664058012628994.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1664058012628994.webp\"},{\"unique_id\":\"bts_army3024\",\"uid\":\"6652473811592380422\",\"nickname\":\"taetae💜💜💜\",\"sec_uid\":\"MS4wLjABAAAAx9hOUozpvoYcmYuugFc0Oj90_27aGlglIqhmtEWiQNU8QtlmRbsTYzJU85zVXQJE\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/d93e6d55e26e4a4e51e8b3fb1a3a6698.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/d93e6d55e26e4a4e51e8b3fb1a3a6698.webp\"},{\"unique_id\":\"nurulejamkecil\",\"uid\":\"6824472746024010754\",\"nickname\":\"Nurul Ejamkecil\",\"sec_uid\":\"MS4wLjABAAAA9EAddzCPRfF11IW_0mNR27nVaswtxyzImXuP-e4EI0l_qoTIbK2OSWsIi19bOQVw\",\"is_verified\":false,\"is_private\":false,\"country\":\"MY\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666131301439489.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666131301439489.webp\"},{\"unique_id\":\"styleboy70\",\"uid\":\"6817304475226588165\",\"nickname\":\"☘ Sohan☘\",\"sec_uid\":\"MS4wLjABAAAA8lJFpmsN5Am0alKSktdViBkqFHL3EEoCE7Gh6oIzZrb2oGN0tGZHdbzAotCfLH06\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/6df20e288cb0b577f844c62756ee29d2.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/6df20e288cb0b577f844c62756ee29d2.webp\"},{\"unique_id\":\"sardarawais49\",\"uid\":\"6817072187795588101\",\"nickname\":\"Awais Sardar\",\"sec_uid\":\"MS4wLjABAAAAIDSazLIp6ERUPgRgxCSPgbz_rA1Gta_rLa1fhwNWkAJ17i6odtjpHPTk051iNdBu\",\"is_verified\":false,\"is_private\":false,\"country\":\"PK\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1665965571046402.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1665965571046402.webp\"},{\"unique_id\":\"raaz.29\",\"uid\":\"6811815279699018754\",\"nickname\":\"I m raaz☑️\",\"sec_uid\":\"MS4wLjABAAAAWVLr6JGtdkRT-CwchC3j0Ew_wzgd8mu4Nteo6sSgyTQSCkae_VzP0Q4Eym50XSnc\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1663042882426881.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1663042882426881.webp\"},{\"unique_id\":\"_ali_lhaji_fazl_\",\"uid\":\"6658295192203902981\",\"nickname\":\"💪💙SHEX_ALI💙💪\",\"sec_uid\":\"MS4wLjABAAAA3e010VaC71SBhJtxGLnBtFFJkedRN9G5pCYhAkfKdu3J3VvkaNkcsEUplUoukSLx\",\"is_verified\":false,\"is_private\":false,\"country\":\"IQ\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1666453255109638~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1666453255109638~c5_1080x1080.webp\"},{\"unique_id\":\"pujjuofficial_1\",\"uid\":\"6522783549391639552\",\"nickname\":\"_Miss_tata_wat\",\"sec_uid\":\"MS4wLjABAAAA9qxyYtIytUkAqgyMnGyQps0sjPYnOBmHXlT0rMIdi2cH8r2zbtNfkBc2YgiRzupn\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666120975977474.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666120975977474.webp\"},{\"unique_id\":\"senjamwrn\",\"uid\":\"6795493741164692481\",\"nickname\":\"Senjakd✨\",\"sec_uid\":\"MS4wLjABAAAAmjyT_y_hJaWU0r-eAK5fYp6Fk56v2IpzAA4EPdPWdMYcNuGs_RcsTmV3ngK7caUP\",\"is_verified\":false,\"is_private\":false,\"country\":\"ID\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/a1fb2e8920c4bde4263d0411f852b8d3.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/a1fb2e8920c4bde4263d0411f852b8d3.webp\"},{\"unique_id\":\"brigh.tonperry\",\"uid\":\"6788319027938264070\",\"nickname\":\"Brighton 🤗\",\"sec_uid\":\"MS4wLjABAAAAjlVBi9v5IfPUMbEUM-LALlaHAHa-tGQ7wlim6lcH3azqpl6Lkh6z3K0kDdmrFW_f\",\"is_verified\":false,\"is_private\":false,\"country\":\"US\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/e9d10dddeba0db415ee5c863ccf3c778~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/e9d10dddeba0db415ee5c863ccf3c778~c5_1080x1080.webp\"},{\"unique_id\":\"djnoushad25\",\"uid\":\"6785462901191050245\",\"nickname\":\"NOUSHAD 786 786🇳\",\"sec_uid\":\"MS4wLjABAAAAPgeYR5THGfEVEBTOlNXzDbcRLnpg0uxDA876czFLC2tJeqqun6KtVSxG_0Wo1LXu\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/7cc812aa5b067a02070c8236d15454b6.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/7cc812aa5b067a02070c8236d15454b6.webp\"},{\"unique_id\":\"user2346292839367\",\"uid\":\"6767388684780471301\",\"nickname\":\"user2346292839367\",\"sec_uid\":\"MS4wLjABAAAAR0yDjzfY-G-sUGcUKDLSa8w7UkLmv_nDzOT55RLxLwEBGUBurEryBlh8dfaQYYdl\",\"is_verified\":false,\"is_private\":false,\"country\":\"RU\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1652345197624325~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1652345197624325~c5_1080x1080.webp\"},{\"unique_id\":\"tokiewokiepokie\",\"uid\":\"6724596550418301958\",\"nickname\":\"Tokiewokie\",\"sec_uid\":\"MS4wLjABAAAAIYXmuMMg_vvtk-LzLosvhitRfavs8eEmzgJMXp9_JTxVcnGkLTreBZlZKKq1z5qD\",\"is_verified\":false,\"is_private\":true,\"country\":\"BE\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1659350221722629~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1659350221722629~c5_1080x1080.webp\"},{\"unique_id\":\"mohdrashid1874\",\"uid\":\"6597296860623290374\",\"nickname\":\"mohdrashid1874\",\"sec_uid\":\"MS4wLjABAAAAJKfvcBHVaq_h-nrAFKGnwBiAG4ZVqfzQ0-yHGf-epxctUJWbmtbQ5ftFfstg6DZs\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1665066286704642.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1665066286704642.webp\"},{\"unique_id\":\"jagirdar_hr\",\"uid\":\"308654005453406209\",\"nickname\":\"Hitesh Rajpurohit\",\"sec_uid\":\"MS4wLjABAAAAjEr1FMX327ac-Yjih7XWR16_nQkkEeo8vYswI35CFZcAN3YwDTuyV7OMCkXvMYXw\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1660800620906501~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1660800620906501~c5_1080x1080.webp\"},{\"unique_id\":\"mosquerayuliana0\",\"uid\":\"6826905444301407237\",\"nickname\":\"Mosquera Yuliana\",\"sec_uid\":\"MS4wLjABAAAAWZ9oCAgOlEqn1VEKqIM0r_3xR3vj9xKPymi_jhGpxBl8pU--rWlQqntHV5sTvBed\",\"is_verified\":false,\"is_private\":true,\"country\":\"VE\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1666725259213830~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1666725259213830~c5_1080x1080.webp\"},{\"unique_id\":\"pijun_the_bird\",\"uid\":\"6696485623928390662\",\"nickname\":\"Fexhri Papenbrock\",\"sec_uid\":\"MS4wLjABAAAA5B_lNTwvO-bYPDMmwQLM7_JPnnxvjZO1UU6oND7NKgn959zqxzypXOJv5Gl8VGms\",\"is_verified\":false,\"is_private\":false,\"country\":\"DE\",\"avatar_medium\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1665623334303750~c5_720x720.webp\",\"avatar_large\":\"https://p16-va-default.akamaized.net/img/musically-maliva-obj/1665623334303750~c5_1080x1080.webp\"},{\"unique_id\":\"devidattawniya0\",\"uid\":\"6608063554304491525\",\"nickname\":\"Devidat Tawniya\",\"sec_uid\":\"MS4wLjABAAAAAqNoEkyT6sdekDuz0Uon1El7ZojkWCVbwq9bSsEmJq35ec0ZJUjd2l1fMGDqcBW0\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1667038865236994.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1667038865236994.webp\"},{\"unique_id\":\"tushardassagor\",\"uid\":\"6546042046962996224\",\"nickname\":\"Tushar Das Sagor\",\"sec_uid\":\"MS4wLjABAAAAcT8B_R9TJeaC-DQC-oOuAmRTj8I12cXa8uYxU8GuBMsuUc16GtZZSiUK-ExPDKML\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1664836985919490.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1664836985919490.webp\"},{\"unique_id\":\"elijah_edward\",\"uid\":\"6827091738361349122\",\"nickname\":\"user4642448258824\",\"sec_uid\":\"MS4wLjABAAAAdJPs7FHQSbYdpLWhwnQaIGKnIYD2Z8HUGtKSJs_zH5d-9C19Q40YyKFaNpVjdRvC\",\"is_verified\":false,\"is_private\":false,\"country\":\"PH\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/a163539241c5aec94d8f98ccd6bef9a7.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/a163539241c5aec94d8f98ccd6bef9a7.webp\"},{\"unique_id\":\"danishjain775\",\"uid\":\"6821856402261492737\",\"nickname\":\"Prakash\",\"sec_uid\":\"MS4wLjABAAAA4BSXCy9fR2j_hQOUsghx-eGZzpfgzRbA_abTCgNcXm8JMVY1A-H2hXQuWMieLAmX\",\"is_verified\":false,\"is_private\":true,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1666032144072706.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1666032144072706.webp\"},{\"unique_id\":\"abhishek.patil1\",\"uid\":\"6816365519139046402\",\"nickname\":\"Abhishek Abhi\",\"sec_uid\":\"MS4wLjABAAAAWb8sAYP0214iyt2ocBoW7Y8BMTyPwSe2Gxs2SWjMalszf7sJWinJgcEYnFr8KN_8\",\"is_verified\":false,\"is_private\":false,\"country\":\"IN\",\"avatar_medium\":\"https://p16-sg-default.akamaized.net/aweme/720x720/tiktok-obj/1664152046779393.webp\",\"avatar_large\":\"https://p16-sg-default.akamaized.net/aweme/1080x1080/tiktok-obj/1664152046779393.webp\"}]}");
                        }
                        root3 = new JSONObject("{\n" +
                                "  \"has_more\": false,\n" +
                                "  \"total_followers\": 0,\n" +
                                "  \"followers\": [\n" +
                                "\n" +
                                "  ]\n" +
                                "}");
                        PrintWriter writer1 = new PrintWriter(OutputFilePicker.outputDirectory.getPath() + "/User1.json", "UTF-8");
                        writer1.print(root1.toString(4));
                        writer1.close();
                        PrintWriter writer2 = new PrintWriter(OutputFilePicker.outputDirectory.getPath() + "/User2.json", "UTF-8");
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
                                        } else
                                        {
                                            root3 = root3.put("followers", new JSONArray().put(followers1.getJSONObject(i1)));
                                        }
                                    }
                                }
                            }
                            root3 = root3.put("total_followers", overlappedFollowersList.size());
                            PrintWriter writer3 = new PrintWriter(OutputFilePicker.outputDirectory.getPath() + "/User3.json", "UTF-8");
                            writer3.print(root3.toString(4));
                            writer3.close();
                            JColorPaneWriter textWriter = new JColorPaneWriter(UserFollowerOverlapJsonTextPane);
                            JsonGenerator delegate = new JsonFactory().createGenerator(textWriter);
                            SyntaxHighlightingJsonGenerator jsonGenerator = new SyntaxHighlightingJsonGenerator(delegate, new DefaultSyntaxHighlighter());
                            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");
                            DefaultTreeModel model = new DefaultTreeModel(traverseJSON(jsonGenerator, rootNode, root3));
                            UserFollowerOverlapJsonTree.setModel(model);
                            model.reload();
                            UserFollowerOverlapList.setListData(overlappedFollowersList.toArray());
                            OverlapListStatus.setText("Total User1: " + overlappedFollowersList.size() + "/" + root1.getInt("total_followers") + " Total User2: " + overlappedFollowersList.size() + "/" + root2.getInt("total_followers"));
                        }

                    } catch (UnirestException | JSONException | IOException ee)
                    {
                        ee.printStackTrace();
                    }
                } else if (OutputFilePicker.outputDirectory == null)
                {
                    OverlapListStatus.setText("Error: Invalid output path");
                } else
                {
                    OverlapListStatus.setText("Error: Missing inputs");
                }


            }
        });
        UserFollowerOverlapJsonTreeToggle.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                if (Expanded)
                {
                    setTreeExpandedState(UserFollowerOverlapJsonTree, false);
                    Expanded = false;
                } else
                {
                    setTreeExpandedState(UserFollowerOverlapJsonTree, true);
                    Expanded = true;
                }
            }
        });
    }

    public static void setTreeExpandedState(JTree tree, boolean expanded)
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
        setNodeExpandedState(tree, node, expanded);
    }

    public static void setNodeExpandedState(JTree tree, DefaultMutableTreeNode node, boolean expanded)
    {
        ArrayList<DefaultMutableTreeNode> list = new ArrayList<>();
        for (int i = 0; i < tree.getModel().getChildCount(node); i++)
        {
            list.add((DefaultMutableTreeNode) tree.getModel().getChild(node, i));
        }
        for (DefaultMutableTreeNode treeNode : list)
        {
            setNodeExpandedState(tree, treeNode, expanded);
        }
        if (!expanded && node.isRoot())
        {
            return;
        }
        TreePath path = new TreePath(node.getPath());
        if (expanded)
        {
            tree.expandPath(path);
        } else
        {
            tree.collapsePath(path);
        }
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

    public DefaultMutableTreeNode traverseJSON(SyntaxHighlightingJsonGenerator jsonGenerator, DefaultMutableTreeNode rootNode, JSONObject rootJson) throws IOException
    {
        jsonGenerator.useDefaultPrettyPrinter();
        jsonGenerator.writeStartObject();
        for (String keyString : rootJson.keySet())
        {
            Object keyValue = rootJson.get(keyString);
            if (keyValue instanceof JSONObject)
            {
                jsonGenerator.writeObjectFieldStart(keyString);
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("Object: " + keyString);
                rootNode.add(treeNode);
                traverseJSONObject(jsonGenerator, treeNode, rootJson.getJSONObject(keyString));
                jsonGenerator.writeEndObject();
            } else if (keyValue instanceof JSONArray)
            {
                jsonGenerator.writeArrayFieldStart(keyString);
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("Array: " + keyString);
                rootNode.add(treeNode);
                traverseJSONArray(jsonGenerator, treeNode, rootJson.getJSONArray(keyString));
                jsonGenerator.writeEndArray();
            }
        }
        jsonGenerator.writeEndObject();
        jsonGenerator.close();
        return rootNode;
    }

    public void traverseJSONObject(SyntaxHighlightingJsonGenerator jsonGenerator, DefaultMutableTreeNode itemNode, JSONObject itemJson) throws IOException
    {
        for (String keyString : itemJson.keySet())
        {
            Object keyValue = itemJson.get(keyString);
            if (keyValue instanceof JSONObject)
            {
                jsonGenerator.writeStartObject();
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("Object: " + keyString);
                itemNode.add(treeNode);
                traverseJSONObject(jsonGenerator, treeNode, itemJson.getJSONObject(keyString));
                jsonGenerator.writeEndObject();
            } else if (keyValue instanceof JSONArray)
            {
                jsonGenerator.writeStartArray();
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("Array: " + keyString);
                itemNode.add(treeNode);
                traverseJSONArray(jsonGenerator, treeNode, itemJson.getJSONArray(keyString));
                jsonGenerator.writeEndArray();
            } else if (keyValue instanceof String)
            {
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(keyString + " : \"" + itemJson.getString(keyString) + "\"");
                itemNode.add(treeNode);
                jsonGenerator.writeStringField(keyString, itemJson.getString(keyString));
            } else if (keyValue instanceof Integer)
            {
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(keyString + " : " + itemJson.getInt(keyString));
                itemNode.add(treeNode);
                jsonGenerator.writeNumber(itemJson.getInt(keyString));
            }
        }
    }

    public void traverseJSONArray(SyntaxHighlightingJsonGenerator jsonGenerator, DefaultMutableTreeNode itemNode, JSONArray itemJson) throws IOException
    {
        for (int i = 0; i < itemJson.length(); i++)
        {
            Object keyValue = itemJson.get(i);
            if (keyValue instanceof JSONObject)
            {
                jsonGenerator.writeStartObject();
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("Object");
                itemNode.add(treeNode);
                traverseJSONObject(jsonGenerator, treeNode, itemJson.getJSONObject(i));
                jsonGenerator.writeEndObject();
            } else if (keyValue instanceof JSONArray)
            {
                jsonGenerator.writeStartArray();
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("Array");
                itemNode.add(treeNode);
                traverseJSONArray(jsonGenerator, treeNode, itemJson.getJSONArray(i));
                jsonGenerator.writeEndArray();
            } else if (keyValue instanceof String)
            {
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("\"" + itemJson.getString(i) + "\"");
                itemNode.add(treeNode);
                jsonGenerator.writeString(itemJson.getString(i));
            } else if (keyValue instanceof Integer)
            {
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(itemJson.getInt(i));
                itemNode.add(treeNode);
                jsonGenerator.writeNumber(itemJson.getInt(i));
            }
        }
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(9, 1, new Insets(0, 0, 0, 0), -1, -1));
        constraintPanel = new JPanel();
        constraintPanel.setLayout(new GridLayoutManager(9, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(constraintPanel, new GridConstraints(0, 0, 9, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(800, 800), null, new Dimension(800, 800), 0, false));
        User1Label = new JLabel();
        User1Label.setText("User 1 Name");
        constraintPanel.add(User1Label, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        User1TextField = new JTextField();
        constraintPanel.add(User1TextField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        User2Label = new JLabel();
        User2Label.setText("User 2 Name");
        constraintPanel.add(User2Label, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        User2TextField = new JTextField();
        constraintPanel.add(User2TextField, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        OutputLabel = new JLabel();
        OutputLabel.setText("Output Directory");
        constraintPanel.add(OutputLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        OutputFilePicker = new JFilePicker();
        constraintPanel.add(OutputFilePicker.$$$getRootComponent$$$(), new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        UserFollowerOverlapButton = new JButton();
        UserFollowerOverlapButton.setText("Start");
        constraintPanel.add(UserFollowerOverlapButton, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        OverlapListStatus = new JLabel();
        OverlapListStatus.setText("Ready");
        constraintPanel.add(OverlapListStatus, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        OutputPane = new JTabbedPane();
        constraintPanel.add(OutputPane, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        UserFollowerOverlapListPanel = new JPanel();
        UserFollowerOverlapListPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        OutputPane.addTab("List", UserFollowerOverlapListPanel);
        UserFollowerOverlapListScrollPane = new JScrollPane();
        UserFollowerOverlapListPanel.add(UserFollowerOverlapListScrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        UserFollowerOverlapList = new JList();
        UserFollowerOverlapListScrollPane.setViewportView(UserFollowerOverlapList);
        UserFollowerOverlapListScrollBar = new JScrollBar();
        UserFollowerOverlapListPanel.add(UserFollowerOverlapListScrollBar, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        UserFollowerOverlapJsonPanel = new JPanel();
        UserFollowerOverlapJsonPanel.setLayout(new GridBagLayout());
        OutputPane.addTab("Json", UserFollowerOverlapJsonPanel);
        UserFollowerOverlapJsonTreeScrollPane = new JScrollPane();
        UserFollowerOverlapJsonTreeScrollPane.setHorizontalScrollBarPolicy(30);
        UserFollowerOverlapJsonTreeScrollPane.setVerticalScrollBarPolicy(20);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        UserFollowerOverlapJsonPanel.add(UserFollowerOverlapJsonTreeScrollPane, gbc);
        UserFollowerOverlapJsonTree = new JTree();
        UserFollowerOverlapJsonTreeScrollPane.setViewportView(UserFollowerOverlapJsonTree);
        UserFollowerOverlapJsonTextScrollPane = new JScrollPane();
        UserFollowerOverlapJsonTextScrollPane.setDoubleBuffered(false);
        UserFollowerOverlapJsonTextScrollPane.setHorizontalScrollBarPolicy(30);
        UserFollowerOverlapJsonTextScrollPane.setName("");
        UserFollowerOverlapJsonTextScrollPane.setVerticalScrollBarPolicy(20);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        UserFollowerOverlapJsonPanel.add(UserFollowerOverlapJsonTextScrollPane, gbc);
        UserFollowerOverlapJsonTextPane = new JColorPane();
        UserFollowerOverlapJsonTextPane.setEditable(false);
        UserFollowerOverlapJsonTextScrollPane.setViewportView(UserFollowerOverlapJsonTextPane);
        UserFollowerOverlapJsonTreeToggle = new JButton();
        UserFollowerOverlapJsonTreeToggle.setText("Expand/Collapse Tree");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        UserFollowerOverlapJsonPanel.add(UserFollowerOverlapJsonTreeToggle, gbc);
        UserFollowerOverlapListScrollPane.setVerticalScrollBar(UserFollowerOverlapListScrollBar);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return rootPanel;
    }
}
