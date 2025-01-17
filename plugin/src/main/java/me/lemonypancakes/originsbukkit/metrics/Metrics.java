package me.lemonypancakes.originsbukkit.metrics;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * The type Metrics.
 *
 * @author LemonyPancakes
 */
public class Metrics {

    private final Plugin plugin;

    private final MetricsBase metricsBase;

    /**
     * Instantiates a new Metrics.
     *
     * @param plugin    the plugin
     * @param serviceId the service id
     */
    public Metrics(JavaPlugin plugin, int serviceId) {
        this.plugin = plugin;
        // Get the config file
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", true);
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", false);
            config.addDefault("logSentData", false);
            config.addDefault("logResponseStatusText", false);
            // Inform the server owners about bStats
            config
                    .options()
                    .header(
                            "bStats (https://bStats.org) collects some basic information for plugin authors, like how\n"
                                    + "many people use their plugin and their total player count. It's recommended to keep bStats\n"
                                    + "enabled, but if you're not comfortable with this, you can turn this setting off. There is no\n"
                                    + "performance penalty associated with having metrics enabled, and data sent to bStats is fully\n"
                                    + "anonymous.")
                    .copyDefaults(true);
            try {
                config.save(configFile);
            } catch (IOException ignored) {
            }
        }
        // Load the data
        boolean enabled = config.getBoolean("enabled", true);
        String serverUUID = config.getString("serverUuid");
        boolean logErrors = config.getBoolean("logFailedRequests", false);
        boolean logSentData = config.getBoolean("logSentData", false);
        boolean logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        metricsBase =
                new MetricsBase(
                        "bukkit",
                        serverUUID,
                        serviceId,
                        enabled,
                        this::appendPlatformData,
                        this::appendServiceData,
                        submitDataTask -> Bukkit.getScheduler().runTask(plugin, submitDataTask),
                        plugin::isEnabled,
                        (message, error) -> this.plugin.getLogger().log(Level.WARNING, message, error),
                        (message) -> this.plugin.getLogger().log(Level.INFO, message),
                        logErrors,
                        logSentData,
                        logResponseStatusText);
    }

    /**
     * Add custom chart.
     *
     * @param chart the chart
     */
    public void addCustomChart(CustomChart chart) {
        metricsBase.addCustomChart(chart);
    }

    /**
     * Append platform data.
     *
     * @param builder the builder
     */
    private void appendPlatformData(JsonObjectBuilder builder) {
        builder.appendField("playerAmount", getPlayerAmount());
        builder.appendField("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
        builder.appendField("bukkitVersion", Bukkit.getVersion());
        builder.appendField("bukkitName", Bukkit.getName());
        builder.appendField("javaVersion", System.getProperty("java.version"));
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }

    /**
     * Append service data.
     *
     * @param builder the builder
     */
    private void appendServiceData(JsonObjectBuilder builder) {
        builder.appendField("pluginVersion", plugin.getDescription().getVersion());
    }

    /**
     * Gets player amount.
     *
     * @return the player amount
     */
    private int getPlayerAmount() {
        try {
            // Around MC 1.8 the return type was changed from an array to a collection,
            // This fixes java.lang.NoSuchMethodError:
            // org.bukkit.Bukkit.getOnlinePlayers()Ljava/util/Collection;
            Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
            return onlinePlayersMethod.getReturnType().equals(Collection.class)
                    ? ((Collection<?>) onlinePlayersMethod.invoke(Bukkit.getServer())).size()
                    : ((Player[]) onlinePlayersMethod.invoke(Bukkit.getServer())).length;
        } catch (Exception e) {
            // Just use the new method if the reflection failed
            return Bukkit.getOnlinePlayers().size();
        }
    }

    /**
     * The type Metrics base.
     *
     * @author LemonyPancakes
     */
    public static class MetricsBase {

        public static final String METRICS_VERSION = "2.2.1";

        private static final ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1, task -> new Thread(task, "bStats-Metrics"));

        private static final String REPORT_URL = "https://bStats.org/api/v2/data/%s";

        private final String platform;

        private final String serverUuid;

        private final int serviceId;

        private final Consumer<JsonObjectBuilder> appendPlatformDataConsumer;

        private final Consumer<JsonObjectBuilder> appendServiceDataConsumer;

        private final Consumer<Runnable> submitTaskConsumer;

        private final Supplier<Boolean> checkServiceEnabledSupplier;

        private final BiConsumer<String, Throwable> errorLogger;

        private final Consumer<String> infoLogger;

        private final boolean logErrors;

        private final boolean logSentData;

        private final boolean logResponseStatusText;

        private final Set<CustomChart> customCharts = new HashSet<>();

        private final boolean enabled;

        /**
         * Instantiates a new Metrics base.
         *
         * @param platform                    the platform
         * @param serverUuid                  the server uuid
         * @param serviceId                   the service id
         * @param enabled                     the enabled
         * @param appendPlatformDataConsumer  the append platform data consumer
         * @param appendServiceDataConsumer   the append service data consumer
         * @param submitTaskConsumer          the submit task consumer
         * @param checkServiceEnabledSupplier the check service enabled supplier
         * @param errorLogger                 the error logger
         * @param infoLogger                  the info logger
         * @param logErrors                   the log errors
         * @param logSentData                 the log sent data
         * @param logResponseStatusText       the log response status text
         */
        public MetricsBase(
                String platform,
                String serverUuid,
                int serviceId,
                boolean enabled,
                Consumer<JsonObjectBuilder> appendPlatformDataConsumer,
                Consumer<JsonObjectBuilder> appendServiceDataConsumer,
                Consumer<Runnable> submitTaskConsumer,
                Supplier<Boolean> checkServiceEnabledSupplier,
                BiConsumer<String, Throwable> errorLogger,
                Consumer<String> infoLogger,
                boolean logErrors,
                boolean logSentData,
                boolean logResponseStatusText) {
            this.platform = platform;
            this.serverUuid = serverUuid;
            this.serviceId = serviceId;
            this.enabled = enabled;
            this.appendPlatformDataConsumer = appendPlatformDataConsumer;
            this.appendServiceDataConsumer = appendServiceDataConsumer;
            this.submitTaskConsumer = submitTaskConsumer;
            this.checkServiceEnabledSupplier = checkServiceEnabledSupplier;
            this.errorLogger = errorLogger;
            this.infoLogger = infoLogger;
            this.logErrors = logErrors;
            this.logSentData = logSentData;
            this.logResponseStatusText = logResponseStatusText;
            checkRelocation();
            if (enabled) {
                startSubmitting();
            }
        }

        /**
         * Compress byte [ ].
         *
         * @param str the str
         *
         * @return the byte [ ]
         *
         * @throws IOException the io exception
         */
        private static byte[] compress(final String str) throws IOException {
            if (str == null) {
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
                gzip.write(str.getBytes(StandardCharsets.UTF_8));
            }
            return outputStream.toByteArray();
        }

        /**
         * Add custom chart.
         *
         * @param chart the chart
         */
        public void addCustomChart(CustomChart chart) {
            this.customCharts.add(chart);
        }

        /**
         * Start submitting.
         */
        private void startSubmitting() {
            final Runnable submitTask =
                    () -> {
                        if (!enabled || !checkServiceEnabledSupplier.get()) {
                            // Submitting data or service is disabled
                            scheduler.shutdown();
                            return;
                        }
                        if (submitTaskConsumer != null) {
                            submitTaskConsumer.accept(this::submitData);
                        } else {
                            this.submitData();
                        }
                    };
            // Many servers tend to restart at a fixed time at xx:00 which causes an uneven distribution
            // of requests on the
            // bStats backend. To circumvent this problem, we introduce some randomness into the initial
            // and second delay.
            // WARNING: You must not modify and part of this Metrics class, including the submit delay or
            // frequency!
            // WARNING: Modifying this code will get your plugin banned on bStats. Just don't do it!
            long initialDelay = (long) (1000 * 60 * (3 + Math.random() * 3));
            long secondDelay = (long) (1000 * 60 * (Math.random() * 30));
            scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(
                    submitTask, initialDelay + secondDelay, 1000 * 60 * 30, TimeUnit.MILLISECONDS);
        }

        /**
         * Submit data.
         */
        private void submitData() {
            final JsonObjectBuilder baseJsonBuilder = new JsonObjectBuilder();
            appendPlatformDataConsumer.accept(baseJsonBuilder);
            final JsonObjectBuilder serviceJsonBuilder = new JsonObjectBuilder();
            appendServiceDataConsumer.accept(serviceJsonBuilder);
            JsonObjectBuilder.JsonObject[] chartData =
                    customCharts.stream()
                            .map(customChart -> customChart.getRequestJsonObject(errorLogger, logErrors))
                            .filter(Objects::nonNull)
                            .toArray(JsonObjectBuilder.JsonObject[]::new);
            serviceJsonBuilder.appendField("id", serviceId);
            serviceJsonBuilder.appendField("customCharts", chartData);
            baseJsonBuilder.appendField("service", serviceJsonBuilder.build());
            baseJsonBuilder.appendField("serverUUID", serverUuid);
            baseJsonBuilder.appendField("metricsVersion", METRICS_VERSION);
            JsonObjectBuilder.JsonObject data = baseJsonBuilder.build();
            scheduler.execute(
                    () -> {
                        try {
                            // Send the data
                            sendData(data);
                        } catch (Exception e) {
                            // Something went wrong! :(
                            if (logErrors) {
                                errorLogger.accept("Could not submit bStats metrics data", e);
                            }
                        }
                    });
        }

        /**
         * Send data.
         *
         * @param data the data
         *
         * @throws Exception the exception
         */
        private void sendData(JsonObjectBuilder.JsonObject data) throws Exception {
            if (logSentData) {
                infoLogger.accept("Sent bStats metrics data: " + data.toString());
            }
            String url = String.format(REPORT_URL, platform);
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            // Compress the data to save bandwidth
            byte[] compressedData = compress(data.toString());
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Connection", "close");
            connection.addRequestProperty("Content-Encoding", "gzip");
            connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Metrics-Service/1");
            connection.setDoOutput(true);
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.write(compressedData);
            }
            StringBuilder builder = new StringBuilder();
            try (BufferedReader bufferedReader =
                         new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
            }
            if (logResponseStatusText) {
                infoLogger.accept("Sent data to bStats and received response: " + builder);
            }
        }

        /**
         * Check relocation.
         */
        private void checkRelocation() {
            // You can use the property to disable the check in your test environment
            if (System.getProperty("bstats.relocatecheck") == null
                    || !System.getProperty("bstats.relocatecheck").equals("false")) {
                // Maven's Relocate is clever and changes strings, too. So we have to use this little
                // "trick" ... :D
                final String defaultPackage =
                        new String(new byte[]{'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's'});
                final String examplePackage =
                        new String(new byte[]{'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
                // We want to make sure no one just copy & pastes the example and uses the wrong package
                // names
                if (MetricsBase.class.getPackage().getName().startsWith(defaultPackage)
                        || MetricsBase.class.getPackage().getName().startsWith(examplePackage)) {
                    throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
                }
            }
        }
    }

    /**
     * The type Advanced bar chart.
     *
     * @author LemonyPancakes
     */
    public static class AdvancedBarChart extends CustomChart {

        private final Callable<Map<String, int[]>> callable;

        /**
         * Instantiates a new Advanced bar chart.
         *
         * @param chartId  the chart id
         * @param callable the callable
         */
        public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) {
            super(chartId);
            this.callable = callable;
        }

        /**
         * Gets chart data.
         *
         * @return the chart data
         *
         * @throws Exception the exception
         */
        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<String, int[]> map = callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean allSkipped = true;
            for (Map.Entry<String, int[]> entry : map.entrySet()) {
                if (entry.getValue().length == 0) {
                    // Skip this invalid
                    continue;
                }
                allSkipped = false;
                valuesBuilder.appendField(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                // Null = skip the chart
                return null;
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    /**
     * The type Simple bar chart.
     *
     * @author LemonyPancakes
     */
    public static class SimpleBarChart extends CustomChart {

        private final Callable<Map<String, Integer>> callable;

        /**
         * Instantiates a new Simple bar chart.
         *
         * @param chartId  the chart id
         * @param callable the callable
         */
        public SimpleBarChart(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        /**
         * Gets chart data.
         *
         * @return the chart data
         *
         * @throws Exception the exception
         */
        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<String, Integer> map = callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                valuesBuilder.appendField(entry.getKey(), new int[]{entry.getValue()});
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    /**
     * The type Multi line chart.
     *
     * @author LemonyPancakes
     */
    public static class MultiLineChart extends CustomChart {

        private final Callable<Map<String, Integer>> callable;

        /**
         * Instantiates a new Multi line chart.
         *
         * @param chartId  the chart id
         * @param callable the callable
         */
        public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        /**
         * Gets chart data.
         *
         * @return the chart data
         *
         * @throws Exception the exception
         */
        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<String, Integer> map = callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean allSkipped = true;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) {
                    // Skip this invalid
                    continue;
                }
                allSkipped = false;
                valuesBuilder.appendField(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                // Null = skip the chart
                return null;
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    /**
     * The type Advanced pie.
     *
     * @author LemonyPancakes
     */
    public static class AdvancedPie extends CustomChart {

        private final Callable<Map<String, Integer>> callable;

        /**
         * Instantiates a new Advanced pie.
         *
         * @param chartId  the chart id
         * @param callable the callable
         */
        public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        /**
         * Gets chart data.
         *
         * @return the chart data
         *
         * @throws Exception the exception
         */
        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<String, Integer> map = callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean allSkipped = true;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) {
                    // Skip this invalid
                    continue;
                }
                allSkipped = false;
                valuesBuilder.appendField(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                // Null = skip the chart
                return null;
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    /**
     * The type Custom chart.
     *
     * @author LemonyPancakes
     */
    public abstract static class CustomChart {

        private final String chartId;

        /**
         * Instantiates a new Custom chart.
         *
         * @param chartId the chart id
         */
        protected CustomChart(String chartId) {
            if (chartId == null) {
                throw new IllegalArgumentException("chartId must not be null");
            }
            this.chartId = chartId;
        }

        /**
         * Gets request json object.
         *
         * @param errorLogger the error logger
         * @param logErrors   the log errors
         *
         * @return the request json object
         */
        public JsonObjectBuilder.JsonObject getRequestJsonObject(
                BiConsumer<String, Throwable> errorLogger, boolean logErrors) {
            JsonObjectBuilder builder = new JsonObjectBuilder();
            builder.appendField("chartId", chartId);
            try {
                JsonObjectBuilder.JsonObject data = getChartData();
                if (data == null) {
                    // If the data is null we don't send the chart.
                    return null;
                }
                builder.appendField("data", data);
            } catch (Throwable t) {
                if (logErrors) {
                    errorLogger.accept("Failed to get data for custom chart with id " + chartId, t);
                }
                return null;
            }
            return builder.build();
        }

        /**
         * Gets chart data.
         *
         * @return the chart data
         *
         * @throws Exception the exception
         */
        protected abstract JsonObjectBuilder.JsonObject getChartData() throws Exception;
    }

    /**
     * The type Single line chart.
     *
     * @author LemonyPancakes
     */
    public static class SingleLineChart extends CustomChart {

        private final Callable<Integer> callable;

        /**
         * Instantiates a new Single line chart.
         *
         * @param chartId  the chart id
         * @param callable the callable
         */
        public SingleLineChart(String chartId, Callable<Integer> callable) {
            super(chartId);
            this.callable = callable;
        }

        /**
         * Gets chart data.
         *
         * @return the chart data
         *
         * @throws Exception the exception
         */
        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            int value = callable.call();
            if (value == 0) {
                // Null = skip the chart
                return null;
            }
            return new JsonObjectBuilder().appendField("value", value).build();
        }
    }

    /**
     * The type Simple pie.
     *
     * @author LemonyPancakes
     */
    public static class SimplePie extends CustomChart {

        private final Callable<String> callable;

        /**
         * Instantiates a new Simple pie.
         *
         * @param chartId  the chart id
         * @param callable the callable
         */
        public SimplePie(String chartId, Callable<String> callable) {
            super(chartId);
            this.callable = callable;
        }

        /**
         * Gets chart data.
         *
         * @return the chart data
         *
         * @throws Exception the exception
         */
        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            String value = callable.call();
            if (value == null || value.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            return new JsonObjectBuilder().appendField("value", value).build();
        }
    }

    /**
     * The type Drilldown pie.
     *
     * @author LemonyPancakes
     */
    public static class DrilldownPie extends CustomChart {

        private final Callable<Map<String, Map<String, Integer>>> callable;

        /**
         * Instantiates a new Drilldown pie.
         *
         * @param chartId  the chart id
         * @param callable the callable
         */
        public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
            super(chartId);
            this.callable = callable;
        }

        /**
         * Gets chart data.
         *
         * @return the chart data
         *
         * @throws Exception the exception
         */
        @Override
        public JsonObjectBuilder.JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<String, Map<String, Integer>> map = callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean reallyAllSkipped = true;
            for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
                JsonObjectBuilder valueBuilder = new JsonObjectBuilder();
                boolean allSkipped = true;
                for (Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
                    valueBuilder.appendField(valueEntry.getKey(), valueEntry.getValue());
                    allSkipped = false;
                }
                if (!allSkipped) {
                    reallyAllSkipped = false;
                    valuesBuilder.appendField(entryValues.getKey(), valueBuilder.build());
                }
            }
            if (reallyAllSkipped) {
                // Null = skip the chart
                return null;
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    /**
     * The type Json object builder.
     *
     * @author LemonyPancakes
     */
    public static class JsonObjectBuilder {

        private StringBuilder builder = new StringBuilder();

        private boolean hasAtLeastOneField = false;

        /**
         * Instantiates a new Json object builder.
         */
        public JsonObjectBuilder() {
            builder.append("{");
        }

        /**
         * Escape string.
         *
         * @param value the value
         *
         * @return the string
         */
        private static String escape(String value) {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '"') {
                    builder.append("\\\"");
                } else if (c == '\\') {
                    builder.append("\\\\");
                } else if (c <= '\u000F') {
                    builder.append("\\u000").append(Integer.toHexString(c));
                } else if (c <= '\u001F') {
                    builder.append("\\u00").append(Integer.toHexString(c));
                } else {
                    builder.append(c);
                }
            }
            return builder.toString();
        }

        /**
         * Append null json object builder.
         *
         * @param key the key
         *
         * @return the json object builder
         */
        public JsonObjectBuilder appendNull(String key) {
            appendFieldUnescaped(key, "null");
            return this;
        }

        /**
         * Append field json object builder.
         *
         * @param key   the key
         * @param value the value
         *
         * @return the json object builder
         */
        public JsonObjectBuilder appendField(String key, String value) {
            if (value == null) {
                throw new IllegalArgumentException("JSON value must not be null");
            }
            appendFieldUnescaped(key, "\"" + escape(value) + "\"");
            return this;
        }

        /**
         * Append field json object builder.
         *
         * @param key   the key
         * @param value the value
         *
         * @return the json object builder
         */
        public JsonObjectBuilder appendField(String key, int value) {
            appendFieldUnescaped(key, String.valueOf(value));
            return this;
        }

        /**
         * Append field json object builder.
         *
         * @param key    the key
         * @param object the object
         *
         * @return the json object builder
         */
        public JsonObjectBuilder appendField(String key, JsonObject object) {
            if (object == null) {
                throw new IllegalArgumentException("JSON object must not be null");
            }
            appendFieldUnescaped(key, object.toString());
            return this;
        }

        /**
         * Append field json object builder.
         *
         * @param key    the key
         * @param values the values
         *
         * @return the json object builder
         */
        public JsonObjectBuilder appendField(String key, String[] values) {
            if (values == null) {
                throw new IllegalArgumentException("JSON values must not be null");
            }
            String escapedValues =
                    Arrays.stream(values)
                            .map(value -> "\"" + escape(value) + "\"")
                            .collect(Collectors.joining(","));
            appendFieldUnescaped(key, "[" + escapedValues + "]");
            return this;
        }

        /**
         * Append field json object builder.
         *
         * @param key    the key
         * @param values the values
         *
         * @return the json object builder
         */
        public JsonObjectBuilder appendField(String key, int[] values) {
            if (values == null) {
                throw new IllegalArgumentException("JSON values must not be null");
            }
            String escapedValues =
                    Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","));
            appendFieldUnescaped(key, "[" + escapedValues + "]");
            return this;
        }

        /**
         * Append field json object builder.
         *
         * @param key    the key
         * @param values the values
         *
         * @return the json object builder
         */
        public JsonObjectBuilder appendField(String key, JsonObject[] values) {
            if (values == null) {
                throw new IllegalArgumentException("JSON values must not be null");
            }
            String escapedValues =
                    Arrays.stream(values).map(JsonObject::toString).collect(Collectors.joining(","));
            appendFieldUnescaped(key, "[" + escapedValues + "]");
            return this;
        }

        /**
         * Append field unescaped.
         *
         * @param key          the key
         * @param escapedValue the escaped value
         */
        private void appendFieldUnescaped(String key, String escapedValue) {
            if (builder == null) {
                throw new IllegalStateException("JSON has already been built");
            }
            if (key == null) {
                throw new IllegalArgumentException("JSON key must not be null");
            }
            if (hasAtLeastOneField) {
                builder.append(",");
            }
            builder.append("\"").append(escape(key)).append("\":").append(escapedValue);
            hasAtLeastOneField = true;
        }

        /**
         * Build json object.
         *
         * @return the json object
         */
        public JsonObject build() {
            if (builder == null) {
                throw new IllegalStateException("JSON has already been built");
            }
            JsonObject object = new JsonObject(builder.append("}").toString());
            builder = null;
            return object;
        }

        /**
         * The type Json object.
         *
         * @author LemonyPancakes
         */
        public static class JsonObject {

            private final String value;

            /**
             * Instantiates a new Json object.
             *
             * @param value the value
             */
            private JsonObject(String value) {
                this.value = value;
            }

            /**
             * To string string.
             *
             * @return the string
             */
            @Override
            public String toString() {
                return value;
            }
        }
    }
}