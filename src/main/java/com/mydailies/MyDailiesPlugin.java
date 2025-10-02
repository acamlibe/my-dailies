package com.mydailies;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "My Dailies"
)
public class MyDailiesPlugin extends Plugin
{
	@Inject
	private Client client;

    @Inject
    private ClientToolbar clientToolbar;

	@Inject
	private MyDailiesConfig config;

    private MyDailiesPanel panel;
    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception
    {
        panel = injector.getInstance(MyDailiesPanel.class);
        panel.init();

        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/task.png");

        navButton = NavigationButton.builder()
                .tooltip("My Dailies")
                .icon(icon)
                .priority(10)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);


    }

    @Override
    protected void shutDown()
    {
        panel.deinit();
        clientToolbar.removeNavigation(navButton);
        panel = null;
        navButton = null;
    }


    @Subscribe
    public void onWidgetLoaded(final WidgetLoaded widgetLoaded) {
        int id = widgetLoaded.getGroupId();

        if (id == InterfaceID.BARROWS_REWARD) {
            addMessage("Barrows SUCCESS: " + widgetLoaded.getGroupId());
        }
        else if (id == InterfaceID.PMOON_REWARD) {
            addMessage("Moons SUCCESS: " + widgetLoaded.getGroupId());
        }
    }

	@Provides
    MyDailiesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MyDailiesConfig.class);
	}

    private void addMessage(String msg) {
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, null);

    }
}
