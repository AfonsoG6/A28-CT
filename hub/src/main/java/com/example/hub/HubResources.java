package com.example.hub;

import java.io.File;

public class HubResources {
	private static final String RESOURCES_PATH = "src/main/resources/";
	private static final String KEYS_PATH = RESOURCES_PATH + "keys_certs/";

	public static File getCertChainFile() {
		return new File(KEYS_PATH + "hub_cert.crt");
	}

	public static File getPrivateKeyFile() {
		return new File(KEYS_PATH + "hub_privkey.pem");
	}
}
