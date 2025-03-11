package mames1.net.mamesosu.Event;

import mames1.net.mamesosu.Main;
import mames1.net.mamesosu.Object.Tourney;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Start extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if(e.getChannel().getIdLong() != Main.bot.getManageChannelId()) {
            return;
        }

        // Bot以外のメッセージは無視する
        if(e.getAuthor().isBot()) {
            return;
        }

        if(!e.getMessage().getContentRaw().equals("!start")) {
            return;
        }

        Main.tourney = new Tourney();

        // トーナメントを開始する
        Main.tourney.startTourney();
    }
}
