package com.example.app;

import android.net.SSLCertificateSocketFactory;
import com.example.hub.grpc.Hub;
import com.example.hub.grpc.HubServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.okhttp.SslSocketFactoryChannelCredentials;
import org.conscrypt.Conscrypt;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.callback.PasswordCallback;
import java.security.Security;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class HubFrontend {

	public HubFrontend() {
		// Install Conscrypt provider
		Security.insertProviderAt(Conscrypt.newProvider(), 1);
	}

	private static ManagedChannel buildChannel() {
		SSLSocketFactory sslSocketFactory = ;
		return OkHttpChannelBuilder.forAddress("10.0.2.2", 29292)
				.useTransportSecurity()
				.sslSocketFactory(sslSocketFactory)
				.build();
	}

	public String ping(String content) {
		ManagedChannel channel = buildChannel();
		HubServiceGrpc.HubServiceBlockingStub stub = HubServiceGrpc.newBlockingStub(channel);
		Hub.PingRequest pingRequest = Hub.PingRequest.newBuilder().setContent(content).build();
		Hub.PingResponse pingResponse = stub.withDeadlineAfter(5, TimeUnit.SECONDS).ping(pingRequest);
		channel.shutdown();
		return pingResponse.getContent();
	}

}
