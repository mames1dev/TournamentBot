package mames1.net.mamesosu.Event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mames1.net.mamesosu.Embed;
import mames1.net.mamesosu.Main;
import mames1.net.mamesosu.Utils.Modal;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
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
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Match extends ListenerAdapter {

    public static String getMapName(int beatmap, int mode) throws SQLException {

        String API = "https://osu.ppy.sh/api/get_beatmaps?k=" + Main.tourney.getApi() + "&b=" + beatmap;
        String slot;

        System.out.println(API);

        JsonNode jsonNode = getNodeData(API).get(0);

        System.out.println(jsonNode);

        slot = Main.tourney.getMaps().get(beatmap);

        return slot + " | "
            + (jsonNode != null ? jsonNode.get("artist_unicode").asText() : "Unknown Artist")
            + " - "
            + (jsonNode != null ? jsonNode.get("title_unicode").asText() : "Unknown Title")
            + " ["
            + (jsonNode != null ? jsonNode.get("version").asText() : "Unknown Version")
            + "]";
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

        if (!rMods.toString().equals("")) {
            return rMods.toString();
        } else {
            return "NM";
        }
    }

    private static boolean checkBeatmap(int beatmap) {

        return Main.tourney.getMaps().get(beatmap).isEmpty();
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

        if (slot.toUpperCase().contains("FM")) {
            for (Score score : scores) {
                msg.append(score.getUserName()).append(": ").append(String.format("%,d", score.getScore())).append(" ").append(String.format("%.2f", score.getAcc())).append("%").append(" | ").append(getModsName(score.getEnabled_mods())).append("\n");
            }
        } else {
            for (Score score : scores) {
                msg.append(score.getUserName()).append(": ").append(String.format("%,d", score.getScore())).append(" ").append(String.format("%.2f", score.getAcc())).append("%").append("\n");
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

    public void startSchedule(ModalInteractionEvent e) {
        final int[] size = {0};
        final boolean[] isOnceSend = {false};

        e.getJDA().getPresence().setActivity(Activity.watching("マッチを開始します"));

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                final String API = "https://osu.ppy.sh/api/get_match?k=" + Main.tourney.getApi() + "&mp=" + Main.tourney.getMpID();
                JsonNode jsonNode;

                jsonNode = getNodeData(API);

                System.out.println(jsonNode.get("match").get("end_time").asText());

                if (!jsonNode.get("match").get("end_time").asText().equals("null")) {
                    System.out.println("end");
                    e.getJDA().getPresence().setActivity(Activity.playing("試合開始を待機しています"));
                    timer.cancel();

                    if (isOnceSend[0]) {

                        StringBuilder sb = new StringBuilder();
                        int team1_total = Main.tourney.getTotalScores().get(1).stream().mapToInt(Integer::intValue).sum();
                        int team2_total = Main.tourney.getTotalScores().get(2).stream().mapToInt(Integer::intValue).sum();

                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle("**" +  jsonNode.get("match").get("name").asText() + "**", "https://osu.ppy.sh/community/matches/" + Main.tourney.getMpID());
                        eb.setDescription((team1_total > team2_total ? ":medal:" : "") + ":red_circle: ``" + Main.tourney.getTeamName(1) + "``  | スコア差 : **" + String.format("%,d", Math.abs(team1_total - team2_total)) + "** | **" + Main.tourney.getTeam_point(1) + " - " + Main.tourney.getTeam_point(2) + "** | ``" + Main.tourney.getTeamName(2) + "`` :blue_circle:" + (team1_total < team2_total ? " :medal:" : ""));
                        eb.addField("**Bans**",  ":red_circle: ``" + Main.tourney.getTeamName(1) +"``: **" + Main.tourney.getBanList(1).toString().toUpperCase() + "**\n:blue_circle: ``" + Main.tourney.getTeamName(2) +"``: **" + Main.tourney.getBanList(2).toString().toUpperCase() + "**", false);
                        eb.addField("**First Pick**", Main.tourney.getFirstPickTeam() == 1 ? ":red_circle: ``" + Main.tourney.getTeamName(1) + "``" : ":blue_circle: ``" + Main.tourney.getTeamName(2) + "``", false);

                        int team_tmp = Main.tourney.getFirstPickTeam();
                        for(int i = 0; i < Main.tourney.getWinTeam().size(); i++) {
                            if (Main.tourney.getPickedMaps().get(i).toLowerCase().contains("tb")) {
                                sb.append(":fire: ``").append(Main.tourney.getTeamName(team_tmp == 1 ? 1 : 2)).append("`` が **").append(Main.tourney.getPickedMaps().get(i)).append("** をピックし, ").append(Main.tourney.getWinTeam().get(i) == 1 ? ":red_circle: ``" : ":blue_circle: ``").append(Main.tourney.getTeamName(Main.tourney.getWinTeam().get(i))) .append("`` が勝利しました!\n");
                            } else {
                                sb.append(team_tmp == 1 ? ":red_circle: " : ":blue_circle: ``").append(Main.tourney.getTeamName(team_tmp == 1 ? 1 : 2)).append("`` が **").append(Main.tourney.getPickedMaps().get(i)).append("** をピックし, ").append(Main.tourney.getWinTeam().get(i) == 1 ? ":red_circle: ``" : ":blue_circle: ``").append(Main.tourney.getTeamName(Main.tourney.getWinTeam().get(i))) .append("`` が勝利しました!\n");
                            }
                            team_tmp = team_tmp == 1 ? 2 : 1;
                        }

                        eb.addField("**試合結果**", sb.toString(), false);
                        eb.setColor(team1_total > team2_total ? Color.RED : Color.BLUE);
                        eb.setTimestamp(new Date().toInstant());

                        e.getJDA().getTextChannelById(Main.bot.getTourneyChannelId()).sendMessageEmbeds(
                                eb.build()
                        ).queue();
                    }
                    return;
                }

                if (jsonNode.get("games").isEmpty()) {
                    System.out.println("empty slot");
                    return;
                }

                if (size[0] == jsonNode.get("games").size()) {
                    System.out.println("size error");
                    return;
                }
                //

                JsonNode lastGameNode = jsonNode.get("games").get(jsonNode.get("games").size() - 1);

                if (lastGameNode.get("scores").isEmpty()) {
                    System.out.println("score is empty");
                    return;
                }

                if (!lastGameNode.get("team_type").asText().equals("2")) {
                    System.out.println("scoring type error");
                    return;
                }

                if(checkBeatmap(Integer.parseInt(lastGameNode.get("beatmap_id").asText()))) {
                    System.out.println("beatmap error");
                    return;
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
                size[0] = jsonNode.get("games").size();
                url = beatmap.getBeatmap_url();
                v2 = lastGameNode.get("scoring_type").asText().equals("3");
                slot = beatmap.slot;

                eb.setTitle("**" + beatmap_name + "**", url);
                eb.setColor(Main.tourney.getCurrentlyPickTeam() == 1 ? Color.RED : Color.BLUE);
                // チームのスコアを取得

                Iterator<JsonNode> slotIterator = lastGameNode.get("scores").elements();

                while (slotIterator.hasNext()) {
                    JsonNode scoreNode = slotIterator.next();

                    Score score = new Score(scoreNode, v2, mode);
                    if (score.getTeam() == 1) {
                        team1_scores.add(score);
                    } else {
                        team2_scores.add(score);
                    }
                }

                scores.put(1, team1_scores);
                scores.put(2, team2_scores);

                team1_total = team1_scores.stream().mapToInt(Score::getScore).sum();
                team2_total = team2_scores.stream().mapToInt(Score::getScore).sum();

                Main.tourney.setTeam_point(team1_total > team2_total ? 1 : 2);

                e.getJDA().getPresence().setActivity(Activity.watching( Main.tourney.getTeamName(1) + " " + Main.tourney.getTeam_point(1) + " - " + Main.tourney.getTeam_point(2) + " " + Main.tourney.getTeamName(2)));

                eb.addField("**:red_circle: " + Main.tourney.getTeamName(1) +  " " + String.format("%,d", team1_total) + "**", getScoreFormat(slot, team1_scores), false);
                eb.addField("**:blue_circle: " + Main.tourney.getTeamName(2) + " " + String.format("%,d", team2_total) + "**", getScoreFormat(slot, team2_scores), false);

                eb.addField("**結果**",   "**" + String.format("%,d", Math.abs(team1_total - team2_total)) + "** 点差で, チーム ``" + (team1_total > team2_total ? Main.tourney.getTeamName(1) : Main.tourney.getTeamName(2)) + "`` の勝利\n\n``" +
                        Main.tourney.getTeamName(1) + "`` " + "** " + String.format("%,d", Main.tourney.getTeam_point(1)) + "** - **" + String.format("%,d", Main.tourney.getTeam_point(2)) + "** ``" + Main.tourney.getTeamName(2) + "``", false);

                eb.setFooter("Picked by " + "チーム" +  (Main.tourney.getCurrentlyPickTeam() == 1 ? Main.tourney.getTeamName(1) : Main.tourney.getTeamName(2)), null);

                eb.setTimestamp(new Date().toInstant());

                e.getJDA().getTextChannelById(Main.bot.getTourneyChannelId()).sendMessageEmbeds(
                        eb.build()
                ).queue();

                Main.tourney.setScores(1, team1_total);
                Main.tourney.setScores(2, team2_total);
                Main.tourney.setPickedMaps(slot);
                Main.tourney.setWinTeam(team1_total > team2_total ? 1 : 2);
                isOnceSend[0] = true;

                System.out.println("送信しました");
            }
        };
        timer.schedule(timerTask, 0, 4000);
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
            Main.tourney.setMpID(Integer.parseInt(matcher.group(1)));
        } else {
            e.reply("試合のリンク形式が正しくありません。").setEphemeral(true).queue();
            return;
        }

        e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds()).setComponents().queue();

        e.reply("全ての処理が完了しました。\n" +
                "マッチが完了するまで指定されたチャンネルにマッチの結果が送信されます。").queue();

        // ここから更新処理

        startSchedule(e);
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

                System.out.println(jsonNode);

                beatmap_name = Match.getMapName(Integer.parseInt(jsonNode.get("beatmap_id").asText()), Integer.parseInt(jsonNode.get("play_mode").asText()));
                beatmap_id = Integer.parseInt(jsonNode.get("beatmap_id").asText());
                mode = Integer.parseInt(jsonNode.get("play_mode").asText());
                slot = loadSlot(beatmap_id);
                beatmap_url = getURL(beatmap_id, mode);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public String getURL(int beatmap, int mode) {
            String API = "https://osu.ppy.sh/api/get_beatmaps?k=" + Main.tourney.getApi() + "&b=" + beatmap;
            JsonNode jsonNode = Match.getNodeData(API).get(0);
            String[] mode_str = {"osu", "taiko", "fruits", "mania"};

            return "https://osu.ppy.sh/beatmaps/" + Integer.parseInt(jsonNode.get("beatmapset_id").asText()) + "#" + mode_str[mode] + "/" + jsonNode.get("beatmap_id").asText();
        }

        private String loadSlot(int beatmap_id) {
            return Main.tourney.getMaps().get(beatmap_id);
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
}

    class Score {
        int team;
        int score;
        double acc;
        int enabled_mods;
        int userid;
        String userName;

        private double getAcc(JsonNode jsonNode, boolean v2, int mode) {
            int count300 = Integer.parseInt(jsonNode.get("count300").asText());
            int count100 = Integer.parseInt(jsonNode.get("count100").asText());
            int count50 = Integer.parseInt(jsonNode.get("count50").asText());
            int countmiss = Integer.parseInt(jsonNode.get("countmiss").asText());
            int countgeki = Integer.parseInt(jsonNode.get("countgeki").asText());
            int countkatu = Integer.parseInt(jsonNode.get("countkatu").asText());

            int total;

            if (mode == 0) {
                total = count300 + count100 + count50 + countmiss;

                if (total == 0) {
                    return 0;
                }

                return 100 * (count300 * 300 + count100 * 100 + count50 * 50) / (double) (total * 300);
            } else if (mode == 1) {
                total = count300 + count100 + countmiss;

                if (total == 0) {
                    return 0;
                }

                return 100 * (count100 * 0.5 + count300) / (double) total;
            } else if (mode == 2) {
                total = count300 + count100 + count50 + countkatu + countmiss;

                if (total == 0) {
                    return 0;
                }

                return 100 * (count300 + count100 + count50) / (double) (total);
            } else if (mode == 3) {
                total = count300 + count100 + count50 + countkatu + countmiss + countgeki;

                if (total == 0) {
                    return 0;
                }

                if (v2) {
                    return 100.0 * (count50 * 50 + count100 * 100 + countkatu * 200 + count300 * 300 + countgeki * 305) / (double) (total * 305);
                }

                return 100.0 * (count50 * 50 + count100 * 100 + countkatu * 200 + (count300 + countgeki) * 300) / (double) (total * 300);
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

