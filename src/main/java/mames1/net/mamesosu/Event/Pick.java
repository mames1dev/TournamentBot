package mames1.net.mamesosu.Event;

import mames1.net.mamesosu.Embed;
import mames1.net.mamesosu.Main;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Pick extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {

        if(e.getComponentId().contains("btn_first_pick_")) {

            e.getMessage().editMessageEmbeds(e.getMessage().getEmbeds()).setComponents().queue();
            Main.tourney.setFirstPickTeam(Integer.parseInt(e.getComponentId().replace("btn_first_pick_", "")));
            e.replyEmbeds(Embed.getPickEndEmbed().build())
                    .addActionRow(
                            Button.success("btn_mp_link", "試合のリンクを入力")
                    ).queue();
        }
    }
}
