package io.github.majusko.grpc.example

import io.github.majusko.grpc.jwt.GrpcJwtSpringBootStarterApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(GrpcJwtSpringBootStarterApplication::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
