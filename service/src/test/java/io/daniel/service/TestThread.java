package io.daniel.service;

/**
 * Created by Ira on 21.11.2019.
 */
public class TestThread implements Runnable {
    private BankService bankService;
    private TransferTest transferTest;

    public TestThread(BankService bankService, TransferTest transferTest) {
        this.bankService = bankService;
        this.transferTest = transferTest;
    }

    public void run() {
        System.out.println(Thread.currentThread().getName() + " Start.");
        processCommand();
        System.out.println(Thread.currentThread().getName() + " End.");
    }

    private void processCommand() {
        try {
            bankService.transfer(transferTest.from, transferTest.to, transferTest.amount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
