type WaveDividerProps = {
  fill?: string;
  className?: string;
  flip?: boolean;
};

export function WaveDivider({
  fill = "#ffffff",
  className = "",
  flip = false,
}: WaveDividerProps) {
  return (
    <div
      className={`pointer-events-none w-full leading-[0] ${flip ? "rotate-180" : ""} ${className}`}
      aria-hidden="true"
    >
      <svg
        viewBox="0 0 1440 80"
        preserveAspectRatio="none"
        className="block h-[40px] w-full sm:h-[60px] lg:h-[80px]"
      >
        <path
          d="M0,40 C360,80 720,0 1080,40 C1260,60 1380,50 1440,40 L1440,80 L0,80 Z"
          fill={fill}
        />
      </svg>
    </div>
  );
}
