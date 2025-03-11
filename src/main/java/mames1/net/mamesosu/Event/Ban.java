package mames1.net.mamesosu.Event;

import mames1.net.mamesosu.Embed;
import mames1.net.mamesosu.Main;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.sql.SQLException;

public class Ban extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        if(e.getComponentId().contains("btn_f_ban_team")) {

            StringSelectMenu.Builder builder = StringSelectMenu.create("ban_cnt:dropdown");

            for(int i = 1; i <= 2; i++) {
                builder.addOption(i + "個", "cnt_" + i);
            }

            // 先にBanを行うチームを設定する
            Main.tourney.setFirstBanTeam(Integer.parseInt(e.getComponentId().replace("btn_f_ban_team", "")));

            e.getMessage().editMessageEmbeds(Embed.getSelectBanEmbed().build()).setComponents().queue();
            e.getMessage().replyEmbeds(Embed.getBanCountEmbed(Main.tourney.getFirstBanTeam()).build())
                    .addActionRow(builder.build()).queue();
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent e) {
        if(e.getComponentId().equals("ban_cnt:dropdown")) {

            // for文になってますが実際にはループしません (getvalues()はList<String>を返すため)
            for(String value : e.getValues()) {
                Main.tourney.setBanCount(Integer.parseInt(value.replace("cnt_", "")));

                e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds()).setComponents().queue();

                // Banするマップを選択する (1回目)
                e.replyEmbeds(
                        Embed.getBanMapEmbed(Main.tourney.getFirstBanTeam(), Main.tourney.getBanCount()).build()
                ).addActionRow(
                        Main.tourney.loadMapsBuilder().build()
                ).queue();
            }
        // 2回目以降のBan
        } else if(e.getComponentId().equals("ban_map:dropdown")) {

            Main.tourney.addBanList(Main.tourney.getCurrentlyBanTeam(), e.getValues().get(0));
            Main.tourney.setCurrentlyBanCount(Main.tourney.getCurrentlyBanCount() + 1);

            Main.tourney.setCurrentlyBanTeam(Main.tourney.getCurrentlyBanTeam() == 1 ? 2 : 1);

            if(Main.tourney.getCurrentlyBanCount() == Main.tourney.getBanCount() * 2) {
                e.getMessage().editMessageEmbeds(Embed.getBanMapEmbed(Main.tourney.getCurrentlyBanTeam(), Main.tourney.getBanCount()).build()).setComponents().queue();
                    e.replyEmbeds(
                            Embed.getBanEndEmbed().build()
                    ).addActionRow(
                            Button.danger("btn_first_pick_1", Main.tourney.getTeamName(1)),
                            Button.primary("btn_first_pick_2", Main.tourney.getTeamName(2))
                    ).queue();
                    return;
            }

             e.getMessage().editMessageEmbeds(Embed.getBanMapEmbed(Main.tourney.getCurrentlyBanTeam(), Main.tourney.getBanCount()).build()).queue();

            e.reply("Banしました: **" + e.getValues().get(0).replace("ban_", "") + "**").setEphemeral(true).queue();
        }
    }
}
