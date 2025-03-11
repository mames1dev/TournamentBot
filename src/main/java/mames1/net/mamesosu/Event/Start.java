package mames1.net.mamesosu.Event;

import mames1.net.mamesosu.Embed;
import mames1.net.mamesosu.Main;
import mames1.net.mamesosu.Object.Tourney;
import mames1.net.mamesosu.Utils.Modal;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

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

                e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds()).setComponents().queue();

                e.getMessage().replyEmbeds(Embed.getSetTeamNameEmbed().build())
                                .addActionRow(
                                        Button.primary("btn_set_team", "チーム名を設定")
                                ).queue();

                /*
                e.getMessage().editMessageEmbeds(Embed.getSelectBanEmbed().build())
                        .setActionRow(
                                Button.danger("btn_f_ban_team1", "Red"),
                                Button.primary("btn_f_ban_team2", "Blue")
                        ).queue();
                 */

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent e) {
        if(e.getModalId().equals("modal_set_team")) {
            String team1 = e.getValue("team1").getAsString();
            String team2 = e.getValue("team2").getAsString();
            Main.tourney.setTeamName(1, team1);
            Main.tourney.setTeamName(2, team2);

            e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds()).setComponents().queue();

            e.replyEmbeds(
                    Embed.getSelectBanEmbed().build()
            ).addActionRow(
                    Button.danger("btn_f_ban_team1", Main.tourney.getTeamName(1)),
                    Button.primary("btn_f_ban_team2", Main.tourney.getTeamName(2))
            ).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        if(e.getComponentId().equals("btn_set_team")) {
            TextInput team1 = Modal.createTextInput("team1", "Team1の名前", "チーム1", true, TextInputStyle.SHORT);
            TextInput team2 = Modal.createTextInput("team2", "Team2の名前", "チーム2", true, TextInputStyle.SHORT);
            e.replyModal(net.dv8tion.jda.api.interactions.modals.Modal.create(
                    "modal_set_team", "チーム名を設定してください。"
            ).addActionRows(
                    ActionRow.of(team1),
                    ActionRow.of(team2)
            ).build()).queue();
        }
    }
}
