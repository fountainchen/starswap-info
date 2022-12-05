package org.starcoin.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.starcoin.bean.TransactionEntity;
import org.starcoin.bean.UserTransactionEntity;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveScanClientTest {

    MoveScanClient client = new MoveScanClient("http", "localhost", 8700);

    @Test
    void getSwapTxn() throws IOException {
        List<TransactionEntity> transactionEntityList = client.getSwapTxn(9936307, 10);
        assertTrue(transactionEntityList.size() > 0);
    }

    @Test
    void getUserTxn() throws IOException {
        UserTransactionEntity userTransactionEntity = client.getUserTxn("0xae2064f4d41d24c88429083ead58a90d24cf9bb5691b5bbeb47222e523173fbf");
        assertNotNull(userTransactionEntity);
    }
}