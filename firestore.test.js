const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const MASTER_UID = "master_uid_789";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTimestamp(offsetMs = 2000) {
  return new Date(Date.now() - offsetMs);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
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

test("Unauthenticated requests are denied across all collections", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  const ts = pastTimestamp();

  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauth.collection("masters").where("visibility", "==", "public").get()
  );
  await assertFails(
    unauth.collection("services").where("visibility", "==", "public").get()
  );
  await assertFails(
    unauth.collection("bookings").where("clientUid", "==", ALICE_UID).get()
  );
  await assertFails(
    unauth.collection("bookings").doc("bk_1").set({
      clientUid: ALICE_UID,
      serviceName: "Manicure",
      serviceCategory: "Manicure",
      date: "20.11",
      timeSlot: "14:00",
      masterName: "Victoria",
      price: 45,
      clientName: "Alice",
      clientPhone: "+34600000001",
      paymentMethod: "studio_visit",
      status: "Подтверждена",
      ticketToken: "ABCDEF1234567890",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("User profile isolation: owner create/update succeeds; cross-user & immutable tampering fail", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastTimestamp(10000);

  const profilePayload = {
    userId: ALICE_UID,
    displayName: "Alice",
    role: "client",
    referralCode: "NLX-ALICE1",
    loyaltyPoints: 150,
    loyaltyLevel: "Silver",
    friendsReferred: 0,
    preferredSkin: "gaudi",
    preferredCurrency: "EUR",
    preferredLanguage: "ru",
    createdAt: created,
    updatedAt: created,
  };

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).set(profilePayload)
  );
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).get());

  // Cross-user read/write blocked
  await assertFails(bob.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bob.collection("users").doc(ALICE_UID).update({
      loyaltyPoints: 99999,
      updatedAt: pastTimestamp(),
    })
  );

  // Partial update by owner works
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).update({
      loyaltyPoints: 250,
      loyaltyLevel: "Silver",
      updatedAt: pastTimestamp(),
    })
  );

  // Immutable referralCode or createdAt mutation fails
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      referralCode: "NLX-HACKED",
      updatedAt: pastTimestamp(),
    })
  );
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      createdAt: pastTimestamp(),
      updatedAt: pastTimestamp(),
    })
  );

  // Future timestamp rejected
  await assertFails(
    bob.collection("users").doc(BOB_UID).set({
      ...profilePayload,
      userId: BOB_UID,
      referralCode: "NLX-BOB123",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: pastTimestamp(),
    })
  );
});

test("Bookings: scoped clientUid/masterUid access, terminal state lock, and no unfiltered lists", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const master = testEnv.authenticatedContext(MASTER_UID).firestore();
  const ts = pastTimestamp(10000);

  const bookingPayload = {
    clientUid: ALICE_UID,
    masterUid: MASTER_UID,
    serviceName: "Аппаратный маникюр",
    serviceCategory: "Маникюр",
    date: "22.11 (Суббота)",
    timeSlot: "15:00",
    masterName: "Виктория",
    price: 45,
    clientName: "Alice",
    clientPhone: "+34611111111",
    paymentMethod: "studio_visit",
    status: "Подтверждена",
    ticketToken: "A1B2C3D4E5F67890",
    createdAt: ts,
    updatedAt: ts,
  };

  await assertSucceeds(
    alice.collection("bookings").doc("bk_100").set(bookingPayload)
  );

  // Scoped queries succeed for client & master
  await assertSucceeds(
    alice.collection("bookings").where("clientUid", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    master.collection("bookings").where("masterUid", "==", MASTER_UID).get()
  );

  // Unfiltered list or third-party access fails
  await assertFails(bob.collection("bookings").get());
  await assertFails(bob.collection("bookings").doc("bk_100").get());
  await assertFails(
    bob.collection("bookings").doc("bk_100").update({
      status: "Отменена",
      updatedAt: pastTimestamp(),
    })
  );

  // Client reschedules confirmed booking -> allowed
  await assertSucceeds(
    alice.collection("bookings").doc("bk_100").update({
      date: "23.11 (Воскресенье)",
      timeSlot: "16:30",
      updatedAt: pastTimestamp(),
    })
  );

  // Master completes booking -> allowed
  await assertSucceeds(
    master.collection("bookings").doc("bk_100").update({
      status: "Выполнена",
      updatedAt: pastTimestamp(),
    })
  );

  // Terminal state lock: subsequent status or schedule update fails
  await assertFails(
    alice.collection("bookings").doc("bk_100").update({
      status: "Отменена",
      updatedAt: pastTimestamp(),
    })
  );
  await assertFails(
    alice.collection("bookings").doc("bk_100").update({
      date: "24.11 (Понедельник)",
      timeSlot: "12:00",
      updatedAt: pastTimestamp(),
    })
  );
});

test("Public studio catalog (masters, services, nailArts, reviews) enforces visibility query and owner mutation", async () => {
  const master = testEnv.authenticatedContext(MASTER_UID).firestore();
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp(5000);

  await assertSucceeds(
    master.collection("services").doc("svc_1").set({
      userId: MASTER_UID,
      visibility: "public",
      name: "SMART Педикюр",
      category: "Педикюр",
      price: 55.0,
      durationMin: 90,
      description: "Профессиональный педикюр",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    alice.collection("services").where("visibility", "==", "public").get()
  );
  // Unfiltered list rejected
  await assertFails(alice.collection("services").get());
  // Non-owner cannot overwrite or delete service
  await assertFails(
    alice.collection("services").doc("svc_1").update({
      price: 0,
      updatedAt: pastTimestamp(),
    })
  );
  await assertFails(alice.collection("services").doc("svc_1").delete());

  // NailArt likes/views atomic increment by another signed-in user works, tampering extraPrice fails
  await assertSucceeds(
    master.collection("nailArts").doc("art_1").set({
      userId: MASTER_UID,
      visibility: "public",
      title: "Gaudí Mosaic French",
      masterName: "Виктория",
      complexity: "Hard",
      extraPrice: 25.0,
      imageUrl: "https://example.com/art.jpg",
      likes: 0,
      views: 0,
      description: "Мозаичный френч",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    alice.collection("nailArts").doc("art_1").update({
      likes: 1,
      updatedAt: pastTimestamp(),
    })
  );

  await assertFails(
    alice.collection("nailArts").doc("art_1").update({
      likes: 100,
      updatedAt: pastTimestamp(),
    })
  );
  await assertFails(
    alice.collection("nailArts").doc("art_1").update({
      extraPrice: 0,
      updatedAt: pastTimestamp(),
    })
  );
});

test("Referral codes: atomic cross-user redemption succeeds once; self-redemption and listing fail", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTimestamp(5000);

  await assertSucceeds(
    alice.collection("referralCodes").doc("NLX-ALICE1").set({
      ownerUid: ALICE_UID,
      ownerName: "Alice",
      redemptionCount: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Listing all codes denied
  await assertFails(bob.collection("referralCodes").get());

  // Self-redemption blocked
  await assertFails(
    alice
      .collection("referralCodes")
      .doc("NLX-ALICE1")
      .collection("redemptions")
      .doc(ALICE_UID)
      .set({
        claimerUid: ALICE_UID,
        claimerName: "Alice",
        code: "NLX-ALICE1",
        createdAt: pastTimestamp(),
      })
  );

  // Non-atomic increment without redemption doc fails
  await assertFails(
    bob.collection("referralCodes").doc("NLX-ALICE1").update({
      redemptionCount: 1,
      lastRedeemedBy: BOB_UID,
      updatedAt: pastTimestamp(),
    })
  );

  // Atomic batch redemption by Bob succeeds
  const batch = bob.batch();
  const codeRef = bob.collection("referralCodes").doc("NLX-ALICE1");
  const redemptionRef = codeRef.collection("redemptions").doc(BOB_UID);
  batch.set(redemptionRef, {
    claimerUid: BOB_UID,
    claimerName: "Bob",
    code: "NLX-ALICE1",
    createdAt: pastTimestamp(),
  });
  batch.update(codeRef, {
    redemptionCount: 1,
    lastRedeemedBy: BOB_UID,
    updatedAt: pastTimestamp(),
  });
  await assertSucceeds(batch.commit());
});

test("Loyalty transactions: owner create/list allowed, immutable update denied, cross-user denied", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTimestamp(3000);

  const txRef = alice.collection("users").doc(ALICE_UID).collection("loyalty_transactions").doc("tx_1");
  await assertSucceeds(
    txRef.set({
      userId: ALICE_UID,
      title: "Визит в студию",
      points: 150,
      createdAt: ts,
    })
  );

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("loyalty_transactions").get()
  );

  // Immutable ledger: update forbidden even for owner
  await assertFails(
    txRef.update({
      points: 9999,
    })
  );

  // Cross-user read/write blocked
  await assertFails(
    bob.collection("users").doc(ALICE_UID).collection("loyalty_transactions").get()
  );
  await assertFails(
    bob.collection("users").doc(ALICE_UID).collection("loyalty_transactions").doc("tx_2").set({
      userId: BOB_UID,
      title: "Hack",
      points: 500,
      createdAt: ts,
    })
  );
});

test("Masters & Reviews: public visibility queries work, owner edit enforced, invalid rating rejected", async () => {
  const master = testEnv.authenticatedContext(MASTER_UID).firestore();
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTimestamp(4000);

  await assertSucceeds(
    master.collection("masters").doc("master_1").set({
      userId: MASTER_UID,
      visibility: "public",
      name: "Victoria",
      address: "Calella, Moragas 74",
      workingSchedule: "10:00 - 20:00",
      experienceYears: 7,
      languages: ["Русский", "Español"],
      instagramUsername: "nailix.studio",
      currency: "EUR",
      ratingAvg: 5.0,
      ratingCount: 1,
      lat: 41.6152,
      lon: 2.6624,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    alice.collection("masters").where("visibility", "==", "public").get()
  );
  await assertSucceeds(
    master.collection("masters").doc("master_1").update({
      workingSchedule: "09:00 - 21:00",
      updatedAt: pastTimestamp(),
    })
  );
  await assertFails(
    alice.collection("masters").doc("master_1").update({
      name: "Hijacked",
      updatedAt: pastTimestamp(),
    })
  );

  // Valid review creation & owner edit
  await assertSucceeds(
    alice.collection("reviews").doc("rev_1").set({
      authorUid: ALICE_UID,
      visibility: "public",
      targetId: "master_1",
      targetType: "master",
      authorName: "Alice",
      rating: 5,
      comment: "Безупречный френч!",
      dateStr: "20.11",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    bob.collection("reviews").where("visibility", "==", "public").get()
  );
  await assertFails(
    bob.collection("reviews").doc("rev_1").update({
      rating: 1,
      updatedAt: pastTimestamp(),
    })
  );
  // Out-of-bounds rating rejected
  await assertFails(
    bob.collection("reviews").doc("rev_bad").set({
      authorUid: BOB_UID,
      visibility: "public",
      targetId: "master_1",
      targetType: "master",
      authorName: "Bob",
      rating: 6,
      comment: "Too high",
      dateStr: "20.11",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

