
import { useEffect, useMemo, useState } from "react";
import "./App.css";

const API_URL: string =
    import.meta.env.VITE_API_URL ?? "http://localhost:8080";

interface Game {
    id: number;
    name: string;
    description: string;
    price: number;
    genre: string;
    available: boolean;
}

function formatPrice(price: number): string {
    return Number(price).toFixed(2);
}

function getInitials(name: string): string {
    return name
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join("");
}

function getCoverStyle(genre: string): string {
    const value = genre.toLowerCase();

    if (value.includes("rpg")) return "cover--rpg";
    if (value.includes("action")) return "cover--action";
    if (value.includes("adventure")) return "cover--adventure";
    if (value.includes("strategy")) return "cover--strategy";
    if (value.includes("horror")) return "cover--horror";

    return "cover--default";
}

export default function App() {
    const [games, setGames] = useState<Game[]>([]);
    const [status, setStatus] = useState<
        "loading" | "success" | "error"
    >("loading");
    const [errorMessage, setErrorMessage] = useState("");
    const [search, setSearch] = useState("");
    const [showAvailableOnly, setShowAvailableOnly] = useState(false);

    async function loadGames() {
        setStatus("loading");
        setErrorMessage("");

        try {
            const response = await fetch(`${API_URL}/api/games`);

            if (!response.ok) {
                throw new Error(`Server responded with ${response.status}`);
            }

            const data: Game[] = await response.json();

            setGames(data);
            setStatus("success");
        } catch (error) {
            setErrorMessage(
                error instanceof Error ? error.message : String(error),
            );
            setStatus("error");
        }
    }

    useEffect(() => {
        void loadGames();
    }, []);

    const filteredGames = useMemo(() => {
        const query = search.trim().toLowerCase();

        return games.filter((game) => {
            const matchesSearch =
                game.name.toLowerCase().includes(query) ||
                game.genre.toLowerCase().includes(query) ||
                game.description.toLowerCase().includes(query);

            const matchesAvailability =
                !showAvailableOnly || game.available;

            return matchesSearch && matchesAvailability;
        });
    }, [games, search, showAvailableOnly]);

    return (
        <div className="app-shell">
            <aside className="sidebar">
                <a className="brand" href="#" aria-label="GameVault home">
                    <span className="brand-mark">G</span>
                    <span className="brand-name">
            Game<span>Vault</span>
          </span>
                </a>

                <div className="sidebar-label">WORKSPACE</div>

                <nav className="sidebar-nav" aria-label="Main navigation">
                    <button className="nav-item nav-item--active" type="button">
                        <span className="nav-icon">▦</span>
                        <span>Library</span>
                        <span className="nav-count">{games.length}</span>
                    </button>

                    <button
                        className="nav-item"
                        type="button"
                        onClick={() => {
                            setSearch("");
                            setShowAvailableOnly(false);
                        }}
                    >
                        <span className="nav-icon">⌕</span>
                        <span>Discover</span>
                    </button>
                </nav>

                <div className="sidebar-label sidebar-label--spaced">
                    YOUR COLLECTION
                </div>

                <button
                    className={`nav-item ${showAvailableOnly ? "nav-item--active" : ""}`}
                    type="button"
                    onClick={() => setShowAvailableOnly((value) => !value)}
                >
                    <span className="nav-icon">◈</span>
                    <span>Available games</span>
                </button>

                <div className="sidebar-bottom">
                    <div className="sidebar-status">
                        <span className={`status-dot ${status === "error" ? "status-dot--error" : ""}`} />
                        <span>
              {status === "loading"
                  ? "Connecting to API..."
                  : status === "error"
                      ? "API unavailable"
                      : "GameVault connected"}
            </span>
                    </div>

                    <div className="profile">
                        <div className="profile-avatar">GV</div>
                        <div className="profile-info">
                            <strong>GameVault User</strong>
                            <span>Desktop client</span>
                        </div>
                        <span className="profile-menu">•••</span>
                    </div>
                </div>
            </aside>

            <main className="main-content">
                <header className="topbar">
                    <div className="breadcrumbs">
                        <span>Workspace</span>
                        <span className="breadcrumb-separator">/</span>
                        <strong>Library</strong>
                    </div>

                    <label className="search-box">
                        <span className="search-icon">⌕</span>
                        <input
                            type="search"
                            placeholder="Search games, genres..."
                            value={search}
                            onChange={(event) => setSearch(event.target.value)}
                            aria-label="Search games"
                        />
                        <kbd>⌘ K</kbd>
                    </label>
                </header>

                <div className="page-content">
                    <section className="welcome-section">
                        <div>
                            <div className="eyebrow">
                                <span className="eyebrow-line" />
                                YOUR GAMING SPACE
                            </div>

                            <h1>
                                Your games.
                                <br />
                                <span>Your universe.</span>
                            </h1>

                            <p className="welcome-description">
                                All your games, gathered in one place.
                                Your next adventure starts here.
                            </p>
                        </div>

                        <div className="hero-art" aria-hidden="true">
                            <div className="hero-orbit hero-orbit--one" />
                            <div className="hero-orbit hero-orbit--two" />
                            <div className="hero-core">G</div>
                            <div className="hero-spark hero-spark--one">✦</div>
                            <div className="hero-spark hero-spark--two">✧</div>
                        </div>
                    </section>

                    <section className="library-section">
                        <div className="section-heading">
                            <div>
                                <div className="section-kicker">YOUR COLLECTION</div>
                                <h2>Game library</h2>
                                <p className="section-description">
                                    Browse your collection and find your next game.
                                </p>
                            </div>

                            <div className="library-count">
                <span className="count-number">
                  {status === "success" ? filteredGames.length : "—"}
                </span>
                                <span className="count-label">GAMES</span>
                            </div>
                        </div>

                        <div className="library-toolbar">
                            <div className="filter-tabs">
                                <button
                                    className={!showAvailableOnly ? "filter-tab filter-tab--active" : "filter-tab"}
                                    type="button"
                                    onClick={() => setShowAvailableOnly(false)}
                                >
                                    All games
                                </button>

                                <button
                                    className={showAvailableOnly ? "filter-tab filter-tab--active" : "filter-tab"}
                                    type="button"
                                    onClick={() => setShowAvailableOnly(true)}
                                >
                                    Available
                                </button>
                            </div>

                            <span className="sort-label">SORT BY: NAME</span>
                        </div>

                        {status === "loading" && (
                            <div className="state-panel">
                                <div className="loading-spinner" />
                                <h3>Loading your games</h3>
                                <p>Connecting to the GameVault server...</p>
                            </div>
                        )}

                        {status === "error" && (
                            <div className="state-panel state-panel--error">
                                <div className="state-icon">!</div>
                                <h3>Could not load games</h3>
                                <p>
                                    Check that your Spring Boot backend is running.
                                </p>
                                <code>{errorMessage}</code>
                                <button
                                    className="primary-button"
                                    type="button"
                                    onClick={() => void loadGames()}
                                >
                                    Try again
                                </button>
                            </div>
                        )}

                        {status === "success" && filteredGames.length === 0 && (
                            <div className="state-panel">
                                <div className="state-icon">⌕</div>
                                <h3>
                                    {games.length === 0
                                        ? "Your library is empty"
                                        : "No games found"}
                                </h3>
                                <p>
                                    {games.length === 0
                                        ? "Games returned by your API will appear here."
                                        : "Try another search or change your filters."}
                                </p>
                                {search !== "" && (
                                    <button
                                        className="secondary-button"
                                        type="button"
                                        onClick={() => setSearch("")}
                                    >
                                        Clear search
                                    </button>
                                )}
                            </div>
                        )}

                        {status === "success" && filteredGames.length > 0 && (
                            <div className="game-grid">
                                {filteredGames.map((game) => (
                                    <article className="game-card" key={game.id}>
                                        <div className={`game-cover ${getCoverStyle(game.genre)}`}>
                                            <div className="cover-glow" />
                                            <div className="cover-topline">
                                                <span className="cover-logo">GV / {String(game.id).padStart(3, "0")}</span>
                                                <span className="cover-symbol">✦</span>
                                            </div>

                                            <div className="cover-art-letter">
                                                {getInitials(game.name)}
                                            </div>

                                            <div className="cover-caption">
                                                <span>GAMEVAULT COLLECTION</span>
                                                <strong>{game.name}</strong>
                                            </div>

                                            <span className="cover-index">
                        {String(game.id).padStart(2, "0")}
                      </span>
                                        </div>

                                        <div className="game-card-body">
                                            <div className="game-card-title-row">
                                                <h3 title={game.name}>{game.name}</h3>
                                                <span
                                                    className={`availability-dot ${game.available ? "availability-dot--yes" : ""}`}
                                                    title={game.available ? "Available" : "Unavailable"}
                                                />
                                            </div>

                                            <div className="game-meta">
                                                <span>{game.genre}</span>
                                                <span className="meta-separator">·</span>
                                                <span>ID {game.id}</span>
                                            </div>

                                            <p className="game-description">
                                                {game.description || "No description available."}
                                            </p>

                                            <div className="game-card-footer">
                                                <div className="price-block">
                                                    <span>PRICE</span>
                                                    <strong>{formatPrice(game.price)}</strong>
                                                </div>

                                                <span
                                                    className={`availability-label ${game.available ? "availability-label--yes" : ""}`}
                                                >
                          {game.available ? "Available" : "Unavailable"}
                        </span>
                                            </div>
                                        </div>
                                    </article>
                                ))}
                            </div>
                        )}
                    </section>

                    <footer className="app-footer">
                        <span>GAMEVAULT <span className="footer-version">DESKTOP</span></span>
                        <span>MADE FOR YOUR NEXT ADVENTURE <span className="footer-star">✦</span></span>
                    </footer>
                </div>
            </main>
        </div>
    );
}
