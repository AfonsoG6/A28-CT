package com.example.hub;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class HubMain {
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length > 0) {
			System.out.println("Hub does not take any arguments");
			return;
		}

		int hubPort = 29292;

		HubServiceImpl service = new HubServiceImpl();
		Server server = ServerBuilder.forPort(hubPort).addService(service).build();
		server.start();
		System.out.println("Server started");
		server.awaitTermination();
	}
}
