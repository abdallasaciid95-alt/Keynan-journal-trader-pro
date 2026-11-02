const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any trade", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.doc("users/user_1/trades/trade_1").get());
  await assertFails(unauthedDb.doc("users/user_1/trades/trade_1").set({
    userId: "user_1",
    accountId: "acc_1",
    instrument: "XAUUSD",
    date: "2026-11-02",
    entryTime: "10:00",
    direction: "BUY",
    entryPrice: 2750.50,
    exitPrice: 2760.50,
    lotSize: 1.0,
    result: "win",
    pnl: 1000.0,
    session: "london",
    strategy: "Breakout",
    createdAt: new Date()
  }));
});

test("Authenticated user can create valid trade in their own collection", async () => {
  const user1Db = testEnv.authenticatedContext("user_1").firestore();
  await assertSucceeds(user1Db.doc("users/user_1/trades/trade_1").set({
    userId: "user_1",
    accountId: "acc_1",
    instrument: "XAUUSD",
    date: "2026-11-02",
    entryTime: "10:00",
    direction: "BUY",
    entryPrice: 2750.50,
    exitPrice: 2760.50,
    lotSize: 1.0,
    result: "win",
    pnl: 1000.0,
    session: "london",
    strategy: "Breakout",
    createdAt: new Date()
  }));
});

test("Authenticated user cannot access another user's trades", async () => {
  const user1Db = testEnv.authenticatedContext("user_1").firestore();
  await assertSucceeds(user1Db.doc("users/user_1/trades/trade_1").set({
    userId: "user_1",
    accountId: "acc_1",
    instrument: "XAUUSD",
    date: "2026-11-02",
    entryTime: "10:00",
    direction: "BUY",
    entryPrice: 2750.50,
    exitPrice: 2760.50,
    lotSize: 1.0,
    result: "win",
    pnl: 1000.0,
    session: "london",
    strategy: "Breakout",
    createdAt: new Date()
  }));

  const user2Db = testEnv.authenticatedContext("user_2").firestore();
  await assertFails(user2Db.doc("users/user_1/trades/trade_1").get());
  await assertFails(user2Db.doc("users/user_1/trades/trade_1").delete());
});

test("Non-XAUUSD trades or invalid lot size are rejected", async () => {
  const user1Db = testEnv.authenticatedContext("user_1").firestore();
  await assertFails(user1Db.doc("users/user_1/trades/trade_inv1").set({
    userId: "user_1",
    accountId: "acc_1",
    instrument: "EURUSD", // Forbidden
    date: "2026-11-02",
    entryTime: "10:00",
    direction: "BUY",
    entryPrice: 1.08,
    exitPrice: 1.09,
    lotSize: 1.0,
    result: "win",
    pnl: 100.0,
    session: "london",
    strategy: "Breakout",
    createdAt: new Date()
  }));

  await assertFails(user1Db.doc("users/user_1/trades/trade_inv2").set({
    userId: "user_1",
    accountId: "acc_1",
    instrument: "XAUUSD",
    date: "2026-11-02",
    entryTime: "10:00",
    direction: "BUY",
    entryPrice: 2750.50,
    exitPrice: 2760.50,
    lotSize: -0.5, // Negative lot size forbidden
    result: "win",
    pnl: 1000.0,
    session: "london",
    strategy: "Breakout",
    createdAt: new Date()
  }));
});

test("Account creation and isolation", async () => {
  const user1Db = testEnv.authenticatedContext("user_1").firestore();
  await assertSucceeds(user1Db.doc("users/user_1/accounts/acc_1").set({
    userId: "user_1",
    name: "Funded 100k",
    type: "funded",
    startingBalance: 100000.0,
    status: "active",
    createdAt: new Date()
  }));

  const user2Db = testEnv.authenticatedContext("user_2").firestore();
  await assertFails(user2Db.doc("users/user_1/accounts/acc_1").get());
});
