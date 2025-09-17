JVM Download:
https://docs.aws.amazon.com/corretto/latest/corretto-24-ug/downloads-list.html

Postgres version 17.6
Make root account postgres have password postgres
Create two databases scroogebank and scroogebank_test

With JAVA_HOME set to at least jkd17:
.\gradlew test to run the tests
.\gradlew bootRun to run the application

By default the application will delete/recreate the database on startup. To prevent that change
`spring.jpa.hibernate.ddl-auto=create` to `spring.jpa.hibernate.ddl-auto=create-only` after dropping the database.
The app defaults to port 9090 to change that set `server.port=8090`

Both in `src/main/resources/application.properties`
Swagger UI: http://localhost:9090/swagger-ui/index.html

Extra user stories:
* Can only close a loan that is paid off and can't pay more than the loan amount
  * To prevent the user from creating issues only allow paying up to the max amount of the loan and only then can the account be closed
* get a table of accounts for a user
  * This is to allow a user to track old accounts and check their balance.
* Added a create user endpoint
  * Allow creating either operators or bank users to avoid needing to manually create them in the database
* Loose money parsing - Money must be positive and only have an optional two digits (after a period) for cents and a leading optional dollar sign.
  * Looser input handling allows for easier use by users but tightly constrains input to only valid values 

 