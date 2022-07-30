/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.vault.ksm.conf;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleServerException;
import org.apache.guacamole.environment.Environment;
import org.apache.guacamole.properties.BooleanGuacamoleProperty;
import org.apache.guacamole.properties.StringGuacamoleProperty;
import org.apache.guacamole.vault.conf.VaultConfigurationService;

import com.keepersecurity.secretsManager.core.InMemoryStorage;
import com.keepersecurity.secretsManager.core.KeyValueStorage;
import com.keepersecurity.secretsManager.core.SecretsManagerOptions;

/**
 * Service for retrieving configuration information regarding the Keeper
 * Secrets Manager authentication extension.
 */
@Singleton
public class KsmConfigurationService extends VaultConfigurationService {

    /**
     * The Guacamole server environment.
     */
    @Inject
    private Environment environment;

    /**
     * The name of the file which contains the YAML mapping of connection
     * parameter token to secrets within Keeper Secrets Manager.
     */
    private static final String TOKEN_MAPPING_FILENAME = "ksm-token-mapping.yml";

    /**
     * The name of the properties file containing Guacamole configuration
     * properties whose values are the names of corresponding secrets within
     * Keeper Secrets Manager.
     */
    private static final String PROPERTIES_FILENAME = "guacamole.properties.ksm";

    /**
     * The base64-encoded configuration information generated by the Keeper
     * Commander CLI tool.
     */
    private static final StringGuacamoleProperty KSM_CONFIG = new StringGuacamoleProperty() {

        @Override
        public String getName() {
            return "ksm-config";
        }
    };

    /**
     * Whether unverified server certificates should be accepted.
     */
    private static final BooleanGuacamoleProperty ALLOW_UNVERIFIED_CERT = new BooleanGuacamoleProperty() {

        @Override
        public String getName() {
            return "ksm-allow-unverified-cert";
        }
    };

    /**
     * Whether windows domains should be stripped off from usernames that are
     * read from the KSM vault.
     */
    private static final BooleanGuacamoleProperty STRIP_WINDOWS_DOMAINS = new BooleanGuacamoleProperty() {

        @Override
        public String getName() {
            return "ksm-strip-windows-domains";
        }
    };

    /**
     * Creates a new KsmConfigurationService which reads the configuration
     * from "ksm-token-mapping.yml" and properties from
     * "guacamole.properties.ksm". The token mapping is a YAML file which lists
     * each connection parameter token and the name of the secret from which
     * the value for that token should be read, while the properties file is an
     * alternative to guacamole.properties where each property value is the
     * name of a secret containing the actual value.
     */
    public KsmConfigurationService() {
        super(TOKEN_MAPPING_FILENAME, PROPERTIES_FILENAME);
    }

    /**
     * Return whether unverified server certificates should be accepted when
     * communicating with Keeper Secrets Manager.
     *
     * @return
     *     true if unverified server certificates should be accepted, false
     *     otherwise.
     *
     * @throws GuacamoleException
     *     If the value specified within guacamole.properties cannot be
     *     parsed.
     */
    public boolean getAllowUnverifiedCertificate() throws GuacamoleException {
        return environment.getProperty(ALLOW_UNVERIFIED_CERT, false);
    }

    @Override
    public boolean getSplitWindowsUsernames() throws GuacamoleException {
        return environment.getProperty(STRIP_WINDOWS_DOMAINS, false);
    }


    /**
     * Return the globally-defined base-64-encoded JSON KSM configuration blob
     * as a string.
     *
     * @return
     *     The globally-defined base-64-encoded JSON KSM configuration blob
     *     as a string.
     *
     * @throws GuacamoleException
     *     If the value specified within guacamole.properties cannot be
     *     parsed or does not exist.
     */
    public String getKsmConfig() throws GuacamoleException {
        return environment.getRequiredProperty(KSM_CONFIG);
    }

    /**
     * Given a base64-encoded JSON KSM configuration, parse and return a
     * KeyValueStorage object.
     *
     * @param value
     *     The base64-encoded JSON KSM configuration to parse.
     *
     * @return
     *     The KeyValueStorage that is a result of the parsing operation
     *
     * @throws GuacamoleException
     *     If the provided value is not valid base-64 encoded JSON KSM configuration.
     */
    private static KeyValueStorage parseKsmConfig(String value) throws GuacamoleException {

        // Parse base64 value as KSM config storage
        try {
            return new InMemoryStorage(value);
        }
        catch (IllegalArgumentException e) {
            throw new GuacamoleServerException("Invalid base64 configuration "
                    + "for Keeper Secrets Manager.", e);
        }

    }

    /**
     * Returns the options required to authenticate with Keeper Secrets Manager
     * when retrieving secrets. These options are read from the contents of
     * base64-encoded JSON configuration data generated by the Keeper Commander
     * CLI tool. This configuration data must be passed directly as an argument.
     *
     * @param ksmConfig
     *     The KSM configuration blob to parse.
     *
     * @return
     *     The options that should be used when connecting to Keeper Secrets
     *     Manager when retrieving secrets.
     *
     * @throws GuacamoleException
     *     If an invalid ksmConfig parameter is provided.
     */
    public SecretsManagerOptions getSecretsManagerOptions(@Nonnull String ksmConfig) throws GuacamoleException {

        return new SecretsManagerOptions(
                parseKsmConfig(ksmConfig), null, getAllowUnverifiedCertificate());
    }
}