package com.gft.demo.hedera;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Data;

@Data
public final class AccountCred {
    private final AccountId accountId;
    private final PrivateKey privateKey;

    public static AccountCred fromString(String accountIdInput, String privateKeyInput) {
        return new AccountCred(
                AccountId.fromString(accountIdInput),
                PrivateKey.fromString(privateKeyInput)
        );
    }

    public static AccountCred fromEnv(String accountIdInput, String privateKeyInput) {
        var env = Dotenv.load();
        return AccountCred.fromString(env.get(accountIdInput), env.get(privateKeyInput));
    }
}
