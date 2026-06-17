/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        bg: {
          deepest: 'var(--bg-deepest)',
          deep: 'var(--bg-deep)',
          base: 'var(--bg-base)',
          surface: 'var(--bg-surface)',
          elevated: 'var(--bg-elevated)',
          overlay: 'var(--bg-overlay)',
          hover: 'var(--bg-hover)',
        },
        accent: {
          cyan: 'var(--accent-cyan)',
          purple: 'var(--accent-purple)',
          green: 'var(--accent-green)',
          amber: 'var(--accent-amber)',
          red: 'var(--accent-red)',
          blue: 'var(--accent-blue)',
          pink: 'var(--accent-pink)',
        },
        text: {
          primary: 'var(--text-primary)',
          secondary: 'var(--text-secondary)',
          muted: 'var(--text-muted)',
          dim: 'var(--text-dim)',
        }
      },
      fontFamily: {
        sans: ['var(--font-sans)', 'sans-serif'],
        mono: ['var(--font-mono)', 'monospace'],
      }
    },
  },
  plugins: [],
}
