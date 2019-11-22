package io.daniel.service;

import io.daniel.dto.Transfer;
import io.daniel.service.impl.BankServiceImpl;

/**
 * Created by Ira on 21.11.2019.
 */
public class TestThread implements Runnable {
    private BankServiceImpl bankService;
    private Transfer transfer;

    public TestThread(BankServiceImpl bankService, Transfer transfer) {
        this.bankService = bankService;
        this.transfer = transfer;
    }

    public void run() {
        System.out.println(Thread.currentThread().getName() + " Start.");
        processCommand();
        System.out.println(Thread.currentThread().getName() + " End.");
    }

    private void processCommand() {
        try {
            bankService.transferMoney(transfer.getIdAccountFrom(), transfer.getIdAccountTo(), transfer.getAmount());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
