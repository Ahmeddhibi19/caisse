import { readFileSync } from 'node:fs';
import { after, before, beforeEach, describe, test } from 'node:test';
import { assertFails, assertSucceeds, initializeTestEnvironment } from '@firebase/rules-unit-testing';

const STORE = 'stores/demo-store';
const TIMESTAMP = { '.sv': 'timestamp' };

let env;

before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-caisse',
    database: { rules: readFileSync(new URL('../database.rules.json', import.meta.url), 'utf8') },
  });
});

after(async () => {
  await env.cleanup();
});

beforeEach(async () => {
  await env.clearDatabase();
  await env.withSecurityRulesDisabled(async (context) => {
    await context.database().ref(`${STORE}/registers/C01`).set({ uid: 'alice', model: 'Pixel Tablet', enrolledAt: 1 });
    await context.database().ref(`${STORE}/registers/C02`).set({ uid: 'bob', model: 'Pixel Tablet', enrolledAt: 1 });
  });
});

const db = (uid) => env.authenticatedContext(uid).database();

const sale = (overrides = {}) => ({
  ticketNumber: 'C01-000001',
  registerKey: 'C01',
  sequence: 1,
  totalCents: 320,
  currency: 'EUR',
  createdAt: 1_700_000_000_000,
  syncedAt: TIMESTAMP,
  uid: 'alice',
  lines: [{ productId: 'espresso', name: 'Espresso', unitPriceCents: 160, quantity: 2 }],
  ...overrides,
});

// Same shape as the app's multi-path update: the sale and its ticket index in one atomic write.
const pushSale = (database, saleId, value) =>
  database.ref(STORE).update({
    [`sales/${saleId}`]: value,
    [`ticketIndex/${value.ticketNumber}`]: saleId,
  });

describe('register counter', () => {
  test('starts at 1 and only moves forward by one', async () => {
    await assertSucceeds(db('alice').ref(`${STORE}/registerCounter`).set(1));
    await assertSucceeds(db('bob').ref(`${STORE}/registerCounter`).set(2));
    await assertFails(db('bob').ref(`${STORE}/registerCounter`).set(4));
    await assertFails(db('bob').ref(`${STORE}/registerCounter`).set(1));
  });

  test('requires an authenticated user', async () => {
    await assertFails(env.unauthenticatedContext().database().ref(`${STORE}/registerCounter`).set(1));
  });
});

describe('registers', () => {
  test('a free key can be claimed for oneself', async () => {
    await assertSucceeds(
      db('carol').ref(`${STORE}/registers/C03`).set({ uid: 'carol', model: 'Pixel', enrolledAt: TIMESTAMP }),
    );
  });

  test('a key is never handed to a second device', async () => {
    await assertFails(db('carol').ref(`${STORE}/registers/C01`).set({ uid: 'carol', model: 'Pixel', enrolledAt: TIMESTAMP }));
  });

  test('a key cannot be claimed for someone else', async () => {
    await assertFails(db('carol').ref(`${STORE}/registers/C03`).set({ uid: 'dave', model: 'Pixel', enrolledAt: TIMESTAMP }));
  });

  test('keys follow the C + two digits format', async () => {
    await assertFails(db('carol').ref(`${STORE}/registers/C3`).set({ uid: 'carol', model: 'Pixel', enrolledAt: TIMESTAMP }));
  });
});

describe('sales', () => {
  test('the owner of a register can push its sale and ticket index together', async () => {
    await assertSucceeds(pushSale(db('alice'), 'sale-1', sale()));
  });

  test('pushing the same sale again is accepted, so retries never duplicate', async () => {
    await assertSucceeds(pushSale(db('alice'), 'sale-1', sale()));
    await assertSucceeds(pushSale(db('alice'), 'sale-1', sale()));
  });

  test('a synced sale cannot be altered', async () => {
    await assertSucceeds(pushSale(db('alice'), 'sale-1', sale()));
    await assertFails(pushSale(db('alice'), 'sale-1', sale({ totalCents: 1 })));
  });

  test('a sale cannot be deleted', async () => {
    await assertSucceeds(pushSale(db('alice'), 'sale-1', sale()));
    await assertFails(db('alice').ref(`${STORE}/sales/sale-1`).remove());
  });

  test('a device cannot sell on a register it does not own', async () => {
    await assertFails(pushSale(db('bob'), 'sale-1', sale({ uid: 'bob' })));
  });

  test('a ticket number cannot be reused by another sale', async () => {
    await assertSucceeds(pushSale(db('alice'), 'sale-1', sale()));
    await assertFails(pushSale(db('alice'), 'sale-2', sale()));
  });

  test('a ticket number must belong to the register of the sale', async () => {
    await assertFails(pushSale(db('alice'), 'sale-1', sale({ ticketNumber: 'C02-000001' })));
  });

  test('unknown fields are rejected', async () => {
    await assertFails(pushSale(db('alice'), 'sale-1', sale({ discount: 100 })));
  });

  test('a sale needs at least one line', async () => {
    await assertFails(pushSale(db('alice'), 'sale-1', sale({ lines: null })));
  });

  test('a ticket number cannot be reserved without its sale', async () => {
    await assertFails(db('bob').ref(`${STORE}/ticketIndex/C02-000001`).set('ghost-sale'));
  });
});

describe('reads', () => {
  test('need an authenticated user', async () => {
    await assertFails(env.unauthenticatedContext().database().ref(STORE).get());
    await assertSucceeds(db('alice').ref(STORE).get());
  });
});
