// @ts-ignore
const API_URL: string = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

interface Game {
  id: number;
  name: string;
  description: string;
  price: number;
  genre: string;
  available: boolean;
}

async function loadGames(): Promise<void> {
  const status = document.querySelector<HTMLParagraphElement>("#status")!;
  const list = document.querySelector<HTMLUListElement>("#games")!;

  try {
    const response = await fetch(`${API_URL}/api/games`);
    if (!response.ok) {
      throw new Error(`Server responded with ${response.status}`);
    }

    const games: Game[] = await response.json();

    status.textContent = games.length === 0 ? "No games yet." : `${games.length} game(s)`;
    list.replaceChildren(
        ...games.map((game) => {
          const item = document.createElement("li");
          item.textContent = `${game.name} — ${game.price} (${game.genre})`;
          return item;
        }),
    );
  } catch (error) {
    status.textContent = `Cannot reach the API: ${String(error)}`;
  }
}

window.addEventListener("DOMContentLoaded", loadGames);