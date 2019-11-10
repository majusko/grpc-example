package io.github.majusko.grpc.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class ApplicationTests {

    @Autowired
    lateinit var clientExample: ClientExample

    @Test
    fun testBasicExecutionOfTheExample() {
        val nameToChange = "Some Name"
        val response = clientExample.loginAndUpdateName(nameToChange)

        assert(nameToChange == response.name)
    }
}
