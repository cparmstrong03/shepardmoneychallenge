package com.shepherdmoney.interviewproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "MyUser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;

    private String email;

    // TODO: User's credit card I think done, still deciding if arraylist is right
    // HINT: A user can have one or more, or none at all. We want to be able to
    // query credit cards by user
    // and user by a credit card.
    private ArrayList<Integer> creditCards;
}
