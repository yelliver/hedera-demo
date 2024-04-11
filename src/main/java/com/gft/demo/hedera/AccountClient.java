package com.gft.demo.hedera;

import com.hedera.hashgraph.sdk.*;

import java.util.Collections;
import java.util.concurrent.TimeoutException;

public class AccountClient implements AutoCloseable {
    private final AccountId mainAccountId;
    private final PrivateKey mainPrivateKey;
    private final Client client = Client.forTestnet();

    public AccountClient(AccountId mainAccountId, PrivateKey mainPrivateKey) {
        this.mainAccountId = mainAccountId;
        this.mainPrivateKey = mainPrivateKey;

        client.setOperator(mainAccountId, mainPrivateKey);
        client.setDefaultMaxTransactionFee(new Hbar(100));
        client.setMaxQueryPayment(new Hbar(50));
    }

    public static AccountClient fromCred(AccountCred accountCred) {
        return new AccountClient(accountCred.getAccountId(), accountCred.getPrivateKey());
    }

    public AccountBalance balanceQuery() throws PrecheckStatusException, TimeoutException {
        return new AccountBalanceQuery()
                .setAccountId(mainAccountId)
                .execute(client);
    }

    public AccountBalance balanceQuery(AccountId accountId) throws PrecheckStatusException, TimeoutException {
        return new AccountBalanceQuery()
                .setAccountId(accountId)
                .execute(client);
    }

    public AccountCred createAccount(int balance) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        var privateKey = PrivateKey.generateED25519();
        var publicKey = privateKey.getPublicKey();
        var newAccountTx = new AccountCreateTransaction()
                .setKey(publicKey)
                .setInitialBalance(Hbar.fromTinybars(balance))
                .execute(client);
        var newAccountId = newAccountTx.getReceipt(client).accountId;
        return new AccountCred(newAccountId, privateKey);
    }

    public Status sendTo(AccountId receiver, long amount) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        var transferTx = new TransferTransaction()
                .addHbarTransfer(this.mainAccountId, Hbar.fromTinybars(-amount))
                .addHbarTransfer(receiver, Hbar.fromTinybars(amount))
                .execute(client);
        return transferTx.getReceipt(client).status;
    }

    public Status sendTokenTo(AccountId receiver, TokenId tokenId, long amount) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        var transferTx = new TransferTransaction()
                .addTokenTransfer(tokenId, this.mainAccountId, -amount)
                .addTokenTransfer(tokenId, receiver, amount)
                .execute(client);
        return transferTx.getReceipt(client).status;
    }

    public TokenInfo tokenInfo(TokenId tokenId) throws PrecheckStatusException, TimeoutException {
        return new TokenInfoQuery().setTokenId(tokenId).execute(client);
    }

    public Status associateToken(AccountCred accountCred, TokenId tokenId) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        var transaction = new TokenAssociateTransaction()
                .setAccountId(accountCred.getAccountId())
                .setTokenIds(Collections.singletonList(tokenId));
        var txResponse = transaction.freezeWith(client).sign(accountCred.getPrivateKey()).execute(client);
        var receipt = txResponse.getReceipt(client);
        return receipt.status;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
