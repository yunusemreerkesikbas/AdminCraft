import { Nav } from "@/components/Nav";
import { SmoothScroll } from "@/components/SmoothScroll";
import { Hero } from "@/components/sections/Hero";
import { About } from "@/components/sections/About";
import { Features } from "@/components/sections/Features";
import { UseCase } from "@/components/sections/UseCase";
import { Social } from "@/components/sections/Social";
import { Counter } from "@/components/sections/Counter";
import { ManagedProcess } from "@/components/sections/AISuggestions";
import { PlatformDemo } from "@/components/sections/PlatformDemo";
import { Reviews } from "@/components/sections/Reviews";
import { FAQs } from "@/components/sections/FAQs";
import { CTAFooter } from "@/components/sections/CTAFooter";

export default function Home() {
  return (
    <>
      <SmoothScroll />
      <Nav />
      <main className="relative">
        <Hero />
        <About />
        <Features />
        <UseCase />
        <Social />
        <Counter />
        <ManagedProcess />
        <PlatformDemo />
        <Reviews />
        <FAQs />
        <CTAFooter />
      </main>
    </>
  );
}
