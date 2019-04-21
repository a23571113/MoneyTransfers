# MoneyTransfers

To build the project, run:
```
gradle clean build
```

To start the server, run:
```
java -jar build/libs/MoneyTransfers-1.0.jar
```

Server's HTTP API is rather minimalistic:

```
POST hostname:8080/v1/accounts/?amount=<amount>
```
Creates new account with specified amount of money **amount**, where **amount** is a nonnegative big decimal number.
Returns 201 and id of newly created account in plaintext.
Returns 400 in case of malformed request.

```
GET hostname:8080/v1/accounts/<account>
```
Retrives current current amount of money associated with the **account**.
Returns 200 and the current amount of money in plaintext.
Returns 404 if there is no account with specified id.
Returns 400 in case of malformed request.

```
POST hostname:8080/v1/transfers/?sender=<sender>&receiver=<receive>r&amount=<amount>
```
Transfers specified amount of money **amount** from the **sender** to the **receiver**, where **amount** is a nonnegative big decimal number.
Returns 200 upon success.
Returns 404 if either **sender** or **receiver** doesn't exist.
Returns 400 in case of malformed request.
