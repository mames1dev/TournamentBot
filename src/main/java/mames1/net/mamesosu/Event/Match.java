package mames1.net.mamesosu.Event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mames1.net.mamesosu.Embed;
import mames1.net.mamesosu.Main;
import mames1.net.mamesosu.Object.MySQL;
import mames1.net.mamesosu.Utils.Modal;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.sql.rowset.spi.SyncFactory.setLogger;

public class Match extends ListenerAdapter {

    public static String getMapName(int beatmap, int mode) throws SQLException {

        MySQL mySQL = new MySQL();
        Connection connection = mySQL.getConnection();
        PreparedStatement ps;
        ResultSet result;

        String API = "https://osu.ppy.sh/api/get_beatmaps?k=" + Main.tourney.getApi() + "&b=" + beatmap + "&m=" + mode;
        String slot = null;
        JsonNode jsonNode = getNodeData(API).get(0);

        ps = connection.prepareStatement("select category from beatmaps where beatmap_id = ?");
        ps.setInt(1, beatmap);
        result = ps.executeQuery();

        if (result.next()) {
            slot = result.getString("category");
        }

        connection.close();

        return slot + " | " + jsonNode.get("artist_unicode").asText() + " - " + jsonNode.get("title_unicode").asText() + " [" + jsonNode.get("version").asText() + "]";
    }

     private static String getModsName(int n) {

        ArrayList<String> mod = new ArrayList<>();
        final String[] mods = {"NF", "EZ", "TS", "HD", "HR", "SD", "DT", "RX", "HT", "NC", "FL", "", "SO", "AP", "PF", "4K", "5K", "6K", "7K", "8K", "FD", "RD", "CM", "TG", "9K", "KC", "1K", "3K", "2K", "V2", "MR"};
        StringBuilder rMods = new StringBuilder();

        for (int i = 30; i >= 0; i--) {
            if (i != 2 && i != 11 && n >= Math.pow(2, i)) {
                switch (i) {
                    case 14 -> n -= Math.pow(2, 5);
                    case 9 -> n -= Math.pow(2, 6);
                }
                mod.add(mods[i]);
                n -= Math.pow(2, i);
            }
        }

        for (String s : mod) {
            rMods.append(s);
        }

        if(!rMods.toString().equals("")) {
            return rMods.toString();
        } else {
            return "NM";
        }
    }


    public static JsonNode getNodeData(String url) {

        JsonNode node = null;

        try {
            String line;
            URL obj = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) obj.openConnection();

            urlConnection.setRequestMethod("GET");

            ObjectMapper mapper = new ObjectMapper();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder responce = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                responce.append(line);
            }
            reader.close();

            node = mapper.readTree(responce.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return node;
    }


    private String getScoreFormat(String slot, List<Score> scores) {

        StringBuilder msg = new StringBuilder();

        if(slot.toUpperCase().contains("FM")) {
            for(Score score : scores) {
                msg.append(score.getUserName()).append(": ").append(String.format("%,d", score.getScore())).append(" ").append(score.getAcc()).append("%").append("\n");
            }
        }  else {
            for(Score score : scores) {
                msg.append(score.getUserName()).append(": ").append(String.format("%,d", score.getScore())).append(" ").append(score.getAcc()).append("%").append(" | ").append(getModsName(score.getEnabled_mods())).append("\n");
            }
        }

        return msg.toString();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        if (e.getComponentId().equals("btn_mp_link")) {

            TextInput mp_link = Modal.createTextInput("mp_link", "試合のリンク", "https://osu.ppy.sh/community/matches/117436162", true, TextInputStyle.SHORT);

            e.replyModal(
                    net.dv8tion.jda.api.interactions.modals.Modal.create(
                            "modal_mp_link", "試合のリンクの入力"
                    ).addActionRows(
                            ActionRow.of(mp_link)
                    ).build()
            ).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent e) {
        if (!e.getModalId().equals("modal_mp_link")) {
            return;
        }


        String pattern = "(\\d+)$";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(e.getValue("mp_link").getAsString());

        System.out.println("!" + e.getValue("mp_link").getAsString() + "!");

        if (matcher.find()) {
            Main.tourney.setFirstPickTeam(Integer.parseInt(matcher.group(1)));
        } else {
            e.reply("試合のリンク形式が正しくありません。").setEphemeral(true).queue();
            return;
        }

        e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds()).setComponents().queue();

        e.reply("全ての処理が完了しました。\n" +
                "マッチが完了するまで指定されたチャンネルにマッチの結果が送信されます。").queue();

        // ここから更新処理

        int size = 0;

        while (true) {
                final String API = "https://osu.ppy.sh/api/get_match?k=" + Main.tourney.getApi() + "&mp=" + Main.tourney.getMpID();
                JsonNode jsonNode;

                jsonNode = getNodeData(API);

                if (jsonNode.get("match").get("end_time") != null) {
                    break;
                }

                if(jsonNode.get("games").isEmpty()) {
                    continue;
                }

                if(size == jsonNode.get("games").size()) {
                    continue;
                }
                //

                JsonNode lastGameNode = jsonNode.get("games").get(jsonNode.get("games").size() - 1);

                if(lastGameNode.get("scores").isEmpty()) {
                    continue;
                }

                Main.tourney.setCurrentlyPickTeam(Main.tourney.getCurrentlyPickTeam() == 1 ? 2 : 1);

                EmbedBuilder eb = new EmbedBuilder();

                int mode;

                int team1_total;
                int team2_total;

                String beatmap_name;
                String slot;
                String url;
                boolean v2;
                Map<Integer, java.util.List<Score>> scores = new HashMap<>();
                java.util.List<Score> team1_scores = new ArrayList<>();
                java.util.List<Score> team2_scores = new ArrayList<>();

                // ビートマップの情報を取得
                Beatmap beatmap = new Beatmap(lastGameNode);

                mode = beatmap.getMode();
                beatmap_name = beatmap.getBeatmapName();
                slot = beatmap.getSlot();
                size = jsonNode.get("games").size();
                url = beatmap.getBeatmap_url();
                v2 = jsonNode.get("match").get("scoring_type").asInt() == 3;

                eb.setTitle("**" + slot + " | " + beatmap_name + "**", url);
                eb.setColor(Main.tourney.getCurrentlyPickTeam() == 1 ? Color.RED : Color.BLUE);
                // チームのスコアを取得

                Iterator<JsonNode> slotIterator = lastGameNode.get("scores").elements();

                while(slotIterator.hasNext()) {
                    JsonNode scoreNode = slotIterator.next();

                    Score score = new Score(scoreNode, v2, mode);
                    if(score.getTeam() == 1) {
                        team1_scores.add(score);
                    } else {
                        team2_scores.add(score);
                    }
                }

                scores.put(1, team1_scores);
                scores.put(2, team2_scores);

                team1_total = team1_scores.stream().mapToInt(Score::getScore).sum();
                team2_total = team2_scores.stream().mapToInt(Score::getScore).sum();

                eb.addField("**:red_circle: Redチーム " + team1_total + "**", getScoreFormat(slot, team1_scores), false);
                eb.addField("**:blue_circle: Blueチーム " + team2_total + "**", getScoreFormat(slot, team2_scores), false);

                eb.addField("**結果**", Math.abs(team1_total - team2_total) + "点差で" + (team1_total > team2_total ? "Red" : "Blue") + "チームの勝利", false);

                eb.setFooter("Picked by " + (Main.tourney.getCurrentlyPickTeam() == 1 ? "Red" : "Blue") + "チーム", null);

                e.getJDA().getTextChannelById(Main.bot.getTourneyChannelId()).sendMessageEmbeds(
                        eb.build()
                ).queue();
        }
    }
}

class Beatmap {

    int mode;
    int beatmap_id;
    String beatmap_name;
    String slot;
    String beatmap_url;

    // get(0)以降を渡す (確実に値が存在する場合だけ)
    public Beatmap(JsonNode jsonNode) {
        try {
            beatmap_name = Match.getMapName(jsonNode.get("beatmap_id").asInt(), jsonNode.get("game_mode").asInt());
            beatmap_id = jsonNode.get("beatmap_id").asInt();
            mode = jsonNode.get("game_mode").asInt();
            slot = loadSlot(beatmap_id);
            beatmap_url = getURL(beatmap_id, mode);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getURL(int beatmap, int mode) {
        String API = "https://osu.ppy.sh/api/get_beatmaps?k=" + Main.tourney.getApi() + "&b=" + beatmap + "&m=" + mode;
        JsonNode jsonNode = Match.getNodeData(API).get(0);
        String[] mode_str = {"osu", "taiko", "fruits", "mania"};

        return "https://osu.ppy.sh/beatmaps/" + jsonNode.get("beatmapset_id").asInt() + "#" + mode_str[mode] + "/" + jsonNode.get("beatmap_id").asInt();
    }

    public int getMode() {
        return mode;
    }

    public String getBeatmap_url() {
        return beatmap_url;
    }

    public int getBeatmapId() {
        return beatmap_id;
    }

    public String getBeatmapName() {
        return beatmap_name;
    }

    public String getSlot() {
        return slot.toUpperCase();
    }

    private String loadSlot(int beatmap_id) {
        MySQL mySQL = new MySQL();

        try {
            Connection connection = mySQL.getConnection();
            PreparedStatement ps;
            ResultSet result;

            ps = connection.prepareStatement("select category from beatmaps where beatmap_id = ?");
            ps.setInt(1, beatmap_id);
            result = ps.executeQuery();

            if(result.next()) {
                return result.getString("category");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class Score {
    int team;
    int score;
    double acc;
    int enabled_mods;
    int userid;
    String userName;

    private double getAcc(JsonNode jsonNode, boolean v2, int mode) {
        int count300 = jsonNode.get("count300").asInt();
        int count100 = jsonNode.get("count100").asInt();
        int count50 = jsonNode.get("count50").asInt();
        int countmiss = jsonNode.get("countmiss").asInt();
        int countgeki = jsonNode.get("countgeki").asInt();
        int countkatu = jsonNode.get("countkatu").asInt();
        int total;

        if(mode == 0) {
            total = count300 + count100 + count50 + countmiss;

            if (total == 0) {
                return 0;
            }

            return 100 * (count300 * 300 + count100 * 100 + count50 * 50) / (double)(total * 300);
        } else if (mode == 1) {
            total = count300 + count100 + countmiss;

            if (total == 0) {
                return 0;
            }

            return 100 * (count100 * 0.5 + count300) / (double)total;
        } else if (mode == 2) {
            total = count300 + count100 + count50 + countkatu + countmiss;

            if (total == 0) {
                return 0;
            }

            return 100 * (count300 + count100 + count50) / (double)(total);
        } else if (mode == 3) {
            total = count300 + count100 + count50 + countkatu + countmiss + countgeki;

            if (total == 0) {
                return 0;
            }

            if (v2) {
                return 100.0 * (count50 * 50 + count100 * 100 + countkatu * 200 + count300 * 300 + countgeki * 305) / (double)(total * 305);
            }

            return 100.0 * (count50 * 50 + count100 * 100 + countkatu * 200 + (count300 + countgeki) * 300) / (double)(total * 300);
        }

        // acc error
        return 0;
    }

    public Score(JsonNode jsonNode, boolean v2, int mode) {

        team = jsonNode.get("team").asInt();
        score = jsonNode.get("score").asInt();
        acc = getAcc(jsonNode, v2, mode);
        userid = jsonNode.get("user_id").asInt();
        userName = Match.getNodeData("https://osu.ppy.sh/api/get_user?k=" + Main.tourney.getApi() + "&u=" + userid).get(0).get("username").asText();

        if (jsonNode.get("enabled_mods") == null) {
            enabled_mods = 0;
        } else {
            enabled_mods = jsonNode.get("enabled_mods").asInt();
        }
    }

    public int getTeam() {
        return team;
    }

    public double getAcc() {
        return acc;
    }

    public int getEnabled_mods() {
        return enabled_mods;
    }

    public int getScore() {
        return score;
    }

    public int getUserid() {
        return userid;
    }

    public String getUserName() {
        return userName;
    }
}
