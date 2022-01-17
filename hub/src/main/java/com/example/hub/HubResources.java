package com.example.hub;

import java.io.InputStream;

public class HubResources {
	private static final String KEYS_PATH = "/keys_certs/";

	public static InputStream getCertChainFile() {
		return HubResources.class.getResourceAsStream(KEYS_PATH + "hub_cert.pem");
	}

	public static InputStream getPrivateKeyFile() {
		return HubResources.class.getResourceAsStream(KEYS_PATH + "hub_privkey_pk8.pem");
	}
}
