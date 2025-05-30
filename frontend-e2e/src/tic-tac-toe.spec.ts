import { test, expect } from '@playwright/test';

const BASE_URL = '/';

test.describe('Tic-Tac-Toe E2E', () => {
  test('User can create a game, join as another user, play, and see win/loss/draw stats', async ({ page, browser }) => {
    // User 1: Create game and join as Alice
    await page.goto(BASE_URL);
    await page.fill('input[placeholder="Enter your username"]', 'Alice');
    await page.click('button[type="submit"]');
    await page.click('button:has-text("Create New Game")');
    await page.pause();
    await expect(page.locator('p.message:has-text("Game created")')).toBeVisible();
    const gameId = await page.locator('Game created. Share the ID to join:').textContent();
    const idMatch = gameId?.match(/([\w-]{36})/);
    expect(idMatch).not.toBeNull();
    const id = idMatch![1];
    // Wait for game to be ready
    await expect(page.locator('text=Game ID:')).toContainText(id);
    // Open new browser context for Bob
    const context2 = await browser.newContext();
    const page2 = await context2.newPage();
    await page2.goto(BASE_URL);
    await page2.fill('input[placeholder="Enter your username"]', 'Bob');
    await page2.click('button[type="submit"]');
    // Bob joins Alice's game
    await page2.click(`button:has-text("Join Alice's Game (${id})")`);
    await expect(page2.locator('text=You are Bob (O)')).toBeVisible();
    // Play a full game: X (Alice) moves first
    // Alice's move
    await page.click('td:has-text("")'); // First empty cell
    // Bob's move
    await page2.click('td:has-text("")'); // Next empty cell
    // Alice's move
    await page.click('td:has-text("")');
    // Bob's move
    await page2.click('td:has-text("")');
    // Alice's move (should win or draw)
    await page.click('td:has-text("")');
    // Wait for game to finish
    await expect(page.locator('text=Status:')).not.toContainText('IN_PROGRESS');
    // Check stats for Alice
    await page.click('button:has-text("Back")');
    await expect(page.locator('b:has-text("Your Record")')).toBeVisible();
    // Check that at least one win or draw is present
    const wins = await page.locator('div:has-text("Wins:")').textContent();
    const draws = await page.locator('div:has-text("Draws:")').textContent();
    expect(Number(wins?.replace(/\D/g, '')) + Number(draws?.replace(/\D/g, ''))).toBeGreaterThanOrEqual(1);
    // Check stats for Bob
    await page2.click('button:has-text("Back")');
    await expect(page2.locator('b:has-text("Your Record")')).toBeVisible();
    const losses = await page2.locator('div:has-text("Losses:")').textContent();
    expect(Number(losses?.replace(/\D/g, ''))).toBeGreaterThanOrEqual(1);
    await context2.close();
  });

  test('User can see finished games and view their state', async ({ page }) => {
    await page.goto(BASE_URL);
    await page.fill('input[placeholder="Enter your username"]', 'Viewer');
    await page.click('button[type="submit"]');
    // Wait for finished games list
    await expect(page.locator('h2:has-text("Recently Finished Games")')).toBeVisible();
    // If there are finished games, click the first one
    const finished = page.locator('ul >> text=Winner:');
    if (await finished.count() > 0) {
      await finished.first().click();
      await expect(page.locator('text=Status:')).not.toContainText('IN_PROGRESS');
      await expect(page.locator('table')).toBeVisible();
    }
  });
});
