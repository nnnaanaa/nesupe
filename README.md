# NW Afternoon CBT Mock Exam

A CBT-style practice tool for past papers of the Japanese Network Specialist Examination (Afternoon I and II), solved one problem at a time.

Live site: https://nnnaanaa.github.io/nesupe/

- Afternoon I: Reiwa 3 (2021) spring to Heisei 21 (2009) autumn. Model answers are included for Heisei 21-29.
- Afternoon II: Heisei 21 (2009) to Heisei 30 (2018) autumn, plus Reiwa 1 autumn and Reiwa 3-5 spring. Model answers are included for all of them.
- Static site (`index.html` plus page images). On GitHub Pages, answers and history are saved only in your browser (`localStorage`).
- The UI and exam content are in Japanese.

## Run locally

```
py -3 -m http.server 8000
```

Then open `http://localhost:8000/`.

## Run with Docker (local server, results saved on the server)

```
docker compose up -d --build
```

Then open `http://localhost:18420/`.

- A small Kotlin server (JDK `HttpServer`, no extra libraries) serves `index.html` and `pages/`, and stores your answers, history and notes in `./data/state.json` (a volume), so they survive restarts and are shared by every browser that opens the same server.
- The first time a browser connects, its existing local data is merged into the server data. If the same item exists on both sides, the more recently saved one wins.
- When there is no server API (GitHub Pages, or `py -3 -m http.server`), the app behaves exactly as before and saves only in the browser.
- The port is `18420` (chosen to avoid common ports) and it listens on all interfaces (`0.0.0.0`), so other machines on the network can use it. There is no authentication, so use it only on a trusted network. Set `BIND=127.0.0.1` to allow this machine only, or `PORT=9000` to change the port.
- To back up, copy `data/state.json`.

## Sources and rights

The problems and official answer keys are from IPA (Information-technology Promotion Agency, Japan), who owns the rights. The explanations were written by the author of this tool. Check IPA's terms of use before reusing the materials.
