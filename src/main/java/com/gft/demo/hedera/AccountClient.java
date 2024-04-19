package com.gft.demo.hedera;

import com.hedera.hashgraph.sdk.*;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

  @SneakyThrows
  public AccountCred createAccount(int balance) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
    var privateKey = PrivateKey.generateED25519();
    var publicKey = privateKey.getPublicKey();
    AccountCreateTransaction transaction = new AccountCreateTransaction()
      .setKey(publicKey)
      .setInitialBalance(Hbar.fromTinybars(balance));

    findMethod(transaction, "mergeFromClient", Client.class).invoke(transaction, client);
    findMethod(transaction, "onExecute", Client.class).invoke(transaction, client);
    findMethod(transaction, "checkNodeAccountIds").invoke(transaction);
    findMethod(transaction, "setNodesFromNodeAccountIds", Client.class).invoke(transaction, client);

//    mergeFromClient(client);
//    onExecute(client);
//    checkNodeAccountIds();
//    setNodesFromNodeAccountIds(client);
//    @Nullable Network network, int attempt, Duration grpcDeadline
    //            GrpcRequest grpcRequest = new GrpcRequest(client.network, attempt, currentTimeout);
    Class grpcRequestClass = findNested(transaction, "GrpcRequest");
    Class<?> networkClass = Class.forName("com.hedera.hashgraph.sdk.Network");
    System.out.println(grpcRequestClass);
    System.out.println(networkClass);

    Object network = findField(client, "network");
    System.out.println(network);

//    System.out.println(findNested);
    Constructor declaredConstructor1 = grpcRequestClass.getDeclaredConstructors()[0];
    declaredConstructor1.setAccessible(true);
    Object o = declaredConstructor1.newInstance(transaction,network, 1, client.getRequestTimeout());
//    Constructor declaredConstructor = grpcRequestClass.getDeclaredConstructor(Executable.class, networkClass, int.class, Duration.class);
//    Object o = declaredConstructor.newInstance();
//    System.out.println(o);
    var newAccountTx = transaction.execute(client);
    var newAccountId = newAccountTx.getReceipt(client).accountId;
    return new AccountCred(newAccountId, privateKey);
  }

  @SneakyThrows
  private static Method findMethod(Object object, String name, Class<?>... parameterTypes) {
    Class clazz = object.getClass();
    while (true) {
      try {
        Method method = clazz.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method;
      } catch (NoSuchMethodException | SecurityException e) {
        clazz = clazz.getSuperclass();
        if (clazz == null) {
          throw e;
        }
      }
    }
  }

  @SneakyThrows
  private static Class findNested(Object object, String name) {
    Class clazz = object.getClass();
    while (true) {
      for (Class declaredClass : clazz.getDeclaredClasses()) {
        if (declaredClass.getName().contains(name))
          return declaredClass;
      }
      clazz = clazz.getSuperclass();
      if (clazz == null) {
        throw null;
      }
    }
  }

  @SneakyThrows
  private static void invoke(Method method, Object obj, Object... args) {
    method.invoke(obj, args);
  }

  @SneakyThrows
  private static Object findField(Object object, String name){
    Field declaredField = object.getClass().getDeclaredField(name);
    declaredField.setAccessible(true);
    return declaredField.get(object);
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
