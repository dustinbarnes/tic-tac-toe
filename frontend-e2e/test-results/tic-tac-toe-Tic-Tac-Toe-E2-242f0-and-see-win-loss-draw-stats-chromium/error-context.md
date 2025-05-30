# Test info

- Name: Tic-Tac-Toe E2E >> User can create a game, join as another user, play, and see win/loss/draw stats
- Location: /Users/dustin/ws/tic-tac-toe/frontend-e2e/src/tic-tac-toe.spec.ts:6:3

# Error details

```
Error: locator.textContent: Unexpected token " " while parsing css selector "Game created. Share the ID to join:". Did you mean to CSS.escape it?
Call log:
  - waiting for Game created. Share the ID to join:

    at /Users/dustin/ws/tic-tac-toe/frontend-e2e/src/tic-tac-toe.spec.ts:14:78
```

# Page snapshot

```yaml
- heading "Tic-Tac-Toe" [level=1]
- paragraph: "Game ID: a62c4779-6731-4f50-8d95-111babd585eb"
- paragraph: "Status: WAITING"
- table:
  - rowgroup:
    - row:
      - cell
      - cell
      - cell
    - row:
      - cell
      - cell
      - cell
    - row:
      - cell
      - cell
      - cell
- text: "Players:"
- list:
  - listitem: "X: (waiting...)"
  - listitem: "O: (waiting...)"
- text: You are Alice (X)
- button "Refresh"
- button "Back"
- paragraph: "Game created. Share the ID to join: a62c4779-6731-4f50-8d95-111babd585eb"
```

# Test source

```ts
   1 | import { test, expect } from '@playwright/test';
   2 |
   3 | const BASE_URL = '/';
   4 |
   5 | test.describe('Tic-Tac-Toe E2E', () => {
   6 |   test('User can create a game, join as another user, play, and see win/loss/draw stats', async ({ page, browser }) => {
   7 |     // User 1: Create game and join as Alice
   8 |     await page.goto(BASE_URL);
   9 |     await page.fill('input[placeholder="Enter your username"]', 'Alice');
  10 |     await page.click('button[type="submit"]');
  11 |     await page.click('button:has-text("Create New Game")');
  12 |     await page.pause();
  13 |     await expect(page.locator('p.message:has-text("Game created")')).toBeVisible();
> 14 |     const gameId = await page.locator('Game created. Share the ID to join:').textContent();
     |                                                                              ^ Error: locator.textContent: Unexpected token " " while parsing css selector "Game created. Share the ID to join:". Did you mean to CSS.escape it?
  15 |     const idMatch = gameId?.match(/([\w-]{36})/);
  16 |     expect(idMatch).not.toBeNull();
  17 |     const id = idMatch![1];
  18 |     // Wait for game to be ready
  19 |     await expect(page.locator('text=Game ID:')).toContainText(id);
  20 |     // Open new browser context for Bob
  21 |     const context2 = await browser.newContext();
  22 |     const page2 = await context2.newPage();
  23 |     await page2.goto(BASE_URL);
  24 |     await page2.fill('input[placeholder="Enter your username"]', 'Bob');
  25 |     await page2.click('button[type="submit"]');
  26 |     // Bob joins Alice's game
  27 |     await page2.click(`button:has-text("Join Alice's Game (${id})")`);
  28 |     await expect(page2.locator('text=You are Bob (O)')).toBeVisible();
  29 |     // Play a full game: X (Alice) moves first
  30 |     // Alice's move
  31 |     await page.click('td:has-text("")'); // First empty cell
  32 |     // Bob's move
  33 |     await page2.click('td:has-text("")'); // Next empty cell
  34 |     // Alice's move
  35 |     await page.click('td:has-text("")');
  36 |     // Bob's move
  37 |     await page2.click('td:has-text("")');
  38 |     // Alice's move (should win or draw)
  39 |     await page.click('td:has-text("")');
  40 |     // Wait for game to finish
  41 |     await expect(page.locator('text=Status:')).not.toContainText('IN_PROGRESS');
  42 |     // Check stats for Alice
  43 |     await page.click('button:has-text("Back")');
  44 |     await expect(page.locator('b:has-text("Your Record")')).toBeVisible();
  45 |     // Check that at least one win or draw is present
  46 |     const wins = await page.locator('div:has-text("Wins:")').textContent();
  47 |     const draws = await page.locator('div:has-text("Draws:")').textContent();
  48 |     expect(Number(wins?.replace(/\D/g, '')) + Number(draws?.replace(/\D/g, ''))).toBeGreaterThanOrEqual(1);
  49 |     // Check stats for Bob
  50 |     await page2.click('button:has-text("Back")');
  51 |     await expect(page2.locator('b:has-text("Your Record")')).toBeVisible();
  52 |     const losses = await page2.locator('div:has-text("Losses:")').textContent();
  53 |     expect(Number(losses?.replace(/\D/g, ''))).toBeGreaterThanOrEqual(1);
  54 |     await context2.close();
  55 |   });
  56 |
  57 |   test('User can see finished games and view their state', async ({ page }) => {
  58 |     await page.goto(BASE_URL);
  59 |     await page.fill('input[placeholder="Enter your username"]', 'Viewer');
  60 |     await page.click('button[type="submit"]');
  61 |     // Wait for finished games list
  62 |     await expect(page.locator('h2:has-text("Recently Finished Games")')).toBeVisible();
  63 |     // If there are finished games, click the first one
  64 |     const finished = page.locator('ul >> text=Winner:');
  65 |     if (await finished.count() > 0) {
  66 |       await finished.first().click();
  67 |       await expect(page.locator('text=Status:')).not.toContainText('IN_PROGRESS');
  68 |       await expect(page.locator('table')).toBeVisible();
  69 |     }
  70 |   });
  71 | });
  72 |
```