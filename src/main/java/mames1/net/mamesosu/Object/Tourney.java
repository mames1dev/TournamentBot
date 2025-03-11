package mames1.net.mamesosu.Object;

import io.github.cdimascio.dotenv.Dotenv;
import mames1.net.mamesosu.Embed;
import mames1.net.mamesosu.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// トーナメントの進捗を管理する
public class Tourney {

    int firstBanTeam;
    int firstPickTeam;
    int banCount;
    int currentlyBanCount = 0;
    int currentlyBanTeam = 0;
    int team1_point = 0;
    int team2_point = 0;
    int mp_id;
    int currentlyPickTeam;
    boolean scoreSent = false;
    String api;
    List<String> team1BanList = new ArrayList<>();
    List<String> team2BanList = new ArrayList<>();
    Map<Integer, List<Integer>> totalScores = new HashMap<>();
    List<String> pickedMaps = new ArrayList<>();
    List<Integer> winTeam = new ArrayList<>();

    Map<Integer, String> maps = new HashMap<>();

    public Tourney () {
        Dotenv dotenv = Dotenv.configure().load();
        this.api = dotenv.get("API");
    }

    public void setWinTeam(int team) {
        winTeam.add(team);
    }

    public List<Integer> getWinTeam() {
        return winTeam;
    }

    public void setPickedMaps(String map) {
        pickedMaps.add(map);
    }

    public List<String> getPickedMaps() {
        return pickedMaps;
    }

    public void setScores(int team, int score) {
        if (totalScores.containsKey(team)) {
            List<Integer> scores = totalScores.get(team);
            scores.add(score);
            totalScores.put(team, scores);
        } else {
            List<Integer> scores = new ArrayList<>();
            scores.add(score);
            totalScores.put(team, scores);
        }
    }

    public Map<Integer, List<Integer>> getTotalScores() {
        return totalScores;
    }

    public String getApi() {
        return api;
    }

    public void setMpID(int id) {
        this.mp_id = id;
    }

    public void setMaps(int beatmap, String slot) {
        this.maps.put(beatmap, slot);
    }

    public int getMpID() {
        return this.mp_id;
    }

        public void setTeam_point(int team) {
        if (team == 1) {
            team1_point++;
        } else {
            team2_point++;
        }
    }

    public int getTeam_point(int team) {
        if (team == 1) {
            return team1_point;
        } else {
            return team2_point;
        }
    }

    public void setScoreSent(boolean scoreSent) {
        this.scoreSent = scoreSent;
    }

    public boolean getScoreSent() {
        return this.scoreSent;
    }

    public void setFirstPickTeam(int team) {
        this.firstPickTeam = team;
        this.currentlyPickTeam = team;
    }

    public void setCurrentlyPickTeam(int currentlyPickTeam) {
        this.currentlyPickTeam = currentlyPickTeam;
    }

    public int getCurrentlyPickTeam() {
        return currentlyPickTeam;
    }

    public int getFirstPickTeam() {
        return this.firstPickTeam;
    }

    public void setFirstBanTeam(int team) {
        this.firstBanTeam = team;
        this.currentlyBanTeam = team;
    }

    public int getFirstBanTeam() {
        return this.firstBanTeam;
    }

    public void addBanList(int team, String map) {
        if (team == 1) {
            System.out.println("team1: " + map);
            team1BanList.add(map);
        } else {
            System.out.println("team2: " + map);
            team2BanList.add(map);
        }
    }

    public Map<Integer, String> getMaps() {
        return maps;
    }

    public void addBanList() {
        currentlyBanCount++;
    }

    public int getCurrentlyBanCount() {
        return currentlyBanCount;
    }

    public void setCurrentlyBanCount(int count) {
        this.currentlyBanCount = count;
    }

    public void setCurrentlyBanTeam(int team) {
        this.currentlyBanTeam = team;
    }

    public int getCurrentlyBanTeam() {
        return this.currentlyBanTeam;
    }

    public List<String> getBanList(int team) {
        if (team == 1) {
            return team1BanList;
        } else {
            return team2BanList;
        }
    }

    public void setBanCount(int count) {
        this.banCount = count;
    }

    public int getBanCount() {
        return this.banCount;
    }

    public StringSelectMenu.Builder loadMapsBuilder() {

        StringSelectMenu.Builder builder = StringSelectMenu.create("ban_map:dropdown");

        for(Map.Entry<Integer, String> entry : maps.entrySet()) {
            builder.addOption(
                    entry.getValue().toUpperCase(), entry.getValue().toUpperCase()
            );
        }

        return builder;
    }

    // トーナメントを開始する
    public void startTourney() {
        int MAX_DESIRED = 10000;
        Bot bot = Main.bot;
        JDA jda = bot.getJda();

        // 開始前に分かりにくいので、全てのメッセージを削除します

        /*
        *
        * .getHistoryFromBeginning(c).complete() を使ってもいいのですが、50までしかメッセージを削除できないので、
        * 少し難しいコードを使用して全てのメッセージを削除します。(次回以降使いたい時はコピペで大丈夫です)
        *
        *  */

        List<Message> messages = new ArrayList<>();

        MessageChannel messageChannel = jda.getTextChannelById(bot.getManageChannelId());

        if (messageChannel == null) {
            System.out.println("正しいIDが入力されていないため、処理を開始できませんでした (エラー01)");
            return;
        }

        messageChannel.getIterableHistory()
                .forEachAsync(message -> {
                    messages.add(message);
                    return messages.size() < MAX_DESIRED;
                }).thenAccept(_ignored -> {
                    for (Message message : messages) {
                        message.delete().queue();
                    }

                    File dir = new File("load");
                    File files[] = dir.listFiles();
                    StringSelectMenu.Builder builder = StringSelectMenu.create("load_map:dropdown");

                    for(int i = 0; i < files.length; i++) {
                        String file_name = files[i].getName();
                        if(file_name.endsWith(".cfg")) {
                            builder.addOption(file_name.replace(".cfg", ""), "load_" + file_name.replace(".cfg", ""));
                        }
                    }

                    jda.getTextChannelById(bot.getManageChannelId()).sendMessageEmbeds(Embed.getSelectLoadFile().build())
                        .addActionRow(
                            builder.build()
                        ).queue();
                });
    }
}
