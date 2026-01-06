package com.billing.billingsystem.users.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import com.billing.billingsystem.dto.PageResponse;
import com.billing.billingsystem.users.application.UserService;
import com.billing.billingsystem.users.domain.User;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping()
    public String hello() {
        return "Hello, Billing System User!";
    }


    @GetMapping("/all")
    public ResponseEntity<PageResponse<User>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<User> userPage = userService.getAllUsers(size, page);
        PageResponse<User> response = new PageResponse<>(
            userPage.getContent(),
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements(),
            userPage.getTotalPages(),
            userPage.hasNext(),
            userPage.hasPrevious()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping()
    public ResponseEntity<?>  createUser(@RequestBody User user) {
        User createUser=userService.createUser(user);
        return ResponseEntity.ok(createUser);
    }
}
