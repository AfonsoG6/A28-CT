package com.example.hub;

import com.example.hub.grpc.Hub.PingResponse;
import com.example.hub.grpc.Hub.PingRequest;
import com.example.hub.grpc.HubServiceGrpc;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import static io.grpc.Status.*;

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
		builder.setContent("Hello " + content + "!");
		PingResponse response = builder.build();
		// Send Response
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
