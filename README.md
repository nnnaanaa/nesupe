# NW Afternoon I CBT Mock Exam

A study tool for practicing past papers of the Japanese **Network Specialist Examination, Afternoon I (written-answer section)** on a screen that resembles the CBT (computer-based testing) exam, one problem at a time.

Live site: https://nnnaanaa.github.io/nesupe/

The app is a static site (`index.html` plus problem images) with no server. Answers, memos, and history are stored in your browser's `localStorage` and are never sent anywhere. The user interface and the exam content are in Japanese.

## Included past exams

| Exam | Problems | Model answers and explanations |
|---|---|---|
| Reiwa 3 (2021) Spring | Q1-Q3 | No |
| Reiwa 1 (2019) Autumn | Q1-Q3 | No |
| Heisei 30 (2018) Autumn | Q1-Q3 | No |
| Heisei 29 (2017) Autumn | Q1-Q3 | Yes |
| Heisei 28 (2016) Autumn | Q1-Q3 | Yes |
| Heisei 27 (2015) Autumn | Q1-Q3 | Yes |
| Heisei 26 (2014) Autumn | Q1-Q3 | Yes |
| Heisei 25 (2013) Autumn | Q1-Q3 | Yes |

- The problems are page images (under `pages/`) of the past exam papers published by IPA (Information-technology Promotion Agency, Japan).
- The model answers follow the official answer keys published by IPA. The explanations were written by the author of this tool and are not official IPA material.
- Questions that ask you to complete a diagram cannot be drawn on screen, so they are answered in text instead.

## Features

- Pick a problem and solve it one at a time.
- Problem booklet on the left, answer form on the right, with a draggable divider (stacked vertically on phones).
- Zoom in/out, highlighter markers (yellow, blue, red), memo, calculator, inverted colors, and opening the problem in a separate window.
- A 45-minute countdown for each problem. Input keeps working after it reaches zero.
- Answer fields with a character limit reject input beyond the limit.
- "Review later" flags and an overview of answer status (unanswered, answered, flagged, in progress).
- On the result screen: review your answers, enter the "correct answer", re-read the problem, save the answer history, print preview, and copy as text.
- For problems with model answers, the result screen can show the model answer and explanation (hidden by default).

There is no automatic scoring. Compare your answers with the model answers and score yourself.

## Run locally

```
py -3 -m http.server 8000
```

Then open `http://localhost:8000/` in a browser. Serve it over HTTP as above instead of opening `index.html` directly, because the problem images are loaded from files.

## Structure

- `index.html`: a single file containing the UI, the problem data, and the model answers.
- `pages/<exam>/NN.png`: page images of the problem booklet. The folder name is the era (`r` = Reiwa, `h` = Heisei), the year, and the season (`h` = spring, `a` = autumn). Examples: `r03h` (Reiwa 3 spring), `r01a` (Reiwa 1 autumn), `h30a` (Heisei 30 autumn).
- `.gitignore`: keeps the source PDFs and working folders out of the repository.

## Sources and rights

The copyright of the problem texts, figures, and answer keys belongs to the original source (IPA, Information-technology Promotion Agency, Japan). This repository only makes publicly available materials easier to solve on screen, for study purposes. Please check IPA's terms of use before reusing them. The explanations were written by the author of this tool.
