/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'main-green': '#2E7D32',
        'sub-green': '#66BB6A',
        'white-green': '#E8F5E9',
        'main-blue': '#1565C0',
        'sub-blue': '#42A5F5',
        'white-blue': '#E3F2FD',
        'main-ivory': '#FAF3E0',
        'sub-ivory': '#FDFCF9',
        'warning': '#ffc107',
        'danger': '#dc3545',
      },
    },
  },
  plugins: [],
};
