package com.example.hub;

import com.example.hub.database.PostgreSQLJDBC;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.InputStream;

public class HubMain {

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			System.out.println("Hub does not take any arguments");
			return;
		}
		PostgreSQLJDBC.connect();
		int hubPort = 29292;
		Server server = prepareServer(hubPort);
		server.start();
		System.out.println("Server started on port: " + hubPort);
		server.awaitTermination();
	}

	private static Server prepareServer(int port) throws Exception {
		InputStream certChainFile = HubResources.getCertChainFile();
		InputStream privateKeyFile = HubResources.getPrivateKeyFile();
		if (certChainFile == null || privateKeyFile == null) {
			throw new Exception("Certificate or private key file not found"); // TODO Create dedicated exception
		}

		HubServiceImpl service = new HubServiceImpl();
		return ServerBuilder.forPort(port)
				.useTransportSecurity(certChainFile, privateKeyFile)
				.addService(service)
				.build();
	}
}
