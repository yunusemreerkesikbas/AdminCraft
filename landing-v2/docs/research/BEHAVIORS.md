# Behaviors — Habitline

## Global

### Smooth scroll
- **Library:** Lenis (class `lenis` on `<html>`)
- **Implementation:** Install `lenis`, wrap app with Lenis provider in a client component, smooth-scrolling all anchor navigation

### Appear animations
- Nearly every element has a fade-up appear animation (opacity 0 → 1, translateY ~20px → 0) triggered when it enters viewport
- Duration ~0.6s, ease-out
- **Implementation:** Custom `useReveal` hook with IntersectionObserver, OR Framer Motion `whileInView`. Prefer raw IntersectionObserver to avoid heavy dependency.

### Sticky nav
- Position: fixed top, centered horizontally
- Pill shape: white background, large rounded corners, drop shadow
- Visible from y=0; no shrink/transform on scroll observed in extraction
- Items: brand logo on left + center nav links (What's inside, Use case, Metrics, Smart Assist) + right App Store/Play Store icon buttons

## Per-section

### Hero
- Background portrait image stays fixed-position behind, blurred at bottom transitioning to clouds
- Three floating cards (left streak, center phone, right goal) have subtle float animation (Framer "perpetual" or no animation, static after appear)

### About
- Center phone mockup floats over scrolling tile marquee in background
- Tile rows are likely two opposing marquees (top row scrolls right, bottom scrolls left) — CSS animation `marquee` infinite linear, ~40s duration

### Features
- Cards appear with stagger as user scrolls into section
- "Flexible streak rules" card: vertical marquee of pill labels animating up infinitely
- "Smart daily planner": vertical list scrolling up infinitely showing different habits
- "Gentle reminders": notification cards stack scrolling up infinitely (multiple columns)

### Use Case
- Tab pills (Professionals/Students/Remote workers/Busy parents) — click switches background portrait image (use placeholder for now: just show "Professionals" active state, no swap)

### Social
- Horizontal scroll/marquee — auto-cycle horizontal, infinite

### Counter
- 62,000+ counter uses CountUp animation when entering viewport (count from 0 to 62000)
- Smaller stats use odometer-style rolling digits (each digit rolls into place)

### Reviews
- Top avatar strip: infinite horizontal marquee
- Phone video mockups on hero band: each `<video>` autoplays on hover/in-view, mute, loop

### FAQs
- Accordion: first item open by default ("How many habits can I track?")
- Clicking another item closes the current and opens the clicked one (single-open accordion)
- Smooth height animation on expand

## Responsive
- At <768px: Cards stack to single column, nav collapses to hamburger (or compressed pill)
- At <390px: All sections become single-column, hero phone moves below text

## Interaction model summary
- Nav: link-based smooth scroll to anchored sections (`#hero`, `#features`, `#use-case`, `#metrics`, `#smart-assist`, `#reviews`)
- Use Case tabs: click to switch (we'll implement as static "Professionals" tab for v1)
- FAQs: click to toggle (single-open accordion)
- All other "carousel-like" elements: auto-cycling marquees (CSS @keyframes animation)
