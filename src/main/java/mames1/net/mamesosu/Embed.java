package mames1.net.mamesosu;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.Date;

public abstract class Embed {

    public static EmbedBuilder getSelectLoadFile() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("**トーナメントのセットアップ**");
        eb.setDescription("読み込みたいマッププールを選択してください。");
        eb.setTimestamp(new Date().toInstant());

        return eb;
    }

    public static EmbedBuilder getSelectBanEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("**トーナメントのセットアップ**");
        eb.setDescription("先にBanを行うチームを選択してください。\n" +
                "間違えた場合は再度!startを実行してください。");
        eb.setTimestamp(new Date().toInstant());

        return eb;
    }

    public static EmbedBuilder getBanCountEmbed(int firstBanTeam) {
        EmbedBuilder eb = new EmbedBuilder();
        String[] team = {"Red", "Blue"};
        Color[] teamColor = {Color.RED, Color.BLUE};

        eb.setTitle("**トーナメントのセットアップ**");
        eb.setDescription("先にBanを行うチームは**" + team[firstBanTeam-1] + "**です。\n何個のマップをBanしますか？");
        eb.setColor(teamColor[firstBanTeam-1]);
        eb.setTimestamp(new Date().toInstant());

        return eb;
    }

    public static EmbedBuilder getBanMapEmbed(int team, int count) {
        EmbedBuilder eb = new EmbedBuilder();

        String[] teamName = {"Red", "Blue"};
        StringBuilder msg = new StringBuilder("Team1: ");
        Color[] teamColor = {Color.RED, Color.BLUE};

        for(String s : Main.tourney.getBanList(1)) {
            msg.append(s.replace("ban_", "**").toUpperCase()).append("** ");
        }

        msg.append("\nTeam2: ");

        for (String s : Main.tourney.getBanList(2)) {
            msg.append(s.replace("ban_", "**").toUpperCase()).append("** ");
        }

        eb.setTitle("**トーナメントのセットアップ**");
        eb.setDescription(teamName[team-1] + "チームの" + "Banするマップを選択してください。\n" +
                "現在BANされたマップ:\n" + msg.toString());
        eb.setColor(teamColor[team-1]);
        eb.setTimestamp(new Date().toInstant());

        return eb;
    }

    public static EmbedBuilder getBanEndEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder msg = new StringBuilder("Team1: ");

        for(String s : Main.tourney.getBanList(1)) {
            msg.append(s.replace("ban_", "**").toUpperCase()).append("** ");
        }

        msg.append("\nTeam2: ");

        for (String s : Main.tourney.getBanList(2)) {
            msg.append(s.replace("ban_", "**").toUpperCase()).append("** ");
        }


        eb.setTitle("**トーナメントのセットアップ**");
        eb.setDescription("Banが終了しました。Banされたマップは以下の通りです。\n" +
                msg.toString() + "\n" +
                "First Pickはどのチームにしますか？");
        eb.setTimestamp(new Date().toInstant());
        return eb;
    }

    public static EmbedBuilder getPickEndEmbed() {

        EmbedBuilder eb = new EmbedBuilder();
        String color = Main.tourney.getFirstPickTeam() == 1 ? "Red" : "Blue";
        Color teamColor = Main.tourney.getFirstPickTeam() == 1 ? Color.RED : Color.BLUE;

        eb.setTitle("**トーナメントのセットアップ**");
        eb.setDescription("First Pickを、**" + color + "チーム**に設定しました。\n" +
                "マッチのリンクを入力してください。");
        eb.setTimestamp(new Date().toInstant());
        eb.setColor(teamColor);

        return eb;
    }
}
