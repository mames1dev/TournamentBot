package mames1.net.mamesosu;

import mames1.net.mamesosu.Object.Bot;
import mames1.net.mamesosu.Object.Tourney;


public class Main {

    /*
    * Objectは基本的にクラスを格納するパッケージです。
    * Botとかデータベースの起動系は全部まとめておくと見やすいです。(名前はなんでもいいです)
    * */

    public static Bot bot;
    public static Tourney tourney;

    public static void main(String[] args) {

        // トークン類を読み込む
        bot = new Bot();

        // Botを読み込む
        bot.startBot();
    }
}