package com.wyliodrin.mobileapp.api;

import android.util.Base64;

import com.wyliodrin.mobileapp.DashboardActivity;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Element;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Andreea Stoican on 08.06.2015.
 */
public class ServerConnection {
    private static ServerConnection instance = null;

    private static final String SERVER_HOST = "wyliodrin.org";
    private static final int SERVER_PORT = 5222;

    private AbstractXMPPConnection connection;
    private DashboardActivity dashboard;
    private String to;
    private String user;

    private List<String> devices = new ArrayList<String>();

    public void setTo(String to) {
        this.to = to;
    }

    private class WyliodrinDevicesExtension implements PacketExtension {
        private List<String> devices;

        WyliodrinDevicesExtension(List<String> devices) {
            this.devices = devices;
        }

        @Override
        public String getNamespace() {
            return "wyliodrin";
        }

        @Override
        public String getElementName() {
            return "devices";
        }

        @Override
        public CharSequence toXML() {
            return "<devices></devidec>";
        }

        public List<String> getDevices() {
            return this.devices;
        }
    }

    private class WyliodrinExtension implements PacketExtension {
        private String port;
        private String message;

        WyliodrinExtension(String port, String message) {
            this.port = port;
            this.message = message;
        }

        @Override
        public String getNamespace() {
            return "wyliodrin";
        }

        @Override
        public String getElementName() {
            return "communication";
        }

        public String getExtensionMessage() {
            return message;
        }

        public String getPort() {
            return port;
        }

        @Override
        public CharSequence toXML() {
            String xml = "<communication xmlns=\"wyliodrin\" port=\"" + port + "\">";
            byte[] data = new byte[0];
            try {
                data = message.getBytes("UTF-8");
                xml += Base64.encodeToString(data, Base64.NO_WRAP);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            xml += "</communication>";
            return xml;
        }

    }

    private class WyliodrinExtensionProvider extends PacketExtensionProvider {

        @Override
        public Element parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            String label = parser.getAttributeValue(null, "port");
            byte[] messageBytes = Base64.decode(parser.nextText(), Base64.DEFAULT);
            String message = new String(messageBytes, "UTF-8");
            return new WyliodrinExtension(label, message);
        }

    }

    private class WyliodrinDevicesProvider extends PacketExtensionProvider {

        @Override
        public Element parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            List<String> devices = new ArrayList<String>();
            int type = parser.getEventType();

            while (type != XmlPullParser.END_DOCUMENT) {
                if (type == XmlPullParser.START_TAG && parser.getName().equals("device")) {
                    String device = parser.getAttributeValue(0);
                    if (device != null && !device.equals("") && !device.equals(user)) {
                        devices.add(device);
                    }
                } else if (type == XmlPullParser.END_TAG){
                    String name = parser.getName();
                    if (name != null && name.equals("devices")) {
                        break;
                    }
                }

                parser.next();
                type = parser.getEventType();

            }

            if (devices != null)
                ServerConnection.this.devices = devices;

            return new WyliodrinDevicesExtension(devices);
        }

    }

    public enum LoginResult {
        Success,
        Failed
        }

    private ServerConnection() {
        this.dashboard = null;
    }

    public static ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }

        return instance;
    }

    public void disconnect() {
        Presence presence = new Presence(Presence.Type.unavailable, "logout", 50, Presence.Mode.away);
        try {
            connection.disconnect(presence);
        } catch (Exception exception) {
            exception.printStackTrace();
            connection.disconnect();
        }
    }

    public LoginResult connect(String username, final String password, final String owner) {
        user = username;

        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(username, password)
                .setServiceName(SERVER_HOST)
                .setHost(SERVER_HOST)
                .setPort(SERVER_PORT);

        try {
            TLSUtils.acceptAllCertificates(configBuilder);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        TLSUtils.disableHostnameVerificationForTlsCertificicates(configBuilder);

        ProviderManager.addExtensionProvider("communication", "wyliodrin", new WyliodrinExtensionProvider());
        ProviderManager.addExtensionProvider("devices", "wyliodrin", new WyliodrinDevicesProvider());

        configBuilder.allowEmptyOrNullUsernames();
        final XMPPTCPConnectionConfiguration config = configBuilder.build();

        connection = new XMPPTCPConnection(config);
        try {
            connection.connect();
            connection.login();
        } catch (Exception e) {
            e.printStackTrace();
            return LoginResult.Failed;
        }

        Roster roster = Roster.getInstanceFor(connection);
        if (!roster.isLoaded())
                      try {
                           roster.reloadAndWait();
                      } catch (SmackException.NotLoggedInException e) {
                            e.printStackTrace();
                      } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        Collection<RosterEntry> entries = roster.getEntries();

        connection.addAsyncPacketListener(new PacketListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {

                if (packet instanceof Presence) {
                    Presence prs = (Presence) packet;

                    if (prs.getType() == Presence.Type.subscribe) {
                        Presence presence = new Presence(Presence.Type.subscribed);
                        presence.setTo(owner);

                        try {
                            connection.sendPacket(presence);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (packet.hasExtension("wyliodrin")) {

                    if (packet.getExtension("wyliodrin") instanceof WyliodrinDevicesExtension) {
                        WyliodrinDevicesExtension devicesPacket = (WyliodrinDevicesExtension) packet.getExtension("wyliodrin");
                        devices = devicesPacket.getDevices();
                    } else {
                        if (dashboard != null) {
                            WyliodrinExtension extension = (WyliodrinExtension) packet.getExtension("wyliodrin");
                            dashboard.messageReceived(extension.getPort(), extension.getExtensionMessage());
                            if (!devices.isEmpty())
                                to = devices.get(0);
                        }
                    }
                }
            }
        }, new PacketFilter() {
            @Override
            public boolean accept(Stanza packet) {
                return packet.hasExtension("wyliodrin") || packet instanceof Presence;
            }
        });

        Presence presence = new Presence(Presence.Type.available, "waiting messages", 50, Presence.Mode.available);

        Presence presenceSubscribe = new Presence(Presence.Type.subscribe);
        presenceSubscribe.setTo(owner);
        try {
            connection.sendPacket(presence);
            connection.sendPacket(presenceSubscribe);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        try {
            to = getBoardsList().get(0);
        } catch (Exception e) {
            to = "";
        }

        return LoginResult.Success;
    }

    public List<String> getBoardsList() {
        return devices;
    }

    public void sendMessage(String label, String message) {
        Message m = new Message(to);
        m.addExtension(new WyliodrinExtension(label, message));

        try {
            connection.sendPacket(m);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void sendStringMessage(String label, String message) {
        sendMessage(label, "\"" + message + "\"");
    }

    public void setDashboard(DashboardActivity dashboard) {
        this.dashboard = dashboard;
    }

}
