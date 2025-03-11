package mames1.net.mamesosu.Event;

import mames1.net.mamesosu.Embed;
import mames1.net.mamesosu.Main;
import mames1.net.mamesosu.Object.Tourney;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent e) {
        if(e.getComponentId().equals("load_map:dropdown")) {
            String file_txt = e.getValues().get(0).replace("load_", "");
            file_txt = file_txt + ".cfg";
            Path path = Path.of("load/" + file_txt);

            try {

                List<String> line = Files.readAllLines(path);

                for(String s : line) {
                    Main.tourney.setMaps(Integer.parseInt(s.split(":")[0]), s.split(":")[1]);
                }

                e.getMessage().editMessageEmbeds(Embed.getSelectBanEmbed().build())
                        .setActionRow(
                                Button.danger("btn_f_ban_team1", "Red"),
                                Button.primary("btn_f_ban_team2", "Blue")
                        ).queue();

                e.reply("マッププールの読み込みが完了しました。").setEphemeral(true).queue();

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }
}
