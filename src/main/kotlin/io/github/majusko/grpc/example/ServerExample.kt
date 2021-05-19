package io.github.majusko.grpc.example

import com.google.protobuf.Empty
import io.github.majusko.grpc.example.proto.Example
import io.github.majusko.grpc.example.proto.MyAuthServiceGrpc
import io.github.majusko.grpc.jwt.annotation.Allow
import io.github.majusko.grpc.jwt.data.GrpcJwtContext
import io.github.majusko.grpc.jwt.service.GrpcRole
import io.github.majusko.grpc.jwt.service.JwtService
import io.github.majusko.grpc.jwt.service.dto.JwtData
import io.grpc.stub.StreamObserver
import org.lognet.springboot.grpc.GRpcService

@GRpcService
class ServerExample(private val jwtService: JwtService) : MyAuthServiceGrpc.MyAuthServiceImplBase() {

    private val nameStorage = hashMapOf<String, String>()

    companion object {
        const val PLAYER = "player"
        const val ADMIN = "admin"
    }

    @Allow(roles = [GrpcRole.INTERNAL])
    override fun login(request: Example.LoginRequest, response: StreamObserver<Example.Token>) {
        val jwtData = JwtData(request.userId, setOf(PLAYER))
        val token = jwtService.generate(jwtData)
        val proto = Example.Token.newBuilder()
            .setToken(token)
            .build()

        response.onNext(proto)
        response.onCompleted()
    }

    @Allow(roles = [PLAYER])
    override fun getProfile(request: Empty, response: StreamObserver<Example.Profile>) {
        val auth = GrpcJwtContext.get().orElseThrow { throw Exception("Missing auth data!") }
        val proto = Example.Profile.newBuilder()
            .setUserId(auth.userId)
            .setName(nameStorage[auth.userId] ?: "")
            .build()

        response.onNext(proto)
        response.onCompleted()
    }

    @Allow(ownerField = "userId", roles = [ADMIN])
    override fun update(request: Example.Profile, response: StreamObserver<Empty>) {

        nameStorage[request.userId] = request.name

        response.onNext(Empty.getDefaultInstance())
        response.onCompleted()
    }
}