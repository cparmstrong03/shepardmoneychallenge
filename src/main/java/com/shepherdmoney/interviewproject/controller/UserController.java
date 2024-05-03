package com.shepherdmoney.interviewproject.controller;

//personal
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.model.User;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    // TODO: wire in the user repository (~ 1 line)   done
    @Autowired
    private UserRepository userRepository;

    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        // TODO: Create an user entity with information given in the payload, store it in the database     done
        //       and return the id of the user in 200 OK response
        User newUser = new User();  //this weird arraylist intro should be changed
        ArrayList<Integer> lst = new ArrayList<Integer>();
        newUser.setCreditCards(lst);
        newUser.setName(payload.getName());
        newUser.setEmail(payload.getEmail());
        userRepository.save(newUser);
        return ResponseEntity.ok().body(newUser.getId());
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        // TODO: Return 200 OK if a user with the given ID exists, and the deletion is successful          done
        //       Return 400 Bad Request if a user with the ID does not exist
        //       The response body could be anything you consider appropriate
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            return ResponseEntity.status(HttpStatus.OK).body("Deletion successful");
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request");
    }
}
