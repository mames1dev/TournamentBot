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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// トーナメントの進捗を管理する
public class Tourney {

    int firstBanTeam;
    int firstPickTeam;
    int banCount;
    int currentlyBanCount = 0;
    int currentlyBanTeam = 0;
    int mp_id;
    int currentlyPickTeam;
    boolean scoreSent = false;
    String api;
    List<String> team1BanList = new ArrayList<>();
    List<String> team2BanList = new ArrayList<>();

    public Tourney () {
        Dotenv dotenv = Dotenv.configure().load();
        this.api = dotenv.get("API");
    }

    public String getApi() {
        return api;
    }

    public void setMpID(int id) {
        this.mp_id = id;
    }

    public int getMpID() {
        return this.mp_id;
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

    public StringSelectMenu.Builder loadMapsBuilder() throws SQLException {
        MySQL mySQL = Main.mySQL;
        Connection connection = mySQL.getConnection();
        PreparedStatement ps;
        ResultSet result;

        StringSelectMenu.Builder builder = StringSelectMenu.create("ban_map:dropdown");

        ps = connection.prepareStatement("select * from beatmaps");
        result = ps.executeQuery();
        while(result.next()) {
            builder.addOption(
                    result.getString("category").toUpperCase(), "ban_" + result.getString("category").toLowerCase()
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

                    jda.getTextChannelById(bot.getManageChannelId()).sendMessageEmbeds(Embed.getSelectBanEmbed().build())
                        .addActionRow(
                        Button.danger("btn_f_ban_team1", "Red"),
                        Button.primary("btn_f_ban_team2", "Blue")
                        ).queue();
                });
    }
}
