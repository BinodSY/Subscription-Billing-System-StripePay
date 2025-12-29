package com.billing.billingsystem.Users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping()
    public String hello() {
        return "Hello, Billing System User!";
    }
    @PostMapping()
    public ResponseEntity<?>  createUser(@RequestBody User user) {
        User createUser=userService.createUser(user);
        return ResponseEntity.ok(createUser);
    }
}
