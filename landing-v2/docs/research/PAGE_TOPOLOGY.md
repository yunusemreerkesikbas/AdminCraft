# Page Topology — Habitline

Page height (desktop): 14064px. Width: 1425px (uses ~1200px max content width).

## Z-Index Layers
- z-50: Floating nav (sticky, top, pill-shaped, centered)
- z-30: Floating "Made in Framer" badge (bottom-right) — we will NOT include this
- z-20: Page content
- z-10: Hero background image + clouds + gradients

## Sections (top to bottom)

| # | Name | id | Y | Height | Background |
|---|------|----|----|--------|-----------|
| 0 | Nav (sticky) | — | 0 (fixed) | 124 | Pill on transparent |
| 1 | Hero | #hero | 0 | 1282 | Orange portrait + clouds + gradient |
| 2 | About | — | 1282 | 1335 | White, cloud bg at bottom |
| 3 | Features | #features | 2627 | 2314 | White + dark cards |
| 4 | Use Case | #use-case | 4941 | 1238 | White with image fade |
| 5 | Social | #metrics | 6179 | 845 | White, image carousel |
| 6 | Counter | #counter | 7024 | 1127 | Earth/sky bg |
| 7 | AI Suggestions | #smart-assist | 8152 | 1290 | White |
| 8 | Reviews | #reviews | 9442 | 1873 | White + avatar marquee |
| 9 | FAQs | — | 11314 | 484 | White accordion |
| 10 | CTA | — | 11799 | 1800 | Phone hand image + footer in one |

Note: section #8 "Reviews" overlaps with a nested "Carousel" data-framer-name at y=9684 height=700 (the avatar marquee strip at top).

## Page-level systems
- Lenis smooth scroll (html.lenis class)
- Framer scroll-triggered "appear" animations on most elements (fade-up + opacity 0→1)
- Sticky floating nav pill that stays centered at top of viewport
- The hero "BG Image" portrait shrinks/fades into the white About section via blur overlay (image `pUHkyH58LSJDgk8jqtzFiFqUyvY.svg` is a 1920x530 mask)

## Section Anatomy

### 1. Hero
- Full-bleed orange-tinted portrait background (`5AAyXsYWLAPDHoR0Qd3rIax2A4.jpg`)
- Top: Pill badge "New · A calmer way to build habits"
- Heading H1: "Build habits that actually stick" (Stack Sans Headline 90px/500)
- Subhead: "You see the right habits at the right time so your day never feels crowded."
- CTAs: "Start tracking for free" (white pill) + "Watch demo" (white outline with play icon)
- Below: Three glass-morphism cards floating over portrait:
  - Left: "7-day streak unlocked" (small dark card with orange medal icon)
  - Center: Large iPhone mock showing app UI ("Today Task", habits with checkmarks)
  - Right: "Today's goal: Complete 3 habits" card with 3 avatar circles
- Bottom transition: white cloudy band with `pUHkyH58LSJDgk8jqtzFiFqUyvY.svg` blur overlay

### 2. About
- Centered floating phone showing Weekly Overview
- Surrounding tile grid of about images (phone-off, morning walk, focus session, stretch, clean workspace, meditate, track water, write journal) — these scroll horizontally as marquee
- H2: "Build steady daily habits with a layout that keeps your mornings, evenings, and focus simple to follow." (Stack Sans Headline 68px/500) — sentence interleaved with inline weather emoji `38EivRTUD86UdDi4ptLVvUiyP8.svg`
- Pill tags row: "Used by people to improve routines." + #Founders #Students #Busy parents #Remote teams
- 4.7 rating badge with stars
- Subtext: "Stay consistent with a system that fits into real life..."
- Two CTAs: Download for iPhone (App Store badge) + Get it on Android (Google Play badge)
- Cloud images at top and bottom transitions (`c5yKOUxAULPtn3CoxX5b92vgJ8U.png`, `bWh0pVd78Am5KRfPjOhMoF5Ddk.png`)

### 3. Features
- Caption: "Habits with structure"
- H2: "A layout that keeps your day clear."
- Body: "Habitline brings clarity to your routines with clean cards, realistic progress tracking, and guidance that adapts to your day."
- 2x3 grid of feature cards (dark theme):
  - **Flexible streak rules** (with vertical scroll of pills: Travel Mode Active, Sick Day, Weekend Flexibility, Rest Day Credit, Pause Streak)
  - **Smart daily planner** (mock UI showing Good Morning + scheduled habits)
  - **Routine stacks** (hand image + colorful pill UI: Morning, Evening, Focus)
  - **Weekly reflection** (donut progress + counters 12, 07)
  - **Gentle reminders** (notification card examples scrolling)
  - Two cards share row 1, the Routine Stacks card is wide (spans wider), then Weekly Reflection + Gentle Reminders share row

### 4. Use Case
- Caption: "Fits every lifestyle"
- H2: "Adapted for the way you live and work"
- Below image with man's face (portrait `ubUH31gw3mKXQBS06KHaijaBY.jpg`)
- Pill tabs row: Professionals (active) | Students | Remote workers | Busy parents
- Inset card on image: "87% Weekly consistency"
- "And for every kind of daily rhythm" caption + hashtag pills row: #fitness enthusiasts #creatives #entrepreneurs #freelancers #new habit builders #deep-work lovers

### 5. Social
- H2: "What users are achieving with Habitline"
- Caption pill: "Trusted worldwide"
- Horizontal scroll of 6 user cards, each: square portrait + name + role + achievement
  - Maya / Student / Completed 21-day streak using Habitline
  - Daniel gray / Founder / 87% Improved weekly consistency (large stat)
  - Aaron Lee / Remote Engineer / Stopped breaking habits on weekends...
  - Priya / Busy Parent / Logged 40 focus sessions...
  - Leo / Creative Professional / 10 Days Hit hydration goals
  - Ramya / Software Developer / Finally keeps his day organized...

### 6. Counter (Stats over earth)
- Earth-and-sky background (`yyE2pUgUrjBeDKRRIF0gWhZRuhM.jpg` or similar)
- Caption: "Real habits, real numbers"
- H2: "How people stay consistent over time"
- Large counter "62,000+" "Check-ins logged last month"
- 3 secondary stats in odometer-style rolling number format: 87% / 46 / 32+ with descriptions

### 7. AI Suggestions
- Caption: "Smarter habits, less thinking"
- H2: "AI suggestions that adjust to your day"
- Body: "Habitline learns your patterns and offers small, useful suggestions..."
- CTA: "See how suggestions work"
- Right side: Phone mock with screen showing AI suggestion card
- Below: 4 icon-led feature pills: Morning walk / Habit Priorities / Routine Insights / Recovery Suggestion

### 8. Reviews
- Top: Horizontal marquee of avatars (Aisha Khan, Olivia Park, Ryan Cooper, Marcus Reed) sliding
- Caption: "A closer look"
- H2: "How people use Habitline every day"
- Below: large hero image (woman with phone) center with 3-up phone video mockups on either side using video assets (PRGg2Q31, TwMQMPf1, nG8aNSXF, wBHfmVdBC9k)
- Rating: 4.5/5 (Trusted by 1582+ users) + stars
- Carousel arrows + dots
- 2-column grid of testimonial cards with quote + avatar + name + role:
  - "Habitline made my mornings feel manageable again." — Maya Zong, Student
  - "The weekly insights are what sold me..." — Daniel Perez, Software Engineer
  - "This is the first habit app that doesn't overwhelm me..." — Andre Lewis, University Student
  - "I used to ignore reminders..." — Ethan Miller, Gym Trainer
  - "Focus blocks changed the way I work..." — Laura Kim, Product Designer
  - "The simple visuals and progress cues..." — Kevin Brooks, Fitness Coach
  - "It's the first habit app that doesn't overwhelm me." — Hannah Lee, Content Writer
  - "I actually stick to my routines now..." — Priya Shah, Marketing Specialist
  - "Feels tailored to my day..." — Sofia Martinez, UX Researcher
- "View all Reviews" CTA at bottom

### 9. FAQs
- Caption: "Common questions"
- H2: "Frequently asked questions"
- Left rail: "Can't find your answer?" + "Contact us" CTA
- Right rail: Accordion list (5 items, first one open by default)
  - How many habits can I track? (open by default with answer)
  - Do reminders work across all devices?
  - What happens if I miss a day?
  - Can I create routines for different times of day?
  - Is Habitline free to use?

### 10. CTA + Footer
- Top half: large hand-with-phone image (`kmAong4xy6gvqhgzcssHKUFJnmA.jpg`)
- H2 white over image: "Build better habits with less effort"
- Subtext white: "Track what matters, stay organized, and improve at your own pace."
- Two store CTAs (Download for iPhone / Get it on Android)
- Right card: "Scan the QR code to download the app" + QR code image
- Bottom half: white footer with Habitline logo, newsletter signup input, Pages/Resources/Socials columns, "Hosting on Framer Powered by Framer" credits
