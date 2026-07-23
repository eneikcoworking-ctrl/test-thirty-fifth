import forms from '@tailwindcss/forms';
import containerQueries from '@tailwindcss/container-queries';

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{svelte,js,ts,jsx,tsx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        "surface-container-highest": "#d3e4fe",
        "background": "#f8f9ff",
        "inverse-on-surface": "#eaf1ff",
        "on-secondary-fixed": "#111c2d",
        "on-tertiary": "#ffffff",
        "on-primary": "#ffffff",
        "on-primary-fixed": "#001a42",
        "tertiary-fixed-dim": "#c3c7cb",
        "secondary-container": "#d5e0f8",
        "surface-bright": "#f8f9ff",
        "surface-container-low": "#eff4ff",
        "surface-container": "#e5eeff",
        "on-primary-fixed-variant": "#004395",
        "on-error": "#ffffff",
        "on-surface": "#0b1c30",
        "tertiary": "#585d60",
        "outline": "#727785",
        "outline-variant": "#c2c6d6",
        "primary": "#0058be",
        "on-background": "#0b1c30",
        "inverse-primary": "#adc6ff",
        "on-tertiary-container": "#fbfcff",
        "tertiary-container": "#707579",
        "on-secondary-fixed-variant": "#3c475a",
        "surface-tint": "#005ac2",
        "secondary": "#545f73",
        "on-surface-variant": "#424754",
        "secondary-fixed-dim": "#bcc7de",
        "primary-container": "#2170e4",
        "on-secondary-container": "#586377",
        "error-container": "#ffdad6",
        "on-error-container": "#93000a",
        "surface-variant": "#d3e4fe",
        "error": "#ba1a1a",
        "on-secondary": "#ffffff",
        "surface-container-high": "#dce9ff",
        "surface": "#f8f9ff",
        "surface-dim": "#cbdbf5",
        "primary-fixed-dim": "#adc6ff",
        "tertiary-fixed": "#dfe3e7",
        "on-tertiary-fixed-variant": "#43474b",
        "secondary-fixed": "#d8e3fb",
        "primary-fixed": "#d8e2ff",
        "inverse-surface": "#213145",
        "on-tertiary-fixed": "#171c1f",
        "on-primary-container": "#fefcff"
      },
      borderRadius: {
        "DEFAULT": "0.25rem",
        "lg": "0.5rem",
        "xl": "0.75rem",
        "full": "9999px"
      },
      spacing: {
        "md": "16px",
        "lg": "24px",
        "gutter": "16px",
        "base": "8px",
        "xs": "4px",
        "sm": "8px",
        "xl": "32px",
        "margin-mobile": "16px"
      },
      fontFamily: {
        "headline-md": ["Inter"],
        "body-sm": ["Inter"],
        "headline-lg-mobile": ["Inter"],
        "headline-lg": ["Inter"],
        "body-lg": ["Inter"],
        "label-md": ["Inter"]
      },
      fontSize: {
        "headline-md": ["1.25rem", { "lineHeight": "1.75rem", "fontWeight": "600" }],
        "body-sm": ["0.875rem", { "lineHeight": "1.25rem", "fontWeight": "400" }],
        "headline-lg-mobile": ["1.5rem", { "lineHeight": "2rem", "letterSpacing": "-0.01em", "fontWeight": "700" }],
        "headline-lg": ["2rem", { "lineHeight": "2.5rem", "letterSpacing": "-0.02em", "fontWeight": "700" }],
        "body-lg": ["1rem", { "lineHeight": "1.5rem", "fontWeight": "400" }],
        "label-md": ["0.75rem", { "lineHeight": "1rem", "letterSpacing": "0.05em", "fontWeight": "600" }]
      }
    },
  },
  plugins: [forms, containerQueries],
}
