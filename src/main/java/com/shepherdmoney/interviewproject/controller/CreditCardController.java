package com.shepherdmoney.interviewproject.controller;

//personal
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
// import com.shepherdmoney.interviewproject.repository.CardMapRepository;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.SerializableTreeMap;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.HashMap;
import java.time.LocalDate;
import java.util.Comparator;
import java.io.Serializable;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here (~1 line)      done
    @Autowired
    private CreditCardRepository creditCardRepository;
    @Autowired
    private UserRepository userRepository;
    // @Autowired
    // private CardMapRepository cardMapRepository;



    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length

        //first check if user exists
        Optional<User> optUser = userRepository.findById(payload.getUserId());

        if (!optUser.isPresent()) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(-1); }
        
        User currentUser = optUser.get();
        
        CreditCard newCreditCard = new CreditCard();
        newCreditCard.setIssuanceBank(payload.getCardIssuanceBank());
        newCreditCard.setNumber(payload.getCardNumber());
        newCreditCard.setUserId(payload.getUserId());

        creditCardRepository.save(newCreditCard);
        
        currentUser.getCreditCards().add(newCreditCard.getId());
        userRepository.save(currentUser);

        return ResponseEntity.ok().body(newCreditCard.getId());
        

        
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null
        List<CreditCardView> cardViews = new ArrayList();
        
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent()) {
            User currentUser = optUser.get();
            for (int cardId : currentUser.getCreditCards()) {
                CreditCard card = creditCardRepository.findById(cardId).get(); //this needs error checking
                cardViews.add(new CreditCardView(card.getIssuanceBank(), card.getNumber()));
            }
        }
        return ResponseEntity.ok(cardViews);
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        Optional<CreditCard> optCard = findCreditByNumber(creditCardNumber);
        if (optCard.isPresent()) {
            return ResponseEntity.ok(optCard.get().getUserId());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(-1);
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Integer> postMethodName(@RequestBody UpdateBalancePayload[] payload) {  //TODO: for me this looks line an incorrect return class
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      1. For the balance history in the credit card
        //      2. If there are gaps between two balance dates, fill the empty date with the balance of the previous date
        //      3. Given the payload `payload`, calculate the balance different between the payload and the actual balance stored in the database
        //      4. If the different is not 0, update all the following budget with the difference
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 100}]
        //      This is because
        //      1. You would first populate 4/11 with previous day's balance (4/10), so {date: 4/11, amount: 100}
        //      2. And then you observe there is a +10 difference
        //      3. You propagate that +10 difference until today
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.

        //first fetch credit card
        

        //for each update in list, add to list, propogate any changes upward


        for (UpdateBalancePayload payloadItem : payload) {
            Optional<CreditCard> optCard = findCreditByNumber(payloadItem.getCreditCardNumber());
            if (!optCard.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(0);
            }
            CreditCard card = optCard.get();
            ArrayList<BalanceHistory> histories = card.getBalanceHistories();

            LocalDate newDate = payloadItem.getBalanceDate();
            double newAmount = payloadItem.getBalanceAmount();
            BalanceHistory newBalanceHistory = new BalanceHistory(newDate, newAmount);

            //add the new BalanceHistory in the correct order in the list
            int index;
            for (index = 0; index < histories.size(); index++) {
                if (newDate.compareTo(histories.get(index).getDate()) > 0) { //add in proper place of the list
                    histories.add(index, newBalanceHistory);
                    break;
                } else if (newDate.compareTo(histories.get(index).getDate()) == 0) { //remove old value for that date and replace
                    histories.remove(index);
                    histories.add(index, newBalanceHistory);
                    break;
                }
            }

            if (index == histories.size() - 1) { //will be last item in list no need to propogate changes
                histories.add(index, newBalanceHistory);
                break;
            }

            BalanceHistory previousHistory = histories.get(index + 1);
            double balanceDiff = newAmount - previousHistory.getBalance();

            if (balanceDiff != 0 && index > 0) {
                for (int i = index - 1; i >= 0; i--) {
                        BalanceHistory prev = histories.get(i);
                        prev.setBalance(prev.getBalance() + balanceDiff);
                    }
            }

            //add a balance history for todays date if does not exist, balance is last known value
            BalanceHistory mostRecentBalance = histories.get(0);
            if (!mostRecentBalance.getDate().isEqual(LocalDate.now())) {
                histories.add(0, new BalanceHistory(LocalDate.now(), mostRecentBalance.getBalance()));
            }

        } 
        


        return ResponseEntity.ok(0);

    }


    public Optional<CreditCard> findCreditByNumber(String cardNumber) {
        //naive approach, not efficient
        List<CreditCard> allCards = creditCardRepository.findAll();
        for (CreditCard card: allCards) {
            if (card.getNumber().equals(cardNumber)) {
                return Optional.of(card);
            }
        }
        return Optional.empty();


        //below code is implementation using extra repo, still need to adjust to not use raw integer
        // Optional<Integer> cardId = cardMapRepository.findById(cardNumber);
        // if (cardId.isPresent()) {
        //     Optional<CreditCard> card = creditCardRepository.findById(cardId.get());
        //     if (card.isPresent()) {return card;}
        // }
        // return Optional.empty();
    }

}
