import { StrictMode } from "react";
import ReactDOM from "react-dom/client";
import "@fontsource-variable/outfit";
import "@fontsource/instrument-serif/400.css";
import "@fontsource/space-mono/400.css";
import "@fontsource/space-mono/700.css";
import App from "./App";
import "./theme/hotel.css";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
