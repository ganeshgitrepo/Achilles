package info.archinnov.achilles.configuration;

import static info.archinnov.achilles.configuration.CQLConfigurationParameters.*;
import info.archinnov.achilles.validation.Validator;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.ProtocolOptions.Compression;
import com.datastax.driver.core.SSLOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.Policies;
import com.datastax.driver.core.policies.ReconnectionPolicy;
import com.datastax.driver.core.policies.RetryPolicy;

/**
 * CQLArgumentExtractor
 * 
 * @author DuyHai DOAN
 * 
 */
public class CQLArgumentExtractor extends ArgumentExtractor
{

    public Cluster initCluster(Map<String, Object> configurationMap)
    {
        String contactPoints = (String) configurationMap.get(CONNECTION_CONTACT_POINTS_PARAM);
        Integer port = (Integer) configurationMap.get(CONNECTION_PORT_PARAM);

        Compression compression = Compression.SNAPPY;
        if (configurationMap.containsKey(COMPRESSION_TYPE))
        {
            compression = (Compression) configurationMap.get(COMPRESSION_TYPE);
        }

        RetryPolicy retryPolicy = Policies.defaultRetryPolicy();
        if (configurationMap.containsKey(RETRY_POLICY))
        {
            retryPolicy = (RetryPolicy) configurationMap.get(RETRY_POLICY);
        }

        LoadBalancingPolicy loadBalancingPolicy = Policies.defaultLoadBalancingPolicy();
        if (configurationMap.containsKey(LOAD_BALANCING_POLICY))
        {
            loadBalancingPolicy = (LoadBalancingPolicy) configurationMap.get(LOAD_BALANCING_POLICY);
        }

        ReconnectionPolicy reconnectionPolicy = Policies.defaultReconnectionPolicy();
        if (configurationMap.containsKey(RECONNECTION_POLICY))
        {
            reconnectionPolicy = (ReconnectionPolicy) configurationMap.get(RECONNECTION_POLICY);
        }

        String username = null;
        String password = null;
        if (configurationMap.containsKey(USERNAME) && configurationMap.containsKey(PASSWORD))
        {
            username = (String) configurationMap.get(USERNAME);
            password = (String) configurationMap.get(PASSWORD);
        }

        boolean disableJmx = false;
        if (configurationMap.containsKey(DISABLE_JMX))
        {
            disableJmx = (Boolean) configurationMap.get(DISABLE_JMX);
        }

        boolean disableMetrics = false;
        if (configurationMap.containsKey(DISABLE_METRICS))
        {
            disableMetrics = (Boolean) configurationMap.get(DISABLE_METRICS);
        }

        boolean sslEnabled = false;
        if (configurationMap.containsKey(SSL_ENABLED))
        {
            sslEnabled = (Boolean) configurationMap.get(SSL_ENABLED);
        }

        SSLOptions sslOptions = null;
        if (configurationMap.containsKey(SSL_OPTIONS))
        {
            sslOptions = (SSLOptions) configurationMap.get(SSL_OPTIONS);
        }

        Validator.validateNotBlank(contactPoints, "%s property should be provided", CONNECTION_CONTACT_POINTS_PARAM);
        Validator.validateNotNull(port, "%s property should be provided", CONNECTION_PORT_PARAM);
        if (sslEnabled)
        {
            Validator.validateNotNull(sslOptions, "%s property should be provided when SSL is enabled", SSL_OPTIONS);
        }

        String[] contactPointsList = StringUtils.split(contactPoints, ",");

        Builder clusterBuilder = Cluster.builder() //
                .addContactPoints(contactPointsList)
                .withPort(port)
                .withCompression(compression)
                .withRetryPolicy(retryPolicy)
                .withLoadBalancingPolicy(loadBalancingPolicy)
                .withReconnectionPolicy(reconnectionPolicy);

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password))
        {
            clusterBuilder.withCredentials(username, password);
        }

        if (disableJmx)
        {
            clusterBuilder.withoutJMXReporting();
        }

        if (disableMetrics)
        {
            clusterBuilder.withoutMetrics();
        }

        if (sslEnabled)
        {
            clusterBuilder.withSSL().withSSL(sslOptions);
        }

        return clusterBuilder.build();

    }

    public Session initSession(Cluster cluster, Map<String, Object> configurationMap)
    {
        String keyspace = (String) configurationMap.get(KEYSPACE_NAME_PARAM);
        Validator.validateNotBlank(keyspace, "%s property should be provided", KEYSPACE_NAME_PARAM);

        return cluster.connect(keyspace);
    }

}
