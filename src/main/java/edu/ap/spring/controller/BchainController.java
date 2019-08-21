package edu.ap.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ch.qos.logback.core.joran.conditional.ElseAction;
import edu.ap.spring.service.Block;
import edu.ap.spring.service.BlockChain;
import edu.ap.spring.service.Wallet;
import edu.ap.spring.transaction.Transaction;

@Controller
public class BchainController {
    @Autowired
    private BlockChain bChain;
    @Autowired
    private Wallet coinbase, walletA, walletB;
    private Transaction genesisTransaction;

    private void init() {
        bChain.setSecurity();
        coinbase.generateKeyPair();
        walletA.generateKeyPair();
        walletB.generateKeyPair();

        // create genesis transaction, which sends 100 coins to walletA:
        genesisTransaction = new Transaction(coinbase.getPublicKey(), walletA.getPublicKey(), 100f);
        genesisTransaction.generateSignature(coinbase.getPrivateKey()); // manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; // manually set the transaction id

        // creating and Mining Genesis block
        Block genesis = new Block();
        genesis.setPreviousHash("0");
        genesis.addTransaction(genesisTransaction, bChain);
        bChain.addBlock(genesis);
    }
    
    @GetMapping("/")
    public String index(){
        return "redirect:/balance/walletA";
    }
    
    @GetMapping("/balance/{wallet}")
    public String getBalance(@PathVariable("wallet") String wallet, Model model) {
        model.addAttribute("wallet", wallet);
        System.out.println(" geeft ");
        if(wallet.equalsIgnoreCase("walletA")){
            model.addAttribute("balance", walletA.getBalance());
            System.out.println(" geeft"+ walletA.getBalance());
        } else if(wallet.equalsIgnoreCase("walletB")){
            model.addAttribute("balance", walletB.getBalance());
            System.out.println(" geeft" + walletB.getBalance());
        }
        return "balance";
    }

    @GetMapping("/transaction")
    public String getTrans(){
        return "transaction";
    }
    @PostMapping("/transaction")
    public String setTrans(@RequestParam("wallet1") String wallet1,
                            @RequestParam("wallet2") String wallet2,
                            @RequestParam("amount") Float amount) {
        Block block = new Block();
        block.setPreviousHash(bChain.getLastHash());

        try {
            if(wallet1.equals("walletA") && wallet2.equals("walletB")){
                block.addTransaction(walletA.sendFunds(walletB.getPublicKey(), amount), bChain);
            }
            else if(wallet1.equals("walletB") && wallet2.equals("walletA")){
                block.addTransaction(walletB.sendFunds(walletA.getPublicKey(), amount), bChain);
            }
            else {
                block.addTransaction(walletA.sendFunds(walletA.getPublicKey(), amount), bChain);
            }
        } catch (Exception e) { }

        //System.out.println(wallet1 + " geeft "+ amount + " aan "+ wallet2);
        bChain.addBlock(block);
        return "balance";
    }
}