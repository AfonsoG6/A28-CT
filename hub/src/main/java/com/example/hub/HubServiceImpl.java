package com.example.hub;

import com.example.hub.grpc.Hub.PingRequest;
import com.example.hub.grpc.Hub.PingResponse;
import com.example.hub.grpc.Hub.GetNewICCResponse;
import com.example.hub.grpc.Hub.QueryInfectedSKsRequest;
import com.example.hub.grpc.Hub.QueryInfectedSKsResponse;
import com.example.hub.grpc.Hub.ClaimInfectionRequest;
import com.example.hub.grpc.Hub.Empty;
import com.example.hub.grpc.HubServiceGrpc;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static io.grpc.Status.DEADLINE_EXCEEDED;
import static io.grpc.Status.INVALID_ARGUMENT;

public class HubServiceImpl extends HubServiceGrpc.HubServiceImplBase {
	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(DEADLINE_EXCEEDED.withDescription("Deadline exceeded").asRuntimeException());
			return;
		}
		// Build Response
		PingResponse.Builder builder = PingResponse.newBuilder();
		String content = request.getContent();
		if (content.isBlank()) {
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Input cannot be empty!").asRuntimeException());
			return;
		}
		System.out.println("Received: " + content);
		builder.setContent("Hello " + content + "!");
		PingResponse response = builder.build();
		// Send Response
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getNewICC(Empty request, StreamObserver<GetNewICCResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(DEADLINE_EXCEEDED.withDescription("Deadline exceeded").asRuntimeException());
			return;
		}

		byte[] array = new byte[64];
		new Random().nextBytes(array);
		String generatedICC = new String(array, StandardCharsets.UTF_8);
		GetNewICCResponse.Builder builder = GetNewICCResponse.newBuilder();
		builder.setIcc(generatedICC);
		GetNewICCResponse response = builder.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void claimInfection(ClaimInfectionRequest request, StreamObserver<Empty> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(DEADLINE_EXCEEDED.withDescription("Deadline exceeded").asRuntimeException());
			return;
		}
		System.out.println(request.getIcc());
		Empty response = Empty.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void queryInfectedSKs(
			QueryInfectedSKsRequest request, StreamObserver<QueryInfectedSKsResponse> responseObserver
	) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(DEADLINE_EXCEEDED.withDescription("Deadline exceeded").asRuntimeException());
			return;
		}
		QueryInfectedSKsResponse.Builder builder = QueryInfectedSKsResponse.newBuilder();

		QueryInfectedSKsResponse response = builder.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
