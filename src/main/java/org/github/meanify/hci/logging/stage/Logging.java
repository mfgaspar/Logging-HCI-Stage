package org.github.meanify.hci.logging.stage;

import com.hds.ensemble.sdk.config.Config;
import com.hds.ensemble.sdk.config.ConfigProperty;
import com.hds.ensemble.sdk.config.ConfigPropertyGroup;
import com.hds.ensemble.sdk.config.PropertyGroupType;
import com.hds.ensemble.sdk.config.PropertyType;
import com.hds.ensemble.sdk.exception.ConfigurationException;
import com.hds.ensemble.sdk.exception.PluginOperationFailedException;
import com.hds.ensemble.sdk.model.Document;
import com.hds.ensemble.sdk.model.DocumentBuilder;
import com.hds.ensemble.sdk.model.DocumentFieldValue;
import com.hds.ensemble.sdk.model.StreamingDocumentIterator;
// import com.hds.ensemble.sdk.model.StringDocumentFieldValue;
import com.hds.ensemble.sdk.plugin.CertificateProtocol;
import com.hds.ensemble.sdk.plugin.PluginCallback;
import com.hds.ensemble.sdk.plugin.PluginConfig;
import com.hds.ensemble.sdk.plugin.PluginSession;
import com.hds.ensemble.sdk.stage.StagePlugin;
import com.hds.ensemble.sdk.stage.StagePluginCategory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import org.fluentd.logger.FluentLogger;

public class Logging implements StagePlugin {

    private FluentLogger LOG; // = FluentLogger.getLogger("test");
    private static final String PLUGIN_NAME = "org.github.meanify.hci.logging.stage";
    private static final String NAME = "Logging stage";
    private static final String DESCRIPTION = "Logging plugin stage ...";

    private static final String LONG_DESCRIPTION = "Logging Stage Plugin"
            + "\n Long description of logging stage plugin";

    private static final String SUBCATEGORY = "FluentD";

    private final PluginConfig config;
    private final PluginCallback callback;

    public static final ConfigProperty.Builder LOGGING_HOST = new ConfigProperty.Builder()
            .setName("org.github.meanify.hci.logging.stage.logging.host")
            .setValue("localhost")
            .setType(PropertyType.TEXT)
            .setRequired(true)
            .setUserVisibleName("Hostname")
            .setUserVisibleDescription("Hostname to were send the logs.");

    public static final ConfigProperty.Builder LOGGING_PORT = new ConfigProperty.Builder()
            .setName("org.github.meanify.hci.logging.stage.logging.port")
            .setValue("24224")
            .setType(PropertyType.TEXT)
            .setRequired(true)
            .setUserVisibleName("Port number")
            .setUserVisibleDescription("Port number of the host to were send the logs.");

    public static final ConfigProperty.Builder LOGGING_TAG_PREFIX = new ConfigProperty.Builder()
            .setName("org.github.meanify.hci.logging.stage.logging.tag")
            .setValue("project.workflow")
            .setType(PropertyType.TEXT)
            .setRequired(true)
            .setUserVisibleName("Tag Prefix")
            .setUserVisibleDescription("Tag prefix for logging.");


    private static List<ConfigProperty.Builder> groupProperties = new ArrayList<>();

    static {
        groupProperties.add(LOGGING_HOST);
        groupProperties.add(LOGGING_PORT);
        groupProperties.add(LOGGING_TAG_PREFIX);
    }
    public static final ConfigPropertyGroup.Builder LOGGING_CONFIG = new ConfigPropertyGroup.Builder(
            "Group One", null)
            .setType(PropertyGroupType.DEFAULT)
            .setConfigProperties(groupProperties);

    public static final PluginConfig DEFAULT_CONFIG = PluginConfig.builder()
            .addGroup(LOGGING_CONFIG)
            .build();

    public Logging() {
        config = null;
        callback = null;
    }

    private Logging(PluginConfig pluginConfig, PluginCallback pluginCallback) throws ConfigurationException {
        this.config = pluginConfig;
        this.callback = pluginCallback;
        validateConfig(config);
        String logging_host = this.config.getProperty("org.github.meanify.hci.logging.stage.logging.host").getValue();
        int logging_port = Integer.parseInt(this.config.getProperty("org.github.meanify.hci.logging.stage.logging.port").getValue());
        String logging_tag = this.config.getProperty("org.github.meanify.hci.logging.stage.logging.tag").getValue();
        LOG = FluentLogger.getLogger(logging_tag, logging_host, logging_port);
    }

    @Override
    public void validateConfig(PluginConfig config) throws ConfigurationException {
        Config.validateConfig(getDefaultConfig(), config);
        if (config.getPropertyValue(LOGGING_HOST.getName()) == null) {
            throw new ConfigurationException("Missing the hostname for logging.");
        }
        if (config.getPropertyValue(LOGGING_PORT.getName()) == null) {
            throw new ConfigurationException("Missing the port of the hostname for logging.");
        }
        if (config.getPropertyValue(LOGGING_PORT.getName()) == null) {
            throw new ConfigurationException("Missing the tag prefix to be used for logging.");
        }
    }

    @Override
    public void validateConfig(PluginCallback callback, PluginConfig config) throws ConfigurationException {
    }


    @Override
    public StagePlugin build(PluginConfig pluginConfig, PluginCallback pluginCallback) throws ConfigurationException {
        return new Logging(pluginConfig, pluginCallback);
    }

    @Override
    public StagePluginCategory getCategory() {
        return StagePluginCategory.OTHER;
    }


    @Override
    public PluginSession startSession() throws ConfigurationException, PluginOperationFailedException {
        return PluginSession.NOOP_INSTANCE;
    }

    @Override
    public String getDisplayName() {
        return NAME;
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getLongDescription() {
        return LONG_DESCRIPTION;
    }

    @Override
    public PluginConfig getDefaultConfig() {
        return DEFAULT_CONFIG;
    }

    @Override
    public PluginConfig getDefaultConfig(PluginCallback callback) {
        return DEFAULT_CONFIG;
    }

    @Override
    public String getSubCategory() {
        return SUBCATEGORY;
    }

    @Override
    public Iterator<Document> process(PluginSession pluginSession, Document inputDocument) throws ConfigurationException, PluginOperationFailedException {
        final DocumentBuilder docBuilder = callback.documentBuilder().copy(inputDocument);
        try {
            Map<String, Object> logMessage = new HashMap<String, Object>();
            logMessage.put("messageType", "document");
            final Map<String, DocumentFieldValue<?>> metadata = docBuilder.getMetadata();
            for (Map.Entry<String, DocumentFieldValue<?>> entry : metadata.entrySet()) {
                logMessage.put(entry.getKey(), entry.getValue().getFirstRawValue());
            }
            LOG.log("stage", logMessage);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("messageType", "error");
            data.put("message", errorMessage);
            if(e.getCause()!=null){
                throw new PluginOperationFailedException(errorMessage,e.getCause());
            }else{
                throw new PluginOperationFailedException(errorMessage,e);
            }
        }
        return new StreamingDocumentIterator() {
            boolean sentAllDocuments = false;
            @Override
            protected Document getNextDocument() {
                if (!sentAllDocuments) {
                    sentAllDocuments = true;
                    return docBuilder.build();
                }
                return endOfDocuments();
            }
        };
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public Integer getPort() throws ConfigurationException {
        return null;
    }

    @Override
    public CertificateProtocol getCertificateProtocol() throws ConfigurationException {
        return null;
    }
}
