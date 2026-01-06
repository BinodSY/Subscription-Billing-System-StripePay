package com.billing.billingsystem.users.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.billingsystem.users.databaseArchitecture.UserRepository;
import com.billing.billingsystem.users.domain.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public User createUser(User user){
        return userRepository.save(user);
    }

    public Page<User> getAllUsers(int size,int page)
        {
                Pageable pageble=PageRequest.of(page,size,Sort.by("createdAt").descending());
                return userRepository.findAll( pageble);
    }
}
