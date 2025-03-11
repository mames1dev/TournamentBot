package mames1.net.mamesosu.Object;

import io.github.cdimascio.dotenv.Dotenv;
import mames1.net.mamesosu.Event.Ban;
import mames1.net.mamesosu.Event.Match;
import mames1.net.mamesosu.Event.Pick;
import mames1.net.mamesosu.Event.Start;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot {

    /*
    * Botを起動したり、Botの情報を管理するクラスです。
    * 今回はJDA (Java Discord API) を使用してBotを作成します。
    * pom.xmlに依存関係を追加することで使用可能です！
    * */

    String token;
    long manageChannelId;
    long tourneyChannelId;

    JDA jda; // Botの情報を格納する変数

    public Bot() {
        // .envというファイルから値を読み込む
        Dotenv dotenv = Dotenv.configure().load();
        this.manageChannelId = Long.parseLong(dotenv.get("MANAGE_CH_ID"));
        this.tourneyChannelId = Long.parseLong(dotenv.get("TOURNEY_CH_ID"));
        this.token = dotenv.get("TOKEN");
    }

    public JDA getJda() {
        return jda;
    }

    public long getManageChannelId() {
        return manageChannelId;
    }

    public long getTourneyChannelId() {
        return tourneyChannelId;
    }

    // 読み込んだ情報を元にBotを起動する
    public void startBot() {

        // Java Discord APIを使用してBotを起動する
        // 細かい使い方は https://github.com/discord-jda/JDA にあります!
        jda = JDABuilder.createDefault(this.token)
            .setRawEventsEnabled(true)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS
                ).enableCache(
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.ROLE_TAGS,
                        CacheFlag.EMOJI
                )
                .disableCache(
                        CacheFlag.VOICE_STATE,
                        CacheFlag.STICKER,
                        CacheFlag.SCHEDULED_EVENTS
                ).setActivity(
                        Activity.playing("試合開始を待機しています!")) // ここでステータスを変更できます!
                .addEventListeners(
                        new Start(), //トーナメdントの開始を検知するイベント
                        new Ban(), //Banを行うイベント
                        new Pick(),
                        new Match()
                )
                .build();
    }
}
