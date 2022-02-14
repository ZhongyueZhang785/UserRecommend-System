package com.nens.springblockchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nens.springblockchain.utility.*;

@SpringBootApplication
@RestController
public class SpringBlockchainApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBlockchainApplication.class, args);
    }

    @GetMapping("/hello")
    public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @GetMapping("/blockchain")
    public String blockChainResponse(@RequestParam(value = "cc") String encode) {

        String response = cloudChain.blockChainResponse(encode);
        return response;
    }

}
