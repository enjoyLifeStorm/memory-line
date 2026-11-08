# Memory-Line deployment

Deployed: 2026-10-17 (Asia/Shanghai)
URL: https://study.lifestorm.top
Server: deploy@81.70.104.211
Directory: /opt/apps/memory-line
Source snapshot: E:\temp\google-workspace\memory-line
Local build: E:\temp\codex-workspace\memory-line-deploy

Build locally with Java 17: mvn -o clean verify -B
Build result: success; 1 test passed.
JAR SHA256: 21753ef750ebbf33ac9d3c52e859108fcee0d66543eed32f300619f74bff9f87

Runtime uses Dockerfile.runtime and docker-compose.override.yml.
Server commands: docker compose build && docker compose up -d
Database: /opt/apps/memory-line/data/memory_line.mv.db
Logs: /opt/apps/memory-line/logs
Restart policy: unless-stopped

Verification passed:
- Public homepage HTTP 200 with valid TLS from local machine and server.
- Cards, today tasks, calendar summary, voice configs and active voice endpoints HTTP 200.
- H2 console HTTP 404 in production.
- Card JSON identical before/after container restart; service recovered.

Only application artifacts were uploaded in the final release bundle; test database was not included.
Original source directory was not modified. Runtime deployment files are in this working copy and on the server.

## Number selection update — 2026-10-17

Deployed locally built JAR SHA256: b7782a966954a9bf693716887665f28507cb7b0dee8bac6b4f2afb1b77dd9979

Added complete 0–9 digit library with stroke guides, a direct digit picker, free practice, configurable new-card batches (5/10/20/50), and automatic next-card selection. Today/review dates are dynamic. New and due review tasks are separated, and same-day repeated practice cannot repeatedly advance the memory stage.

Validated: 4 backend tests; local desktop/mobile browser flow; live HTTPS/mobile digit selection; exact preservation of all 4 existing cards. Live database now has 13 cards including 10 digits. Other categories still contain the initial examples.

Rollback artifacts on server: /opt/apps/memory-line/backups/before-number-selector-20261017/
Old image: memory-line:before-number-selector-20261017
Cold H2 backup: data.tar.gz in that backup directory.

Source changes synchronized back to E:\temp\google-workspace\memory-line (uncommitted). Runtime Dockerfile and Compose override are also included there. The deployed server runs the JAR artifact; its earlier uploaded src directory is not the source of this build.

## Curriculum and responsive update — 2026-10-17

Deployed artifact SHA256: 121398444a59bf5e031c444012f04b99cf56424f8929b18f51f0e0f5e0e78e4b

Live library: 100 HANZI, 87 PINYIN (including 24 tone cards), 26 LETTER upper/lowercase pairs, 10 NUMBER cards; 223 total. Added explicit curriculum difficulty/order, subject progress and unit selection. New learning follows the curriculum; due reviews retain priority. Letter learning is supplementary. This is an independently organized foundation course, not the complete contents of a specified textbook.

Hanzi strokes use bundled upstream medians, with original JSON files and the Arphic Public License retained. Pinyin listening is explicitly labeled as example-character pronunciation. Curriculum notes are served at /curriculum-notes.html. A local library cache avoids re-downloading all stroke data on every unit switch; successful reviews update the cache.

Validation: 6 backend tests passed; browser interaction tests passed at 320/390/768/1024/1180 px; final public-site checks passed for subject/unit selection and phone/tablet portrait/landscape. All previous 13 cards retained IDs, counts, memory stages, easiness factors, review dates, mastery and creation timestamps. Teaching metadata was updated for the curriculum.

Before-upgrade backup: /opt/apps/memory-line/backups/before-curriculum-20261017/
Previous image: memory-line:before-curriculum-20261017
Cold database backup: data.tar.gz in that folder.
Local source backup: E:\temp\codex-workspace\memory-line-before-curriculum-source.tar.gz

Final source, editable curriculum generation tools, third-party data and docs synchronized to E:\temp\google-workspace\memory-line. Changes remain uncommitted. Local temporary preview services were stopped after verification.

## Demo alignment and spoken stroke hints — 2026-10-17

Deployed JAR SHA256: 915a8ffbbdbf0edb20e48e02cef573ca78ee576d7d3998fff832a63adbb51d71

Removed the 0.8-second stroke-dashoffset CSS transition that conflicted with per-frame drawing. Grid lines, paths and the red pen now share one proportional SVG coordinate system. Hanzi use a native 240x240 square; four-line glyphs use a 320x240 canvas with horizontal padding; number paths have a consistent transform into a 200x300 grid. SVG pen coordinates stay synchronized during resizing. Faint outlines and current-stroke hint highlighting help children follow along.

Added full and individual Chinese stroke-hint narration, stop controls, cancellation on card changes, single-stream playback and device-speech fallback. Chinese chants/hints use Chinese even on English letter cards.

Validation: 6 Maven tests passed; all 223 cards fit proportional grids at 320/390/768/1024; measured pen/visible-ink error below 0.1px during resizing; curriculum and 1180px regression passed. Public-site browser test measured 0.000px alignment in its sample and decoded/played actual MP3 audio in muted mode for full and individual hints. All 223 existing cards retained their learning state; no production practice records were submitted in checks.

Rollback image: memory-line:before-demo-fix-20261017
Cold database and old JAR/config backup: /opt/apps/memory-line/backups/before-demo-fix-20261017/
Source backup: E:\temp\codex-workspace\memory-line-before-demo-fix-source.tar.gz
Original source project synchronized; temporary local preview servers stopped.


## Fixed daily learning flow — 2026-10-19

Deployed locally built JAR SHA256: 0a29c5548e6fe149f8e0c52dc2759e83396644a9a354094393ffb683deaf1468
Artifact: memory-line-daily-flow-final.tar.gz. Server image: memory-line:latest.

Added persisted tb_daily_plan (date key, fixed review/new/extra IDs and selected new-learning settings). GET daily-plan previews without writing; POST start preserves an existing plan; POST extra adds up to five new cards after the current queue finishes. Reviews include every due subject; parent subject/unit choices apply to new learning. Completion counts unique card IDs with practice logs for that Shanghai date. Extra practice does not inflate the daily goal. Existing single shared learner model is retained. Parent preferences for future plans are stored in the current browser.

Homepage now has Start/Continue, fixed progress and an explicit completion page. Today sessions hide the library sidebar and provide child-friendly ratings, review/new instructions and a return button. Phone feedback is fixed to the bottom; scrolling reserves room for the sticky progress banner. Free practice and direct digits remain available. Removed the hardcoded two-day streak in favor of actual today's practiced count. Open daily sessions detect date rollover before submitting a rating.

Validation: nine backend tests passed, including fixed plan/resume, cross-subject reviews, repeated-rating deduplication, extra limits, empty plan and next-day independence. Browser tests covered start, refresh/resume, changing future settings without replacing today's IDs, completing exactly the fixed goal, separately completing extra cards, free practice, five widths (320/390/768/1024/1180), pen synchronization and shared persistence across browser contexts. Service restart retained the plan. Public browser checks verified homepage, session/return/free/digits, proportional demo and actual decoded MP3 playback; production plan creation was intercepted and no production reviews were submitted. All 223 pre-upgrade cards retained learning fields.

Rollback image: memory-line:before-daily-flow-20261019
Cold H2 backup, prior JAR/configs and cards snapshot: /opt/apps/memory-line/backups/before-daily-flow-20261019/
Original source backup: E:\temp\codex-workspace\memory-line-before-daily-flow-source.tar.gz
Original source synchronized to E:\temp\google-workspace\memory-line; changes remain uncommitted. Source HTML also has nonfunctional trailing-space cleanup after artifact packaging.


## Image cues and active recall — 2026-10-19

Deployed local JAR SHA256: f5048012f4affaadc49af4e1dbfed5be6036d2d85a1c8a04c1796dfa3728c7a7
Artifact: memory-line-memory-aids-release.tar.gz. Server uses the runtime image built from this JAR.

Bundled 38 original SVG memory aids (28 Hanzi, all ten digits), spoken short association stories and structural cues. Source: tools/generate_memory_aids.py; output: static/memory-aids.json. Numeric illustrations have exact matching item quantities. Associations are labeled as memory aids rather than historical character etymologies. All 223 cards support conceal/listen/hints/compare, including those without an illustration.

Today new cards require a conceal-and-compare attempt before rating. Due reviews and same-day recall start with answers hidden; character, pinyin text, lesson text, pictures/stories, chants, strokes and the library list are concealed. Optional hints reveal a picture followed by structure guidance. Ratings following hints cannot claim fully independent recall. Ratings are parent-reported after paper practice; no handwriting recognition is claimed.

Newly created plans opt into same-day recall via nullable recallEnabled. Existing plans retain their original finish state. A learned new/extra card becomes eligible after two distinct intervening practiced cards, or at the end of the original queue. Results are persisted separately in tb_recall_result by Shanghai date/card ID; duplicate submissions are idempotent, do not add practice logs or raise memory stages, and do not inflate the daily goal. Assistance or hints schedules next-day review. Extra batches require finishing outstanding recall first. Completion and Continue include pending recall.

Validation: 11 backend tests passed. Browser full-flow checks passed for real spoken stories, concealing every answer source, gradual hints, compare gating, same-day spacing, reload/resume, 5 original + 5 recall completion without duplicated goal counts/stages, free practice, stroke synchronization and 320/390/768/1024/1180 widths. All 38 SVGs parse; all aid keys exist in the library; digits 0–9 have the correct illustrated quantity. Public server verification preserved learning fields of all 223 cards and the prior daily plan exactly. Public browser checks passed for actual decoded MP3 memory-tip playback, picture/structure hints, conceal/compare, numbers and all five widths. They used free practice and submitted no production reviews or recall results.

Rollback image: memory-line:before-memory-aids-20261019
Cold H2 backup, old JAR/configs and card/plan snapshots: /opt/apps/memory-line/backups/before-memory-aids-20261019/
Source backup: E:\temp\codex-workspace\memory-line-before-memory-aids-source.tar.gz
Final source and generator synchronized to E:\temp\google-workspace\memory-line, uncommitted.
