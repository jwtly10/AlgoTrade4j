package dev.jwtly10.liveapi.controller;

import dev.jwtly10.liveapi.model.broker.BrokerAccount;
import dev.jwtly10.liveapi.model.broker.Timezone;
import dev.jwtly10.liveapi.service.broker.BrokerAccountService;
import dev.jwtly10.marketdata.common.Broker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class BrokerAccountController {
    private final BrokerAccountService brokerAccountService;

    public BrokerAccountController(BrokerAccountService brokerAccountService) {
        this.brokerAccountService = brokerAccountService;
    }

    @PostMapping()
    public ResponseEntity<BrokerAccount> createBrokerAccount(@RequestBody BrokerAccount broker) {
        return ResponseEntity.ok().body(brokerAccountService.createBrokerAccount(broker));
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<BrokerAccount> updateBrokerAccount(@RequestBody BrokerAccount broker, @PathVariable("accountId") String accountId) {
        return ResponseEntity.ok().body(brokerAccountService.updateBrokerAccount(accountId, broker));
    }

    @GetMapping
    public ResponseEntity<List<BrokerAccount>> getAccounts() {
        return ResponseEntity.ok().body(brokerAccountService.getAccounts());
    }

    @GetMapping("/brokers")
    public ResponseEntity<List<Broker>> getBrokers() {
        return ResponseEntity.ok().body(brokerAccountService.getBrokers());
    }

    @GetMapping("/timezones")
    public ResponseEntity<List<Timezone>> getTimezones() {
        return ResponseEntity.ok().body(brokerAccountService.getTimezones());
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<String> deleteBrokerAccount(@PathVariable("accountId") String accountId) {
        brokerAccountService.deleteBrokerAccount(accountId);
        return ResponseEntity.ok().body("Broker account deleted successfully");
    }
}