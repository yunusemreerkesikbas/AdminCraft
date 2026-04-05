"use client";

import { motion, useReducedMotion } from "framer-motion";

const layers = [
  { label: "PageTemplate", color: "#60a5fa", y: 0 },
  { label: "PageSlot", color: "#818cf8", y: 72 },
  { label: "CmsComponent", color: "#38bdf8", y: 144 },
];

export function ArchitectureDiagram({ className = "" }: { className?: string }) {
  const shouldReduce = useReducedMotion();

  return (
    <div className={`relative ${className}`} aria-hidden="true">
      <svg viewBox="0 0 280 220" className="w-full max-w-[280px]">
        {/* Connection lines */}
        {[0, 1].map((i) => (
          <motion.line
            key={i}
            x1="140"
            y1={layers[i].y + 40}
            x2="140"
            y2={layers[i + 1].y + 10}
            stroke="rgba(148,163,184,0.4)"
            strokeWidth="2"
            strokeDasharray="6 4"
            initial={shouldReduce ? {} : { pathLength: 0, opacity: 0 }}
            whileInView={shouldReduce ? {} : { pathLength: 1, opacity: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.8, delay: 0.4 + i * 0.3 }}
          />
        ))}

        {/* Arrow heads */}
        {[0, 1].map((i) => (
          <motion.polygon
            key={`arrow-${i}`}
            points={`135,${layers[i + 1].y + 8} 140,${layers[i + 1].y + 16} 145,${layers[i + 1].y + 8}`}
            fill="rgba(148,163,184,0.5)"
            initial={shouldReduce ? {} : { opacity: 0 }}
            whileInView={shouldReduce ? {} : { opacity: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.4, delay: 0.8 + i * 0.3 }}
          />
        ))}

        {/* Layer blocks */}
        {layers.map((layer, i) => (
          <motion.g
            key={layer.label}
            initial={shouldReduce ? {} : { opacity: 0, y: 20 }}
            whileInView={shouldReduce ? {} : { opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6, delay: i * 0.2 }}
          >
            <rect
              x="20"
              y={layer.y}
              width="240"
              height="48"
              rx="14"
              fill={`${layer.color}15`}
              stroke={`${layer.color}40`}
              strokeWidth="1.5"
            />
            <circle
              cx="48"
              cy={layer.y + 24}
              r="10"
              fill={`${layer.color}20`}
              stroke={`${layer.color}50`}
              strokeWidth="1"
            />
            <text
              x="48"
              y={layer.y + 28}
              textAnchor="middle"
              className="fill-white text-[10px] font-bold"
            >
              {i + 1}
            </text>
            <text
              x="72"
              y={layer.y + 28}
              className="text-[13px] font-semibold"
              fill={layer.color}
            >
              {layer.label}
            </text>
          </motion.g>
        ))}

        {/* Decorative dots */}
        {[
          { cx: 10, cy: 30 },
          { cx: 270, cy: 100 },
          { cx: 8, cy: 170 },
          { cx: 272, cy: 200 },
        ].map((dot, i) => (
          <motion.circle
            key={i}
            cx={dot.cx}
            cy={dot.cy}
            r="2"
            fill="rgba(148,163,184,0.3)"
            initial={shouldReduce ? {} : { scale: 0 }}
            whileInView={shouldReduce ? {} : { scale: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.3, delay: 0.6 + i * 0.1 }}
          />
        ))}
      </svg>
    </div>
  );
}
