package br.com.jck.aws_project01.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);


    @GetMapping("/{name}")
    public ResponseEntity<?> userName(@PathVariable String name){
        LOG.info("User controller - o nome do usuário é: {}", name);
        return ResponseEntity.ok("Hello, "+name+ " Welcome in our system. Enjoy with us");
    }
}
