package io.daniel;

import io.daniel.controller.BankController;

/**
 * Created by Ira on 21.11.2019.
 */
public class Main {

    public static void main(String[] args) {
        initializeSparkRouters();
    }

    private static void initializeSparkRouters() {
        BankController.getInstance();
    }
}
