package com.mydailies;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Named;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import net.runelite.api.Client;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.account.SessionManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.SessionClose;
import net.runelite.client.events.SessionOpen;
import net.runelite.client.plugins.info.JRichTextPane;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;

public class MyDailiesPanel extends PluginPanel {
    private static final String RUNELITE_LOGIN = "https://runelite_login/";


    private final JLabel loggedLabel = new JLabel();
    private final JRichTextPane emailLabel = new JRichTextPane();
    private JPanel actionsContainer;

    @Inject
    private Client client;

    @Inject
    private EventBus eventBus;

    @Inject
    private SessionManager sessionManager;

    @Inject
    private ScheduledExecutorService executor;

    @Inject
    @Named("runelite.version")
    private String runeliteVersion;

    @Inject
    @Named("runelite.github.link")
    private String githubLink;

    @Inject
    @Named("runelite.discord.invite")
    private String discordInvite;

    @Inject
    @Named("runelite.patreon.link")
    private String patreonLink;

    @Inject
    @Named("runelite.wiki.link")
    private String wikiLink;



    void init()
    {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel versionPanel = new JPanel();
        versionPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        versionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        versionPanel.setLayout(new GridLayout(0, 1));

        final Font smallFont = FontManager.getRunescapeSmallFont();

        JLabel version = new JLabel(htmlLabel("RuneLite version: ", runeliteVersion));
        version.setFont(smallFont);

        JLabel revision = new JLabel();
        revision.setFont(smallFont);

        String engineVer = engineVer = String.format("Rev %d", client.getRevision());

        revision.setText(htmlLabel("Oldschool revision: ", engineVer));

        JLabel launcher = new JLabel(htmlLabel("Launcher version: ", MoreObjects
                .firstNonNull(RuneLiteProperties.getLauncherVersion(), "Unknown")));
        launcher.setFont(smallFont);

        loggedLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        loggedLabel.setFont(smallFont);

        emailLabel.setForeground(Color.WHITE);
        emailLabel.setFont(smallFont);
        emailLabel.enableAutoLinkHandler(false);
        emailLabel.addHyperlinkListener(e ->
        {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType()) && e.getURL() != null)
            {
                if (e.getURL().toString().equals(RUNELITE_LOGIN))
                {
                    executor.execute(sessionManager::login);
                }
            }
        });

        versionPanel.add(version);
        versionPanel.add(revision);
        versionPanel.add(launcher);
        versionPanel.add(Box.createGlue());
        versionPanel.add(loggedLabel);
        versionPanel.add(emailLabel);

        actionsContainer = new JPanel();
        actionsContainer.setBorder(new EmptyBorder(10, 0, 0, 0));
        actionsContainer.setLayout(new GridLayout(0, 1, 0, 10));

        add(versionPanel, BorderLayout.NORTH);
        add(actionsContainer, BorderLayout.CENTER);

        updateLoggedIn();
        eventBus.register(this);
    }

    void deinit()
    {
        eventBus.unregister(this);
    }

    /**
     * Builds a link panel with a given icon, text and url to redirect to.
     */
    private static JPanel buildLinkPanel(ImageIcon icon, String topText, String bottomText, String url)
    {
        return buildLinkPanel(icon, topText, bottomText, () -> LinkBrowser.browse(url));
    }

    /**
     * Builds a link panel with a given icon, text and callable to call.
     */
    private static JPanel buildLinkPanel(ImageIcon icon, String topText, String bottomText, Runnable callback)
    {
        JPanel container = new JPanel();
        container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        container.setLayout(new BorderLayout());
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        final Color hoverColor = ColorScheme.DARKER_GRAY_HOVER_COLOR;
        final Color pressedColor = ColorScheme.DARKER_GRAY_COLOR.brighter();

        JLabel iconLabel = new JLabel(icon);
        container.add(iconLabel, BorderLayout.WEST);

        JPanel textContainer = new JPanel();
        textContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        textContainer.setLayout(new GridLayout(2, 1));
        textContainer.setBorder(new EmptyBorder(5, 10, 5, 10));

        container.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
                container.setBackground(pressedColor);
                textContainer.setBackground(pressedColor);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                callback.run();
                container.setBackground(hoverColor);
                textContainer.setBackground(hoverColor);
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                container.setBackground(hoverColor);
                textContainer.setBackground(hoverColor);
                container.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                textContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                container.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        JLabel topLine = new JLabel(topText);
        topLine.setForeground(Color.WHITE);
        topLine.setFont(FontManager.getRunescapeSmallFont());

        JLabel bottomLine = new JLabel(bottomText);
        bottomLine.setForeground(Color.WHITE);
        bottomLine.setFont(FontManager.getRunescapeSmallFont());

        textContainer.add(topLine);
        textContainer.add(bottomLine);

        container.add(textContainer, BorderLayout.CENTER);

        return container;
    }

    private void updateLoggedIn()
    {
        final String name = sessionManager.getAccountSession() != null
                ? sessionManager.getAccountSession().getUsername()
                : null;

        if (name != null)
        {
            emailLabel.setContentType("text/plain");
            emailLabel.setText(name);
            loggedLabel.setText("Signed in as");
        }
        else
        {
            emailLabel.setContentType("text/html");
            emailLabel.setText("<a href=\"" + RUNELITE_LOGIN + "\">Sign in</a> to sync settings to the cloud.");
            loggedLabel.setText("Not signed in");
        }
    }

    private static String htmlLabel(String key, String value)
    {
        return "<html><body style = 'color:#a5a5a5'>" + key + "<span style = 'color:white'>" + value + "</span></body></html>";
    }

    @Subscribe
    public void onSessionOpen(SessionOpen sessionOpen)
    {
        updateLoggedIn();
    }

    @Subscribe
    public void onSessionClose(SessionClose e)
    {
        updateLoggedIn();
    }
}
