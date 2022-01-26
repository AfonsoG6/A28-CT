package com.example.hub;

import com.example.hub.database.PostgreSQLJDBC;
import com.example.hub.task.CleanSkTask;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HubMain {

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			System.out.println("Hub does not take any arguments");
			return;
		}
		PostgreSQLJDBC.connect();
		scheduleTasks();
		int hubPort = 29292;
		Server server = prepareServer(hubPort);
		server.start();
		System.out.println("Server started on port: " + hubPort);
		server.awaitTermination();
	}

	private static void scheduleTasks() {
		scheduler.scheduleAtFixedRate(new CleanSkTask(), 12, 24, TimeUnit.HOURS);
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
