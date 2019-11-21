package io.daniel.controller;


import io.daniel.service.AccountService;
import io.daniel.service.BankService;
import io.daniel.utility.JsonUtility;
import io.daniel.dto.Transfer;
import spark.ResponseTransformer;
import spark.Spark;

import java.math.BigDecimal;

import static io.daniel.utility.JsonUtility.convertFromJson;

public class BankController {

    private BankService bankService = BankService.getInstance();
    private AccountService accountService = AccountService.getInstance();

    private static BankController instance;

    private BankController() {
        createAccount();
        getAccount();
        transfer();
        getAllAccounts();
    }

    public static BankController getInstance() {
        if (instance == null) {
            synchronized (BankController.class) {
                if (instance == null) {
                    instance = new BankController();
                }
            }
        }
        return instance;
    }


    private static final String ACCOUNT_PATH = "account";

    private void transfer() {
        Spark.post("bank/transfer", (req, res) -> {
            Transfer transfer = convertFromJson(req.body(), Transfer.class);
            bankService.transferMoney(transfer.getIdAccountFrom(), transfer.getIdAccountTo(), transfer.getAmount());
            return true;
        });
    }

    private void getAccount() {

        Spark.get(ACCOUNT_PATH + "/:id", (req, res) -> accountService.getAccount(Integer.parseInt(req.params("id"))), json());
    }

    private void getAllAccounts() {
        Spark.get(ACCOUNT_PATH, (req, res) -> accountService.getAllAccounts(), json());
    }

    private void createAccount() {
        Spark.post(ACCOUNT_PATH, (req, res) -> {
            String amount = req.queryParams("amount");
            return accountService.createNewAccount(new BigDecimal(amount));
        });
    }


    public static ResponseTransformer json() {
        return JsonUtility::convertToJson;
    }
}
