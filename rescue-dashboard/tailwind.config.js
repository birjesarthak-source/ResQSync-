/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        navy: {
          950: '#040d18',
          900: '#071827',
          850: '#0a1d30',
          800: '#0d2238',
          700: '#14314e',
          600: '#1d4268',
          500: '#2b5887',
        },
        cyan: {
          DEFAULT: '#00c5d2',
          glow: '#00e5f5',
          dark: '#009ba6',
          muted: '#0b4756',
        },
        emergency: {
          critical: '#ef4444',
          high: '#f97316',
          medium: '#eab308',
          low: '#3b82f6',
          resolved: '#10b981',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'sans-serif'],
        mono: ['JetBrains Mono', 'Menlo', 'monospace'],
      }
    },
  },
  plugins: [],
};
