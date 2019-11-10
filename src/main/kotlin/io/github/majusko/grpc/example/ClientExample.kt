package io.github.majusko.grpc.example

import com.google.protobuf.Empty
import io.github.majusko.grpc.example.proto.Example
import io.github.majusko.grpc.example.proto.MyAuthServiceGrpc
import io.github.majusko.grpc.jwt.interceptor.AuthClientInterceptor
import io.github.majusko.grpc.jwt.interceptor.GrpcHeader
import io.grpc.Channel
import io.grpc.ClientInterceptors
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class ClientExample(private val authClientInterceptor: AuthClientInterceptor) {

    private lateinit var channel: Channel

    @Value("\${my.server.configuration}")
    private val target = "localhost:6565"

    @PostConstruct
    private fun initClient() {
        val channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

        this.channel = ClientInterceptors.intercept(channel, authClientInterceptor)
    }

    fun loginAndUpdateName(name: String): Example.Profile {

        //login - validated by default internal token
        val stub = MyAuthServiceGrpc.newBlockingStub(channel)
        val myUserId = UUID.randomUUID().toString()
        val loginResponse = stub.login(Example.LoginRequest.newBuilder().setUserId(myUserId).build())
        val signedStub = getSignedStub(loginResponse.token)

        //update name - validated by ownership
        signedStub.update(Example.Profile.newBuilder().setUserId(myUserId).setName(name).build())

        //get profile - validated by role
        return signedStub.getProfile(Empty.getDefaultInstance())
    }

    private fun getSignedStub(token: String): MyAuthServiceGrpc.MyAuthServiceBlockingStub {
        val header = Metadata()

        header.put(GrpcHeader.AUTHORIZATION, token)

        val stub = MyAuthServiceGrpc.newBlockingStub(channel)

        return MetadataUtils.attachHeaders(stub, header)
    }
}