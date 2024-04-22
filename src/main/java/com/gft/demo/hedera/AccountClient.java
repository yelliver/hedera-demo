package com.gft.demo.hedera;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.proto.Transaction;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static com.hedera.hashgraph.sdk.proto.Transaction.*;

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
  public void createAccount(int balance) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
    var privateKey = PrivateKey.generateED25519();
    var publicKey = privateKey.getPublicKey();
    AccountCreateTransaction transaction = new AccountCreateTransaction()
      .setKey(publicKey)
      .setInitialBalance(Hbar.fromTinybars(balance));

    setField(transaction, "logger", client.getLogger());
    findMethod(transaction, "mergeFromClient", Client.class).invoke(transaction, client);
    findMethod(transaction, "onExecute", Client.class).invoke(transaction, client);
    findMethod(transaction, "checkNodeAccountIds").invoke(transaction);
    findMethod(transaction, "setNodesFromNodeAccountIds", Client.class).invoke(transaction, client);

    var grpcRequest = createGrpcRequest(transaction);
    Transaction request = (Transaction) getField(grpcRequest, "request");
    byte[] signedBytes = request.getSignedTransactionBytes().toByteArray();
    System.out.println(Arrays.toString(signedBytes));


//    ArrayList list = new ArrayList();
//    Transaction build = newBuilder()
//      .setSignedTransactionBytes(ByteString.copyFrom(signedBytes))
//      .build();
//    list.add(build);
//    setField(transaction2, "outerTransactions", list);
//    var o = getField(transaction2, "outerTransactions");
//    List o1 = (List) o;
//    o1.add("test");
//    System.out.println(o1);
//
//
    AccountCreateTransaction transaction2 = new AccountCreateTransaction()
      .setKey(publicKey)
      .setInitialBalance(Hbar.fromTinybars(balance));

    var grpcRequest2 = createGrpcRequest(transaction);

//
//    var grpcRequest2 = grpcRequestConstructor.newInstance(transaction, getField(client, "network"), 1, client.getRequestTimeout());
//    var o = getField(transaction2, "blockingUnaryCall");
//    ((Function) o).apply(grpcRequest2);
//
//    var mapResponse = findMethod(grpcRequest2, "mapResponse");
//    Object invoke = mapResponse.invoke(grpcRequest);
//    System.out.println(invoke);
//    System.out.println(o);
//    transaction.execute(client);

//    var newAccountTx = transaction.executeX(client, client.getRequestTimeout());
//    System.out.println("===>"+newAccountTx.getSignedTransactionBytes());
//    var newAccountId = newAccountTx.getReceipt(client).accountId;
//    return new AccountCred(newAccountId, privateKey);
  }

  private Object createGrpcRequest(AccountCreateTransaction transaction) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
    var grpcRequestClass = findNestedClass(transaction, "GrpcRequest");
    var networkClass = Class.forName("com.hedera.hashgraph.sdk.Network");
    var grpcRequestConstructor = grpcRequestClass.getDeclaredConstructors()[0];
    grpcRequestConstructor.setAccessible(true);
    var grpcRequest = grpcRequestConstructor.newInstance(transaction, getField(client, "network"), 1, client.getRequestTimeout());
    return grpcRequest;
  }

  @SneakyThrows
  private static Object getField(Object object, String name) {
    Field field = findField(object, name);
    return field.get(object);
  }

  @SneakyThrows
  private static void setField(Object object, String name, Object value) {
    Field field = findField(object, name);
    field.set(object, value);
  }

  @SneakyThrows
  private static Field findField(Object object, String name) {
    Class clazz = object.getClass();
    while (true) {
      try {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
      } catch (NoSuchFieldException | SecurityException e) {
        clazz = clazz.getSuperclass();
        if (clazz == null) {
          throw e;
        }
      }
    }
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
  private static void invoke(Method method, Object obj, Object... args) {
    method.invoke(obj, args);
  }

  @SneakyThrows
  private static Class findNestedClass(Object object, String name) {
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
