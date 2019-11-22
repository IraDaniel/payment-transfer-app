package io.daniel.controller;


import io.daniel.dto.Transfer;
import io.daniel.model.Account;
import io.daniel.service.AccountService;
import io.daniel.service.impl.AccountServiceImpl;
import io.daniel.service.impl.BankServiceImpl;
import io.daniel.utils.JsonUtils;
import spark.ResponseTransformer;
import spark.Spark;

import static io.daniel.utils.JsonUtils.convertFromJson;

public class BankController {

    private static BankServiceImpl bankService = BankServiceImpl.getInstance();
    private static AccountService accountService = AccountServiceImpl.getInstance();
    private static final String ACCOUNT_PATH = "account";

    private static volatile BankController instance;

    private BankController() {
        initializeRouters();
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

    private static void initializeRouters() {
        createAccount();
        getAccount();
        transfer();
        getAllAccounts();
    }

    private static void transfer() {
        Spark.post("bank/transfer", (req, res) -> {
            Transfer transfer = convertFromJson(req.body(), Transfer.class);
            bankService.transferMoney(transfer.getIdAccountFrom(), transfer.getIdAccountTo(), transfer.getAmount());
            return true;
        });
    }

    private static void getAccount() {
        Spark.get(ACCOUNT_PATH + "/:id", (req, res) -> accountService.getAccount(Integer.parseInt(req.params("id"))), json());
    }

    private static void getAllAccounts() {
        Spark.get(ACCOUNT_PATH, (req, res) -> accountService.getAllAccounts(), json());
    }

    private static void createAccount() {
        Spark.post(ACCOUNT_PATH, (req, res) -> {
            Account account = convertFromJson(req.body(), Account.class);
            return accountService.createNewAccount(account);
        });
    }

    public static ResponseTransformer json() {
        return JsonUtils::convertToJson;
    }
}
