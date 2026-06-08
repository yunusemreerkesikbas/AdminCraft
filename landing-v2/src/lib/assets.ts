// Centralized list of asset paths under /public so components don't hardcode strings.
// All paths are relative to /public.

export const ASSETS = {
  brand: {
    /** Dark fill (#0d1527) — use on white / light backgrounds */
    logo: "/brand/logo-dark.svg",
    logoDark: "/brand/logo-dark.svg",
    /** White fill — use on dark / coloured backgrounds */
    logoLight: "/brand/logo-light.svg",
    /** Favicon variants */
    faviconDark: "/brand/favicon-dark.svg",
    faviconLight: "/brand/favicon-light.svg",
  },
  navIcons: {
    appStore: "/images/icons/BZYkl56vzaLH9uCPqEid8uQg9ZE.svg",
    googlePlay: "/images/icons/J4VPLNaOdWSrA6fBUpuZ0eLYNAI.svg",
    play: "/images/icons/tS0ZuZlJiNpvPvkALsbfBb1Eig.svg",
  },
  hero: {
    bg: "/images/photos/hero-v2.webp",
    bgMobile: "/images/photos/hero-v2-mobile.webp",
    bgFallback: "/images/photos/hero-v2.jpg",
  },
  clouds: {
    big: "/images/clouds/c5yKOUxAULPtn3CoxX5b92vgJ8U.png",
    medium: "/images/clouds/bWh0pVd78Am5KRfPjOhMoF5Ddk.png",
    small: "/images/clouds/vK1naOOAfBeOfzjSqlloxH7w.png",
    counter: "/images/clouds/oUscxluBetLnso5wtiCRMM5TpDE.png",
  },
  about: {
    weatherEmoji: "/images/icons/38EivRTUD86UdDi4ptLVvUiyP8.svg",
    centerPhone: "/images/icons/craftive-app.webp",
    tile01: "/images/about/platform.webp",
    tile02: "/images/about/multi-lang.webp",
    tile03: "/images/about/database.webp",
    tile04: "/images/about/commerce.webp",
    tile05: "/images/about/saas.webp",
  },
  useCase: {
    agency: "/images/photos/agency.webp",
    saas: "/images/photos/saas.webp",
    corporate: "/images/photos/corporate.webp",
    ecommerce: "/images/photos/ecommerce.webp",
  },
  counter: {
    // The earth/globe is actually an autoplaying video, not a static image.
    earthVideo: "/videos/yjuD7GFXupvQlqYR8xjXBuh5oc.mp4",
  },
  features: {
    bell: "/images/photos/LuRk2mi9PkL9qpq8u7aAhmN27dM.png",
    plannerTexture: "/images/photos/planner-texture.webp",
    routineNotif: "/images/photos/routine-hand.png",
    routineBg: "/images/photos/routine-bg.jpg",
    routinePhone: "/images/icons/routine-phone.svg",
  },
  aiSuggest: {
    phone: "/images/photos/otkB8TZsvM2ULa0N3ALnKskG5ag.png",
  },
  cta: {
    handPhoneUp: "/images/photos/Yxyz4xAO2Afi4x2mrtShsiVlWA.webp",
  },
  videos: {
    earth: "/videos/yjuD7GFXupvQlqYR8xjXBuh5oc.mp4",
    review1: "/videos/PRGg2Q31L3cBm9aIlNsOfMD8QL8.mp4",
    review2: "/videos/TwMQMPf1H0P9cYfxE88cxOnb0qU.mp4",
    review3: "/videos/nG8aNSXF9bVc3wia1eVPP35Z5c8.mp4",
    review4: "/videos/wBHfmVdBC9k7z6MAlqRq6AD7ZWw.mp4",
  },
} as const;
