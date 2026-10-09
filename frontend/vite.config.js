
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/login": "http://localhost:8080",
      "/register": "http://localhost:8080",
      "/verify-otp": "http://localhost:8080",
      "/check-session": "http://localhost:8080",
      "/logout": "http://localhost:8080",
      "/upload": "http://localhost:8080",
      "/view": "http://localhost:8080",
    },
  },
});